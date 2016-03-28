package eu.blky.css;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegexpReplacer {

	   private String format;
	   
	   public RegexpReplacer(String format){
		   this.format = format;
	   }

	   /**
	    * method will be called for any matched - simplest implementation for 1 match-parameter
	    * 
	    * ! U R welcome to override it. !
	    * 
	    * @param sMatchReplacement - input from Matcher-iterator
	    * @param fooPar - java.util.Formatter - confirm format of message with _ONLY_ONE_PARAM 
	    * as demonstration of custom behavior of replacement process.
	    * 
	    * @return formated by pattern __foormatPar__ input string __sMatchReplacement__
	    * @throws ReplacerException
	    */
	   public String format(String sMatchReplacement, String foormatPar) throws ReplacerException { 
		   Formatter f = new Formatter();
		   try{
			   f.format(foormatPar, sMatchReplacement);
			   String retval = f.toString();
			   f.close(); 
			   return  retval;
		   }catch(Exception e){
			   throw new ReplacerException("error by executing format( ["+sMatchReplacement+"],["+ foormatPar+ "] ", e);
		   }
		}

	    /**
	     * replace all occurrences  of *regexpPar* in  *inPar* to formatted by format( {0} ) over  _own_format_from_constructor_
	     * 
	     * @param regexpPar
	     * @param inPar
	     * @return
	     * @throws ReplacerException 
	     */
	    public String replaceAll(String regexpPar, String inPar) throws ReplacerException {
	    	return replaceAll(regexpPar, this.format,  inPar);
	    }
	    
	    /**
	     * replace all occurrences  of *regexpPar* in  *inPar* to formatted by format( {0} ) over  *formatPar*
	     * 
	     * @param regexpPar
	     * @param inPar
	     * @return
	     * @throws ReplacerException 
	     */	    
	    public String replaceAll(String regexpPar, String formatPar, String inPar) throws ReplacerException {
	    	try{
		        String content = ""+inPar;
		        Pattern pattern = Pattern.compile(regexpPar);
		        Matcher m = pattern.matcher(content);
		        StringBuffer sb = new StringBuffer();
	
		        while (m.find()){
		            String groupTmp = m.group();
					String formatedTmp = format(groupTmp, formatPar);
					m.appendReplacement(sb, formatedTmp);
		        }
	
		        m.appendTail(sb);
		        
		        return sb.toString();
		        
		   }catch(Exception e){
			   throw new ReplacerException("error by executing replaceAll( ["+regexpPar+"],["+formatPar+"],["+ inPar+ "] ", e);
		   }	        
	    }	
	
	    /**
	     * translate any garbage-URL into "../"-free  
	     * 
	     * 
	     * @param fullURL
	     * @return
	     */
	    protected static final String cleanUP_SLASH_DOT_DOT_SLASH(String fullURL) {
			final String SLASH_DOT_DOT_SLASH ="/../";
			try {
				URL urlTmp = new URL(fullURL);
				String serverBase = urlTmp.getProtocol()+"://"+urlTmp.getHost()+"/";
				for (int pos = fullURL.indexOf(SLASH_DOT_DOT_SLASH);pos>0;pos = fullURL.indexOf(SLASH_DOT_DOT_SLASH)){
						int beginSDDS = 0;
						int endSDDS = fullURL.lastIndexOf("/", fullURL.lastIndexOf("/", pos)-1);
						String newURL = fullURL.substring(beginSDDS, endSDDS) + "/" + fullURL.substring(fullURL.lastIndexOf("/", pos) + SLASH_DOT_DOT_SLASH .length());
						if (newURL.contains(serverBase)){ // still on the same server
							fullURL = newURL;
						}else{ // cleanup all sub..sub..sub..dir-refs
							fullURL = fullURL.replaceAll("/../", "/");
							fullURL = fullURL.replaceAll("/../", "/");
							fullURL = fullURL.replaceAll("/../", "/");
							fullURL = fullURL.replaceAll("/../", "/");
							fullURL = fullURL.replaceAll("/../", "/");
							break;
						}
				}
			} catch (MalformedURLException e) { 
			}
				
			return fullURL;
		}	    
}
