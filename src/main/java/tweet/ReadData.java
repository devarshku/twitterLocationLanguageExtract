/**
 * ReadData.java
 */

package tweet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
//import java.lang.Thread;
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

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;

/**
 * Class that extracts relevant information from tweets to identify location, language and number of tweets.
 * @author devarsh.k.u devarsh95@gmail.com, devarsh.karekal-umashankar@uni-konstanz.de
 * @version 1.0
 */
public final class ReadData {
   /**
    * Constructor to ensure no objects of this class are created.
    */
   private ReadData() {
   }

   /**
    * Function that makes a GET HTTP request and returns the response.
    * @param urlToRead url
    * @return response of the HTTP request
    * @throws Exception if the reponse is an error
    */
   public static String getHTML(final String urlToRead) throws Exception {
      try {
         final StringBuilder result = new StringBuilder();
         final URL url = new URL(urlToRead);
         final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setRequestMethod("GET");
         final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         String line;
         while ((line = rd.readLine()) != null) {
            result.append(line);
         }
         rd.close();
         return result.toString();
      } catch (final Exception e) {
         return "error";
      }
   }

   /**
    * Function that reads all tweets and extracts location, language and the corresponding count in a dictionary.
    * @param file location and filename
    * @param tweetPlace map containing location and language as the key and the count of tweets as the value
    */
   public static void readFromFile(final File file, final Map<String, Integer> tweetPlace) {
      String[] c;
      try {
         final Reader inputFile = new InputStreamReader(new FileInputStream(file), "UTF-8");
         final BufferedReader bufferReader = new BufferedReader(inputFile);
         String line;
         String lang;
         String latitude;
         String longitude;
         String input = null;
         int tweetCount = 0;
         int nullCount = 0;
         final int currentTweetCount = 1;
         int coordsTotal = 0;
         int coordsUsable = 0;
         int latlongnull = 0;

         while ((line = bufferReader.readLine()) != null) {
            c = line.split("\t");
            tweetCount++;

            // tweet place-10th field
            if (c[9].equals("null")) {
               // coords - 6th field
               if (c[5].equals("null")) {
                  nullCount++;
                  continue;
               } else {
                  coordsTotal++;

                  final String[] tempInp = c[5].split(",");
                  latitude = tempInp[0];
                  longitude = tempInp[1];

                  int i;
                  String[] temp;
                  String city = null;
                  String state = null;
                  String country = null;
                  final String label;

                  String result = getHTML("https://search.mapzen.com/v1/reverse?api_key=search-YNGBFTs&point.lat="
                        + latitude + "&point.lon=" + longitude);

                  final int beginIndex = result.indexOf("properties");
                  // unable to reverse geocode
                  if (beginIndex == -1) {
                     latlongnull++;
                     nullCount++;
                     continue;
                  }

                  final int endIndex = result.indexOf("properties", beginIndex + 10);
                  if (endIndex == -1) {
                     result = result.substring(beginIndex);
                  } else {
                     result = result.substring(beginIndex, endIndex);
                  }

                  final String[] address = result.split(",");

                  try {
                     for (i = 0; i < address.length; i++) {
                        // System.out.println(i+"   " +address[i]);

                        temp = address[i].split(":");
                        if (temp[0].equals("\"country\"")) {
                           country = temp[1];
                           country = country.substring(1, country.length() - 1);
                        } else if (temp[0].equals("\"region\"")) {
                           state = temp[1];
                           state = state.substring(1, state.length() - 1);
                        } else if (temp[0].equals("\"locality\":")) {
                           city = temp[1];
                           break;
                        }
                     }

                     if (i == address.length) {
                        label = result.substring(result.lastIndexOf("\"label\""));
                        String[] t = label.split("\"");

                        t = t[3].split(",");
                        city = t[t.length - 2].trim();
                     }

                     input = city + "," + state + "," + country + ",null,coords";

                     coordsUsable++;
                     // due to the API call restrictions- 5 call per sec
                     Thread.sleep(200);
                  } catch (final Exception e) {
                     nullCount++;
                    }
                  }
            } else {
               input = c[9];
            }

            try {
               final Detector detector = DetectorFactory.create();
               detector.append(c[2]);
               lang = detector.detect();
            } catch (final Exception e) {
               // for tweets with only symbols like smileys
               lang = "sym";
            }
            input += " " + lang;

            /*
             * //counting retweets as new tweets if(c[4].equals("null")) current_tweet_count = 1; else
             * current_tweet_count = Integer.parseInt(c[4]);
             */

            try {
               tweetPlace.put(input, tweetPlace.get(input) + currentTweetCount);
            } catch (final Exception e) {
               tweetPlace.put(input, currentTweetCount);
               continue;
            }
         }

         System.out.println("tweet_count: " + tweetCount + "; null count: " + nullCount + "; coords_total: "
               + coordsTotal + "; coords_usable: " + coordsUsable + "; percentage available: "
               + ((tweetCount + 0.0 - nullCount) / tweetCount));
         System.out.println(latlongnull);
         bufferReader.close();
      } catch (final Exception e) {
         System.out.println("Error while reading file line by line:" + e.getMessage());
         e.printStackTrace();
      }
   }
   /**
    * Function that writes extracted information into a file.
    * @param filename file name
    * @param myhash map containing data extracted from the tweets
    */
   public static void writeToFile(final String filename, final Map<String, Integer> myhash) {
      try {
         final File file = new File(filename);
         file.createNewFile();

         final FileWriter fw = new FileWriter(file.getAbsoluteFile());
         final BufferedWriter bw = new BufferedWriter(fw);

         final Iterator<Entry<String, Integer>> iterator = myhash.entrySet().iterator();
         while (iterator.hasNext()) {
            final Entry<String, Integer> myentry = iterator.next();

            final String content = "" + myentry.getKey() + " " + myentry.getValue() + "\n";
            bw.write(content);
         }

         bw.close();
      } catch (final Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Main function.
    * @param args is not required
    */
   public static void main(final String[] args) {
      final Map<String, Integer> tweetPlace = new HashMap<String, Integer>();

      // To get the date from filename
      final String tweetsLocation = System.getenv("TWEETS_LOCATION");
      if (tweetsLocation == null) {
         System.err.println("Please set the environment variable 'TWEETS_LOCATION'.");
         System.exit(1);
      }

      final File dir = new File(tweetsLocation);
      if (!dir.isDirectory()) {
         System.err.println("'TWEETS_LOCATION' is not an existing directory.");
         System.exit(1);
      }

      try {
         DetectorFactory.loadProfile("src/main/resources/profiles/");

         String filename = "2016_01_09_" + "00" + ".csv";

         // readFromFile(filename, tweet_place);

         int i = 0;
         for (i = 0; i < 24; i++) {
            if (i < 10) {
               filename = "2016_01_09_0" + i + ".csv";
            } else {
               filename = "2016_01_09_" + i + ".csv";
            }
            readFromFile(new File(dir, filename), tweetPlace);
         }

         writeToFile("tweet_place.txt", tweetPlace);
      } catch (final Exception e) {
         System.out.println("Error: " + e.getMessage());
         e.printStackTrace();
      }
   }
}
