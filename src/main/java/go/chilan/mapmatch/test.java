package go.chilan.mapmatch;

import java.io.*;
import java.net.URL;
public class test {
   public static void main(String[] args) {
      String sURL = "https://www.yahoo.com";
      try {
         URL oURL = new URL(sURL);
         
       //  BufferedWriter out = new BufferedWriter(new OutputStreamReader(oURL.))
         BufferedReader in = new BufferedReader(
            new InputStreamReader(oURL.openStream()));
         String line;
         while ((line = in.readLine()) != null) 
            System.out.println(line);
         in.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}