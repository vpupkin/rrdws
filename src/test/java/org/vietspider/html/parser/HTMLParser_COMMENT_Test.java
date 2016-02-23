package org.vietspider.html.parser;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Test;
import org.vietspider.html.HTMLDocument;
import org.vietspider.html.HTMLNode;
 
public class HTMLParser_COMMENT_Test  {
	private static final String TEST_HTML = "org/vietspider/html/parser/COMMENT.html";
	private static String HTML  =""; 
	private static HTMLDocument HDOC ;
	static{
		InputStream inRes = HTMLParser_COMMENT_Test.class.getClassLoader().getResourceAsStream(TEST_HTML);
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
		assertEquals(""+toksTmp.size(),""+25);
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
		HTMLDocument docTmp = p2.createDocument(HTML); //.replace("<noscript>", "<!-- 1<2n3o4s5c6r7i8p9t0>0 -->").replace("</noscript>", "<!-- 01<2n3o4s5c6r7i8p9t0>0 -->")
		docTmp.getRoot().setBeautify(true);
		String textValue = docTmp.getDoctype() + docTmp.getRoot().getTextValue();
		//System.out.println(textValue);
		
		
		String[]  expected = HTML.toLowerCase().replace( "\n","").replace( " ","").replace( "\t","").replace("><", ">\n<").split("\n");
		String[] actual = textValue.toLowerCase().replace( "\n","").replace( " ","").replace( "\t","").replace("><", ">\n<").split("\n")  ;
		
		Diff diff  = new Diff();//System.out.println(textValue);
		String[] diffTmp = diff  .diff(expected,actual);//actual
		assertEquals(diffTmp.length, 4); // TODO 5 -> 0
	 
		
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


 