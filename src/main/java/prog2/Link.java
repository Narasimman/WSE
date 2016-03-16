package prog2;

import java.net.URL;

public class Link {
  private final URL url;
  private int score;
  private final String anchor;
  private final int id;
  private static int nextId;

  public Link(URL url, String anchor) {
    this.id = ++nextId;
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

  public int getId() {
    return id;
  }

  public void setScore(int s) {
    this.score = s;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((url == null) ? 0 : url.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Link)) {
      return false;
    }
    Link other = (Link) obj;
    if (url == null) {
      if (other.url != null) {
        return false;
      }
    } else if (!url.equals(other.url)) {
      return false;
    }
    return true;
  }
}
