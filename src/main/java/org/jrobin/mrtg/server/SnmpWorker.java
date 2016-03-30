/*
 * jcollectd
 * Copyright (C) 2009 Hyperic, Inc.
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; only version 2 of the License is applicable.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 */

package org.jrobin.mrtg.server;
 
import java.util.LinkedList;  
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
  
 
/**
 * collectd UDP protocol receiver.
 * See collectd/src/network.c:parse_packet
 */
public class   SnmpWorker implements Runnable{

		java.util.Queue<SnmpReader> queue;
		private boolean isAlive = true;
		private static Logger log = LoggerFactory.getLogger(SnmpWorker.class.getName());
    	SnmpWorker ( Queue<SnmpReader> q){
    		this.queue = q;
    	}
    	
    	public void kill(){
    		isAlive = false;
    	}
    	
    	public SnmpWorker() {
    		queue = new LinkedList<SnmpReader>();
		}
    	public static final int MAX_ERRORS_ALLOWED = 11000;
    	public static int ERROR_COUNT = 0;
		public void run() {
    		while(isAlive ){ 
    			if (queue.isEmpty()){
    				try {
    					Thread.sleep(1001);
    				} catch (InterruptedException e) {
    					// TODO Auto-generated catch block
    					// e.  printStackTrace();
    				}
    			}else{ 
						try{
							SnmpReader data = queue.poll();//queue.peek();queue.queue.clear()
							// here is not need to read it asynchronously - so __run__ can be called directly
								ThreadGroup tgTmp = Thread.currentThread().getThreadGroup();
							data.run(tgTmp ); // assums the ru could create Threads...
							queue.remove(data); 
						}catch(Throwable e){
							ERROR_COUNT ++;
							// e.  printStackTrace();
						}
    			} 
    			if (ERROR_COUNT > MAX_ERRORS_ALLOWED ){
    				isAlive = false;
    				log.error( "MAX_ERROR_COUNT reached ->>>>>>>>>>>>>>>STOP all SnmpWorker!" );
    			}
    		}
    	}
 

     
}
