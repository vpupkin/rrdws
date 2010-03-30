/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003-2005, by Sasa Markovic.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package org.jrobin.core;

import java.io.IOException;

import javax.jdo.PersistenceManager;

import ws.rdd.jdo.Blob;
import ws.rdd.jdo.RRD_JDOHelper;

/**
 * Backend to be used to store all RRD bytes in memory.<p>
 */
public class RrdJdoBackend extends RrdBackend {
	
	byte[]buffer;
	private boolean isReadOnly = true;
	Blob blob;
	
	private final void checkBeforeWrite(){
		if (isReadOnly) throw new RuntimeException("Write ist not allowed!");
	}

	 
	public RrdJdoBackend(Blob blobTmp, boolean readOnly) {
		super(blobTmp.getName());
		this.isReadOnly = readOnly;
		this.blob = blobTmp;
		this.buffer = blob.getData().getBytes();
	}

	protected synchronized void write(long offset, byte[] b) throws IOException {
		checkBeforeWrite();
		int pos = (int) offset;
		for (byte singleByte : b) {
			buffer[pos++] = singleByte;
		}
	}

	protected synchronized void read(long offset, byte[] b) throws IOException {
		int pos = (int) offset;
		if (pos + b.length <= buffer.length) {
			for (int i = 0; i < b.length; i++) {
				b[i] = buffer[pos++];
			}
		}
		else {
			throw new IOException("Not enough bytes available in memory " + getPath());
		}
	}

	/**
	 * Returns the number of RRD bytes held in memory.
	 *
	 * @return Number of all RRD bytes.
	 */
	public long getLength() {
		return buffer.length;
	}

	/**
	 * Reserves a memory section as a RRD storage.
	 *
	 * @param newLength Number of bytes held in memory.
	 * @throws IOException Thrown in case of I/O error.
	 */
	protected void setLength(long newLength) throws IOException {
		checkBeforeWrite();
		if (newLength > Integer.MAX_VALUE) {
			throw new IOException("Cannot create this big memory backed RRD");
		}
		buffer = new byte[(int) newLength];
	}

	/**
	 * This method is required by the base class definition, but it does not
	 * releases any memory resources at all.
	 */
	public void close() {		
		if (false == isReadOnly ){
			PersistenceManager pm = RRD_JDOHelper.getInstance().getPMF().getPersistenceManager();
			this.blob.setData( this.buffer );
			pm.makePersistent(this.blob);
			pm.close();
		}		 
	}

	/**
	 * This method is overriden to disable high-level caching in frontend JRobin classes.
	 *
	 * @return Always returns <code>false</code>. There is no need to cache anything in high-level classes
	 *         since all RRD bytes are already in memory.
	 */
	protected boolean isCachingAllowed() {
		return false;
	}
}
