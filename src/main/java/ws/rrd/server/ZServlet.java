package ws.rrd.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream; 

import org.vietspider.html.HTMLDocument; 
import org.w3c.tidy.Tidy;

/** 
 * <b>produce xmlht output via JTidy (HTML->XML)</b>
 * 
 * 
 * @http://jtidy.sourceforge.net/howto.html
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  02.02.2011::17:41:08<br> 
 */
public class ZServlet extends LServlet {
	/**
	 * @author vipup
	 */
	private static final long serialVersionUID = 2011044130158675841L;

	public String getMYALIAS() {
		 return "/z/";
	}	
	
	/**
	 * @author vipup
	 * @param documentTmp
	 * @param contextEncStr
	 * @return
	 */
	public String renderDocument(HTMLDocument documentTmp, String contextEncStr) {
		Tidy tidy = new Tidy(); // obtain a new Tidy instance

        // Tell Tidy to convert HTML to XML
        tidy.setXmlOut(true);
        
        tidy.setQuiet(true);
        tidy.setShowWarnings(true);
		
//		tidy.setXHTML(boolean xhtml); // set desired config options using tidy setters 
//		...                           // (equivalent to command line options)
		String textValue; 
		textValue = documentTmp.getRoot().getTextValue(); 
		InputStream in = new ByteArrayInputStream( textValue.getBytes());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		tidy.parse(in, out); // run tidy, providing an input and output stream
		try {
			out.flush();
			textValue = out.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
		}
		return textValue;
	}
}


 