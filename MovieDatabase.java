import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class MovieDatabase extends DefaultHandler{

	List<Movie> movies;

	private String tempVal;

	//to maintain context
	private Movie tempMovie;

	public MovieDatabase(){
		movies = new ArrayList<Movie>();
	}

	public void runExample() {
		parseDocument();
		printData();
	}

	private void parseDocument() {

		//get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {

			//get a new instance of parser
			SAXParser sp = spf.newSAXParser();

			//parse the file and also register this class for call backs
			sp.parse("Movies.xml", this);

		}catch(SAXException se) {
			se.printStackTrace();
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	/**
	 * Iterate through the list and print
	 * the contents
	 */
	private void printData(){
		System.out.println("No of Movies '" + movies.size() + "'.");
	}

	//Event Handlers
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		//reset
		tempVal = "";
		if(qName.equalsIgnoreCase("movie")) {
			//create a new instance of employee
			tempMovie = new Movie();
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		tempVal = new String(ch,start,length);
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if(qName.equalsIgnoreCase("movie")) {
			//add it to the list
			movies.add(tempMovie);
			
		}else if (qName.equalsIgnoreCase("Title")) {
			tempMovie.setTitle(tempVal);
		}
		
	}
	
	public static void main(String[] args){
		MovieDatabase md = new MovieDatabase();
		md.runExample();
	}
	
}
