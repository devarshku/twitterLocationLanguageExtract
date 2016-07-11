package tweet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.sql.Date;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

public class insertDB2 {

	public static Map<String, ArrayList<String>> createCountryState()
	{
		String fileName = "pls.txt",c[], line="";
		Map<String,ArrayList<String>> countryState = new HashMap<String,ArrayList<String>>();
		
		try
		{
			Reader inputFile = new InputStreamReader(new FileInputStream(fileName),"UTF-8");
			//FileReader inputFile = new FileReader(fileName);
			BufferedReader bufferReader = new BufferedReader(inputFile);
			
	        while ((line = bufferReader.readLine()) != null)   
	        {
	        	String country, t, temp[];
	        	ArrayList<String> states = new ArrayList<String>();
	        	int i;
	        		
	        	c=line.split(",");

	        	country = c[0];
	        	country = country.substring(3, country.length()-1);
	        		        	
	        	t = c[5];
	        	t = t.substring(2, t.length()-1);
	        	
	        	t = t.replace('|', ',');
	        	temp = t.split(",");

	        	
	        	for(i=0;i<temp.length;i++)
	        		states.add(temp[i]);

	        	countryState.put(country, states);

 		    }
	        
	        bufferReader.close();
	        
		}
		catch(Exception e){

	          System.out.println("Error while reading file line by line:" + e.getMessage());  
	          e.printStackTrace();
	       }
		
		return countryState;
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
				
				String content = myentry.getKey() + "," + myentry.getValue()  + "\n";
				bw.write(content);
			}
			
