package cc.co.llabor.cache;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL; 
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
 

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

/** 
 * <b>Manager - primary CacheFactory for all of Cache-consumers with global-availability.</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  26.08.2010::20:14:35<br> 
 */
public class Manager {
	static final Logger log = Logger.getLogger("/rrd/src/main/java/cc/co/llabor/cache/Manager.java");
	// try to load default cache conf
	static Object gooTmp = null;	
	static{ 
		log.info( "<INITCACHE>");
		 try{
			 gooTmp = System.getProperty("com.google.appengine.runtime.version");
		 }catch(Throwable e){
			 log.warning(e.getMessage());
			 // e.  printStackTrace();
		 }
		 
		 try{
			 
			 if (gooTmp==null){
				 //net.sf.jsr107cache.CacheFactory=ws.rrd.cache.BasicCacheFactory
				 log.info("<NOGAE/>" );

				 try{
					 //java.io.InputStream in = MemoryFileCache.class.getClassLoader().getResourceAsStream("META-INF/services/net.sf.jsr107cache.CacheFactory");
					 //java.io.InputStream in = MemoryFileCache.class.getClassLoader().getResourceAsStream("jcache.properties");
					 log.info("<rename src='!net.sf.jsr107cache.CacheFactory' dst='net.sf.jsr107cache.CacheFactory'> "  );
					 ClassLoader classLoader = MemoryFileCache.class.getClassLoader();
					 URL resource = classLoader.getResource("META-INF/services/!net.sf.jsr107cache.CacheFactory");
					 String fNameTmp = resource.toString();
					 fNameTmp = fNameTmp.replace("%20", " ");
					 fNameTmp = fNameTmp.replace("file:/", "");
					 //if (myNewCacheSettings.exists())
					 File reserveConfigFile = new File (fNameTmp) ;
					 File newFile4Cfg = new File(fNameTmp.replace("s/!ne", "s/ne"));
					 java.io.InputStream in = null;
					 Properties prTmp = new Properties();
					 
					 try{
						 if (newFile4Cfg .exists() ){
							 log.warning("<!!!EXIST!!!>");
							 in = new FileInputStream(newFile4Cfg);
							 log.info("in ="+in);
						 }else if (reserveConfigFile.exists()){
							 reserveConfigFile.renameTo(newFile4Cfg);
							 in = new FileInputStream(newFile4Cfg);
							 log.info("<compleete>");
						 }else{
							 in = classLoader.getResourceAsStream("META-INF/services/net.sf.jsr107cache.CacheFactory");
							 String cpLocation = classLoader.getResource("META-INF/services/net.sf.jsr107cache.CacheFactory").toString();
							 log.info("<init src='"+cpLocation+"'>");
						 }
						 log.info("</rename>");
						 
						 log.info("<init>");
						 prTmp.load(in);
					 }finally{
						 in.close();
					 }
					 log.info(""+prTmp);
					 
					 String key = "net.sf.jsr107cache.CacheFactory";
					 String val = prTmp.getProperty(key);
					 
					 final String propertyBak = System.getProperty(key);
					 log.info( " <clear.System.property name='"+key +"'><from>"+propertyBak+"</from><to>"+val+"</to>");
					 try{
						 System.clearProperty( key );
					 }catch(Exception e){
						 log.warning(e.getMessage());
					 }
						 
					 log.info( " </clear.System.property>");
					 in.close();
					 log.info("</init>");
				//java.lang.NullPointerException	 
				 }catch(java.lang.NullPointerException e){
					 log.severe( e.getMessage() ); 
					 // e.  printStackTrace();
				 }catch(Exception e){
					 log.severe( e.getMessage() ); 
					 // e.  printStackTrace();
				 } 
			 }else{
				 log.info("<GAE v"+gooTmp+ " CM="+CacheManager.getInstance() +"/>");
			 }
		 }catch(Throwable e){
			 log.severe(  e.getMessage() ); 
			 // e.  printStackTrace();
		 }
		 log.info("</INITCACHE>");
	}
      	
	/**
	 * @deprecated use named getCache instead
	 * @author vipup
	 * @return
	 */
	public static Cache getCache()   {
		String nsTmp = ".defaultcache";
		try{
			throw new RuntimeException("");
		}catch(Throwable  e){
			nsTmp = "Hx0"+e.getStackTrace()[2] ;//getCache
		}
		nsTmp = MemoryFileCache.class.getName();
		return getCache(nsTmp);
	}

	public static Cache getCache(String cacheNS)   {  
		return getCache( cacheNS, true) ;
	}

	public static Cache getCache(String cacheNS, boolean createIfNotExists) {
		if (cacheNS == null)return getCache("DEFAULT");
		
		CacheManager cm = CacheManager.getInstance(); 
		
		boolean isGAE = null != gooTmp;
		
		Cache retval = null;

		if (   isGAE   ) {// for GAE - always create
			retval = extractGAEcache(cacheNS, cm, retval);
		}else {
				retval = cache4cache.get(cacheNS);// cm.getCache (cacheNS);
				if (retval ==null){
					// getting over CM
					if ( createIfNotExists){			
						try {
							if (classloader4cache.size()>0){
								// try to re-use already succesfully-loaded-caches for creating the new one
								ClassLoader clTmp = (ClassLoader) classloader4cache.values().toArray()[0];
								Thread.currentThread().setContextClassLoader(clTmp );
							}
							retval = cm.getCache (cacheNS);
							//http://code.google.com/intl/ru/appengine/docs/java/memcache/usingjcache.html
							Properties props = new Properties();
							props.put(NS.NAMESPACE, cacheNS ); //props.put("delayed.listener", "true")
							CacheFactory cacheFactory = cm.getCacheFactory();
							retval  = retval ==null? cacheFactory.createCache(props):retval;
						} catch (NullPointerException e) {
							//  JEE container with permissions ??
							// drafUndDirty
							Properties arg0= new Properties();
							arg0.setProperty("cacheNS",  ""+cacheNS);
							arg0.setProperty("createIfNotExists",  ""+createIfNotExists);
							arg0.setProperty("lastError",  ""+e.getMessage());
							
							retval  = new FileCache(arg0);
							
						} catch (CacheException e) {	
							log.severe("!"+e.getMessage());
							// e.  printStackTrace();
							throw new RuntimeException(e);
						}
					}else{
						retval =  cm.getCache (cacheNS);
					}
					// TODO caching the cache %-o
					if (retval!=null){
						cache4cache.put(cacheNS, retval);
						// store soccessfull classloader for cache
						ClassLoader cl = Thread.currentThread().getContextClassLoader();
						classloader4cache.put(cacheNS, cl );
					}
				}
			}
		return  retval; 
	}

	private static Cache extractGAEcache(String cacheNS, CacheManager cm, Cache retval) {
		synchronized (CacheManager.class) { 
			try {
				CacheFactory cacheFactory = cm.getCacheFactory();
				Properties props = new Properties();
				props.put(NS.NAMESPACE, cacheNS );
				Cache cacheTmp;
				cacheTmp = cacheFactory.createCache(props);
				cm.registerCache(cacheNS, cacheTmp);
				retval = cacheTmp;
			} catch (CacheException e) {
				log.severe(e.getMessage());
				// e.  printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return retval;
	}
	static Map<String, Cache> cache4cache = new HashMap<String, Cache>();
	static Map<String, ClassLoader> classloader4cache = new HashMap<String, ClassLoader>();
}


 