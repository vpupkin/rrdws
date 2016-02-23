package cc.co.llabor.threshold.rrd.update;
 
import java.io.IOException;
import java.util.Properties;

import org.jrobin.core.RrdException;

import cc.co.llabor.threshold.rrd.Threshold;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  30.08.2011::15:46:23<br> 
 */
public class DrivenRrdWriter extends RddUpdateAlerter implements Threshold {

	/**
	 * @author vipup
	 */
	private static final long serialVersionUID = 1424196870196989214L;
	private boolean isIncident;
	
	
	public boolean isIncident() { 
			return isIncident;
	}

	public void setIncident(boolean isIncident) {
		this.isIncident = isIncident;
	}

	public DrivenRrdWriter(Properties props){
		super(props);
	} 
	
	public DrivenRrdWriter(String rrdName, double baseLine,
			long activationTimeoutInSeconds) {
		super(rrdName, baseLine, activationTimeoutInSeconds);
		 
	}
	@Override
	public String getMonitorType() { 
			return "driven";
	}
 
		
	//  retval have to be setted externally
	@Override
	public boolean checkIncident(double val, long timestamp) {
		return isIncident;
	}
 
	@Override
	protected void act(long timestampSec) {
		try { 
			int lowLevel = isIncident
					? (ACTIVE_VALUE)
					: (this.inIncidentTime()>0?STAGE_VALUE:PASSIVE_VALUE) ;
			
			String valTmp = ""  +lowLevel;
			this.sample.setAndUpdate("" + (timestampSec) + ":" + valTmp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//  // e.  printStackTrace();
		} catch (RrdException e) {
			// TODO Auto-generated catch block
			//  // e.  printStackTrace();
		}
	} 

}


 