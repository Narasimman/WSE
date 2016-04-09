package prog3;

import htmlparser.HTMLParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import prog2.Link;

public class Page {
  private double base;
  private double score;
  private String path;
  private int wordCount;
  private List<String> outLinks;
  private static int id_counter = 0;
  private final int id;

  public Page(File file) throws IOException {
    id = id_counter++;
    path = file.getCanonicalPath();

    Element document = HTMLParser.parse(new FileReader(file));
    String content = HTMLParser.getText(document);
    if(content != null) {
      wordCount = content.split(" ").length;
    }

    base = Math.log(wordCount)/Math.log(2);
    
    outLinks = new ArrayList<String>();
    NodeList anchors = HTMLParser.getAnchors(document);

    for (int i = 0; i < anchors.getLength(); i++) {
      Element element = ((Element) anchors.item(i));
      String url = element.getAttribute("href");
      outLinks.add(url);
    }    
  }

  /**
   * @return the score
   */
  public double getScore() {
    return score;
  }

  public int getId() {
    return id;
  }
  
  public String getPath() {
    return path;
  }

  /**
   * @param score the score to set
   */
  public void setScore(double score) {
    this.score = score;
  }

  public void setBase(double score) {
    this.base = score;
  }

  public List<String> getOutLinks() {
    return outLinks;
  }
  
  public boolean hasOutLinks() {
    return outLinks.size() > 0;
  }

  /**
   * @return the base
   */
  public double getBase() {
    return base;
  }

  /**
   * @return the wordCount
   */
  public int getWordCount() {
    return wordCount;
  }
}
