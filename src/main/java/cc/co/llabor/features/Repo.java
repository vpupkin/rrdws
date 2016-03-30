package cc.co.llabor.features;

import java.io.BufferedInputStream; 
import java.io.IOException;
import java.io.InputStream; 

/** 
 * <b>Provide bundle of resource for any feature, defined in the system</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  06.07.2011::13:25:11<br> 
 */
public class Repo {
	public static final String BASE = "cc/co/llabor/features/";
	/**
	 * produce feature-banner from <feature>.dat file. will be probably showed/printed when the feature is activated first time by app-start.
	 * 
	 * 
	 * @author vipup
	 * @param namePar
	 * @return
	 */
	public static String getBanner(String namePar){
		String retval = ";)";
		try {
			ClassLoader classLoader = Repo.class.getClassLoader();
			
			String namaTmp = BASE+namePar+".dat";
			InputStream resourceAsStream = classLoader.getResourceAsStream(namaTmp); 
			BufferedInputStream in = new BufferedInputStream ( resourceAsStream);
			byte[] buf = null;
			buf = new byte[in.available()];
			in.read(buf);
			retval = new String(buf);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
			retval = ("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ"+namePar+"|||||||||||||||||||||||||||||||||||||||||") ;
		}
		
		return retval;
	}
}


 