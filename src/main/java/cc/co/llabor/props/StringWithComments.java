package cc.co.llabor.props;
/** 
 * <b>represent conf-line with associated comments in form
 *  </b>
 *  <pre>
 *  #CacheEntry stored at 1329469067065
#Fri Feb 17 09:57:47 CET 2012
notification_options=w,u,c,r
...
max_check_attempts=3
service_description=HTTP
retry_check_interval=1
#2min
notification_interval=120
passive_checks_enabled=1
 *  </pre>
 *  Any commended line will be able to be loaded/stored from/in java-standart properties file. 
 *  
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  17.02.2012::10:22:31<br> 
 */
public class StringWithComments {

	private String comments;
	private String line;

	public StringWithComments(String line, String comments) {
		this.setLine(line);
		this.setComments(comments);
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getComments() { 
		return comments;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getLine() { 
		return line;
	}

}


 