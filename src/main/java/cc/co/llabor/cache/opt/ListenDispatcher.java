package cc.co.llabor.cache.opt;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsr107cache.CacheListener;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  29.11.2012::13:40:56<br> 
 */
public class ListenDispatcher implements CacheListener{
	private final transient List<CacheListener> myListeners = new ArrayList<CacheListener>();
	private final transient List<Object> loadQ = new ArrayList<Object>();
	private final transient List<Object> putQ = new ArrayList<Object>();
	private final transient List<Object> evictQ = new ArrayList<Object>();
	private final transient List<Object> removeQ = new ArrayList<Object>();
	private final transient List<Object> clearQ = new ArrayList<Object>();
 
	
	long NOTIFY_INTERVAL_IN_MILLISECONDS = 5*1000; // 5 sec by default
	long timeToNotify = 0;
	private final  boolean isDelayed;
	
	public ListenDispatcher(){
		this("DELAYED.Notification"==null);
	}
	public ListenDispatcher(boolean isDelayed) {
		this.isDelayed = isDelayed;
	}
	String lastNotificationState = " load=0 put=0 evict=0 remove=0 clear=0";
	private final void notyfyAllListeners(){
		if (System.currentTimeMillis() >timeToNotify ){
			synchronized (myListeners) {				
				for (CacheListener l:myListeners){
					for (Object o:putQ)l.onPut(o);
					for (Object o:loadQ)l.onLoad(o);
					for (Object o:evictQ)l.onEvict(o);
					for (Object o:removeQ)l.onRemove(o);
					for (Object o:clearQ){System.out.println("clear::"+o);l.onClear();}					
				}
				putQ.clear();
				loadQ.clear();
				evictQ.clear();
				removeQ.clear();
				clearQ.clear();
				timeToNotify = isDelayed?System.currentTimeMillis()+NOTIFY_INTERVAL_IN_MILLISECONDS:0;
			}
		}		
	}
	

	public void addListener(CacheListener arg0) {
		synchronized (myListeners) {	
			myListeners.add(arg0);
		}
	}
	

	public void removeListener(CacheListener o) {
		synchronized (myListeners) {
			this.myListeners.remove(o);
		}
	}	

	private int load;
	private int put;
	private int evict;
	private int remove;
	private int clear;
	
	public String toString(){
		return 
		" load="+load+
		" put="+put+
		" evict="+evict+
		" remove="+remove+
		" clear="+clear 
		;
	}
	
	public void onLoad(Object key) { 
		load++;
		loadQ.add(key);
		notyfyAllListeners();
	}

	public void onPut(Object key) {
		put++;
		putQ.add(key);
		notyfyAllListeners();
	}

	public void onEvict(Object key) {
		evict++;
		evictQ.add(key);
		notyfyAllListeners();
	}

	public void onRemove(Object key) {
		remove++;
		removeQ.add(key);
		notyfyAllListeners();
	}

	public void onClear() {
		clearQ.add(""+clear++);
		notyfyAllListeners();
	}


}


 