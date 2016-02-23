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

package org.collectd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 

import ws.rrd.collectd.TextLineIterator;
import ws.rrd.csv.Action;
import ws.rrd.csv.RrdUpdateAction;

import cc.co.llabor.cache.Manager;
import cc.co.llabor.features.Repo;
import cc.co.llabor.system.StartStopServlet;

import net.sf.jsr107cache.Cache;
 
/**
 * collectd UDP protocol receiver.
 * See collectd/src/network.c:parse_packet
 */
public class   DataWorker implements Runnable{

    	private java.util.Queue<String> queue;
		private boolean isAlive = true;
		private static final Logger log = LoggerFactory.getLogger(DataWorker.class .getName());
    	public DataWorker() {
    		this( new LinkedList<String>());
		}
		DataWorker ( Queue<String> q){
    		this.queue = q;
    	}
    	
    	public void kill(){
    		isAlive = false;
    	}
    	

		public void run() {
    		while(isAlive ){ 
    			if (queue.isEmpty()){
    				try {
    					Thread.sleep(100);
    				} catch (InterruptedException e) {
    					// TODO Auto-generated catch block
    					// e.  printStackTrace();
    				}
    			}else{
					try {
	    				String data = queue.poll();//queue.peek();queue.queue.clear()
						byte[] b = data.getBytes();
						InputStream in = new ByteArrayInputStream(b ); 
						processData(in );
						queue.remove(data);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						// e.  printStackTrace();
					} finally{
						
					}
    			}
    			Cache cache = Manager.getCache("collectd");
    			Object key ="collectd_4_RRD_data";
    			Object  o = cache.get(key);
    			// TODO Lock + synch
    			if (o!=null){
    				cache.put(key, "");
					if (!"".equals(o))
						queue.offer(""+o);
    				try {
    					Thread.sleep(1001);
    				} catch (InterruptedException e) {
    					// TODO Auto-generated catch block
    					// e.  printStackTrace();
    				}
    			}

    		}
    		log.info(Repo.getBanner( "+rrdDataWorker"));
    	}
		private void processData(InputStream resourceAsStream) throws IOException {
            //
			TextLineIterator p = new TextLineIterator(resourceAsStream);
			// testdrive into System.out
			SimpleDateFormat SDF = new SimpleDateFormat("MMM dd HH:mm:ss yyyy", Locale.US);				
			Action a = new RrdUpdateAction(SDF);
			p.perform(a);
		}

     
}
