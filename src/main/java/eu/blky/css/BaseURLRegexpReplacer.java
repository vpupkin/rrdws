package eu.blky.css;

import java.util.Formatter;

public class BaseURLRegexpReplacer extends RegexpReplacer {
	public final static String URL_PATTERN = 	"[U|u][R|r][L|l]\\s*\\(\\s*([\\\'\\\"\\\u00b4\\`]?+)(.*?)\\1\\s*\\)";
	//"\\s*[U|u][R|r][L|l]\\s*\\(\\s*([\\\'\\\"\\\u00b4]?+)(.*?)\\1\\s*\\)";
	final String baseServerUrl;
	final String START_UNUQUE_ = "<!-- ***!!START_UNUQUE_ **** -->";
	final String END_UNUQUE_ = "<!-- ****!!!END_UNUQUE_*** -->";	

	public BaseURLRegexpReplacer(String format, String baseServerUrl) {
		super(format);
		this.baseServerUrl = baseServerUrl;
	}
	
	@Override
	public String format(String sURL, String sFormat) throws ReplacerException{  
		final Formatter f = new Formatter();
		StringBuffer retvalNewWithReplacements = new StringBuffer(); 
		int endindex = -1;int gebignindex = -1; 
		try{ 
			String markedURL = sURL.replaceAll(URL_PATTERN, START_UNUQUE_ + "$2" + END_UNUQUE_);
			
			int lastStart = 0;
			for(int startPos = markedURL.indexOf(START_UNUQUE_, lastStart); startPos>=0 && startPos<markedURL.length(); ){
				retvalNewWithReplacements .append(markedURL.substring(lastStart, startPos));
				endindex =  markedURL.indexOf(END_UNUQUE_, startPos);
				gebignindex = startPos + START_UNUQUE_.length();
				String DOLLAR_TWO_PARAM = markedURL.substring(gebignindex, endindex);
				DOLLAR_TWO_PARAM = DOLLAR_TWO_PARAM.trim();
				String fullURL = null;
				if (DOLLAR_TWO_PARAM.toLowerCase().startsWith("https://") || DOLLAR_TWO_PARAM.toLowerCase().startsWith("http://")){
					fullURL = DOLLAR_TWO_PARAM.replace("://", ">8-)"). replace("//", "/").replace(">8-)", "://");
				}else if(DOLLAR_TWO_PARAM.startsWith("/")){
					fullURL = baseServerUrl.replace("://", "8-)");
					int beginIndex = fullURL.indexOf("/");
					fullURL  = baseServerUrl.substring(0, beginIndex );
					fullURL  += DOLLAR_TWO_PARAM;
				}else{
					fullURL = baseServerUrl +""+DOLLAR_TWO_PARAM.replace("//", "/");
				}
				fullURL = cleanUP_SLASH_DOT_DOT_SLASH(fullURL);
				
				f.format(sFormat, fullURL);
				String cleanURL =f.toString(); 
				retvalNewWithReplacements.append(cleanURL);
				//System.out.println("{{{"+sURL+"}}}-->["+cleanURL+"]");
				lastStart = endindex;
				startPos = endindex + END_UNUQUE_.length();
			}// FOR 
			f.close(); 
			return retvalNewWithReplacements.toString() ;
		}catch(Exception e){
			throw new ReplacerException("Error in ["+sURL+":"+gebignindex+":"+endindex+":"+"]->"+retvalNewWithReplacements.toString(),e);
		}
	} 
	

}
