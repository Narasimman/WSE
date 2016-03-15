package crawler;

import htmlparser.JTidyHTMLHandler;

import java.util.*;
import java.net.*;
import java.io.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WebCrawler {
	public static final int    SEARCH_LIMIT = 50;  // Absolute max pages 
	public static final boolean DEBUG = false;
	public static final String DISALLOW = "Disallow:";
	public static final int MAXSIZE = 100000; // Max size of file 
	private JTidyHTMLHandler handler = null;
	private String downloadPath;
	private CommandLine cmd = null;
	private final String query;

	// URLs to be searched
	PriorityQueue<Link> newURLs;
	// Known URLs
	Hashtable<String,Integer> knownURLs;
	// max number of pages to download
	int maxPages; 

	public WebCrawler(String[] argv) {
		handler = new JTidyHTMLHandler();

		//set options
		Options options = new Options();
		options.addOption("u", true, "URL");
		options.addOption("q", true, "Query");
		options.addOption("docs", true, "Directory to download saved files");
		options.addOption("m", true, "Max pages to download");

		CommandLineParser parser = new DefaultParser();
		try {
			cmd = parser.parse(options, argv);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		query = cmd.getOptionValue("q");
		System.out.println(query);

	}

	/**
	 * initializes data structures.  argv is the command line arguments.
	 * @param argv
	 */
	public void initialize() {
		URL url;
		Link link;
		knownURLs = new Hashtable<String,Integer>();

		URLScoreComparator comparator = new URLScoreComparator();
		newURLs = new PriorityQueue<Link>(SEARCH_LIMIT, comparator);

		String o_url = cmd.getOptionValue("u");
		String o_max = cmd.getOptionValue("m");
		try { 

			url = new URL(o_url);
			link = new Link(url, "");
		}
		catch (MalformedURLException e) {
			System.out.println("Invalid starting URL " + o_url);
			return;
		}
		knownURLs.put(o_url,new Integer(1));
		newURLs.add(link);
		System.out.println("Starting search: Initial URL " + url.toString());
		maxPages = SEARCH_LIMIT;
		if (o_max != null && !o_max.isEmpty()) {
			int iPages = Integer.parseInt(o_max);
			if (iPages < maxPages) {
				maxPages = iPages; 
			}
		}

		downloadPath = cmd.getOptionValue("docs");

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

		if (DEBUG) {
			System.out.println("Checking robot protocol " +
					urlRobot.toString());
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
	public void addnewurl(URL oldURL, String newUrlString, String anchor, int score) { 
		URL url; 
		Link link;
		if (DEBUG) System.out.println("URL String " + newUrlString);
		try { 
			url = new URL(oldURL,newUrlString);
			link = new Link(url, anchor);
			if (!knownURLs.containsKey(newUrlString)) {
				String filename =  url.getFile();
				int iSuffix = filename.lastIndexOf("htm");
				if ((iSuffix == filename.length() - 3) ||
						(iSuffix == filename.length() - 4)) {
					link.setScore(score);
					
					knownURLs.put(newUrlString,new Integer(1));
					if(!newURLs.contains(link)) {
						newURLs.add(link);
						System.out.println("Adding to Queue: " + url.toString()
							+ "with score " + link.getScore());
					} else {
						link.setScore(link.getScore() + score);
						System.out.println("Adding " + score + " to score of "  + url.toString());		
					}
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
		}  
	}

	private int calculateScore(Element link, String page, String[] query) {
		if (query.length == 0) {
			return 0;
		}

		String url = link.getAttribute("href");
		String anchor = handler.getText(link);

		int k = 0;

		for(String word : query) {
			if(anchor.contains(word)) {
				++k;
			}
		}

		if(k > 0) {
			return k * 50;
		}

		for (String word : query) {
			if(url.contains(word)) {
				return 40;
			}
		}

		/**
		 * #TODO:
		 */
		int counter = 0;
		String textForScore = "";
		Node ele = link.getNextSibling();
		while(ele != null) {
			String text = handler.getText(ele);
			String[] words = text.split(" ");

			for(String word : words){
				textForScore += word + " ";
				++counter;

				if(counter == 5) {
					break;
				}
			}
			if(counter == 5) {
				break;
			}
			
			ele = ele.getNextSibling();
		}
		
		counter = 0;
		ele = link.getPreviousSibling();
		while(ele != null) {
			String text = handler.getText(ele);
			String[] words = text.split(" ");

			for(String word : words){
				textForScore += word + " ";
				++counter;

				if(counter == 5) {
					break;
				}
			}
			if(counter == 5) {
				break;
			}
			
			ele = ele.getPreviousSibling();
		}

		int U = 0;
		int V = 0;
		for(String word : query) {
			if(textForScore.contains(word)) {
				++U;
			}
			if(page.contains(word)) {
				++V;
			}
		}
		
		return 4 * Math.abs(U) + Math.abs(V - U);
	}

	public void processpage(URL url, String page) {

		Element doc  = null;
		try {
			doc  = handler.getRawDocument(page);
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}

		NodeList anchors = handler.getAnchors(doc);

		for(int i = 0; i < anchors.getLength(); i++) {
			Element element = ((Element)anchors.item(i));

			String newUrlString = element.getAttribute("href");
			String anchor = handler.getText(element);
			int score = calculateScore(element, page, query.split(" "));
			addnewurl(url, newUrlString, anchor, score);

		}

	}
	/**
	 * Go through page finding links to URLs.  A link is signalled
	 * by <a href=" ...   It ends with a close angle bracket, preceded
	 * by a close quote, possibly preceded by a hatch mark (marking a
	 * fragment, an internal page marker)	 
	 * @param url
	 * @param page
	 */
	/*public void processpage(URL url, String page) {
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
						String anchor = page.substring(iEnd + 1, lcPage.indexOf("<", iEnd));

						addnewurl(url, newUrlString, anchor); 
					} 
				} 
			}
			index = iEndAngle;
		}
	}
	 */
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
			if (DEBUG) {
				System.out.println("Searching " + url.toString());
			}
			if (robotSafe(url)) {
				String page = getpage(url);
				URLDownloader.downloadPage(downloadPath + url.getPath(), page);
				if (DEBUG) {
					System.out.println(page);
				}
				if (page.length() != 0) {
					processpage(url,page);
				}
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