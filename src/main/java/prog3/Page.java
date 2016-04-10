package prog3;

import htmlparser.HTMLParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
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
  private Map<String, Integer> outLinkScore = new HashMap<String, Integer>();
  private final int id;

  private static int id_counter = 0;
  
  public Page(File file) throws IOException {
    id = id_counter++;
    path = file.getName();

    Element document = HTMLParser.parse(new FileReader(file));
    String content = HTMLParser.getText(document);

    if(content != null) {
      int wordCount = content.trim().split(" ").length;
      base = Math.log(wordCount)/Math.log(2);
    }

    NodeList anchors = HTMLParser.getAnchors(document);

    for (int i = 0; i < anchors.getLength(); i++) {
      Element element = ((Element) anchors.item(i));
      String url = element.getAttribute("href");

      Node parent = element.getParentNode();

      int score = 1;
      while(parent.getNodeName() != null && parent.getNodeName() != "html") {
        if(parent.getNodeName().equals("b") || 
            parent.getNodeName().equals("em") || 
            parent.getNodeName().equals("h1") || 
            parent.getNodeName().equals("h2") ||
            parent.getNodeName().equals("h3")) {
          score++;         
          break;
        }
        parent = parent.getParentNode();
      }

      outLinkScore.put(url, score);      
    }    
  }

  public int getOutLinkScore(String outLink) {
    return outLinkScore.get(outLink) == null? 0 : outLinkScore.get(outLink);
  }

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
    return outLinkScore.keySet();
  }

  public boolean hasOutLinks() {
    return outLinkScore.size() > 0;
  }

  public double getBase() {
    return base;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Page)) {
      return false;
    }
    Page other = (Page) obj;
    if (path == null) {
      if (other.path != null) {
        return false;
      }
    } else if (!path.equals(other.path)) {
      return false;
    }
    return true;
  }
  
  
}
