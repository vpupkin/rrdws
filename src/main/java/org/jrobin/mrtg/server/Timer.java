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

import org.jrobin.mrtg.Debug;
import org.jrobin.mrtg.MrtgConstants;
import org.jrobin.mrtg.MrtgException;

import ws.rrd.logback.ServletListener;

import java.util.Collection;
import java.util.Queue;
import java.util.Vector;

class Timer  implements Runnable , MrtgConstants {
	private volatile boolean active = true;
	
	
	

	private Queue<SnmpReader> queue;

	final SnmpWorker readerTpp ;

	private RrdWriter rrdWriter;
	
	Timer() {
		this(new ThreadGroup( ServletListener.getDefaultThreadGroup() , "MRTG_4j" ));
	}
	Timer(ThreadGroup tgPar) {

		// Init SNMP-queue Reader
		readerTpp = new SnmpWorker();
		// Init Clockwork as SNMP-initiator
		Thread thr2 = new Thread(tgPar, readerTpp, "mrtg.SnmpWorker");
		// ready to start....
		
		
		this.queue = readerTpp.queue;
		
		// Init Clockwork as SNMP-initiator
		Thread thr1 = new Thread(tgPar, this, "mrtg.TimerItself");
		
		
		thr1.setDaemon(true);
		thr2.setDaemon(true);
		thr1 .start();//mrtg.TimerItself  (writer, queue-writer)
		thr2 .start();//mrtg.SnmpWorker   (reader, queue-reader, snmp-resolver, snmp-data-pusher)
		
		rrdWriter = new RrdWriter( );
		// TODO =8-0
		Thread thr3 = new Thread(tgPar , rrdWriter, "mrtg.RRDWriter");
		thr3.setDaemon(true);
		thr3 .start();//mrtg.RRDWriter (  snmp-data-POPer :) )
		
	}

	public void run() {
		DeviceList deviceList;
		try {
			deviceList = Server.getInstance().getDeviceList();
		} catch (MrtgException e1) { 
			// e1. printStackTrace();
			return;
		}
		Debug.print("Scheduler started");
		while(active) {
			Vector routers = deviceList.getRouters();
			for(Object routerO : routers.toArray() ) {
				Device router = (Device)routerO;
				Collection<Port> links = router.getLinks();
				for (Object linkO  :links  ) {
					Port link = (Port) linkO;
                    if(router.isActive())
                    if( link.isActive() )
                    if(	link.isDue() )
                    if( !link.isSampling() ) {
                    	SnmpReader snmpReader = new SnmpReader(router, link); 
                    	this.queue.add(snmpReader); 
					}
				}
			}
			// sleep for a while
			synchronized(this) {
				try {
					wait(SCHEDULER_RESOLUTION * 1000L);
				}
				catch (InterruptedException e) {
				}
			}
		}
		Debug.print("Scheduler ended");
	}

	void terminate() {
    	active = false;
		try {
			readerTpp.kill();
		} catch (Exception e) {
			// e.  printStackTrace();		}    	
		}
    	
		try {
			rrdWriter.terminate();
		} catch (Exception e) {
			// e.  printStackTrace();		}    	
		}
    	
		synchronized(this) {
			notify();
		}
	}
	public RrdWriter getRrdWriter() { 
		return rrdWriter;
	}
}
