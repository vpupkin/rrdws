package org.vietspider.html.parser;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import org.junit.Test;
import org.vietspider.html.HTMLDocument;
import org.vietspider.html.HTMLNode;

import ws.rrd.server.LServlet;


/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  25.08.2010::14:42:58<br> 
 */
public class HTMLParser_IMG_atNOSCRIPT_Test  {
	private static final String TEST_HTML = "org/vietspider/html/parser/noscript@img.htm";
	private static String HTML  =""; 
	private static HTMLDocument HDOC ;
	static{
		try {
			InputStream inRes = HTMLParser_IMG_atNOSCRIPT_Test.class.getClassLoader().getResourceAsStream(TEST_HTML);
			InputStreamReader in  = new InputStreamReader (inRes, "UTF-8");
			BufferedReader readerTmp = new BufferedReader(in);
		
			for (String lineTmp =readerTmp .readLine(); null != lineTmp; lineTmp = readerTmp .readLine() ){
				HTML += lineTmp ;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		try {
			HTMLParser2 p2 =  new HTMLParser2();
			HDOC = p2.createDocument(HTML);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	 
	@Test
	public void testCreateDocumentString() throws Exception {
		HTMLParser2 p2 =  new HTMLParser2();  
		HTMLDocument docTmp = p2.createDocument(HTML); //.replace("<noscript>", "<!-- 1<2n3o4s5c6r7i8p9t0>0 -->").replace("</noscript>", "<!-- 01<2n3o4s5c6r7i8p9t0>0 -->")
		docTmp.getRoot().setBeautify(false);
    	HTMLNode rootTmp = HDOC.getRoot();
    	
		String  SwapServletUrl = "https://WwW.SSSSS.SS/l/";
		URL  realURL = new URL("http://web.de");
		LServlet. testCreateFullLink(rootTmp, SwapServletUrl, realURL);
//    	testCreateImageLink(documentTmp.getRoot(), SwapServletUrl, realURL);
		LServlet.testCreateMetaLink(rootTmp, SwapServletUrl, realURL);    	
		LServlet.testCreateScriptLink(rootTmp, SwapServletUrl, realURL);	   
		LServlet.testCreateStyleLink( rootTmp, SwapServletUrl, realURL);
		LServlet.testCreateNoScriptLink(rootTmp, SwapServletUrl, realURL);	    
		
		HTMLNode doctype = docTmp.getDoctype();
		String sDoctype = (doctype==null?""+"":doctype.getTextValue());
		String textValue = sDoctype  + rootTmp.getTextValue();
		//System.out.println(textValue);
		
		
		
		String[]  expected = HTML.toLowerCase().replace( " ","").replace( "\t","").replace("&gt;", ">").replace("&lt;", "<").replace("><", ">\n<").split("\n");
		String[] actual = textValue.toLowerCase().replace( " ","").replace( "\t","").replace("&gt;", ">").replace("&lt;", "<"). replace("><", ">\n<").split("\n")  ;
		
		Diff diff  = new Diff();//System.out.println(textValue);
		diff.setSAME(true);
		String[] diffTmp = diff  .diff(expected,actual);//actual
		// TODO : 20 are '<scripttype..' AND '<styletype..' ONLY
		assertEquals(diffTmp.length, 10); 
	}

}


 