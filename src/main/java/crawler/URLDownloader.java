package crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;

public class URLDownloader {
	public static boolean savePage(String filename, String content) {
		Writer writer = null;
		File file  = new File(filename);
		File parent = file.getParentFile();

		if(!parent.exists()) {
			parent.mkdirs();
		}

		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "utf-8"));
			writer.write(content);
		} catch (IOException ex) {
			return false;
		} finally {
			try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
		return true;
	}

	//Download contents of URL
	public static String getpage(Link link, int maxSize)	{
		URL url = link.getUrl();
		try { 
			// try opening the URL
			URLConnection urlConnection = url.openConnection();
			System.out.println("Downloading " + url.toString() + " Score = " + link.getScore());

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

}
