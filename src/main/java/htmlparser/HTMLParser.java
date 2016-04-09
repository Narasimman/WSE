package htmlparser;

import java.io.FileNotFoundException;
import java.io.Reader;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class HTMLParser {
  private static final JTidyHTMLHandler handler = new JTidyHTMLHandler();
  
  public static Element parse(Reader page) throws FileNotFoundException {
    return handler.getRawDocument(page);
  }
  
  public static String getText(Element doc) {
    return handler.getText(doc);
  }
  
  public static NodeList getAnchors(Element doc) {
    return handler.getAnchors(doc);
  }
}