			bw.close();
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static Map<String, Integer> readPlace() throws Exception
	{
		
		Map<String,ArrayList<String>> countryState = createCountryState();
		Map<String, Integer> placeCount = new HashMap<String, Integer>();
		String city = null, state = null, country = null;
		
//		java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM/yyyy");
//        java.util.Date ud = fmt.parse("09/01/2016");
//        java.sql.Date sd = new java.sql.Date(ud.getTime());
        //String formattedSqlDate = fmt.format(sd);
        
		//int loc_id_count = 1, loc_id = 0;
		String lang, input;

		try{
	         //file read
	         String temp[] = new String[7];
	         int entries = 0, len;
	 		try
	 		{
	 			FileReader inputFile = new FileReader("tweet_place.txt");
	 	        BufferedReader bufferReader = new BufferedReader(inputFile);
	 	        String line;
	 	        int count;
	 	        
	 	        while ((line = bufferReader.readLine()) != null)   
	 	        {

	 	        	temp=line.split(",");
	 	        	entries++;
	 	        	
	 	        	len = temp.length;
	 	        	if( len < 4)
	 	        		continue;
	 	        	
	 	        	//System.out.println(temp[temp.length - 1]);
	 	        	String x[] = temp[temp.length - 1].split(" ");
	 	        	count = Integer.parseInt(x[x.length - 1]);
	 	        	lang = x[1];
	 	        	
	 	        	if(len == 4)
	 	        	{
	 	        		if(x[0].equals("city") || x[0].equals("admin"))
	 	        		{
	 	        			if(temp[1].equals(temp[0]))
	 	        				continue;

	 	        			//loc_id = locationEdit(prepSelectLoc1, prepInsertLoc, loc_id_count, temp[1].trim(), null, temp[0].trim());
	 	        			city = temp[0].trim();
	 	        			state = null;
	 	        			country = temp[1].trim();
	 	        			
	 	        		}
	 	        		else if(x[0].equals("country") || x[0].equals("neighborhood") || x[0].equals("poi"))
	 	        		{
	 	        			continue;
	 	        		}
	 	        		 
	 	        	}
	 	        	else if(len == 5)
	 	        	{
	 	        		if(x[0].equals("city"))
	 	        		{
	 	        			if( (temp[1].trim()).equals(temp[2].trim()))
	 	        			{
	 	        				//loc_id = locationEdit(prepSelectLoc1, prepInsertLoc, loc_id_count, temp[2].trim(), null, temp[0].trim());
	 	        				city = temp[0].trim();
		 	        			state = null;
		 	        			country = temp[2].trim();
		 	        		}
	 	        			else
	 	        			{
	 	        				//loc_id = locationEdit(prepSelectLoc2, prepInsertLoc, loc_id_count, temp[2].trim(), temp[1].trim(), temp[0].trim());
	 	        				city = temp[0].trim();
		 	        			state = temp[1].trim();
		 	        			country = temp[2].trim();
	 	        			}
	 	        			
	 	        		}
	 	        		else if(x[0].equals("coords"))
	 	        		{	 	        			
	 	        			//loc_id = locationEdit(prepSelectLoc2, prepInsertLoc, loc_id_count, temp[2].trim(), temp[1].trim(), temp[0].trim());
	 	        			city = temp[0].trim();
	 	        			state = temp[1].trim();
	 	        			country = temp[2].trim();
	 	        				
	 	        		}
	 	        		else if(x[0].equals("neighborhood"))
	 	        		{
	 	        			try{

	 	        				ArrayList<String> states = countryState.get(temp[2].trim());
	 	        				if( states.contains(temp[1].trim()) == true)
	 	        				{
	 	        					//loc_id = locationEdit(prepSelectLoc2, prepInsertLoc, loc_id_count, temp[2].trim(), temp[1].trim(), temp[0].trim());
	 	        					city = temp[0].trim();
			 	        			state = temp[1].trim();
			 	        			country = temp[2].trim();
	 	        				}
	 	        				else{
	 	        					//loc_id = locationEdit(prepSelectLoc1, prepInsertLoc, loc_id_count, temp[2].trim(), null, temp[1].trim());
	 	        					city = temp[1].trim();
			 	        			state = null;
			 	        			country = temp[2].trim();
	 	        				}
	 	        						
	 	        			}
	 	        			catch(Exception e){	 	        				
	 	        				//loc_id = locationEdit(prepSelectLoc1, prepInsertLoc, loc_id_count, temp[2].trim(), null, temp[1].trim());
	 	        				city = temp[1].trim();
		 	        			state = null;
		 	        			country = temp[2].trim();
		 	        		}
	 	        			
	 	        		}
	 	        		else if(x[0].equals("admin"))
	 	        		{

	 	        			if((temp[2].trim()).equals(temp[1].trim())) 
	 	        			{
	 	        				try{
		 	        				ArrayList<String> states = countryState.get(temp[2].trim());
		 	        				if( states.contains(temp[0].trim()) == true)
		 	        					continue;
		 	        				else{
		 	        					//loc_id = locationEdit(prepSelectLoc1, prepInsertLoc, loc_id_count, temp[2].trim(), null, temp[0].trim());
		 	        					city = temp[0].trim();
				 	        			state = null;
				 	        			country = temp[2].trim();
		 	        				}
		 	        					
		 	        			}
		 	        			catch(Exception e){
		 	        				//loc_id = locationEdit(prepSelectLoc1, prepInsertLoc, loc_id_count, temp[2].trim(), null, temp[0].trim());
		 	        				city = temp[0].trim();
			 	        			state = null;
			 	        			country = temp[2].trim();
		 	        			}
	 	        			}
	 	        			else{
	 	        				int lim = temp[2].compareTo(temp[1]);
	 	        				if(-10 <= lim && lim <= 10)
	 	        				{
	 	        					//loc_id = locationEdit(prepSelectLoc1, prepInsertLoc, loc_id_count, temp[2].trim(), null, temp[0].trim());
	 	        					city = temp[0].trim();
			 	        			state = null;
			 	        			country = temp[2].trim();
	 	        				}
	 	        				else{
	 	        					//loc_id = locationEdit(prepSelectLoc2, prepInsertLoc, loc_id_count, temp[2].trim(), temp[1].trim(), temp[0].trim());
	 	        					city = temp[0].trim();
			 	        			state = temp[1].trim();
			 	        			country = temp[2].trim();
	 	        				}
	 	        					 	        					
	 	        			}
	 	        		}
	 	        		else if(x[0].equals("poi"))
	 	        			continue;
//	 	        		{
//	 	        			if(temp[1].length() == 2)
//	 	        			{
//	 	        				//loc_id = locationEdit(prepSelectLoc2, prepInsertLoc, loc_id_count, temp[2].trim(), temp[1].trim(), temp[0].trim());
//	 	        				city = temp[0].trim();
//		 	        			state = temp[1].trim();
//		 	        			country = temp[2].trim();
//	 	        			}
//	 	        			else
//	 	        				continue;
//	 	        		}
	 	        	}
	 	        	else if(len == 6)
	 	        	{
	 	        		if(x[0].equals("city"))
	 	        		{
	 	        			continue;
	 	        		}
	 	        		else if(x[0].equals("country"))
	 	        		{

	 	        			//loc_id = locationEdit(prepSelectLoc1, prepInsertLoc, loc_id_count, temp[1].trim(), null, temp[0].trim());
	 	        			city = temp[0].trim();
	 	        			state = null;
	 	        			country = temp[1].trim();
	 	        		}
	 	        		else if(x[0].equals("neighborhood"))
	 	        		{
	 	        			//loc_id = locationEdit(prepSelectLoc1, prepInsertLoc, loc_id_count, temp[3].trim(), null, temp[2].trim());
	 	        			city = temp[2].trim();
	 	        			state = null;
	 	        			country = temp[3].trim();
	 	        		}
	 	        		else if(x[0].equals("admin"))
	 	        		{
	 	        			continue;
	 	        		}
	 	        		else if(x[0].equals("poi"))
	 	        			continue;
//	 	        		{
//	 	        			//loc_id = locationEdit(prepSelectLoc2, prepInsertLoc, loc_id_count, temp[3].trim(), temp[2].trim(), temp[1].trim());
//	 	        			city = temp[1].trim();
//	 	        			state = temp[2].trim();
//	 	        			country = temp[3].trim();
//	 	        		}
	 	        	}
	 	        	else if(len == 7)
	 	        	{
	 	        		if(x[0].equals("city"))
	 	        		{
	 	        			//loc_id = locationEdit(prepSelectLoc2, prepInsertLoc, loc_id_count, temp[4].trim(), temp[3].trim(), temp[0].trim());
	 	        			city = temp[0].trim();
	 	        			state = temp[3].trim();
	 	        			country = temp[4].trim();
	 	        		}
	 	        		else if(x[0].equals("neighborhood"))
	 	        		{
	 	        			//loc_id = locationEdit(prepSelectLoc1, prepInsertLoc, loc_id_count, temp[3].trim(), null, temp[2].trim());
	 	        			city = temp[2].trim();
	 	        			state = null;
	 	        			country = temp[3].trim();
	 	        		}
	 	        		
	 	        		else if(x[0].equals("poi"))
	 	        			continue;
//	 	        		{
//	 	        			//loc_id = locationEdit(prepSelectLoc2, prepInsertLoc, loc_id_count, temp[4].trim(), temp[1].trim(), temp[0].trim());
//	 	        			city = temp[0].trim();
//	 	        			state = temp[1].trim();
//	 	        			country = temp[4].trim();
//	 	        		}
	 	        	}
        			
	 	        	if(state == null)
	 	        		input = city + ",null," + country + "," + lang;
	 	        	else
	 	        		input = city + "," + state + "," + country + "," + lang;
	 	        	
	 	        	try{
	 	        		int curr_count = placeCount.get(input);
	 	        		placeCount.put(input, (count+curr_count));
	 	        	}
	 	        	catch(Exception e)
	 	        	{
	 	        		placeCount.put(input, count);
	 	        	}
 	
	 	        }
	 	        
	 	        System.out.println(entries);
	 	        bufferReader.close();
	 		}
	 		catch(Exception e){
	 	          System.out.println("Error while reading file line by line:" + e.getMessage());  
	 	          e.printStackTrace();
	 	       }

	 		//writeToFile("/Users/intern/Documents/placeCountUnique.txt",placeCount);
	 		
	 		System.out.println(placeCount.size());
	         System.out.println("Done");
	         
	      } catch (Exception e) {
	         e.printStackTrace();
	         System.err.println(e.getClass().getName()+": "+e.getMessage());
	         System.exit(0);
	      }
		
		return placeCount;
	}
	
