package org.vietspider.html.parser;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.junit.Test;
import org.vietspider.html.HTMLDocument;
import org.vietspider.html.HTMLNode;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  24.08.2010::13:08:04<br> 
 */
public class HTMLParser2Test {

	private final static String HTML = "<html><head><title>this is title</title></head><body>test html</body></html>"; 
	private static HTMLDocument HDOC ;
	static{
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
		assertEquals("JUNIT4WORKAROUND"+toksTmp.size(),"JUNIT4WORKAROUND"+10);
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
		HTMLNode root = docTmp.getRoot();
		root.setBeautify(false);
		String textValue = root.getTextValue();
		assertEquals(HTML, textValue );
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


 