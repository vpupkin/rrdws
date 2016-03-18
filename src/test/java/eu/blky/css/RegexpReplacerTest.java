package eu.blky.css;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;  
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.vietspider.html.parser.Diff;

import com.yahoo.platform.yui.compressor.CssCompressor;
 

public class RegexpReplacerTest {
  

  
	@Test
	public void testBaseURLReplacer() throws Exception {
		BaseURLRegexpReplacer repl = new BaseURLRegexpReplacer("  url  ( \'%1$2s\' )", "http://www.g00g1e.net:12312/some/w/h/e/r/e/");
		String cssTmp = readAll(this.getClass().getPackage().getName().replace(".", "/")+"/css_with_urls.css");
		String retval = cssTmp ;
	    retval = repl.replaceAll(BaseURLRegexpReplacer.URL_PATTERN,  retval );
	    //System.out.println(retval);
	    
	    Diff diff = new Diff();
	    String [] o = diff .diff(cssTmp.split("\\n"), retval.split("\\n"));
	    
		for (String s:o){
	    	System.err.println(s);
	    }
	    
		Assert.assertEquals( o.length , 108);
		Assert.assertEquals( cssTmp.toLowerCase().split("url").length , 59);
		Assert.assertEquals( retval.toLowerCase().split("url").length , 59);
	}
	 
		

	@Test
	public void testYUI() throws IOException, ReplacerException {
		String inBuf = readAll(this.getClass().getPackage().getName().replace(".", "/")+"/css_with_urls.css");
	  
		Reader in = new StringReader(inBuf);
		CssCompressor yui = new CssCompressor(in );
		ByteArrayOutputStream outTmp = new ByteArrayOutputStream();
		int linebreakpos = 3;
		Writer out = new PrintWriter(outTmp);
		yui.compress(out  , linebreakpos );
		out.flush();
		out.close();
		// System.out.println(outTmp.toString());
		
		// same as BEFORE----
		BaseURLRegexpReplacer repl = new BaseURLRegexpReplacer("  url  ( \'%1$2s\' )", "http://www.g00g1e.net:12312/some/w/h/e/r/e/");
		String cssTmp = outTmp.toString();//readAll(this.getClass().getPackage().getName().replace(".", "/")+"/css_with_urls.css");
		String retval = cssTmp ;
	    retval = repl.replaceAll(BaseURLRegexpReplacer.URL_PATTERN,  retval );
	    //System.out.println(retval);
	    
	    Diff diff = new Diff();
	    String [] o = diff .diff(cssTmp.split("\\n"), retval.split("\\n"));
	    
		for (String s:o){
	    	System.err.println(s);
	    }
	    
		Assert.assertEquals( 48, o.length );
		Assert.assertEquals( 58, cssTmp.toLowerCase().split("url").length  );
		Assert.assertEquals( 58, retval.toLowerCase().split("url").length );
	}	
	
	private String readAll(String namePar) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(namePar);
		byte[] b = new byte[1024];
		for (int i = in.read(b); i > 0; i = in.read(b)) {
			out.write(b, 0, i);
		}
		in.close();
		out.close();
		return out.toString();
	}
	  
  

}
