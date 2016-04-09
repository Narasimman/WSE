package htmlparser;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

public class JTidyHTMLHandler {

  public org.apache.lucene.document.Document getDocument(InputStream is) {

    Tidy tidy = new Tidy();
    tidy.setQuiet(true);
    tidy.setShowWarnings(false);
    org.w3c.dom.Document root = tidy.parseDOM(is, null);
    Element rawDoc = root.getDocumentElement();

    org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();

    String title = getTitle(rawDoc);

    if (title.length() <= 0) {
      title = getHeading(rawDoc);
    }

    String body = getBody(rawDoc);
    if ((title != null) && (!title.equals(""))) {
      doc.add(new TextField("title", title, Field.Store.YES));
    }
    if ((body != null) && (!body.equals(""))) {
      doc.add(new TextField("body", body, Field.Store.YES));
    }

    return doc;
  }

  public Element getRawDocument(Reader page) throws FileNotFoundException {
    Tidy tidy = new Tidy();
    tidy.setQuiet(true);
    tidy.setShowWarnings(false);
    org.w3c.dom.Document root = tidy.parseDOM(page, null);
    Element rawDoc = root.getDocumentElement();

    return rawDoc;
  }

  public Document parseWebPage(String page) {
    Tidy tidy = new Tidy();
    tidy.setQuiet(true);
    tidy.setShowWarnings(false);
    org.w3c.dom.Document root = tidy.parseDOM(new StringReader(page),
        new StringWriter());
    Element rawDoc = root.getDocumentElement();
    getAnchors(rawDoc);
    return root;

  }

  /**
   * Gets the title text of the HTML document.
   * 
   * @rawDoc the DOM Element to extract title Node from
   * @return the title text
   */
  protected String getTitle(Element rawDoc) {
    if (rawDoc == null) {
      return null;
    }

    String title = "";

    NodeList children = rawDoc.getElementsByTagName("title");
    if (children.getLength() > 0) {
      Element titleElement = ((Element) children.item(0));
      Text text = (Text) titleElement.getFirstChild();
      if (text != null) {
        title = text.getData();
      }
    }
    return title;
  }

  /**
   * Gets the body text of the HTML document.
   * 
   * @rawDoc the DOM Element to extract body Node from
   * @return the body text
   */
  protected String getBody(Element rawDoc) {
    if (rawDoc == null) {
      return null;
    }

    String body = "";
    NodeList children = rawDoc.getElementsByTagName("body");
    if (children.getLength() > 0) {
      body = getText(children.item(0));
    }
    return body;
  }

  /**
   * Gets the body text of the HTML document.
   * 
   * @rawDoc the DOM Element to extract body Node from
   * @return the body text
   */
  protected String getHeading(Element rawDoc) {
    if (rawDoc == null) {
      return null;
    }

    String heading = "";
    NodeList children = rawDoc.getElementsByTagName("h1");
    if (children.getLength() > 0) {
      heading = getText(children.item(0));
    }
    return heading;
  }

  /**
   * Extracts text from the DOM node.
   * 
   * @param node
   *          a DOM node
   * @return the text value of the node
   */
  public String getText(Node node) {
    StringBuffer sb = new StringBuffer();
    NodeList children = node.getChildNodes();

    if (children.getLength() == 0) {
      if (node.getNodeType() == Node.TEXT_NODE) {
        sb.append(((Text) node).getData());
      }
    }

    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      switch (child.getNodeType()) {
      case Node.ELEMENT_NODE:
        sb.append(getText(child));
        sb.append(" ");
        break;
      case Node.TEXT_NODE:
        sb.append(((Text) child).getData());
        break;
      }
    }
    return sb.toString();
  }

  public NodeList getAnchors(Element rawDoc) {
    if (rawDoc == null) {
      return null;
    }
    NodeList children = rawDoc.getElementsByTagName("a");
    return children;
  }

  public static void main(String args[]) throws Exception {
    JTidyHTMLHandler handler = new JTidyHTMLHandler();

    Tidy tidy = new Tidy();
    tidy.setQuiet(true);
    tidy.setShowWarnings(false);
  }
}