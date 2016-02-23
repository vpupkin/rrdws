package cc.co.llabor.threshold;

import java.util.Date;
import java.util.Properties; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import cc.co.llabor.threshold.rrd.Threshold;

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
public class Log4JActionist extends MVELActionist{ 
	/**
	 * @author vipup
	 */
	private static final long serialVersionUID = 3434669922946134100L;
	
	public static final String LOG4J = "log4j";



	public Log4JActionist(String string, String string2, long l) {
		super( string, string2, l); 
	}

	public Log4JActionist(Properties props) {
		super(props);
		init(props);
	}

	@Override
	protected void init(Properties props) 	{ 
		//super.init(props);
		try{
			this.rrdName = props.getProperty(Threshold.DATASOURCE);
			System.out.println("l4j::"+rrdName);	
		}catch (java.lang.NullPointerException e) {
			// TODO: handle exception
			//// e.  printStackTrace();
		}catch (Exception e) {
			// TODO: handle exception
			//// e.  printStackTrace();
		}
		this.dsName = "speed";
		this.action =  LOG4J;
		this.monitorType =  "mvel";
		this.actionArgs =  "hiLog4J @{}#{} {} ,{} "; // slf4j format-string
		
		try {
			rrdName = props.getProperty(Threshold.DATASOURCE);
		} catch (Exception e) {
			rrdName = "test.rrd";
		}
		try {
			monitorArgs = props.getProperty(Threshold.MONITOR_ARGS);
		} catch (Exception e) {
		}
		try {
			baseLine = Double.parseDouble(props
					.getProperty(Threshold.BASE_LINE));
		} catch (Exception e) {
		}
		try {
			activationTimeoutInSeconds = Integer.parseInt(props
					.getProperty(Threshold.SPAN_LENGTH));
		} catch (Exception e) {
		}
	}		

	protected void act(long timestampSec) {
		Logger logTmp = LoggerFactory.getLogger("D_LOG");
		logTmp.error( getActionArgs() , new Object[]{this ,   ""+timestampSec, new Date(timestampSec*1000)});
		//beep();
	}

	@Override
	public void performAction(long timestampSec) {
		act(timestampSec);
	}

	@Override
	public void performSleep(long timestamp) {
		 //
	}




}


 