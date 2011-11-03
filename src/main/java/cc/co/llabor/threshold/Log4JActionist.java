package cc.co.llabor.threshold;

import java.util.Date;
import java.util.Properties; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * <b>Do almost the same as StdOutActionist, except the OUT.</b>
 * demonstration how to redefine the functionality 
 * 
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  04.10.2011::13:43:09<br> 
 */
public class Log4JActionist extends AbstractActionist{ 
	/**
	 * @author vipup
	 */
	private static final long serialVersionUID = 3434669922946134100L; 
	public static final String LOG4J = "log4j"; 
	{ 
		this.dsName = "speed";
		this.action =  LOG4J;
		this.actionArgs =  "hiLog4J @{}#{} {} ,{} "; // slf4j format-string
	}	
	
	public Log4JActionist(String rrdName, String monitorArgs, long notificationInterval) {
		super(rrdName, monitorArgs, notificationInterval);		 
	}
	public Log4JActionist(Properties props){
		super(props);
	}   
 
	
	protected void act(long timestampSec) {
		Logger logTmp = LoggerFactory.getLogger("D_LOG");
		logTmp.error( getActionArgs() , new Object[]{this ,  ""+notificationCounter++, ""+timestampSec, new Date(timestampSec*1000)});
	}


}


 