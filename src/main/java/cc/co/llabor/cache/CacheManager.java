package cc.co.llabor.cache;
/** 
 * <b>Fantom to be compatible with javax.cache.API </b>
 * @author      gennady<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  Mar 31, 2011::9:03:48 PM<br> 
 */
public class CacheManager extends Manager {

	static CacheManager theMyself = new CacheManager (); 
	public static CacheManager getInstance() {
		return theMyself;
	}

}


 