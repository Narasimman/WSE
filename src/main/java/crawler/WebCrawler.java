package crawler;

import htmlparser.JTidyHTMLHandler;

import java.util.*;
import java.net.*;
import java.io.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
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
	
	private final JTidyHTMLHandler handler;
	private CommandLine cmd = null;
	
	private final String downloadPath; //-docs
	private int maxPages; //-m
	private final String[] query; //-q

	private PriorityQueue<Link> newURLs; // URLs to be searched
	private Hashtable<String,Integer> knownURLs; // Known URLs
	
	public WebCrawler(String[] argv) {
		handler = new JTidyHTMLHandler();
		
		setOptions(argv);

		query = cmd.getOptionValues("q");
		downloadPath = cmd.getOptionValue("docs");
		
	}
	
	private boolean setOptions(String[] argv) {
	//set options
			Options options = new Options();
			options.addOption("u", true, "URL");
			Option qOption = new Option("q", true, "Query");
			qOption.setArgs(Option.UNLIMITED_VALUES);
			
			options.addOption(qOption);
			options.addOption("docs", true, "Directory to download saved files");
			options.addOption("m", true, "Max pages to download");

			CommandLineParser parser = new DefaultParser();
			try {
				cmd = parser.parse(options, argv);
			} catch (ParseException e) {
				return false;
			}
			
			return true;
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
		
		knownURLs.put(url.toString(),new Integer(1));
		newURLs.add(link);
		System.out.println("Starting search: Initial URL " + url.toString());
		maxPages = SEARCH_LIMIT;
		if (o_max != null && !o_max.isEmpty()) {
			int max = Integer.parseInt(o_max);
			if (max < maxPages) {
				maxPages = max; 
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
		
		try {
			url = new URL(oldURL,newUrlString);
			link = new Link(url, anchor);
			if (!knownURLs.containsKey(url.toString())) {
				String filename =  url.getFile();
				int iSuffix = filename.lastIndexOf("htm");
				if ((iSuffix == filename.length() - 3) ||
						(iSuffix == filename.length() - 4)) {
					link.setScore(score);
					if(!newURLs.contains(link)) {
						newURLs.add(link);
						System.out.println("Adding to Queue: " + url.toString()
								+ " with score " + link.getScore());
					} else {
						link.setScore(link.getScore() + score);
						System.out.println("Adding " + score + " to score of "  + url.toString());		
					}
				}
			}
		}
		catch (MalformedURLException e) { return; }
	}

	private int calculateScore(Element link, String page, String[] query) {
		page = page.toLowerCase();
		if (query.length == 0) {
			return 0;
		}

		String url = link.getAttribute("href");
		String anchor = handler.getText(link).toLowerCase();

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
				if(!word.isEmpty()) {
					textForScore += word + " ";
					++counter;
				}

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

			for(int i = words.length - 1; i >= 0; --i){
				if(!words[i].isEmpty()) {
					textForScore += words[i] + " ";
					++counter;
				}
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
		textForScore = textForScore.toLowerCase();
		System.out.println("!!! " + textForScore);
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

	/**
	 * Go through page finding links to URLs.  A link is signalled
	 * by <a href=" ...   It ends with a close angle bracket, preceded
	 * by a close quote, possibly preceded by a hatch mark (marking a
	 * fragment, an internal page marker)	 
	 * @param url
	 * @param page
	 */
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
			int score = calculateScore(element, page, query);
			addnewurl(url, newUrlString, anchor, score);
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
			if (robotSafe(url)) {
				String page = URLDownloader.getpage(link, MAXSIZE);
				boolean success = URLDownloader.savePage(downloadPath + url.getPath(), page);
				knownURLs.put(url.toString(),new Integer(1));
				if(success) {
					System.out.println("Received: " + url.toString());
				}
				
				if(knownURLs.size() >= maxPages) {
					break;
				}
				if (page.length() != 0) {
					processpage(url,page);
				}
				System.out.println();
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