/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 */

package org.jrobin.core;

import java.io.IOException; 
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import ws.rdd.jdo.Blob;
import ws.rdd.jdo.RRD_JDOHelper;
 


public class RrdJdoBackendFactory extends RrdBackendFactory {
	/**
	 * factory name, "JDO"
	 */
	public static final String NAME = "JDO";

	/**
	 * Creates RrdMemoryBackend object.
	 *
	 * @param id	   Since this backend holds all data in memory, this argument is interpreted
	 *                 as an ID for this memory-based storage.
	 * @param readOnly This parameter is ignored
	 * @return RrdMemoryBackend object which handles all I/O operations
	 * @throws IOException Thrown in case of I/O error.
	 */
	/* (non-Javadoc)
	 * @see org.jrobin.core.RrdBackendFactory#open(java.lang.String, boolean)
	 */
	protected synchronized RrdBackend open(String id, boolean readOnly)
			throws IOException {
		RrdJdoBackend backend;
		PersistenceManager pm = RRD_JDOHelper.getInstance().getPMF().getPersistenceManager();
		Blob blobTmp ;
		if (exists(id)) {
			blobTmp = getById( id);
			backend = new RrdJdoBackend (blobTmp,  readOnly);
		}
		else {
			blobTmp = new Blob("".getBytes());
			blobTmp .setName(id); 
			pm.makePersistent(blobTmp);				
			backend = new RrdJdoBackend(blobTmp ,readOnly);
		}
		pm.close();
		return backend;
	}

	/**
	 * Method to determine if a memory storage with the given ID already exists.
	 *
	 * @param id Memory storage ID.
	 * @return True, if such storage exists, false otherwise.
	 */
	@SuppressWarnings("unchecked")
	protected synchronized boolean exists(String id) {
		PersistenceManager pm = RRD_JDOHelper.getInstance().getPMF().getPersistenceManager();
	    Query query = pm.newQuery(Blob.class);
	    query.setFilter("name == nameParam");
	    query.setOrdering("createDate desc");
	    query.declareParameters("String nameParam");

	    try {
	        List<Blob> results = (List<Blob>) query.execute( id );
	        if (results.iterator().hasNext()) {
	            return true;
	        } else {
	            return false;
	        }
	    } finally {
	        query.closeAll();
	        pm.close();
	    }

	}
	
	@SuppressWarnings("unchecked")
	public synchronized Blob getById(String id) {
		PersistenceManager pm = RRD_JDOHelper.getInstance().getPMF().getPersistenceManager();
	    Query query = pm.newQuery(Blob.class);
	    query.setFilter("name == nameParam");
	    query.setOrdering("createDate desc");
	    query.declareParameters("String nameParam");
	    Blob retval = null;
	    try {
	        
			List<Blob> results = (List<Blob>) query.execute( id );
	        if (results.iterator().hasNext()) {
	        	retval =  results.iterator().next();
	        } 
	    } finally {
	        query.closeAll();
	        pm.close();
	        
	    }
	    return retval;

	}
	

	/**
	 * Removes the storage with the given ID from the memory.
	 *
	 * @param id Storage ID
	 * @return True, if the storage with the given ID is deleted, false otherwise.
	 */
	public boolean delete(String id) {
		if (exists(id)) {
			PersistenceManager pm = RRD_JDOHelper.getInstance().getPMF().getPersistenceManager();
			pm.deletePersistent( id);
			pm.close();
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Returns the name of this factory.
	 *
	 * @return Factory name (equals to "JDO").
	 */
	public String getFactoryName() {
		return NAME;
	}
 
}
