import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
    	
        try {
            String baseURL = "https://u50g7n0cbj.execute-api.us-east-1.amazonaws.com/v2/locations?"; //base url
            String url1 = ""; //turns into base url + search queries
            boolean success = false; //stays in console loop until success is true
            Scanner input = new Scanner(System.in);
            
            //while loop to stay in the console until proper input is given
            while(success != true) {	     	
            	System.out.println("Enter '1' to search with country code and measured parameter, "
            		+ "Enter '2' to search with decimal-degree coordinates, radius and measured parameter");
            	
            	//switch statement for checking user input
            	switch(input.nextLine().replaceAll("\\s+", "")) {
            	
            		case "1":
            			System.out.println("Please input your desired country using its two letter ISO format: ");
            			String countryCode = "&country_id=" + input.nextLine().replaceAll("\\s+", "");
                
            			System.out.println("Please input your desired measured parameter; acceptable values are pm25, pm10, co, bc, so2, no2, o3");
            			String measuredParam = "&parameter=" + input.nextLine().replaceAll("\\s+", "");
            		
            			//build final URL
            			url1 = baseURL + countryCode + measuredParam;
            			
            			success = true;
            			
            			break;
            		
            		case "2":
            			System.out.println("Please input your desired location using comma seperated decimal-degree coordinates ex:(-22.08,-70.193253): ");
            			String coordinates = "&coordinates=" + input.nextLine().replaceAll("\\s+", "");
            			coordinates = coordinates.replace(",", "%2C"); //replaces the comma with the correct ascii value
                
            			System.out.println("Please input your desired radius (1-100000)");
            			String radius = "&radius=" + input.nextLine().replaceAll("\\s+", "");
                
            			System.out.println("Please input your desired measured parameter; acceptable values are pm25, pm10, co, bc, so2, no2, o3");
            			measuredParam = "&parameter=" + input.nextLine().replaceAll("\\s+", "");
            		
            			//build final URL
            			url1 = baseURL + coordinates  + radius + measuredParam;
            		
            			success = true;
            			
            			break;
            		
            		default:
            			System.out.println("Incorrect input.");
            		
            			success = false;
            	}
            } 
            //done collecting parameters
            input.close();
          
            //convert to type URL
            URL url = new URL(url1);
            
            System.out.println("Connecting to " + url);
            System.out.println();
            
            //attempt to connect with URL
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            //Check if connection is made
            int responseCode = conn.getResponseCode();

            // 200 OK
            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } 
            else {
                StringBuilder informationString = new StringBuilder(); //String that holds all data returned
                Scanner scan = new Scanner(url.openStream());

                while (scan.hasNext()) {
                	informationString.append(scan.nextLine()); //adds data to string
                }
                //Close the scanner
                scan.close();
                
                //prints all info returned
//                System.out.println("Printing Information String: " + informationString);
//                System.out.println();


                //JSON simple library Setup with Maven is used to convert strings to JSON
                JSONParser parse = new JSONParser();
                JSONObject dataObject = (JSONObject) parse.parse(String.valueOf(informationString));

                //Get the results JSON object in the JSON array
                JSONArray data = (JSONArray) dataObject.get("results");
                
                //final JSON Array we are storing all results in
                JSONArray result = new JSONArray();
                
                //loop through data and pick out important info to store
                for(int i = 0; i < data.size(); i++) {
                	JSONObject header = new JSONObject(); //header of each JSON object that will be the name of the location
                	JSONObject obj = new JSONObject(); //each object of data to be stored 
                	JSONObject tmp = (JSONObject) data.get(i); //temporary variable for locating AQI data array
                	JSONArray param = (JSONArray) tmp.get("parameters"); //array of the AQI data for each location
                	String city = (String)tmp.get("city"); //name of location
                	header.put("city",city); //add location header
                	obj.put("bounds", tmp.get("bounds")); //the bounds of the sample's data in coordinates
                	
                	//inner loop for picking out data in the AQI data array
                	for(int j = 0; j < param.size(); j++) {
                		tmp = (JSONObject) param.get(j); //temporary variable to locating AQI data we need
                		obj.put("average", tmp.get("average")); //average value recorded
                		obj.put("count", tmp.get("count"));
                		obj.put("lastValue", tmp.get("lastValue")); //last value recorded
                		obj.put("parameter", tmp.get("parameter")); //parameter used to measure
                		obj.put("lastUpdated", tmp.get("lastUpdated")); //the date the last time it was recorded
                		header.put(city, obj); //adds the AQI data JSON to the city JSON object
                	}
                	result.add(header); //adds each locations JSON data to a final JSON array
                }
                System.out.println(result);
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}