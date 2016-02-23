package cc.co.llabor.cache.replace;  
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern; 

import org.vietspider.html.util.HyperLinkUtil;

import cc.co.llabor.cache.Manager;

import ws.rrd.server.LServlet;
 
   
import net.sf.jsr107cache.Cache; 
  
/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  26.07.2010::11:42:57<br> 
 */ 
public class ReplaceStore { 
	 
	private static final String REPLACE_PROPERTIES_FILE_EXTENSION = ".properties";
	private static final String ReplaceStore = "ReplaceStore";
	private static final ReplaceStore me = new ReplaceStore();
	Map<String, Properties>  replaceStore  = null;
	private boolean collectParents = true;
	@SuppressWarnings("unchecked")
	ReplaceStore() {
		this.replaceStore = Manager.getCache(ReplaceStore);		
	}
  

	public static ReplaceStore getInstanse() {
		return me;
	}

	/**
	 * gives back replace-pairs in format of properties
	 * @author vipup
	 * @param scriptURL
	 * @return
	 */
	public Properties getByURL(String scriptURL) {
		
		String path = normalize( scriptURL.replace("%2F", "/"));
		
		String key = scriptURL + REPLACE_PROPERTIES_FILE_EXTENSION;
		Properties retval = (Properties) replaceStore.get(key );
		if (retval == null){
			retval = new Properties();
			// create placeholder for replacer
			replaceStore.put(key, retval);
		}
		if (collectParents )
		try {
			URI pUrl;
			
			do{
				String parentURl = path.substring(0, path.lastIndexOf("/"));
				if ("".equals(parentURl) || parentURl == scriptURL ) break ;
				Properties pTmp = getByURL(parentURl);
				if (pTmp!=null){ 
					mergeProperties(pTmp, retval);
				}
				pUrl = new URI(parentURl);
				//System.out.println(pUrl);
			}while(pUrl.getHost()!=null);  
		} catch (URISyntaxException e) { 
			// e.  printStackTrace();
		}
		

		return retval;
	}

 
	    /**
	     * Normalize a relative URI path that may have relative values ("/./",
	     * "/../", and so on ) it it. <strong>WARNING</strong> - This method is
	     * useful only for normalizing application-generated paths. It does not try
	     * to perform security checks for malicious input.
	     * 
	     * @param path
	     *      Relative path to be normalized
	     */
	    public static String normalize(String path) {

	        if (path == null)
	            return null;

	        // Create a place for the normalized path
	        String normalized = path;

	        if (normalized.equals("/."))
	            return "/";

	        // Add a leading "/" if necessary
	        if (!normalized.startsWith("/"))
	            normalized = "/" + normalized;

	        // Resolve occurrences of "//" in the normalized path
	        while (true) {
	            int index = normalized.indexOf("//");
	            if (index < 0)
	                break;
	            normalized = normalized.substring(0, index)
	                    + normalized.substring(index + 1);
	        }

	        // Resolve occurrences of "/./" in the normalized path
	        while (true) {
	            int index = normalized.indexOf("/./");
	            if (index < 0)
	                break;
	            normalized = normalized.substring(0, index)
	                    + normalized.substring(index + 2);
	        }

	        // Resolve occurrences of "/../" in the normalized path
	        while (true) {
	            int index = normalized.indexOf("/../");
	            if (index < 0)
	                break;
	            if (index == 0)
	                return (null); // Trying to go outside our context
	            int index2 = normalized.lastIndexOf('/', index - 1);
	            normalized = normalized.substring(0, index2)
	                    + normalized.substring(index + 3);
	        }

	        // Return the normalized path that we have completed
	        return (normalized);

	    }


	public Properties putOrCreate(String cacheKey, Properties value  ) { 
		cacheKey = normalize( cacheKey.replace("%2F", "/"));
		cacheKey += REPLACE_PROPERTIES_FILE_EXTENSION;
		Properties repTmp =  replaceStore.get(cacheKey);
		if (repTmp == null){  
			repTmp = new Properties();
			mergeProperties(value, repTmp); 
		}  else{  // only for existing entries
			//System.out.println(repTmp);
		}
		
		Object o = ((Cache)replaceStore).peek(cacheKey); 
		if (repTmp != o) {// check similarity
			try{
				o = replaceStore.remove(cacheKey);// if (o.hashCode() == jsItem.hashCode());
			}catch(NullPointerException e){
				// e.  printStackTrace();
			}
			if (1==2)System.out.println(o);
			
			boolean changed = true;
			try{
				changed = !((Properties)o).toString().equals(value.toString()) ;
			}catch(NullPointerException e){
				//// e.  printStackTrace();
			}
			if (changed){
				replaceStore.put(cacheKey, value );
			}
		}
		return repTmp;
	}


	/**
	 * merge values from "value" to repTmp;
	 * any duplicated properties will be overwritten
	 * 
	 * @author vipup
	 * @param value
	 * @param repTmp
	 */
	private void mergeProperties(Properties value, Properties repTmp) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			repTmp.store(out, "b:"+new Date());
			value.store(out, "a:"+new Date());
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			repTmp.load(in);  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
		}
	}

	// http://www.javapractices.com/topic/TopicAction.do?Id=80
	private static String replaceLinks(String aHtmlTextWithLinks,  String fLINK, String fFRAGMENT){
			Pattern pattern = Pattern.compile(fLINK);
		    Matcher matcher = pattern.matcher(aHtmlTextWithLinks);
		    return matcher.replaceAll(fFRAGMENT);
	}
	
	/**
	 * make the job!
	 * 1) replace <b>"$referer"</b> by HTTP-referer if any
	 * 
	 * 2) use .filecache/ReplaceStore/http=..=\<hostname>\rrdsaas\pathtoRepaceCong.properties 
	 * 	and all parent XXX.properties for replacements
	 * 
	 * @author vipup
	 * @param rulesUrl
	 * @param scriptIn
	 * @param refererTmp
	 * @return
	 */
	public String replaceByRules(String rulesUrl, String scriptIn, String refererTmp) {
		Properties props = getByURL(rulesUrl);
		String retval = scriptIn;
		Set<Object> keySet = props.keySet();
		String[] keys = keySet.toArray(new String[]{});
		if ( LServlet.TRACE ) putOrCreate(rulesUrl, props);

		for (String key:keys){
			String val = props.getProperty(key);
			retval = replaceLinks(retval, key, val );
			props.put(key, val); 
		}	
		if (null != refererTmp){
			String xrefTmp = HyperLinkUtil.decode(refererTmp.substring(refererTmp.indexOf("/aH")+1)); 
			xrefTmp = ""+xrefTmp;
			retval = retval.replace("__href__", xrefTmp);
		}		
		return retval;
	}


	public boolean isCollectParents() { 
			return collectParents;
	}


	public void setCollectParents(boolean collectParents) {
		this.collectParents = collectParents;
	}


	public String replaceByRules(String rulesUrl, String scriptIn) {
		return this.replaceByRules(rulesUrl, scriptIn, null);
	}
	
}


 