package prog3;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public class PageRanker {

  private final Path collectionPath;
  private final int numberOfPages;
  
  private static final double epsilonFactor = 0.01;
  private static double epsilon;
  private static double F;
  
  private double[][] weight;
  
  public PageRanker(String filePath, double F) {
    
    collectionPath = FileSystems.getDefault().getPath(filePath);    
    numberOfPages = 0;
    
    epsilon = epsilonFactor/numberOfPages;
    this.F = F;
    
    weight = new double[numberOfPages][numberOfPages];
  }
  
  public static void main(String[] args) {
    
  }

}
