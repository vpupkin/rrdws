package cc.co.llabor.threshold.syso;

import java.util.Date;
import java.util.Properties;

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
public class StdErrActionist extends StdOutActionist{ 
	/**
	 * @author vipup
	 */
	private static final long serialVersionUID = -6306830921722082168L;
 
 
	public StdErrActionist(String rrdName, String string2, long tenMins) {
		super(rrdName, string2, tenMins);
	}

	public StdErrActionist(Properties props) {
		super(props);
		init(props); 
	}

	@Override
	protected void act(long timestampSec) { 
				System.err.println(actionArgs +"N" +"Z"+new Date(timestampSec*1000));
				//beep();
	}

}


 