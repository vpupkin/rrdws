package cc.co.llabor.cache.opt;

import net.sf.jsr107cache.CacheEntry;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  29.11.2012::12:45:20<br> 
 */
public class ThinObjWrap implements CacheEntry {

	private long cost;
	private boolean isValid;
	private long version;
	private long expirationTime;
	private long creationTime;
	private long lastUpdateTime;
	private long lastAccessTime;
	private int hits;
	private Object value;
	private Object key;

	public ThinObjWrap(Object keyPar, Object valuePar) {
		 this.key = keyPar;
		 this.value = valuePar;
	}

	public Object getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public Object setValue(Object value) {
		// TODO Auto-generated method stub
		if (1 == 1)
			throw new RuntimeException("not yet implemented since 29.11.2012");
		else {
			return null;
		}
	}

	public int getHits() {
		return hits;
	}

	public long getLastAccessTime() {
		return lastAccessTime;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public long getExpirationTime() {
		return expirationTime;
	}

	public long getVersion() {
		return version;
	}

	public boolean isValid() {
		return isValid;
	}

	public long getCost() {
		return cost;
	}

}


 