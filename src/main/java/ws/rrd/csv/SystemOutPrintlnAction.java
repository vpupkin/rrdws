package ws.rrd.csv;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  27.04.2010::15:34:56<br> 
 */
public class SystemOutPrintlnAction implements Action {
 
			
			public static final SimpleDateFormat SDF = new SimpleDateFormat(RrdUpdateAction.MM_DD_YYYY_HH_MM_SS_SSS);
			@Override
			public Object perform(String xpath, String timestamp, String data) {
				try {
					long timestampTmp =  SDF.parse(timestamp).getTime();
					Object cmdTmp =perform( xpath,   timestampTmp,  data);
					return cmdTmp;
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					// e.  printStackTrace();
					return e;
				}
			}
			@Override
			public Object perform(String xpath, long timestamp, String data) {
				
				String cmdTmp = "rrdtool update \""+xpath+" .rdd\" "+(timestamp/1000L) +":"+ data;
				System.out.println( cmdTmp  );
				return cmdTmp;
			}	
 
}


 