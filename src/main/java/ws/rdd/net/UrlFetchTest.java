package ws.rdd.net;

import gnu.inet.encoding.Punycode;
import gnu.inet.encoding.PunycodeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer; 
import org.htmlparser.http.Cookie; 
//import javax.servlet.http.Cookie; - setMaxAge >8E
//import org.apache.commons.httpclient.Cookie;
//import org.apache.http.cookie.Cookie;
import javax.servlet.http.HttpSession; 
import net.sf.jsr107cache.Cache; 
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest; 
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory; 
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.esxx.js.protocol.GAEConnectionManager;
import org.jrobin.cmd.RrdCommander; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.rrd.server.Base64Coder;
import ws.rrd.server.LServlet;

import cc.co.llabor.cache. Manager;
import cc.co.llabor.cache.MemoryFileItem;

/**
 * <b>Description:TODO</b>
 * 
 * @author vipup<br>
 *         <br>
 *         <b>Copyright:</b> Copyright (c) 2006-2008 Monster AG <br>
 *         <b>Company:</b> Monster AG <br>
 * 
 * Creation: 08.04.2010::12:07:00<br>
 */
public class UrlFetchTest implements Serializable{
	/**
	 * @author vipup
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(LServlet.class .getName());

	private static final String COOKIES_STORE = "COOKIES_STORE";

	transient private HttpSession session;
	// timeout in ms
	private String socketTimeout;

	/**
	 * Cookie expiry date format for parsing.
	 */
	volatile static protected SimpleDateFormat mFormat = new SimpleDateFormat(
			"EEE, dd-MMM-yy kk:mm:ss z");

	public UrlFetchTest(HttpSession sessionTmp) {
		this.session = sessionTmp ;
	}

	public UrlFetchTest() {
		// TODO Auto-generated constructor stub
	}

	public String testFetchUrl(String toFetchStr)
			throws ClientProtocolException, IOException {
		HttpResponse respTmp = fetchGetResp(toFetchStr);
		System.out.println(respTmp);// s.getAllHeaders()
		HttpEntity eTmp = ((BasicHttpResponse) respTmp).getEntity();
		InputStream contentTmp = eTmp.getContent();
		int sizeTmp = Math.max((int) eTmp.getContentLength(), contentTmp
				.available());
		byte buf[] = new byte[sizeTmp];
		int readedTmp = contentTmp.read(buf);
		return new String(buf, 0, readedTmp);
	}
	public static void System_out_print(String txt){
		log.trace(txt);
	}
	public static void System_out_println(Object txt){
		log.trace( ""+ txt);
	}
	public static void System_out_println(String txt){
		log.trace(txt);
	}
	
