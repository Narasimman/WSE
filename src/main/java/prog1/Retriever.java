package prog1;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


public class Retriever {
  public static Map<String,String> search(String indexDir, String q)
      throws Exception {
    Path path = FileSystems.getDefault().getPath(indexDir);
    Directory dir = FSDirectory.open(path);

    IndexReader reader = DirectoryReader.open(dir);
    IndexSearcher is = new IndexSearcher(reader);
    QueryParser parser = new QueryParser("body", new StandardAnalyzer());
    Query query = parser.parse(q);
    long start = System.currentTimeMillis();
    TopDocs hits = is.search(query, 10);
    long end = System.currentTimeMillis();
    System.err.println("Found " + hits.totalHits +
        " document(s) (in " + (end - start) +
        " milliseconds) that matched query '" +
        q + "':");
    
    Map<String,String> result = new HashMap<String,String>();
    
    
    for(int i=0; i < hits.scoreDocs.length; i++) {
      ScoreDoc scoreDoc = hits.scoreDocs[i];
      Document doc = is.doc(scoreDoc.doc);
      //System.out.println(doc.get("filename"));
      //System.out.println(doc.get("title"));
      
      
      result.put(doc.get("title"), doc.get("filename"));
      
    }
    return result;
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      throw new Exception("Usage: java " + Retriever.class.getName()
          + " <index dir> <query>");
    }

    String indexDir = args[0];
    String q = args[1];
    search(indexDir, q);
  }
}