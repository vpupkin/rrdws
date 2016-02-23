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

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.collectd.protocol.Network;
import org.collectd.protocol.Notification;
import org.collectd.protocol.Dispatcher;
import org.collectd.protocol.Sender;
import org.collectd.protocol.UdpSender;
import org.collectd.protocol.ValueList;

/**
 * Process -javaagent configuration and schedule MBeanCollector objects.
 */
public class MBeanSender implements Dispatcher {
	private static int agentCounter = 0; 
	private static Thread hookToVictim = null;
	
    private final class JcollectAgentFactory implements ThreadFactory {
		public Thread newThread(Runnable task) {
		    Thread thread = new Thread(task);
		    thread.setName("jcollectd_agent#" +(agentCounter ++));
		    // TODO thread-hunting way have to be changed to more "loyal"
		    hookToVictim = thread;
		    thread.setDaemon(true);
		    return thread;
		}
	}

	private static final Logger _log =
        Logger.getLogger(MBeanSender.class.getName());
    private static final String UDP = "udp";
    private static final String RUNTIME_NAME =
        "java.lang:type=Runtime";

    private MBeanServerConnection _bs =
        ManagementFactory.getPlatformMBeanServer();

    private ScheduledExecutorService _scheduler =
        Executors.newScheduledThreadPool(1, new JcollectAgentFactory());

    private Map<String,Sender> _senders =
        new HashMap<String,Sender>();

    private MBeanConfig _config = new MBeanConfig();

    private String _instanceName;
	private boolean active = true;

    public void setMBeanServerConnection(MBeanServerConnection server) {
        _bs = server;
    }

    public MBeanServerConnection getMBeanServerConnection() {
        return _bs;
    }

     
    private String getRuntimeName() {
        try {
            ObjectName name = new ObjectName(RUNTIME_NAME); 
            String attribute = (String)getMBeanServerConnection().getAttribute(name, "Name");
            String retval = attribute;
            //retval = //trunc(attribute);
            {
        		 
        		long MAX_ALLOWED_PID = 3L; // Long.MAX_VALUE - unlimited, 4- will be splitted for only 4 dif pids.
        		String[] pidElementsTmp = attribute.split("@");
        		Long pidTmp = Long.parseLong(pidElementsTmp[0]);
        		pidTmp = pidTmp%MAX_ALLOWED_PID;
        		  retval = pidTmp+"@"+pidElementsTmp[1];
        		 
        	}
			return retval;
        } catch (Exception e) {
            return ManagementFactory.getRuntimeMXBean().getName();
        }    
    }

//	private String trunc(String attribute) {
//		String retval;
//		long MAX_ALLOWED_PID = 3L; // Long.MAX_VALUE - unlimited, 4- will be splitted for only 4 dif pids.
//		String[] pidElementsTmp = attribute.split("@");
//		Long pidTmp = Long.parseLong(pidElementsTmp[0]);
//		pidTmp = pidTmp%MAX_ALLOWED_PID;
//		  retval = pidElementsTmp[0]+"@"+pidElementsTmp[1];
//		return retval;
//	}

    public String getInstanceName() {
        if (_instanceName == null) {
            _instanceName = Network.getProperty("instance", getRuntimeName());
        }
        return _instanceName;
    }

    public void setInstanceName(String instanceName) {
        _instanceName = instanceName;
    }

    public void schedule(MBeanCollector collector) {
        collector.setSender(this);
        _scheduler.scheduleAtFixedRate(collector, 0,
                                       collector.getInterval(),
                                       TimeUnit.SECONDS);
    }

    public void addSender(String protocol, Sender sender) {
        _senders.put(protocol, sender);
    }

    public void addDestination(String url) {
        final String psep = "://";
        int ix = url.indexOf(psep);
        if (ix == -1) {
            throw new IllegalArgumentException("Malformed url: " + url);
        }
        String protocol = url.substring(0, ix);
        String server = url.substring(ix + psep.length());
        Sender sender = _senders.get(protocol);
        if (sender == null) {
            if (protocol.equals(UDP)) {
                sender = new UdpSender();
                addSender(UDP, sender);
            }
            else {
                throw new IllegalArgumentException("Unsupported protocol: " + protocol);
            }
        }
        sender.addServer(server);
    }

    public MBeanCollector scheduleTemplate(String name) {
        MBeanCollector collector = null;
        try {
            //check for file via path and classpath,
            //e.g. "javalang", "javalang-jcollectd.xml"
            collector = _config.add(name);
        } catch (Exception e) {
            _log.log(Level.WARNING, "add template " + name +
                     ": " + e.getMessage(), e);
        }
        if (collector != null) {
            schedule(collector);
        }
        return collector;
    }

    public MBeanCollector scheduleMBean(String name) {
        try {
            new ObjectName(name);
        } catch (MalformedObjectNameException e) {
            _log.log(Level.WARNING, "add MBean " + name +
                     ": " + e.getMessage(), e);
            return null;
        }
        MBeanCollector collector = new MBeanCollector();
        collector.addMBean(name);
        schedule(collector);
        return collector;
    }

    public MBeanCollector schedule(String name) {
        MBeanCollector collector = scheduleTemplate(name);
        if (collector == null) {
            //assume ObjectName, e.g. "sigar:*"
            collector = scheduleMBean(name);
        }
        return collector;
    }
    
    public void dispatch(Notification notification) {
        for (Sender sender : _senders.values()) {
            sender.dispatch(notification);
        }
    }

    public void dispatch(ValueList values) {
        for (Sender sender : _senders.values()) {
            sender.dispatch(values);
        }
    }

    public void flush() throws IOException {
        for (Sender sender : _senders.values()) {
            sender.flush();
        }
    }

    @SuppressWarnings("deprecation")
	public void shutdown() {    
    	this.destroy(); 
        _scheduler.shutdownNow();
        try{
        	hookToVictim.stop();
        }catch (Exception e) {
			// TODO: handle exception
        	// e.  printStackTrace();
		}
        
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                shutdown();
            }
        });
    }

    public void configure(Properties props) {
        //java -Djcd.dest=udp://localhost -Djcd.tmpl=javalang -Djcd.beans=sigar:*
        String dest = props.getProperty("jcd.dest");
        if (dest != null) {
            addDestination(dest);
        }
        String tmpl = props.getProperty("jcd.tmpl");
        if (tmpl != null) {
            for (String t : tmpl.split(",")) {
                scheduleTemplate(t);
            }
        }
        String beans = props.getProperty("jcd.beans");
        if (beans != null) {
            for (String b : beans.split("#")) {
                scheduleMBean(b);
            }
        }
    }

    protected void init(String args) {
        if (args == null) {
            return;
        }
        //java -javaagent:collectd.jar="udp://localhost#javalang" -jar sigar.jar
        String[] argv = args.split("#");
        for (int i=0; i<argv.length; i++) {
            String arg = argv[i];
            if (arg.indexOf("://") != -1) {
                //e.g. "udp://address:port"
                addDestination(arg);
            }
            else {
                schedule(arg);
            }
        }        
    }

    protected void premainConfigure(String args) {
        addShutdownHook();
        Properties properties = System.getProperties();
		configure(properties);
        init(args);        
    }

    public static void premain(String args, Instrumentation instr) {
        MBeanSender sender = new MBeanSender();
        sender.premainConfigure(args);
    }

    public boolean isActive() { 
		return this.active;
	}
	
    protected boolean destroy() { 
		return this.active  = false;
	}
	
	
}
