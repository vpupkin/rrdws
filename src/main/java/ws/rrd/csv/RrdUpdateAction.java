package ws.rrd.csv;

import java.io.FileNotFoundException;
import java.io.IOException; 
import java.text.ParseException;
import java.text.SimpleDateFormat; 
import java.util.Date; 

import net.sf.jsr107cache.Cache; 
import org.jrobin.cmd.RrdCommander;
import org.jrobin.core.RrdException;

import cc.co.llabor.cache.Manager;
 

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  27.04.2010::15:33:35<br> 
 */
public class RrdUpdateAction implements Action {
	
		static RrdKeeper keeper = RrdKeeper.getInstance(); 

	    public static final String MM_DD_YYYY_HH_MM_SS_SSS = "MM/dd/yyyy HH:mm:ss.SSS";
		SimpleDateFormat sdf = new SimpleDateFormat(MM_DD_YYYY_HH_MM_SS_SSS);
		public RrdUpdateAction(){
			
		}
		public RrdUpdateAction(SimpleDateFormat sdf){
			this.sdf = sdf;
		}

		/**
		 * the only one method to direct call update RRD-DB.
		 * <br>
		 * make a lot of things:
		 *  <li> produce the rrdtool update-cmd
		 *  <li> process errors
		 *  <li> statistic
		 *    
		 * @param xpath : 'casd3ce1/java.lang/EClipse/Europa-ClassLoading/gauge/TotLoadedClasses'
		 * @param timestamp :55461778
		 * @param data :12345134534
		 * 
		 * 

		 */ 
		public Object perform(String xpath, long timestamp, String data) {
			Object retval = ""; 
			String cmdTmp = makeUpdateCMD(data, timestamp, xpath); 
			try {   
				keeper.update();
				retval  = RrdCommander.execute(cmdTmp );
				keeper.success();
				keeper.performNotification(xpath, timestamp, data);
			} catch (IOException e) { 
				keeper.warning();
				if (e instanceof FileNotFoundException){
					try {
						String cmdCreate = makeCreateCMD(timestamp, xpath);   
						System.out.println(xpath +" --->  "+cmdCreate); 
						retval  =RrdCommander.execute(cmdCreate);
						retval  =RrdCommander.execute(cmdTmp );
						keeper.create();
					} catch (IOException e1) { 
						//e1.printStackTrace();
						retval = e1;
					} catch (RrdException e1) { 
						//e1.printStackTrace();
						retval = e1;
					}
				}else{
					keeper.fatal();
				}
			} catch (RrdException e) { 
				keeper.error();
				//// e.  printStackTrace();
				retval = e; 
			} 			
			return retval;
		}
		/**
		 * do the same as perform(String xpath, long timestamp, String data) 
		 * just try to recognize the Timestamp, if possible
		 * 
		 */
		public Object perform(String xpath, String timestamp, String data) {
			Object retval = "";			
			long timestampTmp = 0;
			try {
				try{
					timestampTmp = Long.parseLong(timestamp);
				}catch(NumberFormatException e){
					Date parseVal = sdf.parse(timestamp);
					timestampTmp = parseVal.getTime();
				}
				synchronized (RrdUpdateAction.class) {  
					retval= perform(xpath, timestampTmp, data); 
				}
			} catch (ParseException e1) { 
				//e1.printStackTrace();
				retval = e1;
			} catch (java.lang.NumberFormatException e) {
				retval = e;
			}

			return retval;
		}

		public static final String makeCreateCMD(long timestampTmp, String xpath) {
			String rrddb = xpath2Hash(xpath);
			String cmdCreate = "rrdtool create " +
				""+rrddb+" --start "+(((timestampTmp-10000)/1000L))+"" + 
				" --step 60 " +
				"				DS:data:GAUGE:240:U:U " +
				"				RRA:AVERAGE:0.5:3:480 " +
				"				RRA:AVERAGE:0.5:17:592 " +
				"				RRA:AVERAGE:0.5:131:340 " +
				"				RRA:AVERAGE:0.5:731:719 " +
				"				RRA:AVERAGE:0.5:10000:273 " +
				"				RRA:MAX:0.5:3:480 " +
				"				RRA:MAX:0.5:17:592 " +
				"				RRA:MAX:0.5:131:340 " +
				"				RRA:MAX:0.5:731:719 " +
				"				RRA:MAX:0.5:10000:273 " +
				"				RRA:MIN:0.5:3:480 " +
				"				RRA:MIN:0.5:17:592 " +
				"				RRA:MIN:0.5:131:340 " +
				"				RRA:MIN:0.5:731:719 " +
				"				RRA:MIN:0.5:10000:273 " +
												" "; 
			return cmdCreate;
		}

	    private static volatile long last_clean = 0;
	    
	    private static Registry reg = Registry.getInstance(); 
	    	
	    static int flushCount=0;
	    static int changeCount=0;
		private static void checkReg(String rrddb, String xpath  ) {

			
			if ( flushCount == 0 || changeCount >10 ||(last_clean +10000) < System.currentTimeMillis()){
				synchronized (Registry.class) { 
					reg.flush();
					last_clean=System.currentTimeMillis();					
					System.out.println("REGISTRY Flush #"+flushCount+++":"+changeCount);
					changeCount =0;
				}
			}
			if (    reg != null && 
					reg.getDb2path()!=null && 
					reg.getDb2path().get(rrddb)!=null && 
					reg.getDb2path().get(rrddb).equals(xpath) ){
				//nothing to do
				return;
			}  
			reg.register(rrddb,xpath);
			changeCount++;
				
		}
 
		/**
		 * BE CAREFULL WITH reimpelmentation THIS METHOD! The risk is to look all exisitng RRD-Databases
		 * 
		 * @author vipup
		 * @param xpath
		 * @return
		 */
		public static final String xpath2Hash(String xpath) {
			String rrddb = "X"+xpath.hashCode()+".rrd";
			checkReg(rrddb, xpath);
			return rrddb;
		}

		public final static String makeUpdateCMD(String data, long timestampTmp, String xpath) {
			String rrddb = xpath2Hash(xpath);
			String cmdTmp = "rrdtool update "+rrddb +" "+(timestampTmp/1000L) +":"+ data;
			return cmdTmp;
		}
 

}


 