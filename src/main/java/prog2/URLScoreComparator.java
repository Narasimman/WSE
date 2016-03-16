package prog2;

import java.util.Comparator;

public class URLScoreComparator implements Comparator<Link> {

  public int compare(Link x, Link y) {
    if (x.getScore() > y.getScore()) {
      return -1;
    }

    if (x.getScore() < y.getScore()) {
      return 1;
    }

    /* FIFO for same score - to break ties */
    if (x.getId() > y.getId()) {
      return 1;
    }

    if (x.getId() < y.getId()) {
      return -1;
    }
    return 0;
  }

}
