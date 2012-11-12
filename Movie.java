import java.util.List;
import java.util.ArrayList;

public class Movie implements Comparable<Movie> {

	private String title;
	private int year;
	private String mpaa;
	private List<String> genres = new ArrayList<String>();
	private List<String> directors = new ArrayList<String>();
	private List<String> actors = new ArrayList<String>();
	private List<String> keywords = new ArrayList<String>();
	private double rank;
	private double distance;
	private String coverArt;

	public Movie() {
		this.rank = 0.0;
		this.distance = 0.0;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getMPAA() {
		return mpaa;
	}

	public void setMPAA(String mpaa) {
		this.mpaa = mpaa;
	}

	public void addGenre(String genre) {
		genres.add(genre);
	}

	public List<String> getGenres() {
		return genres;
	}

	public void addKeyword(String keyword) {
		keywords.add(keyword);
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void addDirector(String director) {
		directors.add(director);
	}

	public List<String> getDirectors() {
		return directors;
	}

	public void addActor(String actor) {
		actors.add(actor);
	}

	public List<String> getActors() {
		return actors;
	}

	public void setCoverArt(String coverArt) {
		this.coverArt = coverArt;
	}

	public String getCoverArt() {
		return coverArt;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");

		result.append("Title: " + title + NEW_LINE);
		result.append("Year: " + year + NEW_LINE);
		result.append("MPAA Rating: " + mpaa + NEW_LINE);
		result.append("Genres: " + NEW_LINE);
		for(String i : genres) {
			result.append("     " + i + NEW_LINE);
		}
		result.append("Directors: " + NEW_LINE);
		for(String d : directors) {
			result.append("     " + d + NEW_LINE);
		}
		result.append("Actors: " + NEW_LINE);
		for(String s : actors) {
			result.append("     " + s + NEW_LINE);
		}
		result.append("Keywords: " + NEW_LINE);
		for(String s : keywords) {
			result.append("     " + s + NEW_LINE);
		}
		result.append("Rank: " + rank + NEW_LINE);
		//result.append("Distance: " + distance + NEW_LINE);
		result.append(NEW_LINE);

		return result.toString();
	}

	public void addRank(double points) {
		this.rank += points;
	}

	public double getRank() {
		return rank;
	}

	public void setDistance(double d) {
		this.distance = d;
	}

	public double getDistance() {
		return distance;
	}

	public int compareTo(Movie anotherInstance) {
		double delta = this.rank - anotherInstance.getRank();
		if(delta > 0) return -1;
		if(delta < 0) return 1;
		return 0;
	}
}
