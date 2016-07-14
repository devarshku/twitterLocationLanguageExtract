/**
 * insertDB2.java
 */

package tweet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
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

/**
 * Class to insert locations and languages along with their tweet-count into a database.
 * @author devarsh.k.u devarsh95@gmail.com, devarsh.karekal-umashankar@uni-konstanz.de
 * @version 1.0
 */
public final class InsertDB2 {
   /**
    * Variable that has the relative location and file name
    * to build a dictionary of the Country and its corresponding states.
    */
   public static final String COUNTRY_STATE_FILE = "asdf";
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
      String line = "";
      final Map<String, ArrayList<String>> countryState = new HashMap<String, ArrayList<String>>();
      try (BufferedReader bufferReader = new BufferedReader(
            new InputStreamReader(new FileInputStream(COUNTRY_STATE_FILE), "UTF-8"))) {
         while ((line = bufferReader.readLine()) != null) {
            final String[] c = line.split(",");

            String country = c[0];
            country = country.substring(3, country.length() - 1);
            String t = c[5];
            t = t.substring(2, t.length() - 1);

            t = t.replace('|', ',');
            final String[] temp = t.split(",");

            final ArrayList<String> states = new ArrayList<String>();
            for (int i = 0; i < temp.length; i++) {
               states.add(temp[i]);
            }

            countryState.put(country, states);
         }
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
         try (final BufferedReader bufferReader = new BufferedReader(
               new InputStreamReader(new FileInputStream("tweet_place.txt"), "UTF-8"))) {
            String line;

            while ((line = bufferReader.readLine()) != null) {

               temp = line.split(",");
               entries++;

               len = temp.length;
               if (len < 4) {
                  continue;
               }
               final String[] x = temp[temp.length - 1].split(" ");
               final int count = Integer.parseInt(x[x.length - 1]);
               lang = x[1];

               temp[0] = temp[0].trim();
               temp[1] = temp[1].trim();
               if (len == 4) {
                  if (x[0].equals("city") || x[0].equals("admin")) {
                     if (temp[1].equals(temp[0])) {
                        continue;
                     }
                     city = temp[0];
                     state = null;
                     country = temp[1];
                  } else if (x[0].equals("country") || x[0].equals("neighborhood") || x[0].equals("poi")) {
                     continue;
                  }
               } else if (len == 5) {
                  temp[2] = temp[2].trim();
                  if (x[0].equals("city")) {
                     if ((temp[1]).equals(temp[2])) {
                        city = temp[0];
                        state = null;
                        country = temp[2];
                     } else {
                        city = temp[0];
                        state = temp[1];
                        country = temp[2];
                     }
                  } else if (x[0].equals("coords")) {
                     city = temp[0];
                     state = temp[1];
                     country = temp[2];
                  } else if (x[0].equals("neighborhood")) {
                     try {

                        final ArrayList<String> states = countryState.get(temp[2]);
                        if (states.contains(temp[1])) {
                           city = temp[0];
                           state = temp[1];
                           country = temp[2];
                        } else {
                           city = temp[1];
                           state = null;
                           country = temp[2];
                        }
                     } catch (final Exception e) {
                        city = temp[1];
                        state = null;
                        country = temp[2];
                     }
                  } else if (x[0].equals("admin")) {

                     if ((temp[2]).equals(temp[1])) {
                        try {
                           final ArrayList<String> states = countryState.get(temp[2]);
                           if (states.contains(temp[0])) {
                              continue;
                           } else {
                              city = temp[0];
                              state = null;
                              country = temp[2];
                           }
                        } catch (final Exception e) {
                           city = temp[0];
                           state = null;
                           country = temp[2];
                        }
                     } else {
                        final int lim = temp[2].compareTo(temp[1]);
                        if (-10 <= lim && lim <= 10) {
                           city = temp[0];
                           state = null;
                           country = temp[2];
                        } else {
                           city = temp[0];
                           state = temp[1];
                           country = temp[2];
                        }
                     }
                  } else if (x[0].equals("poi")) {
                     continue;
                  }
               } else if (len == 6) {
                  temp[2] = temp[2].trim();
                  temp[3] = temp[3].trim();
                  if (x[0].equals("city")) {
                     continue;
                  } else if (x[0].equals("country")) {
                     city = temp[0];
                     state = null;
                     country = temp[1];
                  } else if (x[0].equals("neighborhood")) {
                     city = temp[2];
                     state = null;
                     country = temp[3];
                  } else if (x[0].equals("admin")) {
                     continue;
                  } else if (x[0].equals("poi")) {
                     continue;
                  }
               } else if (len == 7) {
                  temp[2] = temp[2].trim();
                  temp[3] = temp[3].trim();
                  temp[4] = temp[4].trim();
                  if (x[0].equals("city")) {
                     city = temp[0];
                     state = temp[3];
                     country = temp[4];
                  } else if (x[0].equals("neighborhood")) {
                     city = temp[2];
                     state = null;
                     country = temp[3];
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

      int locIdCount = 1;
      int currCount = 0;
      final Map<String, Integer> placeId = new HashMap<String, Integer>();

      // To use when run for multiple days
      // locIdCount = readExistingLocations(placeId);

      PreparedStatement prepInsertLoc = null;
      PreparedStatement prepInsertTweet = null;
      Class.forName("org.postgresql.Driver");
      try (Connection c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Tweet",
            "postgres", "thanks123")) {
        System.out.println("Opened database successfully");

         prepInsertLoc = c.prepareStatement("INSERT INTO LOCATION (ID, COUNTRY, STATE, CITY) VALUES(?,?,?,?)");
         prepInsertTweet = c
               .prepareStatement("INSERT INTO TWEETCOUNT (LOCATION_ID, LANG, TWEETDATE, COUNT) VALUES(?,?,?,?)");

         final Iterator<Entry<String, Integer>> iterator = placeCount.entrySet().iterator();
         while (iterator.hasNext()) {
            final Entry<String, Integer> myentry = iterator.next();

            final String[] fields = myentry.getKey().split(",");
            currCount = myentry.getValue();

            final String location = fields[0] + "," + fields[1] + "," + fields[2];

            int locId;
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

            prepInsertTweet.executeUpdate();
         }

         prepInsertLoc.close();
         prepInsertTweet.close();

         System.out.println("Done");
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