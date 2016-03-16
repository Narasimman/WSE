package prog2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

public class URLHandler {
  private static final String DISALLOW = "Disallow:";

  public static boolean savePage(String filename, String content) {
    Writer writer = null;
    File file = new File(filename);
    File parent = file.getParentFile();

    if (!parent.exists()) {
      parent.mkdirs();
    }

    try {
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
          file), "utf-8"));
      writer.write(content);
    } catch (IOException ex) {
      return false;
    } finally {
      try {
        writer.close();
      } catch (Exception ex) {/* ignore */
      }
    }
    return true;
  }

  // Download contents of URL
  public static String getpage(Link link, int maxSize) {
    URL url = link.getUrl();
    try {
      // try opening the URL
      URLConnection urlConnection = url.openConnection();
      urlConnection.setAllowUserInteraction(false);

      InputStream urlStream = url.openStream();
      // search the input stream for links. first, read in the entire URL
      byte b[] = new byte[1000];
      int numRead = urlStream.read(b);
      String content = new String(b, 0, numRead);
      while ((numRead != -1) && (content.length() < maxSize)) {
        numRead = urlStream.read(b);
        if (numRead != -1) {
          String newContent = new String(b, 0, numRead);
          content += newContent;
        }
      }
      return content;

    } catch (IOException e) {
      System.out.println("ERROR: couldn't open URL " + url);
      return "";
    }
  }

  /**
   * Check that the robot exclusion protocol does not disallow downloading url.
   * 
   * @param url
   * @return safe or not
   */
  public static boolean robotSafe(URL url) {
    String strHost = url.getHost();

    // form URL of the robots.txt file
    String strRobot = "http://" + strHost + "/robots.txt";
    URL urlRobot;
    try {
      urlRobot = new URL(strRobot);
    } catch (MalformedURLException e) {
      // something weird is happening, so don't trust it
      return false;
    }

    String strCommands;
    try {
      InputStream urlRobotStream = urlRobot.openStream();

      // read in entire file
      byte b[] = new byte[1000];
      int numRead = urlRobotStream.read(b);
      strCommands = new String(b, 0, numRead);
      while (numRead != -1) {
        numRead = urlRobotStream.read(b);
        if (numRead != -1) {
          String newCommands = new String(b, 0, numRead);
          strCommands += newCommands;
        }
      }
      urlRobotStream.close();
    } catch (IOException e) {
      // if there is no robots.txt file, it is OK to search
      return true;
    }

    // assume that this robots.txt refers to us and
    // search for "Disallow:" commands.
    String strURL = url.getFile();
    int index = 0;
    while ((index = strCommands.indexOf(DISALLOW, index)) != -1) {
      index += DISALLOW.length();
      String strPath = strCommands.substring(index);
      StringTokenizer st = new StringTokenizer(strPath);

      if (!st.hasMoreTokens()) {
        break;
      }
      String strBadPath = st.nextToken();

      // if the URL starts with a disallowed path, it is not safe
      if (strURL.indexOf(strBadPath) == 0)
        return false;
    }
    return true;
  }
}
