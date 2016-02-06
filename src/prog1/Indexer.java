package prog1;

import htmlparser.JTidyHTMLHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
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
    writer.close(); //4
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
    return writer.numDocs(); //5
  }
  
  protected boolean acceptFile(File f) { //6
    return f.getName().endsWith(".html");
  }
  
  protected Document getDocument(File f) throws Exception {
    //Document doc = new Document();
    JTidyHTMLHandler handler = new JTidyHTMLHandler();
    org.apache.lucene.document.Document doc = handler.getDocument(
            new FileInputStream(f));
    //doc.add(new TextField("contents", new FileReader(f))); //7
    doc.add(new StringField("filename", f.getCanonicalPath(), //8
        Field.Store.YES));
    return doc;
  }
  
  private void indexFile(File f) throws Exception {
    System.out.println("Indexing " + f.getCanonicalPath());
    Document doc = getDocument(f);
    if (doc != null) {
      writer.addDocument(doc); //9
    }
  }
  
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      throw new Exception("Usage: java " + Indexer.class.getName()
          + " <index dir> <data dir>");
    }
    String indexDir = args[0]; //1
    String dataDir = args[1]; //2
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