	public static Map<String, Integer> modifyDictionary(Map<String, Integer> placeCount)
	{
		String fields[], temp[], key;
		Map<String, Integer> cleanPlaceCount = new HashMap<String, Integer>();
		
		Iterator<Entry<String, Integer>> iterator = placeCount.entrySet().iterator();
        while(iterator.hasNext()) 
        {
        	Entry<String, Integer> myentry = iterator.next();
		    fields = myentry.getKey().split(",");
		    
		    key = myentry.getKey();
		    
		    if(fields[1].equals("null"))
		    {
		    	Iterator<Entry<String, Integer>> iterator2 = placeCount.entrySet().iterator();
		        while(iterator2.hasNext()) 
		        {
				    Entry<String, Integer> myentry2 = iterator2.next();
				    temp = myentry2.getKey().split(",");
				    
				    if(!(temp[1].equals("null"))){
					    if(temp[0].equals(fields[0]) && temp[2].equals(fields[2]))
					    {
					    	key = myentry2.getKey();
					    	break;
					    }
				    }
		        }
		    }
		    	
		    try{
	    		int c = cleanPlaceCount.get(key);
	    		cleanPlaceCount.put(key, c + myentry.getValue());
	    	}
		    catch(Exception e){
	    		cleanPlaceCount.put(key, myentry.getValue());
	    	} 
		    
		    
        }
		
		return cleanPlaceCount;
	}
	
