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
public class HTMLParser_SEARCH_Test  {
	private static final String TEST_HTML = "org/vietspider/html/parser/searchForm.html";
	private static String HTML  =""; 
	private static HTMLDocument HDOC ;
	static{
		InputStream inRes = HTMLParser_SEARCH_Test.class.getClassLoader().getResourceAsStream(TEST_HTML);
		InputStreamReader in  = new InputStreamReader (inRes);
		BufferedReader readerTmp = new BufferedReader(in);
		try {
			for (String lineTmp =readerTmp .readLine(); null != lineTmp; lineTmp = readerTmp .readLine() ){
				HTML += lineTmp ;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
		}		
		try {
			HTMLParser2 p2 =  new HTMLParser2();
			HDOC = p2.createDocument(HTML);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
		}
	}	 
	@Test
	public void testCloneHTMLNode() throws Exception {  
		HTMLNode docTmpClone = HTMLParser2.clone(HDOC.getRoot() );
		assertEquals(HDOC.getRoot().toString(), docTmpClone.toString());
	}

	@Test
	public void testCreateTokens() throws Exception {
		HTMLParser2 p2 =  new HTMLParser2(); 
		List<NodeImpl> toksTmp = p2.createTokens(HTML.toCharArray());
		assertEquals(""+toksTmp.size(),""+44);
	}

	@Test
	public void testCreateDocumentCharArray() throws Exception {
		HTMLParser2 p2 =  new HTMLParser2();
		HTMLDocument docTmp = p2.createDocument(HTML.toCharArray());
		HTMLNode docTmpClone = HTMLParser2.clone(docTmp.getRoot() );
		assertEquals(docTmp.getRoot().toString(), docTmpClone.toString());
	}

	@Test
	public void testCreateDocumentCharsToken() throws Exception {
		HTMLParser2 p2 =  new HTMLParser2(); 
		CharsToken tokens = new CharsToken(HDOC);
		HTMLDocument docTmp = p2.createDocument(tokens);
		assertEquals(HDOC.getRoot().toString(), docTmp.getRoot().toString());
	}

	@Test
	public void testCreateDocumentListOfNodeImpl() throws Exception {
		HTMLParser2 p2 =  new HTMLParser2(); 
		List<NodeImpl> toksTmp = p2.createTokens(HTML.toCharArray());
		HTMLDocument docTmp = p2.createDocument(toksTmp);
		assertEquals(HDOC.getRoot().toString(), docTmp.getRoot().toString());
	}

	@Test
	public void testCreateDocumentString() throws Exception {
		HTMLParser2 p2 =  new HTMLParser2();  
		HTMLDocument docTmp = p2.createDocument(HTML);
		HTMLNode rootTmp = docTmp.getRoot(); 
		String  SwapServletUrl ="HtTp://wWw.NoWhErE.ky/!/";
		URL realURL = new URL( "HtTp://wWw.SoMeWhErE.net/!]{]}_+/");
		LServlet. testCreateFullLink(rootTmp, SwapServletUrl , realURL );
//    	testCreateImageLink(documentTmp.getRoot(), SwapServletUrl, realURL);
    	
		LServlet.testCreateMetaLink(rootTmp, SwapServletUrl, realURL);
    	
		LServlet.testCreateScriptLink(rootTmp, SwapServletUrl, realURL);	
		rootTmp.setBeautify(true); 
 
		
	}

	@Test
	public void testCreateDocumentByteArrayString() throws Exception {
		HTMLParser2 p2 =  new HTMLParser2();  
		HTMLDocument docTmp = p2.createDocument(HTML.getBytes(), null);
		assertEquals(HDOC.getRoot().toString(), docTmp.getRoot().toString());
	}

	@Test
	public void testCreateDocumentInputStreamString() throws Exception {
		HTMLParser2 p2 =  new HTMLParser2();  
		HTMLDocument docTmp = p2.createDocument(new ByteArrayInputStream(HTML.getBytes()), null);
		assertEquals(HDOC.getRoot().toString(), docTmp.getRoot().toString());
	}

	@Test
	public void testCreateDocumentFileString() throws Exception {
		HTMLParser2 p2 =  new HTMLParser2();  
		File fileTmp = File.createTempFile("JUNIT"+System.currentTimeMillis(), "html");
		FileOutputStream fout = new FileOutputStream(fileTmp);
		
		fout.write(HTML.getBytes());
		fout.flush();
		fout.close();
		HTMLDocument docTmp = p2.createDocument( fileTmp , null);
		assertEquals(HDOC.getRoot().toString(), docTmp.getRoot().toString());
		fileTmp .delete();
	}

}


 