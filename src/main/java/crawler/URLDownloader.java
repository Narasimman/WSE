package crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class URLDownloader {
	public static boolean downloadPage(String filename, String content) {
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


}
