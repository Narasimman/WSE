package prog3;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class PageRanker {

  private final Path collectionPath;
  private final File[] files;
  private Map<String, Page> pages;

  private final int numberOfPages;

  private static final double epsilonFactor = 0.01;
  private final double epsilon;
  private final double F;

  private double[][] weight;

  public PageRanker(String filePath, double f) {    
    collectionPath = FileSystems.getDefault().getPath(filePath);
    files = collectionPath.toFile().listFiles();
    numberOfPages = files.length;
    epsilon = epsilonFactor/numberOfPages;
    this.F = f;
    weight = new double[numberOfPages][numberOfPages];
    pages = new TreeMap<String, Page>();
  }

  public void parsePages() throws IOException {
    for(File file : files) {
      Page page = new Page(file);
      pages.put(file.getName(), page);
    }
  }

  private double calculateWeight(Page p, Page q) {
    Set<String> outLinks = p.getOutLinks();

    double score = 0.0;
    String Q = q.getPath();
    for(String link : outLinks) {
      if(link.equals(Q)) {
        score += p.getOutLinkScore(link);
      }
    }
    return score;
  }

  private void compute() {
    double sum = 0.0;

    for(Page page : pages.values()) {
      sum += page.getBase();
    }

    // Calculate φ'
    for(Page page : pages.values()) {
      double normalizedSum = page.getBase()/sum;
      page.setScore(normalizedSum);
      page.setBase(normalizedSum);      
    }    

    for(Page page : pages.values()) {      
      int P = page.getId();

      if(!page.hasOutLinks()) {
        for(Page qPage : pages.values()) {          
          int Q = qPage.getId();          
          weight[Q][P] = 1.0/numberOfPages;
        }
      } else {
        Set<String> outLinks = page.getOutLinks();

        for(String outLink : outLinks) {
          Page qPage = pages.get(outLink);
          int Q = qPage.getId();        
          weight[Q][P] = calculateWeight(page, qPage);
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

  private void run() {
    // Compute the weight matrix
    compute();

    boolean changed = true;

    while(changed) {
      changed = false;
      for(Page page : pages.values()) {
        int P = page.getId();

        double normalizedScore = 0.0;
        for(Page qPage : pages.values()) {
          int Q = qPage.getId();
          normalizedScore += (qPage.getScore() * weight[P][Q]);          
        }

        double newScore = ((1.0 - F) * page.getBase()) + (F * normalizedScore);
        page.setNewScore(newScore);

        if(Math.abs(page.getNewScore() - page.getScore()) > epsilon) {
          changed = true;
        }
      }

      for(Page page : pages.values()) {
        page.setScore(page.getNewScore());
      }
    } //while    
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
    pr.run();

    for(Page page : pr.pages.values()) {
      System.out.println(page.getPath() + "\t" + page.getScore());
    }
  }
}
