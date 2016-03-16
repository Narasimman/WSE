package prog2;

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

public class Crawler {
	public static final int SEARCH_LIMIT = 50;  // Absolute max pages 
	public static final boolean DEBUG = false;
	public static final int MAXSIZE = 50000; // Max size of file 

	private static final JTidyHTMLHandler handler = new JTidyHTMLHandler();

	private final String startingURL;
	private final String downloadPath; //-docs
	private final int maxPages; //-m
	private final String[] query; //-q
	private final boolean trace; //-t

	private PriorityQueue<Link> newURLs; // URLs to be searched
	private Hashtable<String,Link> knownURLs; // Known URLs

	private int processedURLs = 0;

	public Crawler(String url, String docs, String[] query, int max, boolean trace) {
		knownURLs = new Hashtable<String,Link>();
		newURLs = new PriorityQueue<Link>(SEARCH_LIMIT, new URLScoreComparator());

		this.startingURL = url;
		this.downloadPath = docs;
		this.query = query;

		if(max < SEARCH_LIMIT) {
			this.maxPages = max;
		} else {
			this.maxPages = SEARCH_LIMIT;
		}	

		this.trace = trace;

		initialize();
	}

	private void printTrace(String message) {
		if(this.trace) {
			System.out.println(message);
		}
	}
	/**
	 * initializes data structures.  argv is the command line arguments.
	 * @param argv
	 */
	private void initialize() {
		URL url;
		Link link;

		try {
			url = new URL(this.startingURL);
		}	catch (MalformedURLException e) {
			System.out.println("Invalid starting URL " + this.startingURL);
			System.exit(-1);
			return;
		}

		link = new Link(url, "");		
		knownURLs.put(url.toString(), link);
		newURLs.add(link);


		printTrace("Crawling for " + maxPages + " pages relevant to "
				+ query + " starting from " + url.toString());

		/* Behind a firewall set your proxy and port here! */
		Properties props= new Properties(System.getProperties());
		props.put("http.proxySet", "true");
		props.put("http.proxyHost", "webcache-cup");
		props.put("http.proxyPort", "8080");

		Properties newprops = new Properties(props);
		System.setProperties(newprops);
	}

	/**
	 * adds new URL to the queue. Accept only new URL's that end in
	 * htm or html. oldURL is the context, newURLString is the link
	 * 	(either an absolute or a relative URL).
	 */	
	private void addnewurl(URL oldURL, String newUrlString, String anchor, int score) { 
		URL url; 
		Link link;

		try {
			url = new URL(oldURL,newUrlString);

			if (!knownURLs.containsKey(url.toString())) {
				link = new Link(url, anchor);
				knownURLs.put(url.toString(),link);

				String filename =  url.getFile();
				int iSuffix = filename.lastIndexOf("htm");
				if ((iSuffix == filename.length() - 3) ||
						(iSuffix == filename.length() - 4)) {

					if(!newURLs.contains(link)) {
						link.setScore(score);						
						newURLs.add(link);						
						printTrace("Adding to Queue: " + url.toString()
								+ " with score " + link.getScore());
					}
				}
			} else { // link seen
				link = knownURLs.get(url.toString());
				if(newURLs.contains(link)) { // Add score only if it is not yet downloaded and still in queue				
					link.setScore(link.getScore() + score);
					printTrace("Adding " + score + " to score of "  + url.toString());
				}
			}
		}
		catch (MalformedURLException e) { return; }
	}

	private String getNeighboringText(Element link, int range) {
		String neighbors = "";
		int counter = 0;

		/* Collect next five strings */
		Node node = link.getNextSibling();
		while(node != null && counter < range) {
			String text = handler.getText(node);
			String[] words = text.split(" ");

			for(String word : words){
				if(!word.isEmpty()) {
					neighbors += word + " ";
					++counter;
				}
				if(counter == range) {
					break;
				}
			}
			node = node.getNextSibling();
		}

		/* Collect previous five strings */
		counter = 0;
		node = link.getPreviousSibling();
		while(node != null && counter < range) {
			String text = handler.getText(node);
			String[] words = text.split(" ");			

			for(int i = words.length - 1; i >= 0; --i){
				if(!words[i].isEmpty()) {
					neighbors += words[i] + " ";
					++counter;
				}
				if(counter == range) {
					break;
				}
			}
			node = node.getPreviousSibling();
		}
		neighbors = neighbors.toLowerCase().replaceAll("[^\\w\\s\\-_]", "");
		return neighbors;
	}

