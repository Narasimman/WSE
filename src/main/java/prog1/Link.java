package prog1;

import java.net.URL;

public class Link {
	private final URL url;
	private int score;
	
	public Link(URL url) {
		this.url = url;
		this.score = 0;
	}
	
	public URL getUrl() {
		return url;
	}
	
	public int getScore() {
		return score;
	}
}
