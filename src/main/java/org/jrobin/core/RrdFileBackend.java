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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jrobin.mrtg.server.Config;

/**
 * JRobin backend which is used to store RRD data to ordinary files on the disk. This was the
 * default factory before 1.4.0 version<p>
 * <p/>
 * This backend is based on the RandomAccessFile class (java.io.* package).
 */
public class RrdFileBackend extends RrdBackend {
	
	private static final Logger log = LoggerFactory.getLogger(RrdFileBackend.class .getName());
	/**
	 * read/write file status
	 */
	protected boolean readOnly;
	/**
	 * radnom access file handle
	 */
	protected RandomAccessFile file;

	/**
	 * Creates RrdFileBackend object for the given file path, backed by RandomAccessFile object.
	 *
	 * @param path	 Path to a file
	 * @param readOnly True, if file should be open in a read-only mode. False otherwise
	 * @throws IOException Thrown in case of I/O error
	 */
	protected RrdFileBackend(String path, boolean readOnly) throws IOException {
		super(path);
		this.readOnly = readOnly;
		String cPath = this.getCanonicalPath(); 
		try{
			new File(cPath).getParentFile().mkdirs();
			this.file = new RandomAccessFile(cPath, readOnly ? "r" : "rw");
		}catch (IOException ex){
			throw ex;
		}
	}

	/**
	 * Closes the underlying RRD file.
	 *
	 * @throws IOException Thrown in case of I/O error
	 */
	public void close() throws IOException {
		file.close();
	}
	 
	/**
	 * Returns canonical path to the file on the disk.
	 *
	 * @param path File path
	 * @return Canonical file path
	 * @throws IOException Thrown in case of I/O error
	 */
	public static String getCanonicalPath(String path) throws IOException { 
		String parentTmp = "rrd.home";
		try { 
			parentTmp = Config.CALC_DEFAULT_WORKDIR() 
					+java.io.File.separator 
					+ "rrd.home";
			if (path.startsWith( parentTmp )){
				path = (new File(path)).getName();
			}
					
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		File fileTmp = new File(parentTmp, path);
//		if (!fileTmp.canWrite()) {
//			log.error("you don't own your HOME! Fix it before using RRDWS. path =["
//					+ fileTmp.getCanonicalPath() + "]");
//			log.error("System.getProperty( \"rrd.home\" ) =["
//					+ System.getProperty("rrd.home"));
//			log.error("System.getProperty(\"user.dir\")   =["
//					+ System.getProperty("user.dir"));
//			log.error("System.getProperty(\"user.home\")  =["
//					+ System.getProperty("user.home"));
//			fileTmp = File.createTempFile("RRD", path);
//			File parentDir = fileTmp.getParentFile();
//			File correctNameInTmpDir = new File(parentDir, path);
//			fileTmp.renameTo(correctNameInTmpDir);
//			System.out.println(path + correctNameInTmpDir.canWrite()
//					+ fileTmp.canWrite());
//			fileTmp = correctNameInTmpDir;
//		}
		return fileTmp.getCanonicalPath();

	}

	/**
	 * Returns canonical path to the file on the disk.
	 *
	 * @return Canonical file path
	 * @throws IOException Thrown in case of I/O error
	 */
	public String getCanonicalPath() throws IOException {
		String pathTmp = getPath();
		return RrdFileBackend.getCanonicalPath(pathTmp);
	}

	/**
	 * Writes bytes to the underlying RRD file on the disk
	 *
	 * @param offset Starting file offset
	 * @param b	  Bytes to be written.
	 * @throws IOException Thrown in case of I/O error
	 */
	protected void write(long offset, byte[] b) throws IOException {
		file.seek(offset);
		file.write(b);
	}

	/**
	 * Reads a number of bytes from the RRD file on the disk
	 *
	 * @param offset Starting file offset
	 * @param b	  Buffer which receives bytes read from the file.
	 * @throws IOException Thrown in case of I/O error.
	 */
	protected void read(long offset, byte[] b) throws IOException {
		file.seek(offset);
		if (file.read(b) != b.length) {
			throw new IOException("Not enough bytes available in file " + getPath());
		}
	}

	/**
	 * Returns RRD file length.
	 *
	 * @return File length.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public long getLength() throws IOException {
		return file.length();
	}

	/**
	 * Sets length of the underlying RRD file. This method is called only once, immediately
	 * after a new RRD file gets created.
	 *
	 * @param length Length of the RRD file
	 * @throws IOException Thrown in case of I/O error.
	 */
	protected void setLength(long length) throws IOException {
		file.setLength(length);
	}
}
