import java.util.List;
import java.util.LinkedList;

public class Cluster
{
	private Movie medoid;
	private List<Movie> mClusterItems;

	public Cluster(Movie center) {
		medoid = center;
		mClusterItems = new LinkedList<Movie>();
	}

	public void removeItem(Movie movie) {
		mClusterItems.remove(movie);
	}

	public void addItem(Movie movie) {
		mClusterItems.add(movie);
	}

	public void clearItems() {
		mClusterItems.clear();
	}

	public List<Movie> getItems() {
		return mClusterItems;
	}

	public Movie getMedoid() {
		return medoid;
	}

	public void setMedoid(Movie m) {
		medoid = m;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();

		for(Movie m : mClusterItems)
			result.append(m.getTitle() + " [" + m.getDistance() + "], ");

		return result.toString();
	}
}
