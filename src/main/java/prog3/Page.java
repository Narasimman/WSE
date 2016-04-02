package prog3;

import java.util.ArrayList;
import java.util.List;

import prog2.Link;

public class Page {
  private double base;
  private double score;
  private Link link;
  private int wordCount;
  private List<Link> outLinks;
  
  public Page(Link link, String content) {
    //#TODO;
    
    outLinks = new ArrayList<Link>();
  }

  /**
   * @return the score
   */
  public double getScore() {
    return score;
  }

  /**
   * @param score the score to set
   */
  public void setScore(double score) {
    this.score = score;
  }

  /**
   * @return the base
   */
  public double getBase() {
    return base;
  }

  /**
   * @return the link
   */
  public Link getLink() {
    return link;
  }

  /**
   * @return the wordCount
   */
  public int getWordCount() {
    return wordCount;
  }
}
