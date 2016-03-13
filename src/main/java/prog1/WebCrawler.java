package prog1;

import java.util.*;
import java.net.*;
import java.io.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class WebCrawler {
	public static final int    SEARCH_LIMIT = 20;  // Absolute max pages 
	public static final boolean DEBUG = false;
	public static final String DISALLOW = "Disallow:";
	public static final int MAXSIZE = 20000; // Max size of file 
	private Options options = new Options();
	private CommandLine cmd = null;

	// URLs to be searched
	PriorityQueue<Link> newURLs;
	// Known URLs
	Hashtable<Link,Integer> knownURLs;
	// max number of pages to download
	int maxPages; 


	public WebCrawler(String[] argv) {
		//set options
		options.addOption("u", true, "URL");
		options.addOption("q", false, "Query");
		options.addOption("docs", false, "Directory to download saved files");
		options.addOption("m", false, "Max pages to download");

		CommandLineParser parser = new DefaultParser();
		try {
			cmd = parser.parse(options, argv);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * initializes data structures.  argv is the command line arguments.
	 * @param argv
	 */
	public void initialize() {
		URL url;
		Link link;
		knownURLs = new Hashtable<Link,Integer>();
		
		URLScoreComparator comparator = new URLScoreComparator();
		newURLs = new PriorityQueue<Link>(SEARCH_LIMIT, comparator);
		String o_url = cmd.getOptionValue("u");
		String o_max = cmd.getOptionValue("m");
		try { 

			url = new URL(o_url);
			link = new Link(url);
		}
		catch (MalformedURLException e) {
			System.out.println("Invalid starting URL " + o_url);
			return;
		}
		knownURLs.put(link,new Integer(1));
		newURLs.add(link);
		System.out.println("Starting search: Initial URL " + url.toString());
		maxPages = SEARCH_LIMIT;
		if (o_max != null && !o_max.isEmpty()) {
			int iPages = Integer.parseInt(o_max);
			if (iPages < maxPages) {
				maxPages = iPages; 
			}
		}
		System.out.println("Maximum number of pages:" + maxPages);

		/* Behind a firewall set your proxy and port here! */
		Properties props= new Properties(System.getProperties());
		props.put("http.proxySet", "true");
		props.put("http.proxyHost", "webcache-cup");
		props.put("http.proxyPort", "8080");

		Properties newprops = new Properties(props);
		System.setProperties(newprops);
	}

	/**
	 * Check that the robot exclusion protocol does not disallow downloading url.
	 * @param url
	 * @return
	 */
	public boolean robotSafe(URL url) {
		String strHost = url.getHost();

		// form URL of the robots.txt file
		String strRobot = "http://" + strHost + "/robots.txt";
		URL urlRobot;
		try { urlRobot = new URL(strRobot);
		} catch (MalformedURLException e) {
			// something weird is happening, so don't trust it
			return false;
		}

		if (DEBUG) System.out.println("Checking robot protocol " + 
				urlRobot.toString());
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
		if (DEBUG) System.out.println(strCommands);

		// assume that this robots.txt refers to us and 
		// search for "Disallow:" commands.
		String strURL = url.getFile();
		int index = 0;
		while ((index = strCommands.indexOf(DISALLOW, index)) != -1) {
			index += DISALLOW.length();
			String strPath = strCommands.substring(index);
			StringTokenizer st = new StringTokenizer(strPath);

			if (!st.hasMoreTokens())
				break;

			String strBadPath = st.nextToken();

			// if the URL starts with a disallowed path, it is not safe
			if (strURL.indexOf(strBadPath) == 0)
				return false;
		}

		return true;
	}

	/**
	 * adds new URL to the queue. Accept only new URL's that end in
	 * htm or html. oldURL is the context, newURLString is the link
	 * 	(either an absolute or a relative URL).
	 */	
	public void addnewurl(URL oldURL, String newUrlString) { 
		URL url; 
		Link link;
		if (DEBUG) System.out.println("URL String " + newUrlString);
		try { 
			url = new URL(oldURL,newUrlString);
			link = new Link(url);
			if (!knownURLs.containsKey(link)) {
				String filename =  url.getFile();
				int iSuffix = filename.lastIndexOf("htm");
				if ((iSuffix == filename.length() - 3) ||
						(iSuffix == filename.length() - 4)) {
					knownURLs.put(link,new Integer(1));
					newURLs.add(link);
					System.out.println("Found new URL " + url.toString());
				}
			} 
		}
		catch (MalformedURLException e) { return; }
	}

	//Download contents of URL
	public String getpage(URL url)	{
		try { 
			// try opening the URL
			URLConnection urlConnection = url.openConnection();
			System.out.println("Downloading " + url.toString());

			urlConnection.setAllowUserInteraction(false);

			InputStream urlStream = url.openStream();
			// search the input stream for links. first, read in the entire URL
			byte b[] = new byte[1000];
			int numRead = urlStream.read(b);
			String content = new String(b, 0, numRead);
			while ((numRead != -1) && (content.length() < MAXSIZE)) {
				numRead = urlStream.read(b);
				if (numRead != -1) {
					String newContent = new String(b, 0, numRead);
					content += newContent;
				}
			}
			return content;

		} catch (IOException e) {
			System.out.println("ERROR: couldn't open URL ");
			return "";
		}  }

	/**
	 * Go through page finding links to URLs.  A link is signalled
	 * by <a href=" ...   It ends with a close angle bracket, preceded
	 * by a close quote, possibly preceded by a hatch mark (marking a
	 * fragment, an internal page marker)	 
	 * @param url
	 * @param page
	 */
	public void processpage(URL url, String page) {
		String lcPage = page.toLowerCase(); // Page in lower case
		int index = 0; // position in page
		int iEndAngle, ihref, iURL, iCloseQuote, iHatchMark, iEnd;
		while ((index = lcPage.indexOf("<a",index)) != -1) {
			iEndAngle = lcPage.indexOf(">",index);
			ihref = lcPage.indexOf("href",index);
			if (ihref != -1) {
				iURL = lcPage.indexOf("\"", ihref) + 1; 
				if ((iURL != -1) && (iEndAngle != -1) && (iURL < iEndAngle)) {
					iCloseQuote = lcPage.indexOf("\"",iURL);
					iHatchMark = lcPage.indexOf("#", iURL);
					if ((iCloseQuote != -1) && (iCloseQuote < iEndAngle)) {
						iEnd = iCloseQuote;
						if ((iHatchMark != -1) && (iHatchMark < iCloseQuote))
							iEnd = iHatchMark;
						String newUrlString = page.substring(iURL,iEnd);
						addnewurl(url, newUrlString); 
					} 
				} 
			}
			index = iEndAngle;
		}
	}

	/**
	 * Top-level procedure. Keep popping a url off newURLs, download it, and accumulate new URLs
	 * @param argv
	 */
	public void run() {
		initialize();
		for (int i = 0; i < maxPages; i++) {
			
			Link link = newURLs.peek();
			URL url = link.getUrl();
			newURLs.poll();
			if (DEBUG) System.out.println("Searching " + url.toString());
			if (robotSafe(url)) {
				String page = getpage(url);
				if (DEBUG) System.out.println(page);
				if (page.length() != 0) processpage(url,page);
				if (newURLs.isEmpty()) break;
			}
		}
		System.out.println("Search complete.");
	} 

	public static void main(String[] argv) {
		WebCrawler wc = new WebCrawler(argv);		
		wc.run();
	}
}