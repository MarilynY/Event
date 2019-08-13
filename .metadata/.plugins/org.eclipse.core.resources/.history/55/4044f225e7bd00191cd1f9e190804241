package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;

/**
 * Servlet implementation class Login
 */
@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    //check if user has session id, if yes, return page,if no, redirect to login page
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		DBConnection connection = DBConnectionFactory.getConnection();
		
		try {
			HttpSession session = request.getSession(false);
			JSONObject obj = new JSONObject();
			if (session != null) {
				String userId = session.getAttribute("user_id").toString();
				obj.put("status", "OK").put("user_id", userId).put("name", connection.getFullname(userId));
			} else {
				response.setStatus(403); //forbidden error
				obj.put("status", "Session Invalid");
			}
			RpcHelper.writeJsonObject(response, obj);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	//check user's input (username and password) in the database or not
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//read username and password from http request body
		//if username and password matched database, create session and return session id to user
		//if not redirect to login page
		DBConnection connection = DBConnectionFactory.getConnection();
		
		try {
			JSONObject input = RpcHelper.readJSONObject(request);
			String userId = input.getString("user_id");
			String password = input.getString("password");
			//for debug
			JSONObject obj = new JSONObject();
			//if userId and password in the database, return session id
			if (connection.verifyLogin(userId, password)) {
				
				//tomcat will add session id to request header and response header
				HttpSession session = request.getSession();
				session.setAttribute("user_id", userId);
				session.setMaxInactiveInterval(600);
				//for debug
				obj.put("status", "OK").put("user_id", userId).put("name", connection.getFullname(userId));
			} else {
				response.setStatus(401); //unauthorized
				obj.put("status : ", "User Doesn't Exist");
			}
			RpcHelper.writeJsonObject(response, obj);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
	}

}
