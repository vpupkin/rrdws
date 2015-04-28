package ws.rrd.csv;

import java.io.Serializable;
import java.util.Collections; 
import java.util.Map;
import java.util.TreeMap;

import net.sf.jsr107cache.Cache;
import cc.co.llabor.cache.Manager;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  29.04.2010::17:28:52<br> 
 */
public class Registry implements Serializable{
	
	private static final String REGISTRY_CACHE_NAME = "REGISTRY";
	static long lastInitTimeStamp = System.currentTimeMillis();
	
	/**
	 * !call it whenever Registry modified somehow externally!
	 * 
	 * @author vipup
	 */
	public static void reset(){
		lastInitTimeStamp = System.currentTimeMillis();
	}
	private long initedAt;
	{
		initedAt = lastInitTimeStamp; 
	}

	/**
	 * @author vipup
	 */
	private static final long serialVersionUID = -5268245947298702548L;
	Map<String, String> db2path = new TreeMap<String, String>();
	Map<String, String> path2db = new TreeMap<String, String>();
	
	public Registry(Map<String, String> db2path2) {
		this.db2path.putAll(db2path2);
		for (String key :this.db2path.keySet()){
			String value = this.db2path.get( key);
			this.path2db.put(value ,key );
		}
	}

	@SuppressWarnings("unchecked")
	public Registry() {
		this(Collections.EMPTY_MAP);
	}

	public void register(String rrddb, String xpath) { 
		synchronized(Registry.class){
			checkForUpdates();
			db2path.put(rrddb, xpath);
			path2db.put(xpath,rrddb );
			reset();
		}
	}
	
	public void unregister(String rrddb, String xpath) {
		synchronized(Registry.class){
			checkForUpdates();
			db2path.remove(rrddb);
			path2db.remove(xpath);
			reset();
		}
	}

	private void checkForUpdates() {
		synchronized(Registry.class){
			if (lastInitTimeStamp>initedAt){
				synchronized(Registry.class){
					Cache cache = Manager.getCache();
					Registry retval = (Registry) cache.get(REGISTRY_CACHE_NAME);
					this.db2path = retval.db2path;
					this.path2db = retval.path2db ;
					initedAt = lastInitTimeStamp;
				}
			}
		}
	}

	public Map<String, String> getDb2path() {
			return Collections.unmodifiableMap(db2path);			
	}

	public Map<String, String> getPath2db() {			
			return  Collections.unmodifiableMap(path2db);
	}

	public static Registry getInstance() {
		Cache cache = Manager.getCache();
	    Registry retval = null;
	    try{
	    	retval = (Registry) cache.get(REGISTRY_CACHE_NAME);
	    }catch(Exception e){}
		if (cache == null) return null ; // GC/destroy-mode
		if (retval == null){ 
			retval = new Registry();
			cache.put(REGISTRY_CACHE_NAME, retval);
		}else{
			// exceptional test-DB - alsway registered DB
			retval.register("test.rrd","test");
		}
		return retval;
	}

	public void flush() {
		synchronized (Cache.class) {
			Cache cache = Manager.getCache();
			cache.remove(REGISTRY_CACHE_NAME);
			Map<String, String> db2path2 = this.getDb2path();
			Registry registry = new Registry( db2path2 );
			cache.put(REGISTRY_CACHE_NAME, registry);
		}
		lastInitTimeStamp = this.initedAt;
	}

}


 