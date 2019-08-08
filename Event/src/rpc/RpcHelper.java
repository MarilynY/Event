package rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class RpcHelper {
	//Write a JSONArray to http response
	public static void writeJsonArray(HttpServletResponse response, JSONArray array) throws IOException{
		//declare the type of response
		response.setContentType("application/json");
		
		//declare that all the request will be handled
		response.setHeader("Access-Control-Allow-Origin", "*");
		
		//create a PrintWriter object to sent to client
		PrintWriter out = response.getWriter();
		
		//put JSONArray in this out object to sent to client
		out.print(array);
		
		out.close();
	}
	
	//Write a JSONObject to http response
	public static void writeJsonObject(HttpServletResponse response, JSONObject obj) throws IOException{
		response.setContentType("application/json");
		response.setHeader("Access-Control-Allow-Origin", "*");
		
		PrintWriter out = response.getWriter();
		out.print(obj);
		out.close();
	}
	
	//Parse a http request to JSONObject
	public static JSONObject readJSONObject(HttpServletRequest request) {
		StringBuilder sBuilder = new StringBuilder();
		try (BufferedReader reader = request.getReader()){
			String line = null;
			while((line = reader.readLine()) != null) {
				sBuilder.append(line);
			}
			return new JSONObject(sBuilder.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new JSONObject();
	}
}
