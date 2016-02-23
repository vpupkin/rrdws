package cc.co.llabor.threshold.rrd.update;

import java.io.IOException;
import java.util.Properties;

import org.jrobin.core.ConsolFuns;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;
import org.jrobin.core.Sample;

import cc.co.llabor.threshold.AbstractActionist; 
import cc.co.llabor.threshold.rrd.Threshold;

/**
 * <b>Hardcoded RRD-linked Alerter, which will controll the RRD-DB directly
 * </b>
 * 
 * for any monitored RRD-sensor will be created associated
 * RRD-DB with CF_AVERAGE,CF_MAX and CF_MIN counters.
 * Depend on the rules AlertCaptain will organize Observer<->Observable mechanism for any defined pair.
 * 
 * 
 * @author vipup<br>
 * <br>
 *         <b>Copyright:</b> Copyright (c) 2006-2008 Monster AG <br>
 *         <b>Company:</b> Monster AG <br>
 * 
 *         Creation: 01.09.2011::16:48:15<br>
 */
public abstract class RddUpdateAlerter extends AbstractActionist {
	/**
	 * @author vipup
	 */
	private static final long serialVersionUID = 6260786617371803021L;

	protected Sample sample;

	private RrdDb rrdDb;

	public RrdDb getRrdDb() { 
			return rrdDb;
	}
	
 	

	public void setRrdDb(RrdDb rrdDb) {
		this.rrdDb = rrdDb;
	}

	private void init(String rrdPar) throws RrdException, IOException {
		RrdDef rrdDef = new RrdDef(rrdPar);
		long startTime = 920800000L; // 920800000L == [Sun Mar 07 10:46:40 CET 1999]
		rrdDef.setStartTime(startTime);
		rrdDef.setStep(1);
		rrdDef.addDatasource("speed", "GAUGE", 600, 0.0, 1111110.0);
		rrdDef.addArchive(ConsolFuns.CF_AVERAGE, 0.1, 1, 3600);
		rrdDef.addArchive(ConsolFuns.CF_AVERAGE, 0.5, 6, 700);
		rrdDef.addArchive(ConsolFuns.CF_AVERAGE, 0.5, 24, 797);
		rrdDef.addArchive(ConsolFuns.CF_AVERAGE, 0.5, 288, 775);
		rrdDef.addArchive(ConsolFuns.CF_MAX, 0.1, 1, 3600);
		rrdDef.addArchive(ConsolFuns.CF_MAX, 0.5, 6, 700);
		rrdDef.addArchive(ConsolFuns.CF_MAX, 0.5, 24, 797);
		rrdDef.addArchive(ConsolFuns.CF_MAX, 0.5, 288, 775);
		rrdDef.addArchive(ConsolFuns.CF_MIN, 0.1, 1, 3600);
		rrdDef.addArchive(ConsolFuns.CF_MIN, 0.5, 6, 700);
		rrdDef.addArchive(ConsolFuns.CF_MIN, 0.5, 24, 797);
		rrdDef.addArchive(ConsolFuns.CF_MIN, 0.5, 288, 775);
		rrdDb = new RrdDb(rrdDef);
		
		sample = rrdDb.createSample();

	}

	
	public RddUpdateAlerter(Properties props){
		super(props);
	}
	
	public RddUpdateAlerter(String rrdName, double baseLine, long activationTimeoutInSeconds) {
		this.rrdName = rrdName;
		this.monitorType = "mvel";
		this.baseLine = baseLine;
		this.activationTimeoutInSeconds = activationTimeoutInSeconds;
		initDB();
	}



	public void initDB() {
		try {
			init(this.rrdName + ".Thold.RRD");
		} catch (RrdException e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
		}
	}
	
	@Override
	protected void init(Properties props) {
		try{
			this.action =props.getProperty(Threshold.ACTION ) ;
		}catch(Exception e){}try{
			this.actionArgs =props.getProperty(Threshold.ACTION_ARGS ) ;
		}catch(Exception e){}try{
			this.rrdName =props.getProperty(Threshold.DATASOURCE ) ;
		}catch(Exception e){}try{
			this.dsName =props.getProperty(Threshold.DS_NAME ) ;
		}catch(Exception e){}try{
			this.monitorType =props.getProperty(Threshold.MONITOR_TYPE ) ;
		}catch(Exception e){}try{
			this.monitorArgs =props.getProperty(Threshold.MONITOR_ARGS ) ;
		}catch(Exception e){}try{
			this.activationTimeoutInSeconds = Integer.parseInt( props.getProperty(Threshold.SPAN_LENGTH  ));
		}catch(Exception e){}try{
			this.baseLine = Double.parseDouble(  props.getProperty(Threshold.BASE_LINE ));
		}catch(Exception e){}
		
		initDB();
	} 
	 
	protected void act(long timestampSec) {
		try { 
			int lowLevel = isInIncident(timestampSec)
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
	public static int ACTIVE_VALUE = 100;
	public static int STAGE_VALUE = 255;
	public static int PASSIVE_VALUE = 0;
	/**
	 * fully override the super.perform to avoid the parent-behavior 
	 * for acting with only "inIncident" state
	 */
 	@Override
	public void performAction(long timestampSec) {  
			act(timestampSec); 
	}		
	@Override
	public void performSleep(long timestampSec) {
		act( timestampSec) ;
	}

	@Override
	public void stop() { 
		if (rrdDb!=null)
		try {
			rrdDb.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
		}
	}
	


}

