package crawler;

import java.net.URL;

public class Link {
	private final URL url;
	private int score;
	private final String anchor;
	
	public Link(URL url, String anchor) {
		this.url = url;
		this.score = 0;
		this.anchor = anchor;
	}
	
	public URL getUrl() {
		return url;
	}
	
	public int getScore() {
		return score;
	}
	
	public String getAnchor() {
		return anchor;
	}
	
	public void setScore(int s) {
		this.score = s;
	}
}
