package eu.blky.urlfetch;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class UrlFetchTest {

 
	@Test
    public void testFetchOracleUrl() throws IOException{

        URL oracle = new URL("http://www.oracle.com/");
        BufferedReader in = new BufferedReader(
        new InputStreamReader(oracle.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null)
            System.out.println(inputLine);
        in.close();
    }
    
	@Test
    public void testFetchUrl() throws IOException{

		InputStream expectedData = this.getClass().getClassLoader().getResourceAsStream("eu/blky/urlfetch/8011a824c2954345b8efd2017fbbf37c.jpg");
		
        URL oracle = new URL("https://habrastorage.org/files/801/1a8/24c/8011a824c2954345b8efd2017fbbf37c.jpg");
         InputStream in = oracle.openStream();

         byte[] b= new byte[1024]; 
         byte[] b2= new byte[1024];
         
        for (int readedTmp = in.read(b); readedTmp > 0;  readedTmp = in.read(b)){
        	assertEquals( readedTmp,  expectedData.read(b2,0,readedTmp) );
        	String expTmp = new String(b2);
			assertTrue(new String(b).equals( expTmp  )  );
        }
             
        in.close();
    }   

	
}
