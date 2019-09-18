package rpc;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.json.JSONArray;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

/**
 * Servlet implementation class SearchItem
 */
@WebServlet("/search")
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// allow access only if session exists
		/*
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}
		String userId = session.getAttribute("user_id").toString();
		*/
		
		String userId = request.getParameter("user_id");
		// parse 是类型转换
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		// Term can be empty or null.
		// 定义这个term是因为，mysqlconnection里面的searchItem function 的parameter有这个 keyword
		String term = request.getParameter("term");
		// 创建一个DBConnection
		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			// 通过call searchItem 得到item
			List<Item> items = connection.searchItems(lat, lon, term);
			Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
			JSONArray array = new JSONArray();
			for (Item item : items) {
				JSONObject obj = item.toJSONObject();
				obj.put("favorite", favoritedItemIds.contains(item.getItemId()));
				array.put(obj);

			}
			RpcHelper.writeJsonArray(response, array); // 返回前端

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
		/*
		 * TicketMasterAPI api = new TicketMasterAPI(); List<Item> items =
		 * api.search(lat, lon, null);
		 * 
		 * JSONArray array = new JSONArray(); for (Item item : items) {
		 * array.put(item.toJSONObject()); } RpcHelper.writeJsonArray(response, array);
		 */
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
}

//Practice

/*
 * Example 4 return JSONArray protected void doGet(HttpServletRequest request,
 * HttpServletResponse response) throws ServletException, IOException {
 * response.setContentType("application/json");//这行可以不加 根据返回的类型浏览器进行解析
 * 
 * JSONArray array = new JSONArray(); try { array.put(new
 * JSONObject().put("username", "abcd")); array.put(new
 * JSONObject().put("username", "1234")); } catch (JSONException e) {
 * e.printStackTrace(); }
 * 
 * RpcHelper.writeJsonArray(response, array); }
 */

/*
 * Example 3 return JSONObject protected void doGet(HttpServletRequest request,
 * HttpServletResponse response) throws ServletException, IOException {
 * response.setContentType("application/json");
 * 
 * PrintWriter out = response.getWriter();
 * 
 * if (request.getParameter("username") != null) { String username =
 * request.getParameter("username"); JSONObject obj = new JSONObject();
 * 
 * try { obj.put("username", username); } catch (JSONException e) { // TODO
 * Auto-generated catch block e.printStackTrace(); } out.print(obj); }
 * 
 * out.close(); }
 */

/*
 * Example 2 动态返回HTML protected void doGet(HttpServletRequest request,
 * HttpServletResponse response) throws ServletException, IOException {
 * response.setContentType("text/html");
 * 
 * PrintWriter out = response.getWriter(); if (request.getParameter("username")
 * != null) { String username = request.getParameter("username");
 * out.print("<html><body>"); out.print("<h1>Hello " + username + "</h1>");
 * out.print("</body></html>"); }
 * 
 * out.close(); }
 */

/*
 * Example 1 静态返回Hello World protected void doGet(HttpServletRequest request,
 * HttpServletResponse response) throws ServletException, IOException {
 * response.setContentType("text/html");
 * 
 * PrintWriter out = response.getWriter();
 * 
 * out.print("<html><body>"); out.print("<h1>Hello World</h1>");
 * out.print("</body></html>");
 * 
 * out.close(); }
 */
