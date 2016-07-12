/**
 * insertDB2.java
 */

package tweet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.sql.Date;

/**
 * Class to insert locations and languages along with their tweet-count into a database.
 * @author devarsh.k.u devarsh95@gmail.com, devarsh.karekal-umashankar@uni-konstanz.de
 * @version 1.0
 */
public final class InsertDB2 {
   /**
    * Constructor to ensure no objects of this class are created.
    */
   private InsertDB2() {
   }

    /** 
     * Function that creates a dictionary of states of a corresponding country.
     * @return map with key as country name and value as a list of states/provinces of that country.
     */
   public static Map<String, ArrayList<String>> createCountryState() {
      final String fileName = "src/main/resources/pls.txt";
      String[] c;
      String line = "";
      final Map<String, ArrayList<String>> countryState = new HashMap<String, ArrayList<String>>();

      try (Reader inputFile = new InputStreamReader(new FileInputStream(fileName), "UTF-8")) {
         // FileReader inputFile = new FileReader(fileName);
         final BufferedReader bufferReader = new BufferedReader(inputFile);

         while ((line = bufferReader.readLine()) != null) {
            String country;
            String t;
            final String[] temp;
            final ArrayList<String> states = new ArrayList<String>();
            int i;

            c = line.split(",");

            country = c[0];
            country = country.substring(3, country.length() - 1);
            t = c[5];
            t = t.substring(2, t.length() - 1);

            t = t.replace('|', ',');
            temp = t.split(",");

            for (i = 0; i < temp.length; i++) {
               states.add(temp[i]);
            }

            countryState.put(country, states);
         }
         bufferReader.close();
      } catch (final Exception e) {

         System.out.println("Error while reading file line by line:" + e.getMessage());
         e.printStackTrace();
      }
      return countryState;
   }

