/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003, by Sasa Markovic.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
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
package org.jrobin.mrtg.server;
 
import org.jrobin.core.RrdException;
import org.jrobin.mrtg.Debug;
import org.jrobin.mrtg.MrtgConstants;
import org.jrobin.mrtg.MrtgException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 
import ws.rrd.csv.Action;
import ws.rrd.csv.RrdUpdateAction;
 
import java.io.IOException; 
import java.util.Collections; 
import java.util.LinkedList;
import java.util.List; 

class RrdWriter  implements Runnable, MrtgConstants {
	
 	private int sampleCount, badSavesCount, goodSavesCount;
	private List<RawSample> queue = Collections.synchronizedList(new LinkedList<RawSample>());

	

	private volatile boolean active = true;
	Thread thr1 ;
	private static final Logger log = LoggerFactory.getLogger(RrdWriter.class .getName());
	
 

    public void run() {
		Debug.print("Archiver started");
		// the code is plain ugly but it should work
		while(active) {
           	while(active && queue.size() == 0) {
			   synchronized(this) {
				   try {
					   wait();
				   } catch (InterruptedException e) {
					   Debug.print(e.toString());
				   }
			   }
			}
			if(active && queue.size() > 0) {
				RawSample rawSample = queue.remove(0);
				try {
					process(rawSample);
				} catch (RrdException e) {
					log.trace( "rrdWriter processing error", e);
				} catch (IOException e) {
					// TODO Auto-generated catch block - has to react to the error and deactivate invalid SNMP-Ports.
					// e.  printStackTrace();
					if (e.getMessage().contains("Bad sample timestamp"))
					try {						
						String host = rawSample.getHost();
						String ifDescr = rawSample.getIfDescr();
						DeviceList deviceList = Server.getInstance().getDeviceList();
						Device routerByHost = deviceList.getRouterByHost( host);
						Port link = routerByHost.getLinkByIfDescr(ifDescr);
						link.deactivate();
						
					} catch (MrtgException e1) { 
						// e1. printStackTrace();
					} catch (Throwable e2) {
						// e2. printStackTrace();
					}
				} catch (Throwable e) {
					// e.  printStackTrace();
				}
			}else{ // if passivated || empty queue
				System.out.println(" store statisctiv for RddWorker....");
			}
		}
		Debug.print("Archiver ended");
	}

	void terminate() {
		active = false;
		synchronized(this) {
			notify();
		}
	}
 
	private void process(RawSample rawSample) throws IOException, RrdException {  	
		
		
		Action a = new RrdUpdateAction();
		String ifDescr = rawSample.getIfDescr();
		long string =  System.currentTimeMillis();
		String value = rawSample.getValue();
		String pathTmp = Server.calPath2RRDb ( rawSample.getHost(),  ifDescr);
		Object retval = a.perform(  pathTmp , string , value );
		if (retval instanceof RrdException){
			throw new RrdException( (Exception)retval);
		}else if (retval instanceof Exception){
				throw new IOException("RRD RawProcessing error with "+ifDescr+" =={"+value+"}",(Exception)retval);
		}else{
			log.debug("processed :{}=[{}]", pathTmp, value );
		}
	}
 

	static String getRrdFilename(String host, String ifDescr) {
		String filename = ifDescr.replaceAll("[^0-9a-zA-Z]", "_") +
			"@" + host.replaceFirst(":", "_") + ".rrd";
		String rrdDir = Config.getRrdDir();
		return rrdDir + filename;
	}

	synchronized void store(RawSample sample) {
		queue.add(sample);
		sampleCount++;
		notify();
	}

 

	int getSampleCount() {
		return sampleCount;
	}

	int getBadSavesCount() {
		return badSavesCount;
	}

	int getGoodSavesCount() {
		return goodSavesCount;
	}

	int getSavesCount() {
		return getGoodSavesCount() + getBadSavesCount();
	}

}
