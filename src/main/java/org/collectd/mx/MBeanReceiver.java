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

package org.collectd.mx;

import java.lang.management.ManagementFactory;
import java.net.DatagramSocket;
import java.net.MulticastSocket;

import javax.management.MBeanServerFactory;

import org.collectd.protocol.Dispatcher;
import org.collectd.protocol.UdpReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend UdpReceiver, dispatching collectd data to a
 * CollectdMBeanRegistry instance.  Invoked via Main-Class:
 * java -jar collectd.jar 
 */
public class MBeanReceiver
    extends UdpReceiver
    implements Runnable {

    private static final String MX = "com.sun.management.jmxremote";
    private static final String DMX = "-D" + MX;
	private static final Logger _log = LoggerFactory.getLogger(MBeanReceiver.class .getName());

    public MBeanReceiver() throws Exception {
        CollectdMBeanRegistry collectdMBeanRegistry = new CollectdMBeanRegistry();
        collectdMBeanRegistry.init();
		setDispatcher(collectdMBeanRegistry);
    }

    public MBeanReceiver(Dispatcher dispatcher) {
        super(dispatcher);
    }

    private void setup() throws Exception {
        DatagramSocket socket = getSocket();
        if (socket instanceof MulticastSocket) {
            MulticastSocket mcast = (MulticastSocket)socket;
            _system_out_println("Multicast interface=" + mcast.getInterface());
        }
        _system_out_println(getListenAddress() + ":" +
                           getPort() +
                           " listening...");
        listen();
    }

    public void run() {
        try {
            setup();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static String getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int ix = name.indexOf('@');
        if (ix == -1) {
            return null;
        }
        return name.substring(0, ix);
    }

    private static boolean hasMBeanServer() {
        return MBeanServerFactory.findMBeanServer(null).size() != 0;
    }

    private static boolean checkMxAgent(String[] args) {
        if (hasMBeanServer()) {
            return true;
        }
        boolean hasMx = false;

        for (int i=0; i<args.length; i++) {
            if (args[i].startsWith(DMX)) {
                hasMx = true;
                break;
            }
        }
        if (hasMx) {
            return true;
        }
        try {
            System.out.print("Enabling " + DMX + "...");
            System.setProperty(MX, "true");
            //jdk 6 has a better way, but this works w/ 5 + 6
            sun.management.Agent.startAgent();
            hasMx = hasMBeanServer();
            _system_out_println(hasMx ? "ok" : "failed");
            return hasMx;
        } catch (Throwable t) {
            _system_out_println("failed: " + t);
            return false;
        }
    }

    static private MBeanReceiver me ;
    static {
    	try {
			me = new MBeanReceiver();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static MBeanReceiver getInstance(){
    	
    	return me;
    }
    
    public static void main(String[] args) throws Exception {
        if (!checkMxAgent(args)) {
            _system_out_println("Try using: java " + DMX + " ...");
            return;
        }
        MBeanReceiver beanReceiver = getInstance();
		Thread lt = new Thread(beanReceiver, "jcollectd.MX.Receiver");
        lt.setDaemon(true);
        lt.start();

        boolean launchJconsole = false;
        for (int i=0; i<args.length; i++) {
            String arg = args[i];
            if (arg.equals("-jconsole")) {
                launchJconsole = true;
            }
            else {
                _system_out_println("Unknown argument: " + arg);
                return;
            }
        }
        if (launchJconsole) {
            String pid = getPid();
            String[] argv = { "jconsole", pid };
            _system_out_println("exec(jconsole, " + pid + ")");
            try {
                Process p = Runtime.getRuntime().exec(argv);
                p.waitFor();
                _system_out_println("jconsole exited");
            } catch (Exception e) {
                _system_out_println(e.getMessage());
            }
        }
        else {
            lt.join();
        }
    }

	private static void _system_out_println(String msg) {
		_log.info(msg);
	}
}
