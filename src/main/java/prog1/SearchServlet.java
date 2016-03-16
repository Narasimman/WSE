package prog1;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class SearchServlet
 */
@WebServlet("/search")
public class SearchServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final String indexDir = "index";
  private static final String dataDir = "Prog1ExampleDirectory";

  /**
   * @see HttpServlet#HttpServlet()
   */
  public SearchServlet() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {

      // Indexer indexer = new Indexer(indexDir);
      // int numIndexed = indexer.index(dataDir);
      // indexer.close();

      Map<String, String> result = Retriever.search(indexDir,
          request.getParameter("q"));
      response.setContentType("text/html;charset=UTF-8");

      request.setAttribute("res", result);
      request.setAttribute("dataDir", dataDir);
      request.getRequestDispatcher("/result.jsp").forward(request, response);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // TODO Auto-generated method stub
  }

}
