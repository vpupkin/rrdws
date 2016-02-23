package cc.co.llabor.props;

import java.io.*;
import java.util.*;
/** 
 * <b> 
 * The CommentedProperties class is an extension of java.util.Properties
 * to allow retention of comment lines and blank (whitespace only) lines
 * in the properties file.
 * 
 * </b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  17.02.2012::10:02:59<br> 
 */
public class CommentedProperties extends java.util.Properties {

	/**
	 * @author vipup
	 */
	private static final long serialVersionUID = 9183011519738049548L;

	private static final String STD_ENCODING = Messages.getString("CommentedProperties.0"); //$NON-NLS-1$

	private static final String EOL = Messages.getString("CommentedProperties.1"); //$NON-NLS-1$

	/**
	 * parallel comments-properties-array
	 */
	java.util.Properties  comments = new Properties();

	private int linecount;

	private String title;

	public CommentedProperties(String sTitle) {
		this.setTitle(sTitle);
	}
	public CommentedProperties() {
		title = null;
	}
	/**
	 * Load properties from the specified InputStream. 
	 * Overload the load method in Properties so we can keep comment and blank lines.
	 * @param   inStream   The InputStream to read.
	 */
	public void load(InputStream inStream) throws IOException
	{
		// The spec says that the file must be encoded using ISO-8859-1.
		BufferedReader inBuf =
		new BufferedReader(new InputStreamReader(inStream, STD_ENCODING));
		StringWithComments line;
		try{
			while ((line = readln(inBuf)) != null) {
				String key_val []= line.getLine().replace(Messages.getString("CommentedProperties.2"), Messages.getString("CommentedProperties.3")).replace(Messages.getString("CommentedProperties.4"), Messages.getString("CommentedProperties.5")).replace(Messages.getString("CommentedProperties.6"), Messages.getString("CommentedProperties.7")).replace(Messages.getString("CommentedProperties.8"), Messages.getString("CommentedProperties.9")).replace(Messages.getString("CommentedProperties.10"), Messages.getString("CommentedProperties.11")).replace(Messages.getString("CommentedProperties.12"), Messages.getString("CommentedProperties.13")).replace(Messages.getString("CommentedProperties.14"), Messages.getString("CommentedProperties.15")).replace(Messages.getString("CommentedProperties.16"), Messages.getString("CommentedProperties.17")).replace(Messages.getString("CommentedProperties.18"), Messages.getString("CommentedProperties.19")).split(Messages.getString("CommentedProperties.20")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$ //$NON-NLS-18$ //$NON-NLS-19$
				String keyTmp;
				String valTmp;
				try{
					keyTmp = key_val [0];
					valTmp= line.getLine();
					valTmp = valTmp.substring(keyTmp.length() +1).trim();
				}catch(ArrayIndexOutOfBoundsException e){
					continue;
				}catch(StringIndexOutOfBoundsException e){
					continue;
				}
				String comments2 = line.getComments();
				this.comments.put(keyTmp, comments2);
				this.put(keyTmp, valTmp); 
			}
		}catch(Exception e){
			// e.  printStackTrace();
		}

	}
	/**
	 * return trimmed, uncommented, non-empty line of CFG-File
	 * @author vipup
	 * @param inBuf 
	 * @return
	 * @throws IOException
	 */
	private StringWithComments readln(BufferedReader inBuf) throws IOException {
		String retval = inBuf.readLine();
		
		linecount++;
		String skipped = Messages.getString("CommentedProperties.21"); //$NON-NLS-1$
		String prefix = Messages.getString("CommentedProperties.22"); //$NON-NLS-1$
		try{
			retval = retval.trim();
			// #
			while (retval.startsWith(Messages.getString("CommentedProperties.23"))||Messages.getString("CommentedProperties.24").equals( retval) ){ //$NON-NLS-1$ //$NON-NLS-2$
				skipped += prefix;
				skipped += retval;
				prefix += EOL;
				retval = inBuf.readLine();
				retval = retval.trim();				
			}
			
		}catch(NullPointerException e){
			//throw new IOException("EOF at line "+linecount);
			return null ;
		} 
		return new StringWithComments(retval, skipped);
	}
	/**
	 * Write the properties to the specified OutputStream.
	 * 
	 * Overloads the store method in Properties so we can put back comment	
	 * and blank lines.													  
	 * 
	 * @param out	The OutputStream to write to.
	 * @param header Ignored, here for compatability w/ Properties.
	 * 
	 * @exception IOException
	 */
	public void store(OutputStream out, String header) throws IOException
	{
		// The spec says that the file must be encoded using ISO-8859-1.
		PrintWriter pw
		= new PrintWriter(new OutputStreamWriter(out, STD_ENCODING));
		if (title!=null){
			String titleTmp = this.title;
			titleTmp = translit(titleTmp);
			pw.println((Messages.getString("CommentedProperties.25")+titleTmp).replace(Messages.getString("CommentedProperties.26"), Messages.getString("CommentedProperties.27"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		for(String key:this.keySet().toArray(new String[]{})){
			String commentTmp = comments.getProperty(key);
			if (commentTmp!=null){
				pw.println(translit(commentTmp));
			}
			pw.println(key+Messages.getString("CommentedProperties.28")+this.getProperty(key)); //$NON-NLS-1$
		} 
		pw.flush ();
	}
	public String translit(String titleTmp) {
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.29").toUpperCase()), Messages.getString("CommentedProperties.30").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.31").toUpperCase()), Messages.getString("CommentedProperties.32").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.33").toUpperCase()), Messages.getString("CommentedProperties.34").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.35").toUpperCase()), Messages.getString("CommentedProperties.36").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.37").toUpperCase()), Messages.getString("CommentedProperties.38").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.39").toUpperCase()), Messages.getString("CommentedProperties.40").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.41").toUpperCase()), Messages.getString("CommentedProperties.42").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.43").toUpperCase()), Messages.getString("CommentedProperties.44").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.45").toUpperCase()), Messages.getString("CommentedProperties.46").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.47").toUpperCase()), Messages.getString("CommentedProperties.48").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.49").toUpperCase()), Messages.getString("CommentedProperties.50").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.51").toUpperCase()), Messages.getString("CommentedProperties.52").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.53").toUpperCase()), Messages.getString("CommentedProperties.54").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.55").toUpperCase()), Messages.getString("CommentedProperties.56").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.57").toUpperCase()), Messages.getString("CommentedProperties.58").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.59").toUpperCase()), Messages.getString("CommentedProperties.60").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.61").toUpperCase()), Messages.getString("CommentedProperties.62").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.63").toUpperCase()), Messages.getString("CommentedProperties.64").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.65").toUpperCase()), Messages.getString("CommentedProperties.66").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.67").toUpperCase()), Messages.getString("CommentedProperties.68").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.69").toUpperCase()), Messages.getString("CommentedProperties.70").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.71").toUpperCase()), Messages.getString("CommentedProperties.72").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.73").toUpperCase()), Messages.getString("CommentedProperties.74").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.75").toUpperCase()), Messages.getString("CommentedProperties.76").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.77").toUpperCase()), Messages.getString("CommentedProperties.78").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.79").toUpperCase()), Messages.getString("CommentedProperties.80").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.81").toUpperCase()), Messages.getString("CommentedProperties.82").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.83").toUpperCase()), Messages.getString("CommentedProperties.84").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.85").toUpperCase()), Messages.getString("CommentedProperties.86").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.87").toUpperCase()), Messages.getString("CommentedProperties.88").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.89").toUpperCase()), Messages.getString("CommentedProperties.90").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.91").toUpperCase()), Messages.getString("CommentedProperties.92").toUpperCase()); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.93").toUpperCase()), Messages.getString("CommentedProperties.94").toUpperCase());  //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.95")), Messages.getString("CommentedProperties.96")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.97")), Messages.getString("CommentedProperties.98")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.99")), Messages.getString("CommentedProperties.100")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.101")), Messages.getString("CommentedProperties.102")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.103")), Messages.getString("CommentedProperties.104")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.105")), Messages.getString("CommentedProperties.106")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.107")), Messages.getString("CommentedProperties.108")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.109")), Messages.getString("CommentedProperties.110")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.111")), Messages.getString("CommentedProperties.112")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.113")), Messages.getString("CommentedProperties.114")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.115")), Messages.getString("CommentedProperties.116")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.117")), Messages.getString("CommentedProperties.118")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.119")), Messages.getString("CommentedProperties.120")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.121")), Messages.getString("CommentedProperties.122")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.123")), Messages.getString("CommentedProperties.124")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.125")), Messages.getString("CommentedProperties.126")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.127")), Messages.getString("CommentedProperties.128")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.129")), Messages.getString("CommentedProperties.130")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.131")), Messages.getString("CommentedProperties.132")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.133")), Messages.getString("CommentedProperties.134")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.135")), Messages.getString("CommentedProperties.136")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.137")), Messages.getString("CommentedProperties.138")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.139")), Messages.getString("CommentedProperties.140")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.141")), Messages.getString("CommentedProperties.142")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.143")), Messages.getString("CommentedProperties.144")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.145")), Messages.getString("CommentedProperties.146")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.147")), Messages.getString("CommentedProperties.148")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.149")), Messages.getString("CommentedProperties.150")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.151")), Messages.getString("CommentedProperties.152")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.153")), Messages.getString("CommentedProperties.154")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.155")), Messages.getString("CommentedProperties.156")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.157")), Messages.getString("CommentedProperties.158")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.159")), Messages.getString("CommentedProperties.160")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.161")), Messages.getString("CommentedProperties.162")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.163")), Messages.getString("CommentedProperties.164")); //$NON-NLS-1$ //$NON-NLS-2$
		titleTmp = titleTmp.replace((Messages.getString("CommentedProperties.165")), Messages.getString("CommentedProperties.166")); //$NON-NLS-1$ //$NON-NLS-2$
		return titleTmp;
	}
  
	@Override
	public Object setProperty(String key, String value){
		return super.setProperty(key, value); 
	}
	
	public Object setProperty(String key, String value, String comment){
		this.comments.put(key, comment.trim());
		return super.setProperty(key, value); 
	}
	
	/**
	 * Return comment, associated with property
	 * @author vipup
	 * @param string
	 * @return
	 */
	public String getPropertyComment(String key) { 
		return comments.getProperty(key); 
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle() { 
		return title;
	}
	
}


 