	public static void slelectedInsert(Map<String, Integer> placeCount) throws Exception
	{
		
		java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM/yyyy");
        java.util.Date ud = fmt.parse("09/01/2016");
        java.sql.Date sd = new java.sql.Date(ud.getTime());
		
		String fields[], location;
		int loc_id_count = 1, loc_id, curr_count = 0;
		Map<String, Integer> placeId = new HashMap<String, Integer>();
		Map<String, Integer> placeNull = new HashMap<String, Integer>();
		
		Connection c = null;
		PreparedStatement prepInsertLoc = null;
		PreparedStatement prepSelectLoc = null;
		PreparedStatement prepInsertTweet = null;
		PreparedStatement prepUpdateTweetCount = null;
		try
		{
			 Class.forName("org.postgresql.Driver");
	         c = DriverManager
	            .getConnection("jdbc:postgresql://localhost:5432/Tweet",
	            "postgres", "thanks123");
	         System.out.println("Opened database successfully");
	         
	         prepInsertLoc = c.prepareStatement("INSERT INTO LOCATION (ID, COUNTRY, STATE, CITY) VALUES(?,?,?,?)");
	         prepInsertTweet = c.prepareStatement("INSERT INTO TWEETCOUNT (LOCATION_ID, LANG, TWEETDATE, COUNT) VALUES(?,?,?,?)");
	         prepSelectLoc = c.prepareStatement("SELECT * FROM LOCATION WHERE COUNTRY = (?) AND CITY = (?)");
	         prepUpdateTweetCount = c.prepareStatement("UPDATE TWEETCOUNT set count = count + (?) WHERE location_id= (?) AND lang = (?) AND tweetdate = (?)");
			
						
 	        Iterator<Entry<String, Integer>> iterator = placeCount.entrySet().iterator();
 	        while(iterator.hasNext()) 
 	        {
 			    Entry<String, Integer> myentry = iterator.next();
 			    
				fields=myentry.getKey().split(",");
				curr_count = myentry.getValue();
				
				location = fields[0]+ "," + fields[1]+ "," + fields[2];
				
				if(!(fields[1].equals("null")))
				{
					try{
						loc_id = placeId.get(location);
					}
					catch(Exception e)
					{
						loc_id = loc_id_count;
						loc_id_count++;
						
						placeId.put(location, loc_id);
						
						prepInsertLoc.setInt(1, loc_id);
						prepInsertLoc.setString(2, fields[2]);
						prepInsertLoc.setString(4, fields[0]);
						
						if(fields[1].equals("null"))
						{
							prepInsertLoc.setNull(3, Types.VARCHAR);
							//placeId.
						}
						else
							prepInsertLoc.setString(3, fields[1]);
												
						prepInsertLoc.executeUpdate();
					}
					
					prepInsertTweet.setInt(1, loc_id);
					prepInsertTweet.setString(2, fields[3]);
					prepInsertTweet.setDate(3, sd);
					prepInsertTweet.setInt(4, curr_count);
					
					//placeCount.remove(myentry.getKey());
					prepInsertTweet.executeUpdate();
				}
				else
					placeNull.put(myentry.getKey(), myentry.getValue());
	        }
			
 	        iterator = placeNull.entrySet().iterator();
	        while(iterator.hasNext()) 
	        {
			    Entry<String, Integer> myentry = iterator.next();
			    
				fields=myentry.getKey().split(",");
				curr_count = myentry.getValue();
				
				location = fields[0]+ "," + fields[1]+ "," + fields[2];
				loc_id = 0;
				
				prepSelectLoc.setString(1, fields[2]);
				prepSelectLoc.setString(2, fields[0]);
				ResultSet rs = prepSelectLoc.executeQuery();
				while(rs.next())
				{
					loc_id = rs.getInt("id");
					prepUpdateTweetCount.setInt(1, curr_count);
					prepUpdateTweetCount.setInt(2, loc_id);
					prepUpdateTweetCount.setString(3, fields[3]);
					prepUpdateTweetCount.setDate(4, sd);
					
					prepUpdateTweetCount.execute();
				}
				
				if(loc_id == 0)
				{
					try{
						loc_id = placeId.get(location);
					}
					catch(Exception e)
					{
						loc_id = loc_id_count;
						loc_id_count++;
						
						placeId.put(location, loc_id);
						
						prepInsertLoc.setInt(1, loc_id);
						prepInsertLoc.setString(2, fields[2]);
						prepInsertLoc.setString(4, fields[0]);
						
						if(fields[1].equals("null"))
						{
							prepInsertLoc.setNull(3, Types.VARCHAR);
							//placeId.
						}
						else
							prepInsertLoc.setString(3, fields[1]);
												
						prepInsertLoc.executeUpdate();
					}
					
					prepInsertTweet.setInt(1, loc_id);
					prepInsertTweet.setString(2, fields[3]);
					prepInsertTweet.setDate(3, sd);
					prepInsertTweet.setInt(4, curr_count);
					
					//placeCount.remove(myentry.getKey());
					prepInsertTweet.executeUpdate();
	
				}
		
	        }
	 	   
			
		prepInsertLoc.close();
        prepInsertTweet.close();
         
        System.out.println("Done");
         
        c.close();
	    } 
		catch (Exception e) {
	         e.printStackTrace();
	         System.err.println(e.getClass().getName()+": "+e.getMessage());
	         System.exit(0);
	    }
		
	}
	
