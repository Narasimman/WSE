package prog3;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class PageRanker {

  private final Path collectionPath;
  private final File[] files;
  private List<Page> pages;
  
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
  }
  
  public static void main(String[] args) {
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
    
  }

}
