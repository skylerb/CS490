import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class MovieHandler extends DefaultHandler {

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

	private final Stack<String> tagsStack = new Stack<String>();
	private final StringBuilder tempVal = new StringBuilder();

	private List<Movie> movies;
	private Movie movie;

	private boolean ADD_FLAG = false;
	private String searchQuery;

	public MovieHandler(String search) {
		this.searchQuery = search;
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

		if(TAG_TITLE.equalsIgnoreCase(tag)) {
			String title = tempVal.toString().trim();
			if(title.equalsIgnoreCase(searchQuery)) {
				ADD_FLAG = true;
			} else {
				ADD_FLAG = false;
			}
			movie.setTitle(title);
		} else if (TAG_YEAR.equalsIgnoreCase(tag)) {
			int year = Integer.valueOf(tempVal.toString().trim());
			movie.setYear(year);
		} else if (TAG_MPAA.equalsIgnoreCase(tag)) {
			String mpaa = tempVal.toString().trim();
			movie.setMPAA(mpaa);
		} else if (TAG_GENRE.equalsIgnoreCase(tag)) {
			String genre = tempVal.toString().trim();
			movie.addGenre(genre);
		} else if (TAG_DIRECTOR.equalsIgnoreCase(tag)) {
			String director = tempVal.toString().trim();
			movie.addDirector(director);
		} else if (TAG_ACTOR.equalsIgnoreCase(tag)) {
			String actor = tempVal.toString().trim();
			movie.addActor(actor);
		} else if (TAG_KEYWORD.equalsIgnoreCase(tag)) {
			String keyword = tempVal.toString().trim();
			movie.addKeyword(keyword);
		} else if (TAG_COVERART.equalsIgnoreCase(tag)) {
			String coverArt = tempVal.toString().trim();
			movie.setCoverArt(coverArt);
		} else if (TAG_MOVIE.equalsIgnoreCase(tag)) {
			if(ADD_FLAG && movies.size() < 1) {
				movies.add(movie);
			} else {
				ADD_FLAG = false;
			}
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
