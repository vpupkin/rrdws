package cc.co.llabor.threshold;

import java.util.Properties;

import cc.co.llabor.threshold.rrd.Threshold; 
import cc.co.llabor.threshold.rrd.update.HighAlerter; 
/** 
 * <b>combine Log- and RRDUpdate actionist</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  02.11.2011::22:26:58<br> 
 */
public class RedAndLogActionist extends CompositeAlerter {
 
	/**
	 * @author vipup
	 */
	private static final long serialVersionUID = -7607540912607278569L;
	
	public RedAndLogActionist(Properties props ) {
		init(props);
	}
	/**
	 * hardcoded Constructor for make composite Thresould A, which delegate activity to two sub-Actionists:
	 * 
	 * - HighAlerter
	 * - Log4JActionist (the class of second logger-pointed actionist comes from param)
	 * 
	 * @author vipup
	 * @param clPar
	 * @param rrdName
	 * @param monitorArgs
	 * @param notificationInterval
	 */
	public RedAndLogActionist( Class clPar,  String rrdName, String monitorArgs, 	int notificationInterval) {
		super( );
		dump2Cache(  rrdName); 		
		String chTmp =storeRRDWRITER(  rrdName);		
		Properties props = new Properties();
		props.put( Threshold.DS_NAME , "data" ) ;		
		props.put( Threshold.MONITOR_ARGS , monitorArgs ) ;		
		props.put( Threshold.SPAN_LENGTH , ""+notificationInterval ) ;
		chTmp+=",";
		chTmp+=storeLOG2GCAL(  rrdName, clPar ,props);
		
		
		Properties myProps = new Properties();
		myProps .put("childs", chTmp);
		myProps .put(Threshold.DATASOURCE, rrdName);
		init(myProps);		
	} 
	@Override
	public Properties toProperties() {
		Properties retval = super.toProperties();
		String value ="";
		for (String child:this.aList){
			value = value.length() ==0?"":value + ",";;
			value += child; 
		}
		//this.aList =  props.getProperty("childs").split(",");
		retval .put("childs", value );
		return retval ;
	}
	
	public static String dump2Cache(String rrdName){
		String retval = rrdName; 
		
		//props4RRDWRITER
		String n1 = storeRRDWRITER( rrdName );
		
		//redalert.props4LOG2GCAL" 
		String n2 = storeLOG2GCAL(rrdName );		 
		
		//redalert.RA" 
		Properties props = new Properties();
		props.put( Threshold.DS_NAME , "data") ;
		
		props.put( "childs", n1+","+n2) ;
		props.put( Threshold.CLASS, RedAndLogActionist.class.getName()) ;
 		try {
 			AlertCaptain.storeToName(retval, props);
		} catch (TholdException e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
		}
		return retval;
	}

 
	public static String storeLOG2GCAL(String rrdName ) {
		return storeLOG2GCAL(  rrdName, Log4JActionist.class);
	}
 
	public static String storeLOG2GCAL(String rrdName, Class clPar) {
		Properties props4log2gcal = new Properties();
		return storeLOG2GCAL(  rrdName,Log4JActionist.class, props4log2gcal);
	}
 
	public static String storeLOG2GCAL(  String rrdNamePar, Class clPar, Properties props4LOG2GCAL ) {
		String namePar = clPar.getCanonicalName();
		String accu = "";
		String theLast = "LeaSt";
		for (String theC :namePar.replace(".", ",").split(",")){
			theLast = theC;
			accu+=theC.substring(0,1).toUpperCase();
		}
		namePar = accu;
		accu = "";
// 		Log4JActionist -> AJL4		
//		String abcTmp = "abcdefghijklmnoprstuvwxwz1234567890".toUpperCase();
//		for (int i=0;i<abcTmp .length();i++){
//			String theC = abcTmp .substring(i,i+1);
//			if(theLast.indexOf(theC)>=0){
//				accu+=theC;
//			}
//			
//		}
//		namePar+=accu;

		
// Log4JActionist ->L4JA		
		accu = "";
		String abcTmp = "abcdefghijklmnoprstuvwxwxyz1234567890".toUpperCase();
		for (int i=0;i<theLast .length();i++){
			String theC = theLast.substring(i,i+1);
			if(abcTmp.indexOf(theC)>=0){
				accu+=theC;
			}
			
		}		
		namePar+=accu;
		namePar +="@"; 
			namePar +=rrdNamePar; 
		props4LOG2GCAL.put( Threshold.CLASS, clPar.getName()) ;
		Threshold l4Tmp;
		try {
			l4Tmp = AlertCaptain.toThreshold(props4LOG2GCAL);  
			AlertCaptain.storeToName(namePar , l4Tmp);
		} catch (TholdException e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
		}
		return namePar;
	}
 
	
	public static String storeRRDWRITER( String rrdName) {
		String retval = "rrw@"+rrdName;//"redalert.RRDWRITER";//"redalert.RRDWRITER"
		Properties props4RRDWRITER =   new Properties();
		props4RRDWRITER.put( Threshold.DS_NAME , "speed") ;
		props4RRDWRITER.put( Threshold.DATASOURCE , rrdName) ;
		props4RRDWRITER.put(Threshold.SPAN_LENGTH , "600" ) ;
		props4RRDWRITER.put(Threshold.BASE_LINE , "130" ) ;
		props4RRDWRITER.put( Threshold.CLASS, HighAlerter.class.getName()) ;
		Threshold rwTmp;
		try {
			rwTmp = AlertCaptain.toThreshold(props4RRDWRITER); 
			
			AlertCaptain .storeToName(retval, rwTmp);
		} catch (TholdException e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
		}
		return retval;
	} 
	protected void init(Properties props) {
		System.out.println(props);
		super.init(props);
		try{
			this.aList =  props.getProperty("childs").split(",");
			for (String namePar:this.aList){
				
				Threshold theNext;
				try {
					theNext = AlertCaptain.restoreByName(namePar);
					this.rrdName = this.rrdName ==null? theNext.getDatasource():this.rrdName;
					this.chainOfAlerters.add(theNext);
					AlertCaptain instance = AlertCaptain.getInstance();
					instance.register( theNext );
				} catch (TholdException e) {
					// TODO Auto-generated catch block
					// e.  printStackTrace();
				} 
			}
		}catch(Exception e){}
	}
}


 