package ws.rrd.xpath;

import gnu.inet.encoding.Punycode;
import gnu.inet.encoding.PunycodeException;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Date; 
  

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException; 
import ws.rrd.csv.Action;
import ws.rrd.csv.SystemOutPrintlnAction; 

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  09.02.2011::11:41:21<br> 
 */
public class XPathContentHandler implements ContentHandler {
	private static final Logger log = LoggerFactory.getLogger(XPathContentHandler.class .getName());
	boolean finished = false;
	
	StringBuffer line = null;
	String token = null;
	String xpath = null;
	
	Action action = null;
	private String timestamp = SystemOutPrintlnAction.SDF.format( new Date()); 

	public XPathContentHandler(Action a){
		this.action = a;
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String token = new String(ch,start, length);
		synchronized (mutex) {
			String trim = token.trim();
			
			if (1==2){
							try {
								trim = Punycode.encode(trim);Punycode.decode(trim+"--");  
							} catch (PunycodeException e) {
								// TODO Auto-generated catch block
								// e.  printStackTrace();
							}
							//import org.apache.geronimo.mail.util.QuotedPrintable;
							//trim = new String(QuotedPrintable.encode( trim .getBytes()));
							trim = trim.replace("?", "/");
			}
			if (trim.startsWith("/")){
				trim = URLEncoder.encode(trim);
				xpath+="[body=\'"+trim.substring(0)+"\']";
			}else{
				line.append(token);
			}
		}
		log.trace("characters {}={}",xpath , token );
	}

	@Override
	public void endDocument() throws SAXException {
		finished = true;
	}
	
	private final Object mutex = this;
	private PrintWriter out;
	
	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		Object o = null;
		synchronized (mutex) {
			String data = (""+line).trim();
			// TODO '10,111' -> '10111'
			data =  data.replace(",", "");
			// TODO "10'111" -> "10111"
			data =  data.replace("'", "");
			// TODO "10 111" -> "10111"
			data =  data.replace(" ", "");
			
			// ignore empty super-nodes
			if (!"".equals( data) ){
				log.trace("[{}]",data);
				String timestamp = this.timestamp ;
				o =  this.action.perform(xpath, timestamp , data); 
				// 	clean data for filtering last-Node-value
				line = new StringBuffer();
			}else{
				log.trace("[{}]",data);
			}
			xpath = xpath.substring(0, xpath.lastIndexOf("/"));
		}
		if (out!=null){ 
			try{
				out.append( ""+ ("" + o).replace(", \\\\", ",\n \\\\"));
			}catch (Exception e) {
				// fully ignore erroes
			}
		}		
		log.trace("end {}{}", uri,name+"="+line);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		log.debug("endPrefixMapping {}:{}", prefix );
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException { 
			log.debug("ignorableWhitespace {},{}",  start,   length);
		 
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		log.trace("processingInstruction {},{}",  target,   data);
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		log.trace("setDocumentLocator {},{}",  locator );
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		log.trace("skippedEntity {},{}",  name );
	}

	@Override
	public void startDocument() throws SAXException {
		xpath = "";
		log.trace("start");
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		xpath += "/";
		xpath +=localName;
		synchronized (mutex) {
			line = new StringBuffer();
		}
		log.trace("startElement {},{}",  uri, name +":::"+localName);
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		log.trace("startPrefixMapping {},{}",  prefix,   uri);
	}

	public PrintWriter getOut() { 
			return out;
	}

	public void setOut(PrintWriter out) {
		this.out = out;
	}

}


 