	/**
	 * perform authorisation for not-autorised requests.
	 * for HTTP-200 -nothig todo, just store success.
	 * @author vipup
	 * @param toFetchStr
	 * @param httpClient
	 * @param m
	 * @param respTmp
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private HttpResponse makeAuth(String toFetchStr, HttpClient httpClient,
			HttpUriRequest m, HttpResponse respTmp) throws IOException,
			ClientProtocolException {
		StatusLine statusLine = respTmp.getStatusLine();
		String statusTmp = statusLine.toString();
		String toFetchKey = null;
		try {
			toFetchKey = toKey(toFetchStr);
		} catch (PunycodeException e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
		}			
		if (statusTmp.indexOf("200 OK") > 0) { 
			System_out_println("Authorisation is succesful. Store success..." + statusLine); 
			Header[] headers = m.getHeaders("Authorization");
			if (headers.length>0){
				Cache cacheAuth = Manager.getCache(CACHE_NAME);
				String uri = toFetchStr;
				int appUrlLen = uri.lastIndexOf( "/");				
				String appUri = uri.substring(0, appUrlLen);
				Header header = headers[0]; 
					String basicAuth = searchForAuth(toFetchKey, m); 
					if (basicAuth == null){
						log.debug("store AUTH to cache :{}", header);
						cacheAuth.put(toFetchKey,""+header.getValue() );
					}else{ // onerwrite AUTH
						log.debug("onerwrite AUTH  4 {}", appUri);
						cacheAuth.put("~"+toFetchKey,""+basicAuth );
						cacheAuth.put(toFetchKey,""+header.getValue() );
					} 
			}			
		} else if (statusLine.getStatusCode() == 401 ||"HTTP/1.1 401 Unauthorized".equals(statusTmp) || "HTTP/1.0 401 Unauthorized".equals(statusTmp) || "HTTP/1.1 401 Authorization Required".equals(statusTmp)) {			 
			String basicAuth = searchForAuth(toFetchKey, m);	
			// User-pwd in the URL like 'http://appspot.com:bossEmailAsPWD@someDot.com@benzinpreis.de/xml/preise.xml'
			String userTmp = null;
			String pwdTmp = null;
			if (basicAuth==null&&
					toFetchStr.indexOf(":")<toFetchStr.lastIndexOf(":")  &&
					toFetchStr.indexOf("@")< toFetchStr.lastIndexOf("@") && // TODO :-\ 
					toFetchStr.indexOf("http")==toFetchStr.lastIndexOf("http")){
				try{
					String urlTmp = toFetchStr;
					toFetchStr = urlTmp.substring(0, urlTmp.indexOf("//")+2)+urlTmp.substring(urlTmp.lastIndexOf("@")+1);
					String userPasswordTmp = urlTmp.substring(urlTmp.indexOf("//")+2, urlTmp.lastIndexOf("@"));
					userTmp = userPasswordTmp.split(":")[0];
					pwdTmp = userPasswordTmp.split(":")[1];
					System.out.println(userPasswordTmp+";;;=="+userTmp+"//"+pwdTmp);
					basicAuth = ""+userTmp+":"+pwdTmp;
					basicAuth = new String(Base64Coder.encode(basicAuth.getBytes()));
				}catch(Throwable e){}
			}			
			if (basicAuth != null) {// go forward with cached
				m.addHeader("Authorization", (basicAuth.startsWith("Basic ")?"":"Basic ") + basicAuth);
				respTmp = httpClient.execute(m);
			} else { // request auth from real user...
				String bRealm = "Basic realm=\"$$$$$$\"";
				Header[] hTmp = respTmp.getHeaders( "WWW-Authenticate");
				if (hTmp.length == 0)
					bRealm = bRealm.replace("$$$$$$", "Tomcat Manager Application");
				else
					bRealm = bRealm.replace("$$$$$$", ""+hTmp);
				respTmp.addHeader("WWW-Authenticate", bRealm);
				respTmp.setStatusCode(401);
				respTmp.setStatusLine(statusLine);
			}
		}
		return respTmp;
	}
	
	private String toKey(String value) throws PunycodeException{
		String eKey = Punycode.encode(value);
		return eKey;
	}

	/**
	 * @author vipup
	 * @param toFetchStr
	 * @param m
	 * @return
	 */
	private String searchForAuth(String toFetchKey, HttpUriRequest m) {
		Cache cacheAuth = Manager.getCache(CACHE_NAME);
		 
		String basicAuth = null; 
		basicAuth = (String) cacheAuth.get(toFetchKey); // auth is path (NOT File) related!
		String uri = m.getURI().toString();
		int domainUrlLen = uri.indexOf( m.getURI().getPath());			
		while(basicAuth == null && toFetchKey.length()> domainUrlLen ){
			toFetchKey = toFetchKey.substring(0,toFetchKey.lastIndexOf("/") );
			basicAuth = (String) cacheAuth.get(toFetchKey);
		} 
		return basicAuth;
	}
	public static final String CACHE_NAME = UrlFetchTest.class.getName()
			+ ":Authorization";

