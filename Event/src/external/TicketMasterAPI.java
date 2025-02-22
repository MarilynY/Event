package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;


public class TicketMasterAPI {
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = ""; //no restriction return all events
	private static final String API_KEY = Config.TicketMasterAPI_KEY;
	
	public List<Item> search(double lat, double lon, String keyword) {
		//if client submit keyword then use it 
		//else set keyword to DEFAULT_KEYWORD
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		
		try {
			//why encode?
			//convert client's input keyword to the format that http protocal can understand
			//eg. space <- encode -> %20
			//we should use the chatset that TicketMaster can recognize
			keyword = URLEncoder.encode(keyword, "UTF-8"); //"Rick Sun" => "Rick%20Sun"
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			//String java.net.URLEncoder.encode(String s, String enc) throws UnsupportedEncodingException
			//so we need to either throw it or catch it. Here we catch it 
			e.printStackTrace();
		}
		
		//transfer lat and lon to geohash
		String geoHash = GeoHash.encodeGeohash(lat, lon, 8);
		
		// query should looks like: "apikey=qqPuP6n3ivMUoT9fPgLepkRMreBcbrjV&latlong=37,-120&keyword=event&radius=50"
		//use geoPoint instead of lat and lon because TicketMaster prefer geoPoint
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY, geoHash, keyword, 50);
		String url = URL + "?" + query;
		
		try {
			//need a cast 
			//create a URLConnection instance that represents a connection to the remote object referred to by the URL
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			//Tell what HTTP method to use
			connection.setRequestMethod("GET");
			//Get status code from HTTP response message
			int responseCode = connection.getResponseCode();
			
			System.out.println("Sending request to url: " + url);
			System.out.println("Response code: " + responseCode);
			
			if (responseCode != 200) {
				System.out.println("error status code is " + responseCode);
				return new ArrayList<>();
			}
			
			//if response code is 200, then we can go ahead read the data
			//create a BufferedReader to help read text from a character-input stream. 
			//Provide for the efficient reading of characters, arrays, and lines
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			StringBuilder response = new StringBuilder();
			
			//append response data to response StringBuilder instance line by line
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			//close the BufferReader after reading the input stream/response data
			reader.close();
			
			JSONObject obj = new JSONObject(response.toString());
			if (!obj.isNull("_embedded")) {
				//return type of embedded is JSONObject(seen from TicketMaster website)
				JSONObject embedded = obj.getJSONObject("_embedded");
				
				//call getItemList function
				return getItemList(embedded.getJSONArray("events"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	//use Item class in TicketMasterAPI to get clean data
	//convert events in JSONArray type to List<Item> objects
	private List<Item> getItemList(JSONArray events) throws JSONException {
		List<Item> itemList = new ArrayList<>();
		
		for (int i = 0; i < events.length(); i++) {
			JSONObject event = events.getJSONObject(i);
			ItemBuilder builder = new ItemBuilder();
			if (!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			if (!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			//TicketMaster does not provide rating now
			//just leave it here for now, it will return 0 by default
			if (!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			builder.setAddress(getAddress(event));
			builder.setImageUrl(getImageUrl(event));
			builder.setCategories(getCategories(event));
			
			//put the item into itemList
			itemList.add(builder.build());
		}
		return itemList;
	}
	
	/*
	 * Some helper functions to get data that is deep (not directly under events)
	 * 1. fetch address
	 * 2. fetch imageUrl
	 * 3. fetch categories
	 */
	
	//fetch address
	private String getAddress(JSONObject event) throws JSONException {
		if (!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			if (!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				for (int i = 0; i < venues.length(); i++) {
					JSONObject venue = venues.getJSONObject(i);
					StringBuilder addressBuilder = new StringBuilder();
					//get address
					if (!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						if (!address.isNull("line1")) {
							addressBuilder.append(address.get("line1"));
						}
						if (!address.isNull("line2")) {
							addressBuilder.append(",");
							addressBuilder.append(address.get("line2"));
						}
						if (!address.isNull("line3")) {
							addressBuilder.append(",");
							addressBuilder.append(address.get("line3"));
						}
					}
					//get city
					if (!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						if (!city.isNull("name")) {
							addressBuilder.append(",");
							addressBuilder.append(city.getString("name"));
						}
					}
					
					//get full address
					String addressStr = addressBuilder.toString();
					if (!addressStr.equals("")) {
						return addressStr;
					}
				}
			}
		}
		return "";	
	}
	
	//fetch imageUrl
	private String getImageUrl(JSONObject event) throws JSONException{
		if (!event.isNull("images")) {
			JSONArray images = event.getJSONArray("images");
			for (int i = 0; i < images.length(); i++) {
				JSONObject image = images.getJSONObject(i);
				if (!image.isNull("url")) {
					return image.getString("url");
				}	
			}
		}
		return "";
	}
	
	//fetch categories
	private Set<String> getCategories(JSONObject event) throws JSONException {
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			for (int i = 0; i < classifications.length(); i++) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if (!segment.isNull("name")) {
						categories.add(segment.getString("name"));
					}
				}
			}
		}
		return categories;
	}
	
	
	
	private void queryAPI(double lat, double lon) {
		List<Item> events = search(lat, lon, null);
		
		for (Item event : events) {
			System.out.println(event.toJSONObject());
		}
	}
	public static void main(String[] args) {
		TicketMasterAPI tmApi = new TicketMasterAPI();
		// Mountain View, CA
		// tmApi.queryAPI(37.38, -122.08);
		// London, UK
		// tmApi.queryAPI(51.503364, -0.12);
		// Houston, TX
		tmApi.queryAPI(29.682684, -95.295410);
	}
}



