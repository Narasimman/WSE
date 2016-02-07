package prog1;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class Searcher {
  public static String search(String indexDir, String q)
      throws Exception {
    Path path = FileSystems.getDefault().getPath(indexDir);
    Directory dir = FSDirectory.open(path);

    IndexReader reader = DirectoryReader.open(dir);
    IndexSearcher is = new IndexSearcher(reader); //3
    QueryParser parser = new QueryParser("body", new StandardAnalyzer()); //4
    Query query = parser.parse(q); //4
    long start = System.currentTimeMillis();
    TopDocs hits = is.search(query, 10); //5
    long end = System.currentTimeMillis();
    System.err.println("Found " + hits.totalHits + //6
        " document(s) (in " + (end - start) +
        " milliseconds) that matched query '" +
        q + "':");
    String result = null;

    for(int i=0;i<hits.scoreDocs.length;i++) {
      ScoreDoc scoreDoc = hits.scoreDocs[i];
      Document doc = is.doc(scoreDoc.doc); //7
      //System.out.println(doc.get("filename")); //8
      //System.out.println(doc.get("title"));

      result += doc.get("filename");

    }
    return result;
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      throw new Exception("Usage: java " + Searcher.class.getName()
          + " <index dir> <query>");
    }

    String indexDir = args[0]; //1
    String q = args[1]; //2
    search(indexDir, q);
  }
}