    /**
     *  Function readPlace reads location, language and count of tweets and
     *  organizes it into a dictionary which is returned.
     * @return a map which has location concatenated with language as key and its corresponding
     * count of tweets as value
     * @throws Exception for missing data or file read errors
     */
   public static Map<String, Integer> readPlace() throws Exception {

      final Map<String, ArrayList<String>> countryState = createCountryState();
      final Map<String, Integer> placeCount = new HashMap<String, Integer>();
      String city = null;
      String state = null;
      String country = null;
      String lang;
      String input;

      try {
         // file read
         String[] temp = new String[7];
         int entries = 0;
         int len;
         try {
            final FileReader inputFile = new FileReader("tweet_place.txt");
            final BufferedReader bufferReader = new BufferedReader(inputFile);
            String line;
            int count;

            while ((line = bufferReader.readLine()) != null) {

               temp = line.split(",");
               entries++;

               len = temp.length;
               if (len < 4) {
                  continue;
               }

               // System.out.println(temp[temp.length - 1]);
               final String[] x = temp[temp.length - 1].split(" ");
               count = Integer.parseInt(x[x.length - 1]);
               lang = x[1];

               if (len == 4) {
                  if (x[0].equals("city") || x[0].equals("admin")) {
                     if (temp[1].equals(temp[0])) {
                        continue;
                     }
                     city = temp[0].trim();
                     state = null;
                     country = temp[1].trim();
                  } else if (x[0].equals("country") || x[0].equals("neighborhood") || x[0].equals("poi")) {
                     continue;
                  }
               } else if (len == 5) {
                  if (x[0].equals("city")) {
                     if ((temp[1].trim()).equals(temp[2].trim())) {
                        city = temp[0].trim();
                        state = null;
                        country = temp[2].trim();
                     } else {
                        city = temp[0].trim();
                        state = temp[1].trim();
                        country = temp[2].trim();
                     }
                  } else if (x[0].equals("coords")) {
                     city = temp[0].trim();
                     state = temp[1].trim();
                     country = temp[2].trim();
                  } else if (x[0].equals("neighborhood")) {
                     try {

                        final ArrayList<String> states = countryState.get(temp[2].trim());
                        if (states.contains(temp[1].trim())) {
                           city = temp[0].trim();
                           state = temp[1].trim();
                           country = temp[2].trim();
                        } else {
                           city = temp[1].trim();
                           state = null;
                           country = temp[2].trim();
                        }
                     } catch (final Exception e) {
                        city = temp[1].trim();
                        state = null;
                        country = temp[2].trim();
                     }
                  } else if (x[0].equals("admin")) {

                     if ((temp[2].trim()).equals(temp[1].trim())) {
                        try {
                           final ArrayList<String> states = countryState.get(temp[2].trim());
                           if (states.contains(temp[0].trim())) {
                              continue;
                           } else {
                              city = temp[0].trim();
                              state = null;
                              country = temp[2].trim();
                           }
                        } catch (final Exception e) {
                           city = temp[0].trim();
                           state = null;
                           country = temp[2].trim();
                        }
                     } else {
                        final int lim = temp[2].compareTo(temp[1]);
                        if (-10 <= lim && lim <= 10) {
                           city = temp[0].trim();
                           state = null;
                           country = temp[2].trim();
                        } else {
                           city = temp[0].trim();
                           state = temp[1].trim();
                           country = temp[2].trim();
                        }
                     }
                  } else if (x[0].equals("poi")) {
                     continue;
                  }
               } else if (len == 6) {
                  if (x[0].equals("city")) {
                     continue;
                  } else if (x[0].equals("country")) {
                     city = temp[0].trim();
                     state = null;
                     country = temp[1].trim();
                  } else if (x[0].equals("neighborhood")) {
                     city = temp[2].trim();
                     state = null;
                     country = temp[3].trim();
                  } else if (x[0].equals("admin")) {
                     continue;
                  } else if (x[0].equals("poi")) {
                     continue;
                  }
               } else if (len == 7) {
                  if (x[0].equals("city")) {
                     city = temp[0].trim();
                     state = temp[3].trim();
                     country = temp[4].trim();
                  } else if (x[0].equals("neighborhood")) {
                     city = temp[2].trim();
                     state = null;
                     country = temp[3].trim();
                  } else if (x[0].equals("poi")) {
                     continue;
                  }
               }

               if (state == null) {
                  input = city + ",null," + country + "," + lang;
               } else {
                  input = city + "," + state + "," + country + "," + lang;
               }

               try {
                  final int currCount = placeCount.get(input);
                  placeCount.put(input, count + currCount);
               } catch (final Exception e) {
                  placeCount.put(input, count);
               }
            }

            System.out.println(entries);
            bufferReader.close();
         } catch (final Exception e) {
            System.out.println("Error while reading file line by line:" + e.getMessage());
            e.printStackTrace();
         }

         // writeToFile("/Users/intern/Documents/placeCountUnique.txt",placeCount);

         System.out.println(placeCount.size());
         System.out.println("Done");
      } catch (final Exception e) {
         e.printStackTrace();
         System.err.println(e.getClass().getName() + ": " + e.getMessage());
         System.exit(0);
      }

      return placeCount;
   }
    /**
     *  Function that enters all existing location in the databse into a dictionary.
     * @param placeId a map that contains all locations that are in the database along with their id
     * @return the id for a new location
     * @throws Exception when connection to databse fails
     */
   public static int readExistingLocations(final Map<String, Integer> placeId) throws Exception {
      int locIdCount = 1;
      PreparedStatement prepSelectLoc = null;
      Class.forName("org.postgresql.Driver");
      try (final Connection c =
            DriverManager.getConnection("jdbc:postgresql://localhost:5432/Tweet", "postgres", "thanks123")) {
         System.out.println("Opened database successfully");

         prepSelectLoc = c.prepareStatement("SELECT * FROM LOCATION");

         final ResultSet rs = prepSelectLoc.executeQuery();
         while (rs.next()) {
            final int id = rs.getInt("id");
            final String city = rs.getString("city");
            String state = rs.getString("state");
            final String country = rs.getString("country");

            if (state.equals(null)) {
               state = "null";
            }

            locIdCount++;
            placeId.put(city + "," + state + "," + country, id);
         }

         prepSelectLoc.close();
      } catch (final Exception e) {
         e.printStackTrace();
         System.err.println(e.getClass().getName() + ": " + e.getMessage());
         System.exit(0);
      }
      return locIdCount;
   }

