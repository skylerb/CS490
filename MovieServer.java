/**
 * MovieServer.java
 *
 *   The MovieServer allows user to search for a movie title and returns up to 5 movie recommendations
 *
 * Name: Skyler Beckman
 * Course: CS490-WIR Fall 2012, Purdue University
 * Date: 10/31/2012
 * Last Update: 11/13/2012
 */


import java.net.Socket;
import java.net.ServerSocket;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class MovieServer {
	static final String SERVERIP = "127.0.0.1";
	static public final int SERVERPORT = 25201, MAXC = 5;

	private static int numConns = 0;

	public static void main(String[] args) {

		MovieServer mserver = new MovieServer();

		try {
			ServerSocket listener = new ServerSocket(SERVERPORT);
			Socket client;

			while((numConns++ < MAXC) || (MAXC == 0)) {
				System.out.println("TCP S: Waiting for new connection...");
				client = listener.accept();
				System.out.println("TCP S: New connection received.");
				acceptThread connect = mserver.new acceptThread(client);
				Thread t = new Thread(connect);
				t.start();
			}
		} catch( IOException e ) {
			System.out.println("TCP S: Error " + e);
			e.printStackTrace();
		}
	}

	// Inner-class for accepting threads
	class acceptThread implements Runnable {

		private Socket client;

		acceptThread(Socket client) {
			this.client = client;
		}

		public void run() {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
				oos.flush();
				ObjectInputStream ois = new ObjectInputStream(client.getInputStream());

				//Search
				int command = ois.readInt();
				if(command != 10) {
					throw new IOException("Unrecognized command");
				}

				String movieQuery = (String)ois.readObject();
				System.out.println("TCP S: Movie request " +movieQuery);
				search(movieQuery, oos, ois);

			} catch( IOException ie) {
				System.out.println(ie);
			} catch(ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
			} catch( SAXException se) {
				se.printStackTrace();
			} catch( ParserConfigurationException pce) {
				pce.printStackTrace();
			}
		}
	}

	private void search(String title,ObjectOutputStream oos,ObjectInputStream ois) throws IOException,ClassNotFoundException,SAXException,ParserConfigurationException {
		// Search database for title
		SAXParserFactory spf = SAXParserFactory.newInstance();
		MovieHandler handler = new MovieHandler(title);
		SAXParser sp = spf.newSAXParser();

		sp.parse("Movies.xml", handler);

		//Movie Not In Database
		if(handler.getMovies().size() == 0) {
			//Send message back to client that movie does not exist in database
			synchronized(oos) {
				System.out.println("S: " + title + " is not in database");
				oos.writeInt(30);
				oos.flush();
				ois.close();
				oos.close();
				numConns--;
				throw new IOException("Movie Not Found");
			}
		} else {
			//Send message back to client that movie exists in database

			synchronized(oos) {
				oos.writeInt(20);
				oos.writeObject("C_SEARCH");
				oos.flush();
			}

			// Movie User Searched For
			Movie movie = handler.getMovies().get(0);

			// Find Related Movies and Rank Them
			MovieRankHandler rHandler = new MovieRankHandler(movie);

			sp.parse("Movies.xml", rHandler);

			//Ranked list of movies
			List<Movie> movies = rHandler.getMovies();

			//List of attributes that have been used in questions
			List<Attribute> usedAttributes = new ArrayList<Attribute>();

			int command = ois.readInt();
			if (command == 11) {
				ois.close();
				oos.close();
				numConns--;
				throw new IOException("Client Quit");
			} else if(command != 12) { //C_READY
				throw new IOException("Unrecognized command");
			}

			//K-Medoids Begins Here
			for(int x = 0; x < 5; x++) { // Repeat for 5 questions
				//Sort movie list based on rank for clustering purposes
				Collections.sort(movies);

				//Generate the intial clusters
				Cluster[] clusters = initClusters(5, movies);

				boolean CHANGE = true;

				//repeat until cluster centers remain stable
				while(CHANGE) {
					//Remove any previous movies from clusters
					clearClusters(clusters);

					// Associate each movie to the closest (movie) medoid
					assignClusterByDistance(clusters, movies);

					CHANGE = false;

					//For each of the k clusters && for each non-medoid data point
					for(int i = 0; i < clusters.length; i++)
					{
						double curMinCost = totalCostIndividual(clusters[i]);
						Movie newMedoid = null;

						for(Movie m1 : clusters[i].getItems()) {
							if(!isClusterMedoid(m1,clusters)) {
								double newTotalCost = 0.0;

								for(Movie m2 : clusters[i].getItems()) {
									if(!m1.getTitle().equalsIgnoreCase(m2.getTitle())) {
									newTotalCost += computeDistance(m2, m1);
									}
								}
								if(newTotalCost < curMinCost) {
									//Better medoid
									newMedoid = m1;
									curMinCost = newTotalCost;
								}
							}
						}
						if(newMedoid != null) {
							//Better medoid was found
							clusters[i] = new Cluster(newMedoid);
							CHANGE = true;
						}
					}
				}

				int cIndex = getClusterWithHighestAvg(clusters);

				Attribute topAttr = findTopAttribute(usedAttributes, clusters[cIndex].getItems());

				usedAttributes.add(topAttr);

				synchronized(oos) {
					oos.writeInt(21);
					StringBuilder result = new StringBuilder();
					switch(topAttr.getIndex()) {
						case 0: //Genre
							result.append("Do you like " + topAttr.getVal() + " movies?");
							oos.writeObject(result.toString());
							break;
						case 1: //Director
							result.append("Do you like movies directed by " + topAttr.getVal() + "?");
							oos.writeObject(result.toString());
							break;
						case 2: //Actor
							result.append("Do you like movies starring " + topAttr.getVal() + "?");
							oos.writeObject(result.toString());
							break;
						case 3: //Keyword
							result.append("Do you want to watch movies about " + topAttr.getVal() + "?");
							oos.writeObject(result.toString());
							break;
					}
					oos.flush();
				}

				int cCommand = ois.readInt();

				// TODO: Check for back button press
				if(cCommand == 11) { //C_DISCONNECT
					ois.close();
					oos.close();
					numConns--;
					throw new IOException("Client Quit");
				} else if(cCommand != 13) { //C_ANSWER_QUESTION
					throw new IOException("Unrecognized Command");
				}
				int answerResult = ois.readInt();

				updateClusterRanks(clusters[cIndex], topAttr, answerResult);
			}

			//Finished with K-Medoid Algorithm. Notify User

			//Sorting the movies one last time to guarantee ranked properly
			Collections.sort(movies);

			for(int i = 0; i < 5 && i < movies.size(); i++) {
				oos.writeInt(22); //S_MOVIE_SEND
				//System.out.println(movies.get(i).getTitle());
				oos.writeObject(movies.get(i).getTitle());
				oos.writeInt(movies.get(i).getYear());
				int rating = 8;
				switch(movies.get(i).getMPAA().toLowerCase()) {
					case "g": 	rating = 0; break;
					case "pg":	rating = 1; break;
					case "pg-13":	rating = 2; break;
					case "r":	rating = 3; break;
					case "nc-17":	rating = 4; break;
					case "approved":rating = 5; break;
					case "tv-14":	rating = 6; break;
					case "ur":	rating = 7; break;
					case "nr":	rating = 8; break;
					default:	rating = 0; break;
				}
				oos.writeInt(rating);
				oos.writeObject(movies.get(i).getGenres());
				//Sending coverart
				File imgPath = new File(movies.get(i).getCoverArt());
				FileInputStream fis = null;
				int size = (int)imgPath.length();
				byte[] bytes = new byte[size];
				fis = new FileInputStream(imgPath);
				int read = fis.read(bytes);
				oos.writeInt(size);
				oos.writeObject(bytes);
				oos.flush();
				fis.close();

				int c = ois.readInt();
				if(c != 14) {
					throw new IOException("Unrecognized Command");
				}
			}

			synchronized(oos) {
				oos.writeInt(23); //S_ALL_MOVIES_SENT;
				oos.flush();
			}

		}
		//Close connection
		ois.close();
		oos.close();
		numConns--;
	}

	public void updateClusterRanks(Cluster cluster, Attribute attr, int change) {
		for(Movie m : cluster.getItems()) {
			switch(attr.getIndex()) {
				case 0: //Genre
					for(String g : m.getGenres()) {
						if(g.equalsIgnoreCase(attr.getVal())) {
							m.addRank(change);
						}
					}
					break;
				case 1: //Director
					for(String d : m.getDirectors()) {
						if(d.equalsIgnoreCase(attr.getVal())) {
							m.addRank(change);
						}
					}
					break;
				case 2: //Actor
					for(String a : m.getActors()) {
						if(a.equalsIgnoreCase(attr.getVal())) {
							m.addRank(change);
						}
					}
					break;
				case 3: //Keyword
					for(String k : m.getKeywords()) {
						if(k.equalsIgnoreCase(attr.getVal())) {
							m.addRank(change);
						}
					}
					break;
			}
		}
	}

	public boolean inAttributeList(List<Attribute> attributes, String val, int index) {
		for(Attribute a : attributes) {
			if(val.equalsIgnoreCase(a.getVal()) && index == a.getIndex()) {
				a.incrCount();
				return true;
			}
		}
		return false;
	}

	public Attribute findTopAttribute(List<Attribute> used, List<Movie> movies) {
		List<Attribute> attributes = new ArrayList<Attribute>();

		for(Movie m : movies) {
			//Genre
			for(String g : m.getGenres()) {
				if(!inAttributeList(attributes, g, 0)) {
					attributes.add(new Attribute(0,g));
				}
			}
			//Director
			for(String d : m.getDirectors()) {
				if(!inAttributeList(attributes, d, 1)) {
					attributes.add(new Attribute(1,d));
				}
			}
			//Actor
			for(String mA : m.getActors()) {
				if(!inAttributeList(attributes, mA, 2)) {
					attributes.add(new Attribute(2,mA));
				}
			}
			//Keyword
			for(String mK : m.getKeywords()) {
				if(!inAttributeList(attributes, mK, 3)) {
					attributes.add(new Attribute(3, mK));
				}
			}
		}

		Collections.sort(attributes);

		//System.out.println(attributes);

		if(used.size() > 0) {
			for(Attribute a1 : attributes) {
				boolean FLAG = true;
				for(Attribute a2 : used) {
					if(a1.getVal().equalsIgnoreCase(a2.getVal()) && a1.getIndex() == a2.getIndex()) {
						FLAG = false;
					}
				}
				if(FLAG) {
					return a1;
				}
			}
		}
		return attributes.get(0);
	}


	public void printClusters(Cluster[] clusters) {
		for(int i = 0; i < clusters.length; i++) {
			System.out.println("Cluster " + (i+1) + ": " + clusters[i].getMedoid().getTitle());
			System.out.println(clusters[i]);
			System.out.println("");
		}
	}

	private double totalCostIndividual(Cluster c) {
		double tc = 0.0;
		for(Movie m : c.getItems()) {
			tc += m.getDistance();
		}

		return tc;
	}

	private int getClusterWithHighestAvg(Cluster[] clusters) {
		int index = 0;
		double currHighRank = 0.0;

		for(int i = 0; i < clusters.length; i++) {
			double totalRank = 0.0;
			for(Movie m : clusters[i].getItems()) {
				totalRank += m.getRank();
			}
			double avgRank = totalRank / clusters[i].getItems().size();

			if(avgRank > currHighRank) {
				index = i;
				currHighRank = avgRank;
			}
		}

		return index;
	}

	private boolean isClusterMedoid(Movie m, Cluster[] clusters) {
		for(int i = 0; i < clusters.length; i++) {
			if(m.getTitle().equalsIgnoreCase(clusters[i].getMedoid().getTitle())) {
				return true;
			}
		}
		return false;
	}

	public void clearClusters(Cluster[] clusters) {
		for(int i = 0; i < clusters.length; i++) {
			clusters[i].clearItems();
		}
	}

	private Cluster[] initClusters(int k, List<Movie> movies) {
		Cluster[] clusters = new Cluster[k];
		int spacer = movies.size() / k;

		for(int i = 0; i < k; i++) {
			clusters[i] = new Cluster(movies.get(i*spacer));
		}

		return clusters;
	}

	private double computeDistance(Movie m, Movie medoid) {
		double distance = 0.0;

		//Compare Years
		distance -= (6-Math.abs(((m.getYear()/10)-(medoid.getYear()/10)))) / 2;
		//Compare Ratings
		if(m.getMPAA().equalsIgnoreCase(medoid.getMPAA())) {
			distance -= 1;
		}
		//Compare Genres
		for(String g : m.getGenres()) {
			for(String dG : medoid.getGenres()) {
				if(g.equalsIgnoreCase(dG)) {
					distance -= 1;
				}
			}
		}
		//Compare Director
		for(String d : m.getDirectors()) {
			for(String dD : medoid.getDirectors()) {
				if(d.equalsIgnoreCase(dD)) {
					distance -= 1;
				}
			}
		}
		//Compare Actors
		for(String a : m.getActors()) {
			for(String dA : medoid.getActors()) {
				if(a.equalsIgnoreCase(dA)) {
					distance -= 1;
				}
			}
		}
		//Compare Keywords
		for(String k : m.getKeywords()) {
			for(String dK : medoid.getKeywords()) {
				if(k.equalsIgnoreCase(dK)) {
					distance -= 1;
				}
			}
		}
		//Compare Ranks (May need revision)
		distance-=(Math.max(m.getRank(),medoid.getRank())-Math.abs(m.getRank()-medoid.getRank()))/2;

		return distance;
	}

	private void assignClusterByDistance(Cluster[] clusters, List<Movie> movies) {

		for(Movie m : movies) {
			Cluster nearestCluster = null;
			double minDistance = 0.0;
			for(int i = 0; i < clusters.length; i++) {
				Cluster cluster = clusters[i];
				double distance = computeDistance(m, cluster.getMedoid());
				if(distance < minDistance) {
					nearestCluster = cluster;
					minDistance = distance;
					m.setDistance(distance);
				}
			}
			if(isClusterMedoid(m,clusters))
				m.setDistance(0);
			nearestCluster.addItem(m);
		}
	}
}
