package prog1;

import htmlparser.JTidyHTMLHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {
  private final IndexWriter writer;

  public Indexer(String indexDir) throws IOException {
    Path path = FileSystems.getDefault().getPath(indexDir);
    Directory dir = FSDirectory.open(path);
    writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()));
  }

  public void close() throws IOException {
    writer.close();
  }

  /**
   * If the file is valid to be indexed, add it to the writer
   * 
   * @param dataDir
   * @return
   * @throws Exception
   */
  public int index(String dataDir) throws Exception {
    File[] files = new File(dataDir).listFiles();
    for (int i = 0; i < files.length; i++) {
      File f = files[i];
      if (!f.isDirectory() && !f.isHidden() && f.exists() && f.canRead()
          && acceptFile(f)) {
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
    org.apache.lucene.document.Document doc = handler
        .getDocument(new FileInputStream(f));
    doc.add(new StringField("filename", f.getCanonicalPath(), Field.Store.YES));
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
    String usage = "Usage: " + Indexer.class.getName()
        + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
        + "This indexes the documents in DOCS_PATH, creating a Lucene index"
        + "in INDEX_PATH that can be searched with SearchFiles";

    String indexPath = "index";
    String docsPath = null;
    boolean create = true;

    for (int i = 0; i < args.length; i++) {
      if ("-index".equals(args[i])) {
        indexPath = args[i + 1];
        i++;
      } else if ("-docs".equals(args[i])) {
        docsPath = args[i + 1];
        i++;
      } else if ("-update".equals(args[i])) {
        create = false;
      }
    }

    if (docsPath == null) {
      System.err.println("Usage: " + usage);
      System.exit(1);
    }

    final Path docDir = Paths.get(docsPath);
    if (!Files.isReadable(docDir)) {
      System.out.println("Document directory '" + docDir.toAbsolutePath()
          + "' does not exist or is not readable, please check the path");
      System.exit(1);
    }

    long start = System.currentTimeMillis();
    Indexer indexer = new Indexer(indexPath);
    int numIndexed = indexer.index(docsPath);
    System.out.println(docsPath);
    indexer.close();
    long end = System.currentTimeMillis();
    System.out.println("Indexing " + numIndexed + " files took "
        + (end - start) + " milliseconds");
  }
}
