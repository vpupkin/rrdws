package ws.rrd.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream; 
import java.io.PrintWriter; 
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.HttpURLConnection; 
import java.net.URLEncoder;
import java.nio.charset.Charset; 
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List; 
import java.util.Map;  
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletOutputStream; 
import javax.servlet.http.*;

import net.sf.jsr107cache.Cache;

import org.apache.http.Header;
import org.apache.http.HttpEntity; 
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vietspider.html.HTMLDocument;
import org.vietspider.html.HTMLNode;
import org.vietspider.html.parser.HTMLParser2;
import org.vietspider.html.util.HyperLinkUtil;
import org.vietspider.token.attribute.Attribute;

import cc.co.llabor.cache.css.CSStore;
import cc.co.llabor.cache.js.Item;
import cc.co.llabor.cache.js.JSStore;
import cc.co.llabor.script.Beauty;
import cc.co.llabor.system.ExitTrappedException;
import cc.co.llabor.cache.Manager;
import cc.co.llabor.cache.MemoryFileItem;
import cc.co.llabor.cache.MemoryFileItemFactory;
import eu.blky.css.BaseURLRegexpReplacer;
import eu.blky.css.ReplacerException;
import ws.rdd.net.UrlFetchTest;  

@SuppressWarnings("serial")
public class LServlet extends HttpServlet {

	public static final String _U_R_L_ = "_u_r_l_";
	
	public static final String SYSTEM_EXIT_RESETED = ExitTrappedException.isReseted()? "YES":"no";
	
	private static final Logger log = LoggerFactory.getLogger(LServlet.class .getName());
	
	public static void System_out_print(String txt){
		log.trace(txt);
	}
	public static void System_out_println(Object txt){
		log.trace( ""+ txt);
	}
	public static void System_out_println(String txt){
		log.trace(txt);
	}

	private static final String CHARSET_PREFIX = "charset=";

	// TODO: Create a config class to dynamic load settings from system.
	// propertiesin appengine-web.xml.
	//	public static String SwapServletUrl = "http://localhost:8080/swap/";		// dev.
	public static String SwapServletUrl = "local".equals(System
			.getProperty("myenviroment"))
			? "http://localhost:8888/l/"
			: "https://rrdsaas.appspot.com/l/"; // prod

	public static int SwaperConnTimeoutMS = 30000;
	public static int SwaperReadTimeoutMS = 30000;

	public static short FWSwaperAppVersion = 1;



