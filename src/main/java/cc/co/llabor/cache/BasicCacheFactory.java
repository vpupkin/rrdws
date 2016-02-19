package cc.co.llabor.cache;

import java.util.Map;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;

/** 
 * <b>Description: default File-based cache-implementation 
 * (= who said that file-system slower as RAM =)."</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  14.04.2010::14:38:10<br> 
 */
public class BasicCacheFactory implements CacheFactory {

	
	public Cache createCache(Map arg0) throws CacheException {
		return new FileCache(arg0);
	}

}


 