Web Search Engines - Programming Assignment 1
I have built this as a web app project using the gradle build tool.
I have also included the gradle.build file that will take care of dependencies and building the project and a WAR file.
After importing the project,

To build: call gradlew build

HMTLParser used: JTideHTMLParser

Web files : index.jsp and result.jsp

Indexer : Indexer.java 
	Usage: Indexer [-index INDEX_PATH] [-docs DOCS_PATH] [-update]
        "This indexes the documents in DOCS_PATH, creating a Lucene index"
        "in INDEX_PATH that can be searched with SearchFiles"
		
Retriever: Retriever.java
	Usage: java Retriever <index dir> <query>"
	
Servlet: SearchServlet.java - To handle HTTP servlet requests.
	@WebServlet("/search")

	
Note: Included all the jar files and WAR in the webapp directory of src