package prog1;

import htmlparser.JTidyHTMLHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {
  private IndexWriter writer;
  
  public Indexer(String indexDir) throws IOException {
    Path path = FileSystems.getDefault().getPath(indexDir);
    Directory dir = FSDirectory.open(path);
    writer = new IndexWriter(dir, 
        new IndexWriterConfig(new StandardAnalyzer()));
  }
  
  public void close() throws IOException {
    writer.close();
  }
  
  public int index(String dataDir) throws Exception {
    File[] files = new File(dataDir).listFiles();
    for (int i = 0; i < files.length; i++) {
      File f = files[i];
      if (!f.isDirectory() &&
          !f.isHidden() &&
          f.exists() &&
          f.canRead() &&
          acceptFile(f)) {
        indexFile(f);
      }
    }
    return writer.numDocs();
  }
  
  protected boolean acceptFile(File f) {
    return f.getName().endsWith(".html") || f.getName().endsWith(".htm");
  }
  
  /**
   * 
   * @param f
   * @return
   * @throws Exception
   */
  protected Document getDocument(File f) throws Exception {
    JTidyHTMLHandler handler = new JTidyHTMLHandler();
    org.apache.lucene.document.Document doc = handler.getDocument(
            new FileInputStream(f));
    doc.add(new StringField("filename", f.getCanonicalPath(),
        Field.Store.YES));
    return doc;
  }
  
  private void indexFile(File f) throws Exception {
    System.out.println("Indexing " + f.getCanonicalPath());
    Document doc = getDocument(f);
    if (doc != null) {
      writer.addDocument(doc);
    }
  }
  
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      throw new Exception("Usage: java " + Indexer.class.getName()
          + " <index dir> <data dir>");
    }
    String indexDir = args[0];
    String dataDir = args[1];
    long start = System.currentTimeMillis();
    Indexer indexer = new Indexer(indexDir);
    int numIndexed = indexer.index(dataDir);
    System.out.println(dataDir);
    indexer.close();
    long end = System.currentTimeMillis();
    System.out.println("Indexing " + numIndexed + " files took "
        + (end - start) + " milliseconds");
  }
}