   /**
    * Function that enters structured data into the database.
    * @param placeCount dictionary of data to be entered into the database
    * @throws Exception due to connection error
    */
   public static void insert(final Map<String, Integer> placeCount) throws Exception {

      final java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM/yyyy");
      final java.util.Date ud = fmt.parse("09/01/2016");
      final java.sql.Date sd = new java.sql.Date(ud.getTime());

      String[] fields;
      String location;
      int locIdCount = 1;
      int locId;
      int currCount = 0;
      final Map<String, Integer> placeId = new HashMap<String, Integer>();

      // To use when run for multiple days
      // locIdCount = readExistingLocations(placeId);

      Connection c = null;
      PreparedStatement prepInsertLoc = null;
      PreparedStatement prepInsertTweet = null;
      try {
         Class.forName("org.postgresql.Driver");
         c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Tweet", "postgres", "thanks123");
         System.out.println("Opened database successfully");

         prepInsertLoc = c.prepareStatement("INSERT INTO LOCATION (ID, COUNTRY, STATE, CITY) VALUES(?,?,?,?)");
         prepInsertTweet = c
               .prepareStatement("INSERT INTO TWEETCOUNT (LOCATION_ID, LANG, TWEETDATE, COUNT) VALUES(?,?,?,?)");

         final Iterator<Entry<String, Integer>> iterator = placeCount.entrySet().iterator();
         while (iterator.hasNext()) {
            final Entry<String, Integer> myentry = iterator.next();

            fields = myentry.getKey().split(",");
            currCount = myentry.getValue();

            location = fields[0] + "," + fields[1] + "," + fields[2];

            try {
               locId = placeId.get(location);
            } catch (final Exception e) {
               locId = locIdCount;
               locIdCount++;

               placeId.put(location, locId);

               prepInsertLoc.setInt(1, locId);
               prepInsertLoc.setString(2, fields[2]);
               prepInsertLoc.setString(4, fields[0]);

               if (fields[1].equals("null")) {
                  prepInsertLoc.setNull(3, Types.VARCHAR);
                  // placeId.
               } else {
                  prepInsertLoc.setString(3, fields[1]);
               }

               prepInsertLoc.executeUpdate();
            }

            prepInsertTweet.setInt(1, locId);
            prepInsertTweet.setString(2, fields[3]);
            prepInsertTweet.setDate(3, sd);
            prepInsertTweet.setInt(4, currCount);

            // placeCount.remove(myentry.getKey());
            prepInsertTweet.executeUpdate();
         }

         prepInsertLoc.close();
         prepInsertTweet.close();

         System.out.println("Done");

         c.close();
      } catch (final Exception e) {
         e.printStackTrace();
         System.err.println(e.getClass().getName() + ": " + e.getMessage());
         System.exit(0);
      }
   }

   /**
    * Main function.
    * @param args is not required
    * @throws Exception when not caught within one of its functions
    */
   public static void main(final String[] args) throws Exception {
      final Map<String, Integer> placeCount = readPlace();
      insert(placeCount);
      // placeCount = modifyDictionary(placeCount);
      // System.out.println(placeCount.size());
      // slelectedInsert(placeCount);
      // Preferred to use
   }
}


//public static void writeToFile(String filename, Map<String, Integer> myhash) {
// try {
// File file = new File(filename);
// file.createNewFile();
//
// FileWriter fw = new FileWriter(file.getAbsoluteFile());
// BufferedWriter bw = new BufferedWriter(fw);
//
// Iterator<Entry<String, Integer>> iterator = myhash.entrySet().iterator();
// while (iterator.hasNext()) {
// Entry<String, Integer> myentry = iterator.next();
//
// String content = myentry.getKey() + "," + myentry.getValue() + "\n";
// bw.write(content);
// }
//
// bw.close();
// } catch (Exception e) {
// e.printStackTrace();
// }
// }

// public static Map<String, Integer> modifyDictionary(Map<String, Integer> placeCount) {
// String[] fields;
// String[] temp;
// String key;
// Map<String, Integer> cleanPlaceCount = new HashMap<String, Integer>();
//
// Iterator<Entry<String, Integer>> iterator = placeCount.entrySet().iterator();
// while (iterator.hasNext()) {
// Entry<String, Integer> myentry = iterator.next();
// fields = myentry.getKey().split(",");
//
// key = myentry.getKey();
//
// if (fields[1].equals("null")) {
// Iterator<Entry<String, Integer>> iterator2 = placeCount.entrySet().iterator();
// while (iterator2.hasNext()) {
// Entry<String, Integer> myentry2 = iterator2.next();
// temp = myentry2.getKey().split(",");
//
// if (!(temp[1].equals("null"))) {
// if (temp[0].equals(fields[0]) && temp[2].equals(fields[2])) {
// key = myentry2.getKey();
// break;
// }
// }
// }
// }
//
// try {
// int c = cleanPlaceCount.get(key);
// cleanPlaceCount.put(key, c + myentry.getValue());
// } catch (Exception e) {
// cleanPlaceCount.put(key, myentry.getValue());
// }
// }
//
// return cleanPlaceCount;
// }

