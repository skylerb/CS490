import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

public class MovieRankHandler extends DefaultHandler {

	private static final String TAG_MOVIES = "movies";
	private static final String TAG_MOVIE = "movie";
	private static final String TAG_TITLE = "title";
	private static final String TAG_YEAR = "year";
	private static final String TAG_MPAA = "mpaa";
	private static final String TAG_GENRE = "genre";
	private static final String TAG_DIRECTOR = "director";
	private static final String TAG_ACTOR = "actor";
	private static final String TAG_KEYWORD = "keyword";
	private static final String TAG_COVERART = "coverart";
	private static final String TAG_TOP250 = "top250";

	private final Stack<String> tagsStack = new Stack<String>();
	private final StringBuilder tempVal = new StringBuilder();

	private List<Movie> movies;
	private Movie movie;

	private boolean ADD_FLAG = false;
	private boolean SAME_MOVIE = false;
	private List<String> genres = null;
	private int mYear = 0;
	private String mTitle = null;
	private List<String> directors = null;
	private String mMPAA = null;
	private List<String> actors = null;
	private List<String> keywords = null;

	public MovieRankHandler(Movie movie) {
		this.mTitle = movie.getTitle();
		this.mMPAA = movie.getMPAA();
		this.directors = movie.getDirectors();
		this.genres = movie.getGenres();
		this.mYear = movie.getYear();
		this.actors = movie.getActors();
		this.keywords = movie.getKeywords();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		pushTag(qName);
		tempVal.setLength(0);
		if( TAG_MOVIES.equalsIgnoreCase(qName)) {
			movies = new ArrayList<Movie>();
		} else if( TAG_MOVIE.equalsIgnoreCase(qName)) {
			movie = new Movie();
		}
	}

	public void characters(char ch[], int start, int length) {
		tempVal.append(ch,start,length);
	}

	public void endElement(String uri, String localName, String qName) {
		String tag = peekTag();
		if(!qName.equals(tag)) {
			throw new InternalError();
		}

		popTag();
		String parentTag = peekTag();

		if(TAG_TITLE.equalsIgnoreCase(tag)) { //Title
			String title = tempVal.toString().trim();
			if(title.equalsIgnoreCase(mTitle)){
				SAME_MOVIE = true;
			}
			movie.setTitle(title);
		} else if (TAG_YEAR.equalsIgnoreCase(tag)) { //Year
			int year = Integer.valueOf(tempVal.toString().trim());
			movie.setYear(year);
			if((year/10) == (mYear/10)) {
				movie.addRank(1);
			} else {
				SAME_MOVIE = false;
			}
		} else if (TAG_MPAA.equalsIgnoreCase(tag)) { //MPAA
			String mpaa = tempVal.toString().trim();
			if(mpaa.equalsIgnoreCase(mMPAA)){
				movie.addRank(1);
			}
			movie.setMPAA(mpaa);
		} else if(TAG_TOP250.equalsIgnoreCase(tag)) { //Top250
			int top250 = Integer.valueOf(tempVal.toString().trim());
			if(top250 == 1) {
				movie.addRank(0.5);
			}
		} else if (TAG_GENRE.equalsIgnoreCase(tag)) { //Genres
			String genre = tempVal.toString().trim();
			for(String g : genres) {
				if(genre.equalsIgnoreCase(g) && !SAME_MOVIE) {
					movie.addRank(2);
					ADD_FLAG = true;
				}
			}
			movie.addGenre(genre);
		} else if (TAG_DIRECTOR.equalsIgnoreCase(tag)) { //Director
			String director = tempVal.toString().trim();
			for(String d : directors) {
				if(director.equalsIgnoreCase(d)){
					movie.addRank(1.5);
				}
			}
			movie.addDirector(director);
		} else if (TAG_ACTOR.equalsIgnoreCase(tag)) { //Actors
			String actor = tempVal.toString().trim();
			for(String a : actors) {
				if(actor.equalsIgnoreCase(a)) {
					movie.addRank(1.25);
				}
			}
			movie.addActor(actor);
		} else if (TAG_KEYWORD.equalsIgnoreCase(tag)) { //Keywords
			String keyword = tempVal.toString().trim();
			for(String k : keywords) {
				if(keyword.equalsIgnoreCase(k)) {
					movie.addRank(1.25);
				}
			}
			movie.addKeyword(keyword);
		} else if (TAG_COVERART.equalsIgnoreCase(tag)) {
			String coverArt = tempVal.toString().trim();
			movie.setCoverArt(coverArt);
		} else if (TAG_MOVIE.equalsIgnoreCase(tag)) { //Add Movie
			if(ADD_FLAG && !SAME_MOVIE) {
				movies.add(movie);
			}
			ADD_FLAG = false;
			SAME_MOVIE = false;
		}
	}

	public void startDocument() {
		pushTag("");
	}

	public List<Movie> getMovies() {
		return movies;
	}

	private void pushTag(String tag) {
		tagsStack.push(tag);
	}

	private String popTag() {
		return tagsStack.pop();
	}

	private String peekTag() {
		return tagsStack.peek();
	}
}