	/**
	 * Calculate score based on the algorithm
	 * @param link
	 * @param page
	 * @param query
	 * @return score
	 */
	private int calculateScore(Element link, String page, String[] query) {
		page = page.toLowerCase().replaceAll("[^\\w\\s\\-_]", "");
		if (query.length == 0) {
			return 0;
		}

		String url = link.getAttribute("href").toLowerCase();
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
			if(url.contains(word.toLowerCase())) {
				return 40;
			}
		}

		/* Get neighboring text of range 5 before and after */
		String neighbors = getNeighboringText(link, 5);
		int U = 0;
		int V = 0;	
		for(String word : query) {
			if(neighbors.matches(".*\\b" + word + "\\b.*")) {
				++U;
			}
			if(page.matches(".*\\b" + word + "\\b.*")) {
				++V;
			}
		}
		return (4 * Math.abs(U)) + Math.abs(V - U);
	}

	/**
	 * Go through page finding links to URLs.  A link is signalled
	 * by <a href=" ...   It ends with a close angle bracket, preceded
	 * by a close quote, possibly preceded by a hatch mark (marking a
	 * fragment, an internal page marker)	 
	 * @param url
	 * @param page
	 */
	private void processpage(URL url, String page) {
		Element doc  = null;
		try {
			doc  = handler.getRawDocument(page);
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}

		NodeList anchors = handler.getAnchors(doc);
		String content = handler.getText(doc);

		for(int i = 0; i < anchors.getLength(); i++) {
			Element element = ((Element)anchors.item(i));
			String newUrlString = element.getAttribute("href");
			String anchor = handler.getText(element);
			int score = calculateScore(element, content, query);
			addnewurl(url, newUrlString, anchor, score);
		}
	}

	/**
	 * Top-level procedure. Keep popping a url off newURLs, download it, and accumulate new URLs
	 */
	public void run() {
		while(!newURLs.isEmpty() && processedURLs < maxPages) {
			Link link = newURLs.poll();
			URL url = link.getUrl();
			if (URLHandler.robotSafe(url)) {
				printTrace("Downloading " + url.toString() + " Score = " + link.getScore());
				String page = URLHandler.getpage(link, MAXSIZE);
				boolean success = URLHandler.savePage(downloadPath + url.getPath(), page);
				++processedURLs;

				if(success) {
					printTrace("Received: " + url.toString());
				}
				if(processedURLs >= maxPages) {
					break;
				}
				if (page.length() > 0) {
					processpage(url,page);
				}
				System.out.println();
			}
		}
		System.out.println("Crawl complete.");
	}

	public static void main(String[] argv) {
		CommandLine cmd = null;
		//set options
		Options options = new Options();
		options.addOption("u", "u", true, "URL");
		Option qOption = new Option("q", "q", true, "Query");
		qOption.setArgs(Option.UNLIMITED_VALUES);

		options.addOption(qOption);
		options.addOption("docs", "docs", true, "Directory to download saved files");
		options.addOption("m", "m", true, "Max pages to download");
		options.addOption("t", "t", false, "Print crawler trace");

		CommandLineParser parser = new DefaultParser();
		try {
			cmd = parser.parse(options, argv);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		String url = cmd.getOptionValue("u");
		String docs = cmd.getOptionValue("docs");
		String[] query = cmd.getOptionValues("q");		
		String max = cmd.getOptionValue("m");
		int maxVal = 0;
		if (max != null && !max.isEmpty()) {
			maxVal = Integer.parseInt(max);
		}
		boolean trace = cmd.hasOption("t");

		Crawler wc = new Crawler(url, docs, query, maxVal, trace);		
		wc.run();
	}
}