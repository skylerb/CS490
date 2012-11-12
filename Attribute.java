public class Attribute implements Comparable<Attribute> {

	private int attrIndex;
	private String attrVal;
	private int count;

	public Attribute(int index, String val) {
		this.attrIndex = index;
		this.attrVal = val;
		this.count = 1;
	}

	public void incrCount() {
		this.count += 1;
	}

	public int getCount() {
		return count;
	}

	public int getIndex() {
		return attrIndex;
	}

	public String getVal() {
		return attrVal;
	}

	public int compareTo(Attribute anotherInstance) {
		int delta = this.count - anotherInstance.getCount();
		if(delta > 0) return -1;
		if(delta < 0) return 1;
		return 0;
	}

	public String toString(){
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");

		switch(attrIndex) {
			case 0:
				result.append("MPAA: " + attrVal + " " + count + NEW_LINE);
				break;
			case 1:
				result.append("Genre: " + attrVal + " " + count + NEW_LINE);
				break;
			case 2:
				result.append("Director: " + attrVal + " " + count + NEW_LINE);
				break;
			case 3:
				result.append("Actor: " + attrVal + " " + count + NEW_LINE);
		}

		return result.toString();
	}
}