// public static void slelectedInsert(Map<String, Integer> placeCount) throws Exception {
//
// java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM/yyyy");
// java.util.Date ud = fmt.parse("09/01/2016");
// java.sql.Date sd = new java.sql.Date(ud.getTime());
//
// String[] fields;
// String location;
// int locIdCount = 1;
// int locId;
// int currCount = 0;
// Map<String, Integer> placeId = new HashMap<String, Integer>();
// Map<String, Integer> placeNull = new HashMap<String, Integer>();
//
// Connection c = null;
// PreparedStatement prepInsertLoc = null;
// PreparedStatement prepSelectLoc = null;
// PreparedStatement prepInsertTweet = null;
// PreparedStatement prepUpdateTweetCount = null;
// try {
// Class.forName("org.postgresql.Driver");
// c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Tweet", "postgres", "thanks123");
// System.out.println("Opened database successfully");
//
// prepInsertLoc = c.prepareStatement("INSERT INTO LOCATION (ID, COUNTRY, STATE, CITY) VALUES(?,?,?,?)");
// prepInsertTweet = c
// .prepareStatement("INSERT INTO TWEETCOUNT (LOCATION_ID, LANG, TWEETDATE, COUNT) VALUES(?,?,?,?)");
// prepSelectLoc = c.prepareStatement("SELECT * FROM LOCATION WHERE COUNTRY = (?) AND CITY = (?)");
// prepUpdateTweetCount = c
// .prepareStatement("UPDATE TWEETCOUNT set count = count + (?) WHERE location_id= (?) AND lang = (?)
// AND tweetdate = (?)");
//
// Iterator<Entry<String, Integer>> iterator = placeCount.entrySet().iterator();
// while (iterator.hasNext()) {
// Entry<String, Integer> myentry = iterator.next();
//
// fields = myentry.getKey().split(",");
// currCount = myentry.getValue();
//
// location = fields[0] + "," + fields[1] + "," + fields[2];
//
// if (!(fields[1].equals("null"))) {
// try {
// locId = placeId.get(location);
// } catch (Exception e) {
// locId = locIdCount;
// locIdCount++;
//
// placeId.put(location, locId);
//
// prepInsertLoc.setInt(1, locId);
// prepInsertLoc.setString(2, fields[2]);
// prepInsertLoc.setString(4, fields[0]);
//
// if (fields[1].equals("null")) {
// prepInsertLoc.setNull(3, Types.VARCHAR);
// // placeId.
// } else {
// prepInsertLoc.setString(3, fields[1]);
// }
//
// prepInsertLoc.executeUpdate();
// }
//
// prepInsertTweet.setInt(1, locId);
// prepInsertTweet.setString(2, fields[3]);
// prepInsertTweet.setDate(3, sd);
// prepInsertTweet.setInt(4, currCount);
//
// // placeCount.remove(myentry.getKey());
// prepInsertTweet.executeUpdate();
// } else{
// placeNull.put(myentry.getKey(), myentry.getValue());
// }
// }
//
// iterator = placeNull.entrySet().iterator();
// while (iterator.hasNext()) {
// Entry<String, Integer> myentry = iterator.next();
//
// fields = myentry.getKey().split(",");
// currCount = myentry.getValue();
//
// location = fields[0] + "," + fields[1] + "," + fields[2];
// locId = 0;
//
// prepSelectLoc.setString(1, fields[2]);
// prepSelectLoc.setString(2, fields[0]);
// ResultSet rs = prepSelectLoc.executeQuery();
// while (rs.next()) {
// locId = rs.getInt("id");
// prepUpdateTweetCount.setInt(1, currCount);
// prepUpdateTweetCount.setInt(2, locId);
// prepUpdateTweetCount.setString(3, fields[3]);
// prepUpdateTweetCount.setDate(4, sd);
//
// prepUpdateTweetCount.execute();
// }
//
// if (locId == 0) {
// try {
// locId = placeId.get(location);
// } catch (Exception e) {
// locId = locIdCount;
// locIdCount++;
//
// placeId.put(location, locId);
//
// prepInsertLoc.setInt(1, locId);
// prepInsertLoc.setString(2, fields[2]);
// prepInsertLoc.setString(4, fields[0]);
//
// if (fields[1].equals("null")) {
// prepInsertLoc.setNull(3, Types.VARCHAR);
// // placeId.
// } else {
// prepInsertLoc.setString(3, fields[1]);
// }
//
// prepInsertLoc.executeUpdate();
// }
//
// prepInsertTweet.setInt(1, locId);
// prepInsertTweet.setString(2, fields[3]);
// prepInsertTweet.setDate(3, sd);
// prepInsertTweet.setInt(4, currCount);
//
// // placeCount.remove(myentry.getKey());
// prepInsertTweet.executeUpdate();
// }
// }
//
// prepInsertLoc.close();
// prepInsertTweet.close();
//
// System.out.println("Done");
//
// c.close();
// } catch (Exception e) {
// e.printStackTrace();
// System.err.println(e.getClass().getName() + ": " + e.getMessage());
// System.exit(0);
// }
// }
