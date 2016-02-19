package cc.co.llabor.cache;

import java.io.Serializable;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  26.04.2010::15:30:03<br> 
 */
public class Blob implements Serializable{

	/**
	 * @author vipup
	 */
	private static final long serialVersionUID = -7485980446361287390L;
	byte[] bytes ;
	public Blob(byte[] bs) {
		this.bytes= bs;
	}
	byte[] getBytes(){
		return bytes;
	}
 

}


 