	public HttpClient makeHTTPClient() {
		HttpParams parmsTmp = new BasicHttpParams();

		org.apache.http.conn.ClientConnectionManager cmTmp = null;
		// ?new RrdGraphCmd():new RrdSvgCmd();
		if (!RrdCommander.isGAE()) {
			SchemeRegistry sregTmp = new SchemeRegistry();
			PlainSocketFactory socketFactory = PlainSocketFactory
					.getSocketFactory();
			sregTmp.register(new Scheme("http", socketFactory, 80));
			SSLSocketFactory socketFactory2 = SSLSocketFactory
					.getSocketFactory();
			sregTmp.register(new Scheme("https", socketFactory2, 443));
			cmTmp = new ThreadSafeClientConnManager(parmsTmp, sregTmp);
		} else {
			cmTmp = new GAEConnectionManager();
		}
		HttpClient cliTmp = new DefaultHttpClient(cmTmp, parmsTmp);
		setupProxy(cliTmp);
		cliTmp.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true); 
		cliTmp.getParams().setParameter(ClientPNames.HANDLE_AUTHENTICATION , true); 
		cliTmp.getParams().setParameter(ClientPNames.REJECT_RELATIVE_REDIRECT, true); 
		cliTmp.getParams().setParameter(ClientPNames.MAX_REDIRECTS, 18); 
		return cliTmp;
	}

	public HttpResponse fetchGetResp(String toFetchStr) throws IOException,
			ClientProtocolException {
		return fetchGetResp(toFetchStr, new String[][]{});
	}

	public HttpResponse fetchPostResp(String toFetchStr, String[][] headers,
			Map parameterMap) throws ClientProtocolException, IOException {
		return fetchPostResp(toFetchStr, headers, parameterMap, null);
	}

	public HttpResponse fetchGetResp(String toFetchStr, String headers[][])
			throws IOException, ClientProtocolException {
		HttpClient httpClient = makeHTTPClient();
		String fetchUrl = null == toFetchStr
				? "http://www.fidu"+"cia.de/service/suchergebnis.html?searchTerm=java"
				: toFetchStr;
		// User-pwd in the URL like 'http://appspot.com:bossEmailAsPWD@someDot.com@benzinpreis.de/xml/preise.xml'
		String userTmp = null;
		String pwdTmp = null;
		if (fetchUrl.indexOf(":")<fetchUrl.lastIndexOf(":") && 
				fetchUrl.indexOf("@")< fetchUrl.lastIndexOf("@") && // TODO :-\ 
				fetchUrl.indexOf("http")==fetchUrl.lastIndexOf("http")){
			System.out.println("????PWD@URL????"+fetchUrl);
			try{
				String urlTmp = fetchUrl;
				fetchUrl = urlTmp.substring(0, urlTmp.indexOf("//")+2)+urlTmp.substring(urlTmp.lastIndexOf("@")+1);
				String userPasswordTmp = urlTmp.substring(urlTmp.indexOf("//")+2, urlTmp.lastIndexOf("@"));
				userTmp = userPasswordTmp.split(":")[0];
				pwdTmp = userPasswordTmp.split(":")[1];
				System.out.println(userPasswordTmp+";;;=="+userTmp+"//"+pwdTmp);
			}catch(Throwable e){}
		}
		HttpUriRequest m = new HttpGet(fetchUrl);
		for (String[] nextHeader : headers){
			String headerNameTmp = nextHeader[0];
			String headerValTmp = nextHeader[1];
			log.trace("HEADER{}={}", headerNameTmp, headerValTmp);
			m.addHeader(headerNameTmp, headerValTmp);
			
		}
		List<Cookie> cookListTmp = addCookies(m);
		if (null!=cookListTmp)
		for (Cookie c:cookListTmp){
			try{
				System.out.println(c);
			}catch(Throwable e){}
			
		}
		m.addHeader("Host", m.getURI().getHost());
		if (this.socketTimeout !=null){
			httpClient.getParams().setParameter("http.socket.timeout", new Integer(this.socketTimeout));
			httpClient.getParams().setParameter("http.connection.timeout", new Integer(this.socketTimeout));
			
		}
		
		System.out.println(fetchUrl);
		HttpResponse respTmp = httpClient.execute(m);
			
		
		respTmp = makeAuth(toFetchStr, httpClient, m, respTmp);
		this.parseCookies(m, respTmp);
		return respTmp;
	}

	public HttpResponse fetchPostResp(String toFetchStr, String[][] headers,
			Map parameterMap, java.util.List<MemoryFileItem> items)
			throws ClientProtocolException, IOException {
		HttpClient httpClient = makeHTTPClient();
		

		String fetchUrl = null == toFetchStr
				? "http://www.fi"+"ducia.de/service/suchergebnis.html?searchTerm=java"
				: toFetchStr;
		HttpPost m = new HttpPost(fetchUrl);
		for (String[] nextHeader : headers)
			m.addHeader(nextHeader[0], nextHeader[1]);
		addCookies(m);  
 
		m.addHeader("Host", m.getURI().getHost()); 
		if (items != null) {// Multipart
			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.STRICT);//HttpMultipartMode.BROWSER_COMPATIBLE
			for (final MemoryFileItem item : items) { 
				String contentType = item.getContentType();
				try{
					contentType = contentType.substring(0, contentType.indexOf(";"));
				}catch (Exception e) {
					// TODO: handle exception
				}
				String nameTmp = item.getName();
				final ContentBody contentBody = new InputStreamBody(item .getInputStream(), contentType, nameTmp);
				final StringBody comment = new StringBody("Filename:" + nameTmp);  
				
				reqEntity.addPart(item.getFieldName(), contentBody);
				reqEntity.addPart("file#"+nameTmp, comment);
				// For File parameters
				m.setEntity(reqEntity); 
			}
			// Params for multipart
			for (Object nextParName : parameterMap.keySet()) {
				String parName = "" + nextParName;
				Object aString = parameterMap.get(parName);
				String valueTmp =  ((String[])  aString)[0]; //(((String[]) parameterMap.get(parName))[0]); 
				final StringBody comment = new StringBody( valueTmp ); 
				reqEntity.addPart(parName, comment);
			}
			
		}else{
			validateContentType(parameterMap, m);
			HttpParams arg0 = httpClient.getParams();
			for (Object nextParName : parameterMap.keySet()) {
				String parName = "" + nextParName;
				Object aString = parameterMap.get(parName);
				String valueTmp =  ((String[])  aString)[0]; //(((String[]) parameterMap.get(parName))[0]); 
				arg0.setParameter(parName, valueTmp);
			}
			m.setParams(arg0);
		}

		HttpResponse respTmp = httpClient.execute(m);
		respTmp = makeAuth(toFetchStr, httpClient, m, respTmp);
		StatusLine statusLine = respTmp.getStatusLine();
		String statusTmp = statusLine.toString();
		this.parseCookies(m, respTmp);
		if ("HTTP/1.1 302 Found".equals(statusTmp)) {
				String movedTo =""+ respTmp.getHeaders("Location")[0];
				movedTo = movedTo.substring("Location: ".length()); 
				String uri = m.getURI().toString();
				int domainUrlLen = uri.indexOf( m.getURI().getPath());
				movedTo = uri.substring(0,domainUrlLen)  +movedTo;
				respTmp = fetchGetResp(movedTo, headers);
				respTmp.addHeader("X-MOVED", movedTo);
		}
		
		return respTmp;
	}

	/**
	 * @author vipup
	 * @param parameterMap
	 * @param m
	 * @throws UnsupportedEncodingException
	 */
	private void validateContentType(Map parameterMap, HttpPost m)
			throws UnsupportedEncodingException {
		Header[] ctTmp = m.getHeaders("Content-Type");
		if(ctTmp.length == 0){ 
			m.addHeader("Content-Type", "application/x-www-form-urlencoded");
			List<NameValuePair> listTmp= new ArrayList<NameValuePair>();
			for (Object nextParName : parameterMap.keySet()) {
				final String parName = "" + nextParName;
				Object aString = parameterMap.get(parName);
				final String valueTmp =  ((String[])  aString)[0]; //(((String[]) parameterMap.get(parName))[0]); 
				NameValuePair newPaar = new  NameValuePair (){ 
					public String getName() { return parName;  } 
					public String getValue() {return valueTmp;  }
				};
				listTmp.add(newPaar );
			}
			HttpEntity entity =  new UrlEncodedFormEntity (listTmp);
			m.setEntity(entity ); //sData = URLEncodedUtils.format(listTmp , "utf-8");
		}
	}

	/**
	 * @author vipup
	 * @param httpClient
	 */
	private void setupProxy(HttpClient httpClient) {
		String schemes[] = {"https", "http", "ftp"};
		for (String scheme : schemes) {
			String proxHostTmp = System.getProperty(scheme + ".proxyHost");// System.getProperties();
			String proxyPortTmp = System.getProperty(scheme + ".proxyPort");// System.setProperty("http.proxyHost","localhost");
			if (("" + proxHostTmp + proxyPortTmp).indexOf("null") == -1) {
				org.apache.http.HttpHost proxyTmp = new org.apache.http.HttpHost(
						proxHostTmp, Integer.parseInt(proxyPortTmp), scheme);
				httpClient.getParams().setParameter(
						ConnRoutePNames.DEFAULT_PROXY, proxyTmp);
			}
		}
	}

	/**
	 * Check for cookie and parse into cookie jar.
	 * 
	 * @param request
	 * @param connection
	 *            The connection to extract cookie information from.
	 */
	public void parseCookies(HttpUriRequest request, HttpResponse response/*
																			 * URLConnection
																			 * connection
																			 */) {
		Header[] setCookieHeaders;
		List<Cookie> cookies;

		String token;
		int index;

		String key;
		String value;
		Cookie cookie;

		setCookieHeaders = response.getHeaders("Set-Cookie");
		for (Header setCookieHeader : setCookieHeaders) {
			// set-cookie = "Set-Cookie:" cookies
			// cookies = 1#cookie
			// cookie = NAME "=" VALUE *(";" cookie-av)
			// NAME = attr
			// VALUE = value
			// cookie-av = "Comment" "=" value
			// | "Domain" "=" value
			// | "Max-Age" "=" value
			// | "Path" "=" value
			// | "Secure"
			// | "Version" "=" 1*DIGIT
			cookies = new ArrayList<Cookie>();
			String strCookieHeader = setCookieHeader.toString();
			strCookieHeader = strCookieHeader.substring(strCookieHeader
					.indexOf("Set-Cookie: ")
					+ "Set-Cookie: ".length());
			System_out_println("\"Set-Cookie: " + strCookieHeader + "\"");
			StringTokenizer tokenizer = new StringTokenizer(strCookieHeader,
					";,", true);
			cookie = null;
			String name;
			while (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken().trim();
				if (token.equals(";"))
					continue;
				else if (token.equals(",")) {
					cookie = null;
					continue;
				}

				index = token.indexOf('=');
				if (-1 == index) {
					if (null == cookie) { // an unnamed cookie
						name = "";
						value = token;
						key = name;
					} else {
						name = token;
						value = null;
						key = name.toLowerCase();
					}
				} else {
					name = token.substring(0, index);
					value = token.substring(index + 1);
					key = name.toLowerCase();
				}

				if (null == cookie) {
					try {
						cookie = new Cookie(name, value);
						cookies.add(cookie);
					} catch (IllegalArgumentException iae) {
						// should print a warning
						// for now just bail
						// e.  printStackTrace();
						break;
					}
				} else {
					if (key.equals("expires")) // Wdy, DD-Mon-YY HH:MM:SS GMT
					{
						try {
							String comma = tokenizer.nextToken();
							String rest = tokenizer.nextToken();
						
							Date date = mFormat.parse(value + comma + rest);
							// http://download.oracle.com/javaee/1.4/api/javax/servlet/http/Cookie.html#setMaxAge(int)
							// cookie.setMaxAge((int) (date.getTime() - System
							// .currentTimeMillis()) / 1000);
							cookie.setExpiryDate(date);
						} catch (Exception pe)// catch (ParseException pe)
						{
							// ok now set it to 1 day!
							// cookie.setMaxAge(24 * 60 * 60);
							long morgenTmp = System .currentTimeMillis() + 1000 * 24 * 60 * 60;
							cookie.setExpiryDate(new Date(morgenTmp));
						}
					} else if (key.equals("domain"))
						cookie.setDomain(value);
					else if (key.equals("path"))
						cookie.setPath(value);
					else if (key.equals("paath"))
						cookie.setPath(value);
					else if (key.equals("secure"))
						cookie.setSecure(true);
					else if (key.equals("comment"))
						cookie.setComment(value);
					else if (key.equals("version"))
						cookie.setVersion(Integer.parseInt(value));
					else if (key.equals("max-age")) {
						Date date = new Date();
						long then = date.getTime() + Integer.parseInt(value)
								* 1000;
						date.setTime(then);
						cookie.setExpiryDate(date);
						// cookie.setMaxAge (date);
						// cookie.setMaxAge((int) (date.getTime() - System
						// .currentTimeMillis()) / 1000);
					} else
						// error,? unknown attribute,
						// maybe just another cookie
						// not separated by a comma
						try {
							cookie = new Cookie(name, value);
							cookies.add(cookie);
						} catch (IllegalArgumentException iae) {
							// should print a warning
							// for now just bail
							break;
						}
				}
			}
			if (0 != cookies.size()){
				for(Cookie cTmp:cookies){ // store all new cookies into JAR-repo
					String domainTmp = cTmp.getDomain();
					List<Cookie> clistTmp = mCookieJar.get( domainTmp );
					if(clistTmp == null){
						clistTmp = new ArrayList<Cookie>();
						mCookieJar.put(domainTmp, clistTmp);
					}
					clistTmp.add(cTmp);
				}
				 				
				saveCookies(cookies, request);
			}
		}
	}

	/**
	 * Adds a cookie to the cookie jar.
	 * 
	 * @param cookie
	 *            The cookie to add.
	 * @param domain
	 *            The domain to use in case the cookie has no domain attribute.
	 * @param request 
	 */
	public void setCookie(Cookie cookie, String domain, HttpUriRequest request) {
		String path;
		Cookie probe;
		boolean found; // flag if a cookie with current name is already there

		if (null != cookie.getDomain())
			domain = cookie.getDomain();
		path = cookie.getPath();
		Map<String, List<Cookie>> mCookieJar = getOrCreateStore();
		List<Cookie> cookies = mCookieJar.get(domain);
		if (null != cookies) {
			found = false;
			for (int j = 0; j < cookies.size(); j++) {
				probe = (Cookie) cookies.get(j);
				if (probe.getName().equalsIgnoreCase(cookie.getName())) {
					// we keep paths sorted most specific to least
					if (probe.getPath().equals(path)) {
						cookies.set(j, cookie); // replace
						found = true; // cookie found, set flag
						break;
					} else if (path.startsWith(probe.getPath())) {
						cookies.add(cookie);
						found = true; // cookie found, set flag
						break;
					}
				}
			}
			if (!found)
				// there's no cookie with the current name, therefore it's added
				// at the end of the list (faster then inserting at the front)
				cookies.add(cookie);
		} else { // new cookie list needed
			cookies = new ArrayList<Cookie>();
			cookies.add(cookie);
			mCookieJar.put(domain, cookies);
		}
	}

	/**
	 * @author vipup
	 * @return
	 */
	private Map<String, List<Cookie>> getOrCreateStore() {
		
		//Map<String, List<Cookie>> mCookieJar = null;
		try{
			mCookieJar = (Map<String, List<Cookie>>) session .getAttribute(COOKIES_STORE);
		}catch (Exception e) {
			// TODO: handle exception
		}
		if (null == mCookieJar) {
			mCookieJar = new HashMap<String, List<Cookie>>(); // turn on
			// cookie
			// processing
			try{
				session.setAttribute(COOKIES_STORE, mCookieJar);
			}catch (Exception e) {
				// TODO: handle exception
			}			
		}
		return mCookieJar;
	}
	
	transient Map<String, List<Cookie>> mCookieJar = null;

	/**
	 * Generate a HTTP cookie header value string from the cookie jar.
	 * 
	 * <pre>
	 *   The syntax for the header is:
	 * 
	 *    cookie          =       &quot;Cookie:&quot; cookie-version
	 *                            1*((&quot;;&quot; | &quot;,&quot;) cookie-value)
	 *    cookie-value    =       NAME &quot;=&quot; VALUE [&quot;;&quot; path] [&quot;;&quot; domain]
	 *    cookie-version  =       &quot;$Version&quot; &quot;=&quot; value
	 *    NAME            =       attr
	 *    VALUE           =       value
	 *    path            =       &quot;$Path&quot; &quot;=&quot; value
	 *    domain          =       &quot;$Domain&quot; &quot;=&quot; value
	 * 
	 * 
	 * </pre>
	 * 
	 * @param connection
	 *            The connection being accessed.
	 * @see <a href="http://www.ietf.org/rfc/rfc2109.txt">RFC 2109</a>
	 * @see <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>
	 */
	public List<Cookie>  addCookies(HttpUriRequest request /* URLConnection connection */) {
		List<Cookie> list = null;
		URI url;
		String host;
		String path;
		String domain;
		
		Map<String, List<Cookie>> mCookieJar = getOrCreateStore();
		if (null != mCookieJar) {
			
			// get the site from the URL
			url = request.getURI();
			host = url.getHost();
			path = url.getPath();
			if (0 == path.length())
				path = "/";
			if (null != host) 
			for(
					String domainTmp =  host  ;
					domainTmp .contains(".");
					domainTmp = domainTmp.startsWith(".")? 
							domainTmp.substring(domainTmp.indexOf(".")): 
							domainTmp.substring(domainTmp.indexOf(".")+1)){ // http://www.objectsdevelopment.com/portal/modules/freecontent/content/javawebserver.html
				List<Cookie> cookListTmp = mCookieJar.get(domainTmp);
				list = mergeCookies(cookListTmp, path, list);
				domain = getDomain(domainTmp);
				String keyCook = null;
				if (null != domain)
					// list = addCookies( mCookieJar.get(domain), path, list);
					keyCook = domain;
				else
					// maybe it is the domain we're accessing
					// list = addCookies( mCookieJar.get("." + host), path,
					// list);
					keyCook = "." + domainTmp;
				cookListTmp = mCookieJar.get(keyCook);
				list = mergeCookies(cookListTmp, path, list);
			}
			if (null != list) {
				String generateCookieProperty = generateCookieProperty(list);
				generateCookieProperty = generateCookieProperty.replace(
						"; HttpOnly=null", "");
				generateCookieProperty = "$Version=\"1\"; "
						+ generateCookieProperty;
				request.addHeader("Cookie", generateCookieProperty); // $Version="1";
			}
		}
		return list;
	}

	/**
	 * Save the cookies received in the response header.
	 * 
	 * @param list
	 *            The list of cookies extracted from the response header.
	 * @param connection
	 *            The connection (used when a cookie has no domain).
	 */
	protected void saveCookies(List<Cookie> list, HttpUriRequest request) {
		for (Cookie cookie : list) {

			String domain = cookie.getDomain();
			if (null == domain)
				domain = request.getURI().getHost();
			setCookie(cookie, domain,request);
		}
	}

	/**
	 * Get the domain from a host.
	 * 
	 * @param host
	 *            The supposed host name.
	 * @return The domain (with the leading dot), or null if the domain cannot
	 *         be determined.
	 */
	protected String getDomain(String host) {
		StringTokenizer tokenizer;
		int count;
		String server;
		int length;
		boolean ok;
		char c;
		String ret;

		ret = null;

		tokenizer = new StringTokenizer(host, ".");
		count = tokenizer.countTokens();
		if (3 <= count) {
			// have at least two dots,
			// check if we were handed an IP address by mistake
			length = host.length();
			ok = false;
			for (int i = 0; i < length && !ok; i++) {
				c = host.charAt(i);
				if (!(Character.isDigit(c) || (c == '.')))
					ok = true;
			}
			if (ok) {
				// so take everything !
				server = tokenizer.nextToken();
				length = server.length();
				ret = host.substring(0);
			}
		}

		return (ret);
	}

	/**
	 * Creates the cookie request property value from the list of valid cookies
	 * for the domain.
	 * 
	 * @param cookies
	 *            The list of valid cookies to be encoded in the request.
	 * @return A string suitable for inclusion as the value of the "Cookie:"
	 *         request property.
	 */
	protected String generateCookieProperty(List<Cookie> cookies) {
		int version;
		StringBuffer buffer;
		String ret;

		ret = null;

		buffer = new StringBuffer();
		version = 0;
		// 1st: search for max
		for (Cookie cTmp : cookies) {
			version = Math.max(cTmp.getVersion(), version);
		}

		if (0 != version) {
			buffer.append("$Version=\"");
			buffer.append(version);
			buffer.append("\"");
		}
		// / 2nd: search for max
		for (Cookie cookie : cookies) {
			{

				if (0 != buffer.length())
					buffer.append("; ");
				buffer.append(cookie.getName());
				buffer.append(cookie.getName().equals("") ? "" : "=");
				if (0 != version)
					buffer.append("\"");
				buffer.append(cookie.getValue());
				if (0 != version)
					buffer.append("\"");
				if (0 != version) {
					if ((null != cookie.getPath())
							&& (0 != cookie.getPath().length())) {
						buffer.append("; $Path=\"");
						buffer.append(cookie.getPath());
						buffer.append("\"");
					}
					if ((null != cookie.getDomain())
							&& (0 != cookie.getDomain().length())) {
						buffer.append("; $Domain=\"");
						buffer.append(cookie.getDomain());
						buffer.append("\"");
					}
				}
			}
			if (0 != buffer.length())
				ret = buffer.toString();
		}
		return (ret);

	}

	/**
	 * Add qualified cookies from cookies into list.
	 * 
	 * @param cookies
	 *            The list of cookies to check (may be null).
	 * @param path
	 *            The path being accessed.
	 * @param list
	 *            The list of qualified cookies.
	 * @return The list of qualified cookies.
	 */
	protected List<Cookie> mergeCookies(List<Cookie> cookies, String path,
			List<Cookie> list) {
		List<Cookie> copyOfCookies = new ArrayList<Cookie>();
		if (cookies != null)
			copyOfCookies.addAll(cookies);
		Date expires;
		Date now;

		if (null != cookies) {
			now = new Date();
			for (Cookie cookie : copyOfCookies) {

				expires = cookie.getExpiryDate();
				if ((null != expires) && expires.before(now)) {
					// clean original List from exired values
					cookies.remove(cookie);
					// i--; // dick with the loop variable
				} else if (path.startsWith(cookie.getPath())) {
					if (null == list)
						list = new ArrayList<Cookie>();
					if (list.indexOf(cookie) == -1)
						list.add(cookie);
				}
			}
		}

		return (list);
	}

	public String getSocketTimeout() {
		//TODO 
		if (1 == 1)
			throw new RuntimeException(
					"autogenerated from vipup return not checked value since13.04.2011 ;)!");
		else
			return socketTimeout;
	}

	public void setSocketTimeout(String socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

}
