package ws.rrd.server;   
import java.io.IOException;  
import javax.servlet.ServletOutputStream; 
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 
  

import cc.co.llabor.cache.js.Item;
import cc.co.llabor.cache.js.JSStore; 
import cc.co.llabor.cache.replace.ReplaceStore;

public class SServlet extends HttpServlet{ /* SCRIPT-mastering servlet*/
	private static final long serialVersionUID = -5308225516841490806L;
	private static final Logger log = LoggerFactory.getLogger(LServlet.class .getName()); 
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		ServletOutputStream out = resp.getOutputStream();
		String uriTmp =   req.getRequestURL().toString() ;
		String baseURL =  System .getProperty("S.baseURL");
		String aliasURL =  System .getProperty("S.aliasURL");
		try{
			uriTmp = uriTmp.replace(aliasURL, baseURL);
		}catch(Throwable e){
			// e.  printStackTrace();
		}
		System.out.println("sendback "+uriTmp+" ...");
		
		resp.setContentType("text/javascript");
		String scriptValue = "";

		// assumes already cached
		final JSStore instanse = JSStore.getInstanse();//http://ejohn.org/blog/bringing-the-browser-to-the-server/
		Item scriptTmp = instanse.getByURL(uriTmp); // http://www.snible.org/java2/uni2java.html
		try{
			scriptValue = scriptTmp.getValue() ;//http://realcode.ru/regexptester/ 
			ReplaceStore replacerTmp = ReplaceStore.getInstanse();//replacerTmp.putOrCreate(cacheKey, value)
			String refererTmp = null;
			try{
				refererTmp  = ""+req.getHeaders("referer").nextElement();
				// DECODE BASE64 -> plain URL
				refererTmp   = ""+ refererTmp   ;
			}catch(Throwable e){
				// e.  printStackTrace();
			}
			String newValue = replacerTmp.replaceByRules(uriTmp,scriptValue, refererTmp);
			
			out.write(newValue.getBytes()); 
			out.flush();
			final String statTmp = "/* "+scriptTmp.isReadOnly() +"" +
			":" + scriptTmp .getAccessCount()+
			":" + scriptTmp .getChangeCount()+
			":" + scriptTmp .getChanged() +
			":" + scriptTmp .getCreated()+
			":" + scriptTmp .getRefs()+
			":" + scriptTmp .getRefs().size()+
			" */";
	out.write( statTmp.getBytes());
	out.flush();
			String refTmp = ""+req.getHeaders("referer").nextElement();
			instanse.putOrCreate(uriTmp, scriptValue, refTmp   );
		}catch(NullPointerException e){
			// ignore NO_REFFERs - org.apache.tomcat.util.http.ValuesEnumerator.nextElement(MimeHeaders.java:443)
			log .trace("uriTmp -|{}", e);
		}catch(Exception e){
			System.out.println("NOSCRIPT in the store! URL=["+uriTmp+"]");
			// e.  printStackTrace();
		}
	}

	 
	
 
}


 