package org.jrobin.svg.awt;
/** 
 * <b>Description:TODO</b>
 * @author      xco5015<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 OXSEED AG <br>
 * <b>Company:</b>       OXSEED AG  <br>
 * 
 * Creation:  31.03.2010::13:41:14<br> 
 */
public class PathIterator {

	private double[] Y;

	public PathIterator(double[] Y) {
		this.Y = Y;
	}

	public int[] getNextPath() {
		return new int[]{1,2,3,4,5};
	}

}


 