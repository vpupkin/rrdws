package eu.blky.logparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.Set;

import eu.blky.logparser.EventLog;
import eu.blky.logparser.Parser;

import junit.framework.TestCase;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  11.09.2012::14:12:49<br> 
 */
public class ParserTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	

	public void test1st() throws Exception{
		assertEquals("TEST".length(), 4);
	}
	public void testAccessLOG000() throws Exception{
		InputStream o = this.getClass().getClassLoader().getResourceAsStream("eu/blky/logparser/access.030.log");
		
		Reader ireader = new InputStreamReader(o);
		BufferedReader bin = new BufferedReader(ireader );
		try{	
			while (true){
				String lineTmp = bin.readLine();
				System.out.println(lineTmp );
				
				//Parser parserTmp = new Parser(Parser.DEFAULT_LINE_PATTERN);
				//  "123.45.67.89 - - [27/Oct/2000:09:27:09 -0400] \"GET /java/javaResources.html HTTP/1.0\" 200 10450 \"-\" \"Mozilla/4.6 [en] (X11; U; OpenBSD 2.8 i386; Nav)\"";
				//   111111111111 2 3 4444444444444444444444444444 55555555555555555555555555555555555555555 666 77777 88888 9999999999999999999999999999999999999999999999999999
				Parser parserTmp = new Parser( "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) \"([^\"]+)\" \"([^\"]+)\" ");
				//                              1111111111 222222 333333 4444444444444444444444444444444 555555555 66666666 777777 888888888888 999999999999
				//  192.168.27.230 - - [23/Apr/2012:13:37:45 +0200] "POST /iboServer3.0/Ibo.Server.rem HTTP/1.1" 200 572 0
				//  11111111111111 2 3 4444444444444444444444444444 55555555555555555555555555555555555555555555 666 777 8
				parserTmp = new Parser( "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) (\\d+)");
				//                       1111111111 222222 333333 4444444444444444444444444444444 555555555 66666666 777777 888888 
				
			    /*System.out.println("IP Address: " + matcher.group(1));
			    System.out.println("Date&Time: " + matcher.group(4));
			    System.out.println("Request: " + matcher.group(5));
			    System.out.println("Response: " + matcher.group(6));
			    System.out.println("Bytes Sent: " + matcher.group(7));
			    if (!matcher.group(8).equals("-"))
			      System.out.println("Referer: " + matcher.group(8));
			    System.out.println("Browser: " + matcher.group(9));*/
				EventLog e = parserTmp .parse8(lineTmp);
				if (e==null)continue;
				System.out.println(e);
			}
		}catch(IOException e){
			// e.  printStackTrace();
		}
		
	}
	
	//access.2012-09-11.log
	
	public void testAccessLOGAgent() throws Exception{
		InputStream o = this.getClass().getClassLoader().getResourceAsStream("eu/blky/logparser/access.2012-09-11.log");
		
		Reader ireader = new InputStreamReader(o);
		BufferedReader bin = new BufferedReader(ireader );
		int ln = 0;
		try{	
			String lineTmp = null;
			
			while ( (lineTmp= bin.readLine() )!=null) {
				ln ++;
				System.out.println(lineTmp );
				
				//  192.168.27.230 - - [11/Sep/2012:15:58:05 +0200] "GET /WebUrlaub31/img/rowFirst.png HTTP/1.1" 200 409 0 "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET CLR 1.1.4322; .NET4.0C; .NET4.0E; SLCC1)" 
				//  11111111111111 2 3 4444444444444444444444444444 55555555555555555555555555555555555555555555 666 777 8 9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999
				Parser parserTmp = new Parser( "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) (\\d+) \"(.+?)\" ");
				//                             1111111111 222222 333333 4444444444444444444444444444444 555555555 66666666 777777 888888  9999999999 
				try{
					EventLog ev = parserTmp .parse9(lineTmp);
					if (ev==null)continue;
					System.out.println(ev);
				}catch(ParseException e){
					continue;
				}
				
			}
		}catch(IOException e){
			// e.  printStackTrace();
		}
		System.out.println(ln);
		Set<String> set = EventLog.getUaSet();
		System.out.println(	set	.toString()	.replace(",", "\n"));
		
	}
	
}


 