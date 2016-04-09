package prog3;

import htmlparser.HTMLParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Page {
  private double base;
  private double score;
  private double newScore;
  private String path;
  private int wordCount;
  private Set<String> outLinks;
  private Map<String, Integer> outLinkScore = new HashMap<String, Integer>();
  private static int id_counter = 0;
  private final int id;

  public Page(File file) throws IOException {
    id = id_counter++;
    path = file.getName();

    Element document = HTMLParser.parse(new FileReader(file));
    String content = HTMLParser.getText(document);
    if(content != null) {
      wordCount = content.split(" ").length;
    }

    base = Math.log(wordCount)/Math.log(2);

    outLinks = new HashSet<String>();
    NodeList anchors = HTMLParser.getAnchors(document);

    for (int i = 0; i < anchors.getLength(); i++) {
      Element element = ((Element) anchors.item(i));
      String url = element.getAttribute("href");


      Node parent = element.getParentNode();
      int score = 1;
      while(parent.getNodeName() != null && parent.getNodeName() != "html") {
        if(parent.getNodeName().equals("b")) {
          score++;         
          break;
        }
        parent = parent.getParentNode();
      }
      
      if(outLinkScore.get(url) != null) {
        outLinkScore.put(url, outLinkScore.get(url) + score);
      } else {
        outLinkScore.put(url, score);
      }

      outLinks.add(url);
    }    
  }

  public int getOutLinkScore(String outLink) {
    return outLinkScore.get(outLink) == null? 0 : outLinkScore.get(outLink);
  }

  /**
   * @return the score
   */
  public double getScore() {
    return score;
  }

  public double getNewScore() {
    return newScore;
  }
  
  public void setNewScore(double s) {
    newScore = s;
  }
  
  public int getId() {
    return id;
  }

  public String getPath() {
    return path;
  }

  public void setScore(double score) {
    this.score = score;
  }

  public void setBase(double score) {
    this.base = score;
  }

  public Set<String> getOutLinks() {
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
