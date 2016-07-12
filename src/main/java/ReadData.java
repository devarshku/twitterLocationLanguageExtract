package tweet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.lang.Thread;

import com.cybozu.labs.langdetect.Detector; 
import com.cybozu.labs.langdetect.DetectorFactory;

public class ReadData {
	
	public static String getHTML(String urlToRead) throws Exception 
	{
		try{
	      StringBuilder result = new StringBuilder();
	      URL url = new URL(urlToRead);
	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	      conn.setRequestMethod("GET");
	      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	      String line;
	      while ((line = rd.readLine()) != null) {
	         result.append(line);
	      }
	      rd.close();
	      return result.toString();
		}
		catch(Exception e){
			return "error";
		}
	}
	
	public static void readFromFile(String fileName, Map<String,Integer> tweet_place)
	{
		String c[];
		
		try
		{
			Reader inputFile = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
	        BufferedReader bufferReader = new BufferedReader(inputFile);
	        String line, lang, latitude, longitude, input = null;
	        int tweet_count=0, null_count=0, current_tweet_count = 1, coords_total = 0, coords_usable = 0, latlongnull = 0;
	        
	        while ((line = bufferReader.readLine()) != null)   
	        {
	        	c=line.split("\t");
	        	tweet_count++;
       	
	        	if(c[9].equals("null"))	//tweet place-10th field
	        	{	        		
	        		if(c[5].equals("null")) //coords - 6th field
	        		{
	        			null_count++;
	        			continue;
	        		}
	        		else
	        		{
	        			coords_total++;
	        			
	        			String tempInp[] = c[5].split(",");
	        			latitude = tempInp[0];
	        			longitude = tempInp[1];
	        			
	        			int i;
	        		    String temp[], city = null, state = null, country = null;
	        			String label;
	        			
	        			String result = getHTML("https://search.mapzen.com/v1/reverse?api_key=search-YNGBFTs&point.lat=" + latitude + "&point.lon="+ longitude);
	        			//System.out.println(result);
	        			
	        			int beginIndex = result.indexOf("properties"); 
	        			if(beginIndex == -1)		//unable to reverse geocode
	        			{
	        				latlongnull++;
	        				null_count++;
	        				continue;
	        			}
	        			
	        		    int endIndex = result.indexOf("properties", beginIndex+10);
	        			if(endIndex == -1)
	        				result = result.substring(beginIndex);
	        			else
	        				result = result.substring(beginIndex, endIndex);
	        			
	        		    //System.out.println(result);
	        		    
	        		    String address[] = result.split(",");
	        		    
	        		    try{
		        	        for(i=0;i<address.length;i++) 
	        		    	{
		        		    	//System.out.println(i+"   " +address[i]); 
		        		    	
		        		    	temp = address[i].split(":");
		        		    	if(temp[0].equals("\"country\""))
		        		    	{
		        		    		country = temp[1];
		        		    		country = country.substring(1, country.length()-1);
		        		    		
		        		    	}
		        		    	else if(temp[0].equals("\"region\""))
		        		    	{
		        		    		state = temp[1];
		        		    		state = state.substring(1, state.length()-1);
		        		    	}
		        		    	else if(temp[0].equals("\"locality\":"))
		        	    		{
		        		    		city = temp[1];
		        		    		break;
		        	    		}
		        		    }
		        	    
		        	        if(i == address.length)
		        		    {
		        		    	label = result.substring(result.lastIndexOf("\"label\""));
		        		    	String t[] = label.split("\"");

		        		    	t = t[3].split(",");
		        		    	city = t[t.length-2].trim();
		        		    }

		        		    //System.out.println(city + "," + state + "," + country);
		        		    input = city + ","+ state + "," + country + ",null,coords";
		        		    
		        		    coords_usable++;
		        			Thread.sleep(200);	//due to the API call restrictions- 1 call per sec
	        		    }
	        		    catch(Exception e){
	        		    	null_count++;
//	        				continue;
	        		    }
	        			
	        		    

//	        			String result = getHTML("http://nominatim.openstreetmap.org/reverse?format=json&lat=" + latitude + "&lon=" + longitude + "&zoom=18&addressdetails=1");
//		     			
//	        			int beginIndex = result.lastIndexOf("address");
//	        			
//	        			if(beginIndex == -1)		//unable to reverse geocode
//	        			{
//	        				null_count++;
//	        				continue;
//	        			}
//	        			
//	        			//System.out.println(result);
//        			        			
//	        			result = result.substring(beginIndex);
//	
//		        		try{
//		        			
//			     			String address[] = result.split(":");
//			     			
//			     			String city = (address[2].split("\""))[1];
//			     			String state = (address[3].split("\""))[1];
//			     			String country = (address[4].split("\""))[1];
//			     			
//		        			input = city + "/"+ state + "/" + country + "/null/coords";
//		        			coords_usable++;
//		        			System.out.println(input + " : " + coords_usable);
//		        			Thread.sleep(1000);	//due to the API call restrictions- 1 call per sec
//	        			}
//	        			catch(Exception e)
//	        			{
//	        				null_count++;
//	        				continue;
//	        			}
	        			
	        			
	        		}
	        	}
	        	else 
	        		input = c[9];
	        	
	        	
    			try{
	    			Detector detector = DetectorFactory.create();
	        		detector.append(c[2]);
	        		lang = detector.detect();
	    			}
    			catch(Exception e){
    				//for tweets with only symbols like smileys
    				lang = "sym";
    				//continue;
    			}
        		input += " " + lang;
	        	
	        	
	        	/*
	        	//counting retweets as new tweets
	        	if(c[4].equals("null"))
	        		current_tweet_count = 1;
	        	else
	        		current_tweet_count = Integer.parseInt(c[4]);
	        	*/
	        		        	
	        	try{
	        		tweet_place.put(input, tweet_place.get(input) + current_tweet_count );
	        	}
	        	catch(Exception e){
	        		tweet_place.put(input, current_tweet_count );
	        		continue;
	        	}
	        }
	        
	        System.out.println("tweet_count: "+tweet_count+"; null count: "+null_count +"; coords_total: "+coords_total +"; coords_usable: "+ coords_usable + "; percentage available: " + ((tweet_count+0.0-null_count)/tweet_count));
	        System.out.println(latlongnull);
	        bufferReader.close();

		}
		
		catch(Exception e){
	          System.out.println("Error while reading file line by line:" + e.getMessage());  
	          e.printStackTrace();
	       }
	}
	
	public static void writeToFile(String filename, Map<String, Integer> myhash)
	{
		try{
			File file = new File(filename);
			file.createNewFile();
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			Iterator<Entry<String, Integer>> iterator = myhash.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<String, Integer> myentry = iterator.next();
				
				String content = "" + myentry.getKey()  +" " + myentry.getValue()  + "\n";
				bw.write(content);
			}
			
			bw.close();
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		Map<String,Integer> tweet_place = new HashMap<String,Integer>();
		
		//To get the date from filename
		
		try{
			DetectorFactory.loadProfile("/Users/intern/Documents/workspace/read_twitter/lib/profiles/");
			
			String filename = "2016_01_09_" + "00" + ".csv";
			
			//readFromFile(filename, tweet_place);
			
			int i=0;
			for(i=0; i<24; i++)
			{
				if(i<10)
					filename = "2016_01_09_0" + i + ".csv";
				else
					filename = "2016_01_09_" + i + ".csv";
				readFromFile(filename, tweet_place);
			}
			
			writeToFile("/Users/intern/Documents/tweet_place.txt",tweet_place);
			
		}
		catch(Exception e){
			System.out.println("Error: " + e.getMessage());  
	        e.printStackTrace();
		}
		
	}

}
