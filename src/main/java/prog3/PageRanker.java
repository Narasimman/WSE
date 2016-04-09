package prog3;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections.map.HashedMap;
import org.apache.lucene.search.Weight;

import prog2.Link;

public class PageRanker {

  private final Path collectionPath;
  private final File[] files;
  private Map<String, Page> pages;

  private final int numberOfPages;

  private static final double epsilonFactor = 0.01;
  private static double epsilon;
  private static double F;

  private double[][] weight;

  public PageRanker(String filePath, double F) {    
    collectionPath = FileSystems.getDefault().getPath(filePath);
    files = collectionPath.toFile().listFiles();
    numberOfPages = files.length;
    epsilon = epsilonFactor/numberOfPages;
    this.F = F;
    weight = new double[numberOfPages][numberOfPages];
    pages = new HashMap<String, Page>();
  }

  public void parsePages() throws IOException {
    for(File file : files) {
      Page page = new Page(file);
      pages.put(file.getName(), page);
    }
  }

  public void compute() {
    double sum = 0.0;
    
    //System.out.println(pages.values());

    for(Page page : pages.values()) {
      sum += page.getBase();
    }

    for(Page page : pages.values()) {
      double normalizedSum = page.getBase()/sum;
      page.setScore(normalizedSum);
      page.setBase(normalizedSum);
    }    
    
    for(String url : pages.keySet()) {
      System.out.println("Page" + url);
      Page page = pages.get(url);
      int P = page.getId();
      
      if(!page.hasOutLinks()) {
        for(String qUrl : pages.keySet()) {
          Page qPage = pages.get(qUrl);
          int Q = qPage.getId();          
          weight[Q][P] = 1.0/numberOfPages;
        }
      } else {
        List<String> outLinks = page.getOutLinks();
        
        int numOutLinks = outLinks.size();
        
        for(String outLink : outLinks) {
          Page qPage = pages.get(outLink);
          int Q = qPage.getId();        
          //System.out.println(numOutLinks);
          weight[Q][P] = F * (1.0/numOutLinks); // not sure what the formula here
          //System.out.println(Q + " " + P + "  :  " + F * weight[Q][P]);
        }
        
        double qSum = 0.0;
        
        for(int i = 0; i < weight.length; i++) {
          qSum += weight[i][P];
        }
        
        
        for(String outLink : outLinks) {
          Page qPage = pages.get(outLink);
          int Q = qPage.getId();        
          
          weight[Q][P] = weight[Q][P]/qSum;
        }
      }
    }
  }
  
  private void iterate() {
    boolean changed = true;
    
    while(changed) {
      changed = false;
      
      for(Page page : pages.values()) {
        
        List<String> outLinks = page.getOutLinks();
        int P = page.getId();
        
        double normailizedScore = 0.0;
        for(String outLink : outLinks) {
          Page qPage = pages.get(outLink);
          int Q = qPage.getId();
          normailizedScore += (qPage.getScore() * weight[P][Q]);
        }
        
        double newScore = (1.0 - F) * page.getBase() + (F * normailizedScore);
        
        if(Math.abs(newScore - page.getScore()) > epsilon) {
          changed = true;
        } 
        
        page.setScore(newScore);
        
        
      }
    } //while
    
    for(Page page : pages.values()) {
      System.out.println(page.getScore());
    }
    
  }

  public static void main(String[] args) throws IOException{
    CommandLine cmd = null;
    // set options
    Options options = new Options();
    options.addOption("docs", "docs", true, "collection of document");
    options.addOption("f", "f", true, "F");

    CommandLineParser parser = new DefaultParser();
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    String path = cmd.getOptionValue("docs");
    double F = Double.parseDouble(cmd.getOptionValue("f"));

    PageRanker pr = new PageRanker(path, F);

     pr.parsePages();
   
    pr.compute();
    
    pr.iterate();
    
    
    
    for(int i = 0; i < pr.weight.length; i++) {
      for(int j = 0; j < pr.weight[0].length; j++) {
        System.out.print(pr.weight[i][j] + " ");
      }
      System.out.println();
    }
    
    
    

  }

}