	public static final String BEAUTIFY = "BEAUTIFY";
	public static final String REPLACER = "REPLACER";
	public static final String TRACER = "TRACER";
	public static final String BLACKLISTER = "BLACKLISTER";
	

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		this.doPost(req, resp);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		this.doGetPost(req, resp);
	}

	  private static HyperLinkUtil handler  = new HyperLinkUtil();

	public static boolean TRACE = true;
 

	  public static void testCreateFullLink(HTMLNode node, String swapServletUrl2, URL home){
	    handler.createFullNormalLink(node,  swapServletUrl2,  home);
	    List<String> list  = handler.scanSiteLink(node);
	    if (TRACE)
	    	for(String ele : list)
	    		System_out_println(ele);
	  }
		  
	  public static void testCreateNoScriptLink(HTMLNode node, String swapServletUrl2, URL home){
		    handler.createNoScriptLink( node,  swapServletUrl2,  home);
		    if (TRACE){
		    	//List<String> list  = handler.scanScriptLink(node );
		    	//for(String ele : list)
		    	//	System_out_println(ele);
		    }
		  }		  
	  
	  public static void testCreateScriptLink(HTMLNode node, String swapServletUrl2, URL home){
	    handler.createScriptLink( node,  swapServletUrl2,  home);
	    if (TRACE){
	    	//List<String> list  = handler.scanScriptLink(node );
	    	//for(String ele : list)
	    	//	System_out_println(ele);
	    }
	  }	  
	  public static void testCreateStyleLink(HTMLNode node, String swapServletUrl2, URL home){
	    handler.createStyleLink( node,  swapServletUrl2,  home);
	    if (TRACE){
	    	//List<String> list  = handler.scanScriptLink(node );
	    	//for(String ele : list)
	    	//	System_out_println(ele);
	    }
	  }
		  
	  public static void testCreateMetaLink(HTMLNode node, String swapServletUrl2, URL home){
	    handler.createMetaLink(node,  swapServletUrl2,  home);
	    if (TRACE){
		    List<String> list  = handler.scanSiteLink(node);
		    for(String ele : list)
		    	if (TRACE) System_out_println(ele);
	    }
	  }	  
 
	
	public void doGetPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		StringBuilder targetUrl = null;
 
		ServletOutputStream outTmp = null;
		String contextTypeStr = null ;
		byte[] dataBuf =null;
		HTMLDocument documentTmp = null;
		String urlStr = null;
		try {
			StringBuffer requestURL = req.getRequestURL();
			String rurlTmp = ""+req.getRequestURL()+""; 
			final String baseURL =  System .getProperty("l.baseURL");
			String decodedUrl = rurlTmp;
			if (baseURL == null){  
				SwapServletUrl  = rurlTmp.substring(0, rurlTmp.indexOf(req.getServletPath()+"/") )+req.getServletPath()+"/";
				decodedUrl = requestURL.substring( SwapServletUrl.length());
			}else{// redefine server/host-based intra.PEGA001.fidu.com -> GLOBaserv.com
				SwapServletUrl  = baseURL;
				decodedUrl = requestURL.substring( requestURL.lastIndexOf(getMYALIAS()) + getMYALIAS().length() );
				
			}
			
			 
			String []decodedUrls ;
			int ind=0;
			try{
				if (TRACE){System_out_println("DECODE :"+decodedUrl);}
				if (decodedUrl.indexOf("/")>0 && HyperLinkUtil.decode(decodedUrl).length()==0){
					String []urls = decodedUrl.split("/");
					decodedUrls = new String[urls.length];
					for (String urlTmp:urls){
						urlStr = HyperLinkUtil.decode(urlTmp);
						decodedUrls[ind++]=urlStr;
					}
				}else{
					urlStr = HyperLinkUtil.decode(decodedUrl);
					
				}
			}catch(Throwable e){
				if (TRACE){e.printStackTrace();}
				outTmp = resp.getOutputStream();
				PrintWriter pw = new PrintWriter(outTmp, true);
				pw.println( requestURL);
				pw.println( contextTypeStr);
				pw.println( SwapServletUrl);
				pw.println( decodedUrl);
								
				e.printStackTrace(pw);
			}
			// fix ROOT-Panel-Request
			if (isRootReq(req) ){ 
				urlStr = req.getParameter(_U_R_L_) ; 
			}
			

			// normalize non-protocol-ADDRESS
			urlStr = (""+urlStr ).startsWith("http")? urlStr:"http://"+urlStr;		
			checkBlack(decodedUrl, req);
			checkBlack(urlStr, req);
			
			if (TRACE) System_out_println(_U_R_L_ + " := "+ urlStr);
			targetUrl = new StringBuilder(urlStr);

			if ((targetUrl.length() > 0) && (req.getQueryString() != null)
					&& (req.getQueryString().length() > 1)) {
				if (targetUrl.toString().endsWith("?"))
					targetUrl.append(String.format("%s", req.getQueryString()));
				else
					targetUrl.append(String.format("?%s", req.getQueryString()));
				
				urlStr = targetUrl.toString();
			}
			
			String[][] headsToResend = calcRequestHeaders(req);
			if (!urlStr.startsWith("http")) {
				// fix via REFeRER
				try{
				 URL refURL = new URL ( HyperLinkUtil.decode( req.getHeader("Referer").substring(SwapServletUrl.length()) ) );
				 urlStr = refURL.getProtocol() + "://"+refURL.getHost() + "/"+(urlStr.startsWith("/")?"":refURL.getPath()+"/../")+urlStr;
				}catch(Throwable e){}
			}
			urlStr  = urlStr.replace(" ", "%20").replace("\t", "%090");
			// http://it-ru.de/forum/viewtopic.php?t=182374&amp;postdays=0&amp;postorder=asc&amp;start=15
			urlStr  = urlStr.replace("&amp;", "&");
			HttpSession sessionTmp = req.getSession();
			
			UrlFetchTest urlFetcherTmp = (UrlFetchTest) sessionTmp.getAttribute("UrlFetcher");
			if (urlFetcherTmp == null){
				urlFetcherTmp = new UrlFetchTest(sessionTmp);
				sessionTmp.setAttribute("UrlFetcher",urlFetcherTmp);
			}
			
			
			HttpResponse xRespTmp = null ;
			Cache getCache = getCache();

			if ("POST".equals( req.getMethod() ) && ! isRootReq(req) ){
				List<MemoryFileItem> items  = null;
			    if(org.apache.commons.fileupload.servlet.ServletFileUpload.isMultipartContent(req)){
		            
		            MemoryFileItemFactory factory = MemoryFileItemFactory.getInstance();
		            org.apache.commons.fileupload.servlet.ServletFileUpload upload = new org.apache.commons.fileupload.servlet.ServletFileUpload(factory);
		            upload.setSizeMax(4*1024*1024); // 4 MB
		  
		            // Parse the request
		            items = upload.parseRequest(req);
			    }				
				Map parameterMap = req.getParameterMap();
				xRespTmp = urlFetcherTmp.fetchPostResp(urlStr, headsToResend,	parameterMap, items);
				// HOTFIX for Login-redirect
				if (xRespTmp.getLastHeader("X-MOVED")!=null){
					urlStr = ""+xRespTmp.getLastHeader("X-MOVED").getValue();
				}
			}				
			else{ // GET
				urlStr = checkGOTO(urlStr);				
				try{					
					String keyTmp = calcCackeKey(urlStr);
					Object dataTmp = null;
					try{
						dataTmp = getCache .get(keyTmp );
					}catch(Exception e){e.printStackTrace();/* ignore any gae-limits, if any */}
					
					if (dataTmp == null){
						xRespTmp = urlFetcherTmp.fetchGetResp(urlStr, headsToResend);
						
					}else if (dataTmp instanceof LCacheEntry  && ((LCacheEntry) dataTmp).getExpired() > System.currentTimeMillis()){
						// write cached !
						LCacheEntry theItem = (LCacheEntry) dataTmp;	
						resp.setContentType(theItem.getCxType());
						outTmp = resp.getOutputStream();
						outTmp.write(theItem.getBytes()); 
						return;	
					}else{
						try{
						 getCache .remove(urlStr);
						 dataTmp =null;
						 xRespTmp = urlFetcherTmp.fetchGetResp(urlStr, headsToResend);
						}catch(Exception e){e.printStackTrace();/* ignore any gae-limits, if any */} 
					}
				}catch(ClientProtocolException e){
					log.error( "URL{"+urlStr+"}::",  e);
					// last try 
					xRespTmp =  urlFetcherTmp.fetchGetResp(urlStr+"/", headsToResend);
				}
			}
			final StatusLine statusLine = xRespTmp.getStatusLine();
			try{
				String wwwAuthTmp = xRespTmp.getHeaders("WWW-Authenticate")[0].getValue();
				wwwAuthTmp = wwwAuthTmp.lastIndexOf("\"")==wwwAuthTmp.length()-1?wwwAuthTmp.substring(0, wwwAuthTmp.length()-1)+"@"+targetUrl+"\"":wwwAuthTmp;
				if (statusLine.getStatusCode() == 401){
					resp.setStatus(401);
					resp.setHeader( "WWW-Authenticate", wwwAuthTmp );
					return;
				}else if (statusLine.getStatusCode() == 301){
					resp.setStatus(301);
					resp.setHeader( "WWW-Authenticate", wwwAuthTmp);
					return;
				}else if (statusLine.getStatusCode() == 302){
					resp.setStatus(302);//xRespTmp.getAllHeaders()
					resp.setHeader(  "Location", requestURL.toString()  );
					return;
				}else if (statusLine.getStatusCode() == 303){
					resp.setStatus(303);
					resp.setHeader( "WWW-Authenticate", wwwAuthTmp);
					return;
				}else if (statusLine.getStatusCode() == 304){
					resp.setStatus(304);
					resp.setHeader( "WWW-Authenticate", wwwAuthTmp);
					return;
				}else if (statusLine.getStatusCode() == 305){
					resp.setStatus(305);
					resp.setHeader( "WWW-Authenticate", wwwAuthTmp);
					return;
				}
			}catch(Exception e){
				if (TRACE)log.trace("WWW-Authenticate",e);
			}
			HttpEntity entity = xRespTmp.getEntity();
			contextTypeStr = ""+entity.getContentType();
			String contextEncStr =  ""+entity.getContentEncoding() ;
			contextEncStr = "null".equals(contextEncStr)?getXEnc(xRespTmp):contextEncStr;
			if ("null" .equals( ""+contextEncStr ) &&  contextTypeStr.toLowerCase().startsWith("content-type: text/html")){
				int encPos = contextTypeStr.toLowerCase().indexOf(CHARSET_PREFIX);
				if (encPos>0){
					
					contextEncStr = contextTypeStr.substring(encPos+CHARSET_PREFIX.length());
					contextEncStr = contextEncStr .toUpperCase();
					if (TRACE) log.warn(contextEncStr + "  }} ENC :=  {  "+contextTypeStr+" } ::: enc ::: "+contextEncStr +"["+urlStr+"]");

				}else{
					
					Header[] contextEncHeaders = xRespTmp.getHeaders("Content-Encoding");
					try{
						contextEncStr =  contextEncHeaders[0].getValue();
					}catch(Throwable e){}
					if (TRACE) log.warn("Content-Encoding[0]::== {"+ contextEncStr + "  }" );
					 
				}
			}else{
				if (TRACE) log.warn("nonull::::"+ contextEncStr + " ::::: "+ contextTypeStr );
			}
			
			if ( isCSS(contextTypeStr)  )
			{
				setupResponseProperty( resp,  xRespTmp);
				outTmp = performCSS(resp, contextTypeStr, urlStr, xRespTmp, entity, contextEncStr);
				return;
			}else if ( isBinary(contextTypeStr) ){
				setupResponseProperty( resp,  xRespTmp);
				outTmp = performBinary(resp, contextTypeStr, urlStr, xRespTmp, entity, contextEncStr);
				return;
			}else if (  isScript(contextTypeStr) ){
				setupResponseProperty( resp,  xRespTmp);
				outTmp = performScript(resp, contextTypeStr, urlStr, entity, contextEncStr);
				return;
				
			}	else{
			 
				String xEncTmp = getXEnc(xRespTmp); 
				if (TRACE) log.warn("x---HTML--- x  contextTypeStr/contextEncStr:"+contextTypeStr+" : :  enc : : "+contextEncStr +"["+urlStr+"]   XXX::"+xEncTmp);
				if (TRACE) log.warn("=====!!!======"+contextTypeStr +"::::"+contextEncStr);
			}
			 
			ByteArrayOutputStream oaos = new ByteArrayOutputStream();
			entity.writeTo(oaos) ;
			oaos.flush();
			oaos = unZIP(xRespTmp, contextEncStr, oaos);
			contextEncStr = calcContextEnc(req, xRespTmp, oaos);
			String data = null;
			try{
				data = oaos.toString(contextEncStr);//xCSS.toUpperCase().substring( 12430)
			}catch(Exception e){
				data = oaos.toString();oaos.toString("utf-8");
			} //data.substring( data.indexOf("&lt;") -100, data.indexOf("&lt;") +20);
			 
			if ("null".equals(  ""+contextEncStr ) &&  data.toLowerCase().indexOf("content=\"text/html")>0)try{
				String contextText = "charset=";
				int lenTmp = contextText.length();
				int posTmp = data.toLowerCase().indexOf(contextText);
				int beginIndex = posTmp +lenTmp;
				int endIndex = beginIndex + data.toLowerCase().substring(beginIndex).indexOf("\"");
				contextEncStr = data.toLowerCase().substring(beginIndex,endIndex );
				contextEncStr = contextEncStr.toUpperCase();
				data = oaos.toString(contextEncStr);
			}catch(Throwable e){}
			try{
				dataBuf = data.trim().getBytes(contextEncStr);// "utf-8"
 			} catch(Exception e) {
 				contextEncStr = null;
 				dataBuf = data.trim().getBytes();// "ISO-8859-1"
			}
			HTMLParser2 parser2 = new HTMLParser2();
			try{
				documentTmp = parser2.createDocument(dataBuf, contextEncStr );// "utf-8"
			}catch(Exception e){
				if (TRACE) log.warn("createDocument EXCEPTION!" +e.getMessage()+" contextTypeStr||contextEncStr:["+contextTypeStr+"||"+contextEncStr+"]  URL =:["+urlStr+"]");
				documentTmp = parser2.createDocument(dataBuf, null );// "utf-8"
			}
			
	    	URL realURL = new URL(urlStr); // new String( oaos.toString(contextEncStr).getBytes(), contextEncStr)
	    	 
	    	HTMLNode rootTmp = documentTmp.getRoot();
	    	if ("true".equals(  sessionTmp.getAttribute(BEAUTIFY) )){
	    		rootTmp.setBeautify(true);
	    	}else{
	    		rootTmp.setBeautify(false);
	    	}
	    	
			testCreateFullLink(rootTmp, SwapServletUrl, realURL);
//	    	testCreateImageLink(documentTmp.getRoot(), SwapServletUrl, realURL);	    	
	    	testCreateMetaLink(rootTmp, SwapServletUrl, realURL);	    	
	    	testCreateScriptLink(rootTmp, SwapServletUrl, realURL);	    	
	    	testCreateStyleLink(rootTmp, SwapServletUrl, realURL);	    	
	    	
	    	int beginIndex = contextTypeStr.toUpperCase().indexOf(" ")+1;

	    	setupResponseProperty( resp,  xRespTmp);
	    	String cxType = contextTypeStr.substring(beginIndex);
			resp.setContentType(cxType);
	    	if (!"null".equals(""+contextEncStr)){
	    		resp.setCharacterEncoding(contextEncStr);
	    	}	    	
	    	outTmp = resp.getOutputStream();	    	
	    	String textValue = null;
	    	// wrap
	    	try{ 
	    		//documentTmp.getRoot().getChild(0).getByXPath("BODY");
	    		HTMLNode headTmp = null;
	    		HTMLNode baseTmp = null;
	    		for (HTMLNode nodeTmp : documentTmp.getRoot().getChild(0).getChildren()){
	    			String nodeNameTmp =nodeTmp .getName().name();
	    			if ("HEAD".equals(nodeNameTmp)){
	    				headTmp = nodeTmp;
	    			}
	    			if ("BASE".equals(nodeNameTmp)){
	    				baseTmp  = nodeTmp;
	    				break;
	    			}
	    		}
	    		if (baseTmp != null){//getTextValue()
	    			System.out.println(baseTmp.getTextValue());
	    			String avalTmp = baseTmp.getAttributes().get("href").getValue();
	    			baseTmp.getAttributes().remove("href");
	    			Attribute hrefTmp = new Attribute("href", ""+HyperLinkUtil.encode(SwapServletUrl, avalTmp));
					baseTmp.getAttributes().add(hrefTmp );
	    			System.out.println(baseTmp.getTextValue());
	    			
	    		}else{
	    			System.out.println(headTmp);
	    		}
	    		HTMLNode bodyTmp = documentTmp.getRoot().getChild(0); // HEAD-modi!
				HTMLDocument htmlTmp = buildToolbar(urlStr, parser2);
				HTMLNode myIFrame = htmlTmp.getRoot().getChild(1).getChild(0); 
				bodyTmp.addChild(0,myIFrame);
	    	}catch(Exception e){
	    		if (TRACE) log.trace("wrap",e);
	    		include(resp, "L.jspX");
	    	} 
	    	textValue = renderDocument(documentTmp, contextEncStr);  
	    	byte[] bytesTmp = null;
	    	if (!"null".equals(""+contextEncStr)){
	    		bytesTmp = textValue.getBytes(contextEncStr);
				
	    	}else{
	    		bytesTmp = textValue.getBytes(); 
	    	} 
	    	outTmp.write(bytesTmp); 
	    	// and cache it! //	    	String cxType = contextTypeStr.substring(beginIndex);
	    	cacheIt(urlStr, getCache, bytesTmp,cxType );
	    	
	    	
		}catch(BlackListedException e){
			goToGoooo(resp);
			
		} catch (java.lang.NoClassDefFoundError e) {
			if (TRACE) System_out_println(contextTypeStr +" ===============  "+e.getMessage());e.printStackTrace();
			if (TRACE) System_out_println(documentTmp);
		} catch (Exception e) {
			processException(req, resp, targetUrl, e);
			
		}  
	}
	public void processException(HttpServletRequest req,
			HttpServletResponse resp, StringBuilder targetUrl, Exception e)
			throws IOException {
		ServletOutputStream outTmp;
		if (!"".equals(""+targetUrl  ) && targetUrl != null){
			
			//TODO FIXME!!!! http://www.google.com/bot.html
			//TODO FIXME!!!! http://www.google.com/bot.html
			//TODO FIXME!!!! http://www.google.com/bot.html
			String agentTmp = req.getHeader("user-agent");
			if (agentTmp !=null )
			if (
					agentTmp .indexOf("google")>=0 ||
					agentTmp .indexOf("bot")>=0 ||
					agentTmp .toUpperCase().indexOf("GOOGLE")>=0 ||
					agentTmp .toUpperCase().indexOf("SAFARI")>=0 ||
					agentTmp .indexOf("google")>0  
						){
				goToGoooo(resp);
				return;
			}
			try{
				checkBlack(""+targetUrl, req);
			}catch(BlackListedException e111){
				goToGoooo(resp);
				return;
			}
			
			ExceptionUtils.swapFailedException(targetUrl.toString(), resp,
					e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			outTmp = resp.getOutputStream();
			e.printStackTrace(new PrintWriter(outTmp, true)); 
		}
		else {
			outTmp = include(resp, "index.html");
			outTmp.write("<pre>".getBytes());
			outTmp.write((""+e.getMessage()+"\n\n\n\n"+e.getStackTrace()).getBytes());
			e.printStackTrace(new PrintWriter(outTmp, true));
		}
	}
	public void goToGoooo(HttpServletResponse resp) throws IOException {
		// http://www.pardontheinformation.com/2010/09/java-servlet-jsp-301-and-302-redirect.html
		resp.setStatus(302 );
		
		
		String urlTo = "http://www.anti-scam.org/scamers-pics/ZZ/scamers_XXXX_Y.jpg";
//			http://www.anti-scam.org/scamers-pics/scamers_4137_1.jpg
		long picId = (1000 + System.nanoTime()%3000);
		urlTo = urlTo.replace("XXXX", ""+picId);
		urlTo = urlTo.replace("Y",  ""+ (1+System.currentTimeMillis()%3) );
		urlTo = urlTo.replace("ZZ",  (""+  picId).substring(0,2	) );
		resp.setHeader( "Location",  urlTo);
		resp.setHeader( "Connection", "close" );
		resp.sendRedirect(urlTo);
	}
	private void checkBlack(String decodedUrl, HttpServletRequest req) throws BlackListedException {
    	if ("true".equals(  req.getSession().getAttribute(LServlet.BLACKLISTER) )){
    		// nothing todo - perform as usual
    	}else{
    		// skip check
    		return;
    	}

		Cache blTmp = Manager.getCache("BlackList"); 
		for (String key:(""+decodedUrl).split("/\\&?")){
			String val = (String) blTmp .get(key);
			if (val!=null )throw new BlackListedException(decodedUrl);
			if ( "12345-proxy.appspot.com/bitbucket.org/twitter.com/".indexOf(key) >0 )throw new BlackListedException(decodedUrl);
		}
	}
	public static Cache getCache() {
		Cache getCache = Manager.getCache("getCache@"+LServlet.class.getName());
		return getCache;
	}
	private static ByteArrayOutputStream unZIP(HttpResponse xRespTmp,
			String contextEncStr, ByteArrayOutputStream oaos)
			throws IOException {
		if ("gzip".equals(contextEncStr)  || isGZip(xRespTmp) ){
			oaos = deZip(oaos);
		}
		return oaos;
	}
	public String calcContextEnc(HttpServletRequest req, HttpResponse xRespTmp,
			ByteArrayOutputStream oaos) {
		String contextEncStr=null;
		try {
			 contextEncStr = ""+xRespTmp.getHeaders("Content-Type")[0];
			 String cxTitle = "charset=";
			 int beginIndex = contextEncStr.indexOf(cxTitle);
			 if (beginIndex==-1){
				 // last chance - seach for charset in the page
				 String htmlTmp = oaos.toString();
				contextEncStr = htmlTmp.substring(htmlTmp.indexOf(cxTitle)+cxTitle.length());
				contextEncStr = contextEncStr.substring(0,contextEncStr.indexOf("\""));
			 }else{
				 contextEncStr = contextEncStr .substring(beginIndex+cxTitle.length());
			 }
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (contextEncStr ==null)
		try {
			int beginIndex = 0;
			String charSetHeader = req.getHeader("Accept-Charset ");
			charSetHeader = charSetHeader == null ? req
					.getHeader("Accept-Charset") : charSetHeader;
			int endIndex = charSetHeader.indexOf(",");
			contextEncStr = charSetHeader.substring(beginIndex,
					endIndex);// "ISO-8859-1";
		} catch (NullPointerException e) {
		}
		return contextEncStr;
	}
	public static void cacheIt(String urlStr, Cache getCache, byte[] bytesTmp, String cxType) {
		String key = calcCackeKey(urlStr);
		try{
			LCacheEntry newData = new LCacheEntry(key, bytesTmp, cxType);
			getCache.put(key, newData);
		}catch(Throwable e){e.printStackTrace();}
	}
	public static String calcCackeKey(String urlStr) {
		String key = urlStr;
		key =    key.lastIndexOf( "/") -key.indexOf("://") <5 ?key+"/.!":key;
		key =    key.lastIndexOf( "/") == key.length() -1 ?key+".!":key;
		return key;
	}
	/**
	 * @author vipup
	 * @param urlStr
	 * @param parser2
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws Exception
	 */
	private HTMLDocument buildToolbar(String urlStr, HTMLParser2 parser2)
			throws IOException, URISyntaxException, Exception {
		// CACHING is disabled - TODO check possibility to modify only one ATTRIBUTE from object.
		// at the moment much faster(for CPU as well as implementation) goes to build new commonToolbar as modify Memory-cached-Obj.
		if (1==2 && commonToolbar!=null){
			HTMLNode urlTextTmp = commonToolbar.getRoot().getById("I01lOO10lOO11I");
			urlTextTmp.getAttributes().get("value").setValue(urlStr );
			urlTextTmp.setValue(urlTextTmp.getTextValue().toCharArray());
			return commonToolbar;
		}
		String strTmp = "<body><div  name=toolbar>"+ new String(getResourceAsBA("L.jspX") ) +"</div></body>";
		strTmp = strTmp.replace( "B8b8B8Bbbb888B", calcBase() ); 			
		// addressBAR
		String toURL = "l1lll1l1ll1l1lll1l1lll1l1ll1ll11lll111111l1l11ll1l1l1l1l1l11l1";
		String encodedURL = url2html( urlStr );
		strTmp = strTmp.replace( toURL,  encodedURL  ); 			
		
		HTMLDocument htmlTmp = parser2.createDocument(strTmp);
		commonToolbar = htmlTmp;
		return htmlTmp;
	}
	
	private static HTMLDocument commonToolbar = null;

	private String url2html(String urlStr) throws URISyntaxException {
		URI uri = new URI(urlStr );
		return uri.toString();
	}

	// any servlet have web.xml alias @see web.xml
	public String getMYALIAS() {
		 return "/l/";
	}

	/**
	 * @author vipup
	 * @param documentTmp
	 * @param contextEncStr
	 * @return
	 */
	public String renderDocument(HTMLDocument documentTmp, String contextEncStr) {
		String textValue;
		if ("KOI8-R".equals(contextEncStr)) {
			textValue = documentTmp.getTextValue(); 
		}else{
			HTMLNode doctype = documentTmp.getDoctype();
			String sDoctype = (doctype==null?"":doctype.getTextValue());
			textValue = sDoctype  + documentTmp.getRoot().getTextValue();
			 
		}
		return textValue;
	}

	/**
	 * @author vipup
	 * @param urlPar
	 * @return
	 * @throws MalformedURLException
	 */
	private static final String checkGOTO(String urlPar)
			throws MalformedURLException {
		// f.ex.
		// https://www.ccc.de/Wxby7/Lswdn.ipx?goto=../Mdfsus/Rsdfrts.usbx
		String GOTO = "?goto=";
		int gotoPos = urlPar.indexOf(GOTO);
		if (gotoPos > 0) {
			String prefixTmp = urlPar.substring(0, gotoPos);
			URL urlTmp = new URL(prefixTmp);
			String suffTmp = urlPar.substring(gotoPos + GOTO.length());
			urlPar = HyperLinkUtil.prepareLinkValue(urlTmp, suffTmp);
		}
		return urlPar;
	}

	static ServletOutputStream performCSS(HttpServletResponse resp,
			String contextTypeStr, String urlStr, HttpResponse xRespTmp,
			HttpEntity entity, String contextEncPar) throws IOException {
		ServletOutputStream outTmp;
		
		
		
		if (TRACE) log.warn("CSS contextTypeStr / contextEncStr:{"+contextTypeStr+" / "+contextEncPar +"}, url== ["+urlStr+"]");
		CSStore store = CSStore.getInstanse();
		cc.co.llabor.cache.css.Item itemTmp =  store.getByURL(urlStr);
		String xCSS = null;
		if (itemTmp == null){
			
			ByteArrayOutputStream oaos = new ByteArrayOutputStream();
			entity.writeTo(oaos) ;
			if (isGZip(xRespTmp)){
				oaos = deZip(oaos);
			    //contextEncStr  = "ISO-8859-1";
			}				
			xCSS = oaos.toString();
			xCSS = justifyCSS(urlStr, xCSS ); 
			
			String refPar = urlStr;
			if (1==2)
			try{
				xRespTmp.getHeaders("Refferer")[0].getValue();
			}catch(Throwable e){}
			store.putOrCreate(urlStr, xCSS, refPar );
		}else{
			xCSS = itemTmp.getValue();
		}
		
		resp.setContentType("text/css");
		outTmp = resp.getOutputStream();
		byte[] bytesTmp = xCSS.getBytes();
		outTmp.write(bytesTmp);
		outTmp.flush();
    	// and cache it! //	    	String cxType = contextTypeStr.substring(beginIndex);
    	cacheIt(urlStr, bytesTmp,contextTypeStr );

		//store.putOrCreate(urlStr, xCSS, urlStr);
		return outTmp;
	}
	private static void cacheIt(String urlStr, byte[] bytesTmp,
			String contextTypeStr) {
		 cacheIt(urlStr, getCache() , bytesTmp, contextTypeStr);
	}
//	public static String justifyCSS(String urlStr, String cssInPar) {// , ByteArrayOutputStream oaos
//		String xCSS;
//		String lBAK_GIF = "URL (/l.gif?";
//		String FSservletURL = "url ( hTtP://"+SwapServletUrl.replace("/l/","/F/h_t_t_p_://");
//		String FSSservletURL = "url  ( hTtPs://"+SwapServletUrl.replace("/l/","/F/h_t_t_p_s_://");
//		//(new FileWriter(File.createTempFile("inPUT", "css"))).write(cssInPar);
//		// ROOT OF SERVER
//		xCSS = cssInPar.replace("url(/", lBAK_GIF)
//		.replace("url (/", lBAK_GIF)
//		.replace("URL(/", lBAK_GIF)
//		.replace("Url(/", lBAK_GIF)
//		.replace("url ( /", lBAK_GIF)
//		.replace(lBAK_GIF, FSservletURL ) //.replace(lBAK_GIF, "url(/l.gif?")
//		// ABSOLUTE-ref like: 
//		// url(http://maps.gstatic.com
//		.replace("url(http://", FSservletURL)
//		.replace("url(https://", FSSservletURL)
//		// 	rel-ref from './'	
//		.replace("url(", "url  (    "+SwapServletUrl.replace("/l/",undescoredProtocol(urlStr))+stripFileName(  stripProtocol(urlStr)))
//		;
//		xCSS = xCSS.replace(" url  (    http", "url(http");
//		xCSS = xCSS.replace("hTtPs://http://localhost","HTTP://lOcAlHoSt");  //(new FileWriter(File.createTempFile("IN_PUT", "css"))).write( (new Beauty()).cleanCSS(  cssInPar ));
//		return xCSS; //(new FileWriter(File.createTempFile("outPUT", "css"))).write( (new Beauty()).cleanCSS(  xCSS ));
//	}
	public static String justifyCSS(String baseUrlPar, String cssInPar){
		// 1st pass:  url("xxx.css") >> url ( 'http://base.host.srv/css/xxx.css" )
		BaseURLRegexpReplacer repl = new BaseURLRegexpReplacer("  url  ( \'%1$2s\' )", baseUrlPar ); 
		String cssTmp = cssInPar; 
		String retval = cssTmp ;
	    try {
			retval = repl.replaceAll(BaseURLRegexpReplacer.URL_PATTERN,  retval );
		} catch (ReplacerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    // 2nd pass: url ( 'http://base.host.srv/css/xxx.css" ) >>> URL ('https://swap-host-server.name/F/h_t_t_p_s_://base.host.srv/css/xxx.css')
	    String u_Tmp_u = SwapServletUrl.replace("/l/",undescoredProtocol(baseUrlPar));
		String strippedTmp  = stripFileName(  stripProtocol(baseUrlPar));
		BaseURLRegexpReplacer repl2 = new BaseURLRegexpReplacer("url(\'" +u_Tmp_u+ "%1$2s\')", strippedTmp  ); 
	    try {
			retval = repl2.replaceAll(BaseURLRegexpReplacer.URL_PATTERN,  retval );
		} catch (ReplacerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	    retval = retval.replace("F/h_t_t_p_s_://https://", "F/h_t_t_p_s_://");
	    retval = retval.replace("F/h_t_t_p_://http://", "F/h_t_t_p_://");
	    return retval;
	}

	private static String undescoredProtocol(String urlStr) {
		return urlStr.startsWith("https://")? "/F/h_t_t_p_s_://":"/F/h_t_t_p_://";
	}
	private static String stripProtocol(String urlStr) {
		return urlStr.startsWith("https://")? urlStr.substring("https://".length()):urlStr.substring("http://".length());
	}
	private static String stripFileName(String urlStr) {
		return urlStr.endsWith("/")? urlStr :urlStr.substring(0, urlStr.lastIndexOf("/"))+"/";
	}

	static ServletOutputStream performBinary(HttpServletResponse resp,
			String contextTypeStr, String urlStr, HttpResponse xRespTmp,
			HttpEntity entity, String contextEncPar) throws IOException {
		ServletOutputStream outTmp;
		if (! "null".equals( contextTypeStr )){
			String contypeTmp = contextTypeStr.substring("Content-Type:".length());
			resp.setContentType(contypeTmp);
			setupResponseProperty( resp,  xRespTmp);
		}
		//log.warning("HTML contextTypeStr||contextEncStr:["+contextTypeStr+"||"+contextEncStr+"]  URL =:["+urlStr+"]");
		
		
		String contextEncStr =  ""+entity.getContentEncoding() ;
		ByteArrayOutputStream oaos = new ByteArrayOutputStream();
		entity.writeTo(oaos) ;
		oaos = unZIP(xRespTmp, contextEncStr, oaos);
		
		outTmp = resp.getOutputStream();
		oaos.writeTo(outTmp) ;
		outTmp.flush();
		outTmp.close();
		cacheIt(urlStr, getCache(), oaos.toByteArray(), contextTypeStr );
		return outTmp;
	}

	static ServletOutputStream performScript(HttpServletResponse resp,
			String contextTypeStr, String urlStr, HttpEntity entity,
			String contextEncPar) throws IOException {
		ServletOutputStream outTmp;
		if (TRACE) log.warn("JS contextTypeStr||contextEncStr:["+contextTypeStr+"||"+contextEncPar+"]  URL =:["+urlStr+"]");
		
		JSStore ssTmp = JSStore.getInstanse();
		Item scriptTmp = ssTmp.getByURL( urlStr);
		if (scriptTmp == null){
			ByteArrayOutputStream oaos = new ByteArrayOutputStream();
			entity.writeTo(oaos) ;				
			String jsToWrap = oaos.toString("UTF-8");
			// 						(new Element('li', { 'class': 'fav', 'html': ((empty && i==0) ? '' : ', ') 
			//+ '<a href="hTTp://rrdsaas.appspot.com/F/h_t_t_p_://rrdsaas.appspot.com/l//HtTp/' + temp.user.login + '.' + temp.base_short + '/favorites/tag/' + tag + '">' + tag + '</a>'}
			String FServletURL = SwapServletUrl.replace("/l/", "/F/");
			jsToWrap = JSStore.performFormatJS(urlStr, jsToWrap ) ;
			jsToWrap = 
				jsToWrap
					.replace("http://",   FServletURL +  "h_t_t_p_://" )
					.replace("HTTP://",   FServletURL +  "h_t_t_p_://" )
					.replace("HTTPS://",   FServletURL +  "h_t_t_p_s_://" )
					.replace("https://",   FServletURL +  "h_t_t_p_s_://" )
				;
			
			scriptTmp = ssTmp.putOrCreate(urlStr, jsToWrap, urlStr);
		}  
		resp.setContentType("application/javascript; charset=utf-8");
		outTmp = resp.getOutputStream();
		String scriptValueTmp = scriptTmp.getValue();
		byte[] bytesTmp = scriptValueTmp.getBytes("UTF-8");
		outTmp.write(bytesTmp) ;
		
    	// and cache it! //	    	String cxType = contextTypeStr.substring(beginIndex);
    	cacheIt(urlStr, bytesTmp,contextTypeStr );		
		outTmp.flush();
		return outTmp;
	}

	static boolean isCSS(String contextTypeStr) {
		return "Content-Type: text/css".equalsIgnoreCase( contextTypeStr)||
		(""+contextTypeStr).toLowerCase().indexOf( "text/css")>0;
	}

	static boolean isBinary(String contextTypeStr) {
		return "null".equalsIgnoreCase( contextTypeStr)||
		"Content-Type: image/jpeg".equalsIgnoreCase( contextTypeStr) ||
		"Content-Type: image/png".equalsIgnoreCase( contextTypeStr) ||	
		"Content-Type: image/x-icon".equalsIgnoreCase( contextTypeStr) ||	
		"Content-Type: text/xml".equalsIgnoreCase( contextTypeStr) ||
		"Content-Type: image/gif".equalsIgnoreCase( contextTypeStr) ||
		"Content-Type: application/pdf".equalsIgnoreCase( contextTypeStr) ||
		"Content-Type: application/x-shockwave-flash".equalsIgnoreCase( contextTypeStr) ||
		"Content-Type: application/postscript".equalsIgnoreCase( contextTypeStr) ||
		"Content-Type: application/octet-stream".equalsIgnoreCase( contextTypeStr) ||
		"Content-Type: application/x-msexcel".equalsIgnoreCase( contextTypeStr) ||
		"Content-Type: image/tiff".equalsIgnoreCase( contextTypeStr) ||
		"Content-Type: image/ief".equalsIgnoreCase( contextTypeStr) ||
		"Content-Type: image/g3fax".equalsIgnoreCase( contextTypeStr) ||
		"Content-Type: application/x-shockwave-flash".equalsIgnoreCase( contextTypeStr)||
		(""+contextTypeStr).indexOf( "application/") >=0||
		(""+contextTypeStr).indexOf( "text/xml") >=0
		
		;
	}

	static boolean isScript(String contextTypeStr) {
		return
		
		"Content-Type: text/javascript".equalsIgnoreCase( contextTypeStr) ||
		"Content-Type: application/javascript".equalsIgnoreCase( contextTypeStr) ||
		"Content-Type: application/x-javascript".equalsIgnoreCase( contextTypeStr) ||
		"Content-Type: application/javascript; charset=utf-8".equalsIgnoreCase( contextTypeStr) ||
		"Content-Type: application/x-javascript; charset=utf-8".equalsIgnoreCase( contextTypeStr) ||
		//"content-type: text/html; charset=ISO8859-1".equalsIgnoreCase( contextTypeStr) ||
		"content-type: text/javascript; charset=UTF-8".equalsIgnoreCase( contextTypeStr) || 
		(""+contextTypeStr).toLowerCase().indexOf("text/javascript")>=0 ||
		(""+contextTypeStr).toLowerCase().indexOf("application/javascript")>=0 ||
		(""+contextTypeStr).toLowerCase().indexOf("application/x-javascript")>=0 
		
		;
	}

	private static boolean isRootReq(HttpServletRequest req) {
		return  req.getParameter(_U_R_L_) != null;
	}



	private ServletOutputStream include(HttpServletResponse resp, String resourceName) {
		ServletOutputStream out = null;
		try {
			byte[] b = getResourceAsBA(resourceName);
			out = include(resp, b);
		} catch (IOException e) {
			log.trace("include:",e);
		}
		return out;
		
	}

	private ServletOutputStream include(HttpServletResponse resp, byte[] bytes)
			throws IOException {
		ServletOutputStream out = resp.getOutputStream(); 
		String newVal = new String(bytes);
		String L1111 = "l11010101010000101010100101lIll1l0O0l10ll1001l1l01ll001";
		newVal = newVal.replace(L1111, SwapServletUrl);
		String B8b8b = "B8b8B8Bbbb888B";
		newVal = newVal.replace(B8b8b, calcBase());
		bytes = newVal.getBytes();
		out.write(bytes);
		return out;
	}

	public static final String calcBase(){
		return SwapServletUrl.substring(0, SwapServletUrl.length()-2);
	}
	
	public static byte[] getResourceAsBA(String namePar) throws IOException {
		InputStream in = LServlet.class.getClassLoader().getResourceAsStream(namePar); 
		byte[] b = new byte[in.available()] ;
		in.read(b);
		return b;
	}
	
	

	private static String getXEnc(HttpResponse respTmp) {
		 String retval = null;
		 try{
			 retval = respTmp.getHeaders("Content-Encoding")[0].getValue();
		 }catch (Exception e) {
			 try{
				 String retvalTmp = respTmp.getHeaders("Content-Type")[0].getValue();
				 // for ex. [Content-Type: text/html; charset=windows-1251]
				 int beginIndex =  retvalTmp.toLowerCase().indexOf("charset=")+"charset=".length();
 
				 retval  = ""+Charset.availableCharsets().get(retvalTmp.substring(beginIndex).toUpperCase()).displayName() ;
			 }catch (Exception e2) {
				// TODO: handle exception
			}
		}
		 return retval;
	}

	private static boolean isGZip(HttpResponse xRespTmp) {
		boolean retval = false;
		try{
			retval = "gzip".equals(xRespTmp.getHeaders("Content-Encoding")[0].getValue());
		}catch(Throwable e){}
		return  retval;
	}

	private static ByteArrayOutputStream deZip(ByteArrayOutputStream oaos)
			throws IOException {
		oaos.close();
		
		ByteArrayInputStream gzippeddata = new ByteArrayInputStream(oaos.toByteArray());
		GZIPInputStream zipin = new GZIPInputStream(gzippeddata);
		byte[] buf = new byte[16*1024];  //size can be  
		int len;
		oaos = new ByteArrayOutputStream();
		try{
			while ((len = zipin.read(buf)) > 0) {
				oaos.write(buf, 0, len);
			}
		}catch(IOException e){
			//System.out.println(new String(oaos.toByteArray()));
			if (oaos.size()>0)return oaos;
		}
		return oaos;
	}

	@SuppressWarnings("unchecked")
	protected static void setupSwaperConnProperty(HttpURLConnection swaperConn,
			HttpServletRequest req) throws ProtocolException {

		swaperConn.setConnectTimeout(SwaperConnTimeoutMS);
		swaperConn.setReadTimeout(SwaperReadTimeoutMS);

		// TODO: PoC: "java.io.IOException: http method POST against".
		swaperConn.setRequestMethod(req.getMethod());

		// http redirecting cookies (response code 3xx).
		swaperConn.setInstanceFollowRedirects(true);
		swaperConn.setUseCaches(false);

		Enumeration<String> e = (Enumeration<String>) req.getHeaderNames();
		String name = null;

		while (e.hasMoreElements()) {
			name = e.nextElement();
			swaperConn.setRequestProperty(name, req.getHeader(name));
		}
	}

	// TODO: Fix "Cookie rejected" warnning. domain must start with a dot.
	// WARNING: Cookie rejected:
	// "$Version=0; _javaeye3_session_=BAh7BiIKZmxhc2hJQzonQWN0aW9uQ29udHJvbGxlcjo6Rmxhc2g6OkZsYXNoSGFzaHsABjoKQHVzZWR7AA%3D%3D--d983012383f33595e8b4015c6235ad6e21fa81cf; $Path=/; $Domain=javaeye.com".
	// Domain attribute "javaeye.com" violates RFC 2109: domain must start with a dot

	static final String headersToSet []= {
//			"Content-Type",
			"Content-Language",
//			"Content-Encoding",
			"Content-Disposition",// : attachment; filename=Personalakte.pdf
			"filename",  
			"Date",
			"Last-Modified" ,
			"Accept",
			"Accept-Charset",
			"Accept-Language",
//			"Accept-Encoding",
			"Referer", 
//			"Cookie",
			"Cache-Control",
			"User-Agent",
			"Cookie2",
//			"X-Powered-By", //: ASP.NET
//			"X-AspNet-Version",//: 2.0.50727
			"Expires",
			"TE",
			"Server",
//			"Set-Cookie",
			"Keep-Alive",
			"Authorization"
			
	};	
	/**
	 * copy headers FROM:bPar TO: aPar
	 * 
	 * @author vipup
	 * @param aPar
	 * @param bPar
	 * @throws IOException
	 */
	protected static void setupResponseProperty(HttpServletResponse aPar,
			HttpResponse bPar) throws IOException {
		for (String headerName :headersToSet)
		for (Header next: bPar.getHeaders(headerName) ){
			String name = next.getName();
			String value = next.getValue();
			aPar.setHeader(name, value);
			log.debug("${}->{}",name, value);
		}
		
	}
	
	protected  static String[][] calcRequestHeaders(
			HttpServletRequest req) {
		Map<String, String> headersTmp = calcRequestHeadersAsMap(req, headersToSet);
		
		String [][]retval = new String[headersTmp.size()][2];
		int i=0;
		for(String  nextKey:headersTmp.keySet()){
			retval[i][0] = nextKey;
			retval[i][1] = headersTmp.get(nextKey);
			log.debug("# : {}=[{}]",  retval[i][0], retval[i][1] );
			i++;			
		}
		return   retval;
	}

	private static Map<String, String> calcRequestHeadersAsMap(HttpServletRequest req, String[] headersToSetPar) {
		Map<String, String> headersTmp = new HashMap<String, String>();
		for (String headerName :headersToSetPar){
			String nextVal =  req.getHeader(headerName);
			if ("Referer".equals(headerName) ){
				if ((""+nextVal).startsWith(SwapServletUrl)){
					nextVal   = HyperLinkUtil.decode(nextVal.substring(SwapServletUrl.length()));
				}
			}
			
			if (nextVal != null)
			headersTmp.put( headerName  , nextVal );
		}
		return headersTmp;
	}
 
}