	public static void readExistingLocations(Map<String, Integer> placeId) throws Exception
	{
		Connection c = null;
		PreparedStatement prepSelectLoc = null;
		try
		{
			 Class.forName("org.postgresql.Driver");
	         c = DriverManager
	            .getConnection("jdbc:postgresql://localhost:5432/Tweet",
	            "postgres", "thanks123");
	         System.out.println("Opened database successfully");
	         
	         prepSelectLoc = c.prepareStatement("SELECT * FROM LOCATION");

	         int id;
	         String city, state, country;
	         
	         ResultSet rs = prepSelectLoc.executeQuery();
	         while(rs.next())
	         {
	        	 id = rs.getInt("id");
	        	 city = rs.getString("city");
	        	 state = rs.getString("state");
	        	 country = rs.getString("country");
	        	 
	        	 if(state.equals(null))
	        		 state = "null";
		     
	        	 placeId.put( city + "," + state + "," + country , id);
	         }
	         						
 	       prepSelectLoc.close();
 	       c.close();
	    } 
		catch (Exception e) {
	         e.printStackTrace();
	         System.err.println(e.getClass().getName()+": "+e.getMessage());
	         System.exit(0);
	    }
	}
	
	public static void insert(Map<String, Integer> placeCount) throws Exception
	{
		
		java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM/yyyy");
        java.util.Date ud = fmt.parse("09/01/2016");
        java.sql.Date sd = new java.sql.Date(ud.getTime());
		
		String fields[], location;
		int loc_id_count = 1, loc_id, curr_count = 0;
		Map<String, Integer> placeId = new HashMap<String, Integer>();
		
		//readExistingLocations(placeId);	//To use when run for multiple days
		
		Connection c = null;
		PreparedStatement prepInsertLoc = null;
		PreparedStatement prepInsertTweet = null;
		try
		{
			 Class.forName("org.postgresql.Driver");
	         c = DriverManager
	            .getConnection("jdbc:postgresql://localhost:5432/Tweet",
	            "postgres", "thanks123");
	         System.out.println("Opened database successfully");
	         
	         prepInsertLoc = c.prepareStatement("INSERT INTO LOCATION (ID, COUNTRY, STATE, CITY) VALUES(?,?,?,?)");
	         prepInsertTweet = c.prepareStatement("INSERT INTO TWEETCOUNT (LOCATION_ID, LANG, TWEETDATE, COUNT) VALUES(?,?,?,?)");
	         						
 	        Iterator<Entry<String, Integer>> iterator = placeCount.entrySet().iterator();
 	        while(iterator.hasNext()) 
 	        {
 			    Entry<String, Integer> myentry = iterator.next();
 			    
				fields=myentry.getKey().split(",");
				curr_count = myentry.getValue();
				
				location = fields[0]+ "," + fields[1]+ "," + fields[2];
				
				try{
					loc_id = placeId.get(location);
				}
				catch(Exception e)
				{
					loc_id = loc_id_count;
					loc_id_count++;
					
					placeId.put(location, loc_id);
					
					prepInsertLoc.setInt(1, loc_id);
					prepInsertLoc.setString(2, fields[2]);
					prepInsertLoc.setString(4, fields[0]);
					
					if(fields[1].equals("null"))
					{
						prepInsertLoc.setNull(3, Types.VARCHAR);
						//placeId.
					}
					else
						prepInsertLoc.setString(3, fields[1]);
											
					prepInsertLoc.executeUpdate();
				}
				
				prepInsertTweet.setInt(1, loc_id);
				prepInsertTweet.setString(2, fields[3]);
				prepInsertTweet.setDate(3, sd);
				prepInsertTweet.setInt(4, curr_count);
				
				//placeCount.remove(myentry.getKey());
				prepInsertTweet.executeUpdate();
				
	        }
			
		prepInsertLoc.close();
        prepInsertTweet.close();
         
        System.out.println("Done");
         
        c.close();
	    } 
		catch (Exception e) {
	         e.printStackTrace();
	         System.err.println(e.getClass().getName()+": "+e.getMessage());
	         System.exit(0);
	    }
		
	}
	
	
	public static void main(String[] args) throws Exception
	{
		Map<String, Integer> placeCount = readPlace();
		//placeCount = modifyDictionary(placeCount);
		//System.out.println(placeCount.size());
		//slelectedInsert(placeCount);
		insert(placeCount);	//Preferred to use
	
	}
	
}
