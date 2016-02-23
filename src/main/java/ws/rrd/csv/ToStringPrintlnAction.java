package ws.rrd.csv;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat; 

import cc.co.llabor.cache.Manager;

import net.sf.jsr107cache.Cache;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  27.04.2010::15:34:56<br> 
 */
public class ToStringPrintlnAction implements Action {
			StringBuffer sb =new StringBuffer();
			public String toString(){
				return sb.toString();
			}
 
			DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss.SSS");
			public ToStringPrintlnAction(){
				
			}
			
			public ToStringPrintlnAction(DateFormat dateFormat){
				this.sdf = dateFormat;
			}
			Registry reg = Registry.getInstance(); 
			@Override
			public Object perform(String xpath, String timestamp, String data) {
				try {
					long timestampTmp =  -1;
					try{
						timestampTmp = Long.parseLong( timestamp);
					}catch(NumberFormatException e){
						timestampTmp  =  sdf.parse(timestamp).getTime(); 
					}
					
					if (reg == null || reg.getPath2db() ==null || reg.getPath2db().get(xpath) == null){
						System_out_println( xpath + "-->"  );  
						String cmdCreateTmp = RrdUpdateAction.makeCreateCMD(timestampTmp, xpath) ;
						System_out_println( cmdCreateTmp  );
						// TODO ??? is it still active?
						reg = Registry.getInstance();
					}
					String  cmdUpdateTmp = RrdUpdateAction.makeUpdateCMD(data, timestampTmp, xpath) ; 
					System_out_println( cmdUpdateTmp  );
		
					return data;
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					//// e.  printStackTrace();
					return e;
				}
			}
			private void System_out_println(String cmdTmp) {
				//System.out.println(cmdTmp);
				sb.append(cmdTmp);
				sb.append("\n");
				
			}

			@Override
			public Object perform(String xpath, long timestamp, String data) { 
				return perform(xpath, ""+timestamp, data);
			}	
 
}


 