package crawler;

import java.util.Comparator;

public class URLScoreComparator implements Comparator<Link> {

	public int compare(Link x, Link y) {
		if(x.getScore() < y.getScore()) {
			return -1;
		}
		
		if(x.getScore() > y.getScore()) {
			return 1;
		}
		
		return 0;
	}

}
