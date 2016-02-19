 
package cc.co.llabor.system;    
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean; 
import java.util.Map;
import java.util.Properties; 
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;    
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.collectd.DataWorker; 
import org.jrobin.core.RrdDbPool;
import org.jrobin.core.RrdException;
import org.jrobin.mrtg.MrtgException;
import org.jrobin.mrtg.server.Config;
import org.jrobin.mrtg.server.IfDsicoverer;
import org.jrobin.mrtg.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;    

import ws.rrd.csv.RrdKeeper;
import ws.rrd.logback.ServletListener;
import cc.co.llabor.features.Repo;
import cc.co.llabor.threshold.AlertCaptain;
import cc.co.llabor.threshold.TholdException;
import cc.co.llabor.threshold.rrd.Threshold; 

public class StartStopServlet extends HttpServlet {
	
	private static final String NOT_ENABLED = "NOT_ENABLED";
	private static final String BROCKEN = "brocken ::";
	/**
	 * @author vipup
	 */
	private static final long serialVersionUID = -3432681267977857824L;
	private static final String SUCCESSFUL = "SUCCESSFUL";
	private static Logger log = LoggerFactory.getLogger(cc.co.llabor.system.StartStopServlet.class);


	
	ServerLauncher serverLauncher;
	 

	DataWorker worker = null;
	public static boolean isGAE() {
		return !(System.getProperty("com.google.appengine.runtime.version")==null);
	}
	 
	static Map<String, String> status = new TreeMap<String, String>(); 
	
	public void init(ServletConfig config) throws ServletException{
		 
		try {
			status.put("initShutdownHook", initShutdownHook()); 
		} catch (Exception e) {
			status.put("initShutdownHook", BROCKEN+e.getMessage());
			log.error("RRD initShutdownHook : ", e);
		}catch(Throwable e){
			status.put("initShutdownHook", BROCKEN+e.getMessage());
			log.error("RRD initShutdownHook : ", e);			
			e.printStackTrace();
		}	
		
		if ( !isGAE()){
				String[] arg0=new String[]{};
			// collectd SERVER
			status.put("collectd-SERVER", startCollectdServer(arg0) );
			// collectd CLIENT (agent)
			status.put("collectd-CLIENT", startColelctdClient() );
			// start collectd queue-worker
			status.put("collectd-Worker", startCollectdWorker() );
		}		
		
		try{
			 
			status.put("MrtgServer (SNMP-backend)", 	startMrtgServer()  );
				 
		}catch(Throwable e){
			status.put("MrtgServer (SNMP-backend)", BROCKEN+e.getMessage());
			log.error("MrtgServer (SNMP-backend)", e.getMessage());		
			e.printStackTrace();
		}
		   
		 
		// do exactly the same as prev-WatchDog, but otherwise
//		Cache tholdRepo = Manager.getCache("thold");
//		Object tholdProps = tholdRepo.get("default.properties");//RRDHighLimitWatchDog
		Thread.currentThread().setContextClassLoader(RrdKeeper.class.getClassLoader());
		try {
//			log.info(Repo.getBanner( "tholdHealthWatchDog"));
//			
//			Threshold watchDog  = ac.toThreshold(tholdProps );
//			ac.register(  watchDog );
//			lookInsideThold(tholdProps);
			AlertCaptain ac = AlertCaptain.getInstance(ServletListener.getDefaultThreadGroup());
			
			ac.init();		
			status.put("AlertCaptain", SUCCESSFUL );
			 
		} catch (Throwable e) {
			status.put("AlertCaptain", BROCKEN+e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		System.out.println("................................." );
		System.out.println("................................." );
		System.out.println(".   status :"+status  );
		System.out.println("................................." );
		System.out.println("................................." );
		super.init(config); 
	}
 
	
	/**
	 * use the log4j-similar semantic for tholds-definition:
	 * 
	 * 
	 * #CacheEntry stored at 1317918241876
	 * #Thu Oct 06 18:24:01 CEST 2011
	 * datasource=test.rrd
	 * dsName=speed
	 * actionArgs=hiLog4J @{}\#{} {} ,{} 
	 * spanLength=600
	 * class=cc.co.llabor.threshold.Log4JActionist
	 * action=log4j
	 * BaseLine=0.0
	 * monitorArgs=\!(dvalue > 1 && dvalue < 111)
	 * monitorType=mvel
	 * A.datasource=test.rrd
	 * A.dsName=speed
	 * A.actionArgs=hiLog4J @{}\#{} {} ,{} 
	 * A.spanLength=600
	 * A.class=cc.co.llabor.threshold.Log4JActionist
	 * A.action=log4j
	 * A.BaseLine=1.0
	 * A.monitorArgs=\!(dvalue > 1 && dvalue < 111)
	 * A.monitorType=mvel
	 * B.datasource=test.rrd
	 * B.dsName=speed
	 * B.actionArgs=hiLog4J @{}\#{} {} ,{} 
	 * B.spanLength=600
	 * B.class=cc.co.llabor.threshold.Log4JActionist
	 * B.action=log4j
	 * B.BaseLine=2.0
	 * B.monitorArgs=\!(dvalue > 1 && dvalue < 111)
	 * B.monitorType=mvel
	 */
	void lookInsideThold(Object tholdProps) throws TholdException {
		for(Object  key: ((Properties )tholdProps).keySet()){
			String keyTmp = ""+key;
			if(keyTmp.endsWith("."+Threshold.BASE_LINE)){ // one more "thold-def" inside
				String prefixTmp = (""+key).substring(0, keyTmp.length() - Threshold.BASE_LINE.length());
				System.out.println(prefixTmp);
				Properties pSubset = new Properties();
				for(Object  keyWithPrefix: ((Properties )tholdProps).keySet()){
					if ((""+keyWithPrefix).indexOf( prefixTmp) == 0){
						String keyToStore = (""+keyWithPrefix).substring(prefixTmp.length());
						String valToStore = ((Properties )tholdProps).getProperty(""+keyWithPrefix);
						pSubset.setProperty(keyToStore, valToStore);
						
					}
				} 
				Threshold watchDogTmp  = AlertCaptain.toThreshold(pSubset );
				AlertCaptain acTmp = AlertCaptain.getInstance();
				acTmp.register(  watchDogTmp );
				
			} 
		}
	}

	/**
	 * 
	 * when JVM gets Sun-SNMP start-parameters, then own JVM-snmp agent will be 
	 * discovered.
	 * 
	 * otherwise the localhost:161 (standart SNMP)will be discovered.  
	 * 
	 * @author vipup
	 * @throws IOException
	 */
	private void checkAutodiscoveringRequestSNMP() throws IOException{
		RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean(); 
		// grep com.sun.management.snmp
		String ifDescr ="/jvmMgtMIB/";
		String numericOid =".0";
		String communityPar="public";
		String hostPar="127.0.0.1:161";
		for(String arg:RuntimemxBean.getInputArguments()) { 
			if (arg.indexOf("com.sun.management.snmp.interface")>=0){
				hostPar = arg.substring(arg.indexOf("=")+1)+ hostPar.substring(hostPar.indexOf(":"));
			}
			if (arg.indexOf("com.sun.management.snmp.port")>=0){
				hostPar = hostPar.substring(0, hostPar.indexOf(":")+1)+arg.substring(arg.indexOf("=")+1);
			} 
		} 
		// ala 234.234.234.234:16161
		// self SNMP-discover  ONLY i case, when  JVM has java-SNMP-params
		boolean autoDiscoverEnabled =hostPar.indexOf(":")>0 && hostPar.indexOf(".")>0&& hostPar.lastIndexOf(".")>0&& hostPar.lastIndexOf(".")>hostPar.indexOf(".") ;
		if (autoDiscoverEnabled ){
			initAutoDiscover(hostPar, communityPar, numericOid, ifDescr);
		}
		
	}
	
	private boolean isMRTGEnabled() throws IOException {
		boolean retval = true; 
		try{
			String strTmp = System.getProperty("mrtg4j", "true");
			retval = Boolean.parseBoolean(strTmp );
		}catch(Exception e){ // ignore any errors
			e.printStackTrace();
		}
		return retval;
		
	}	

	private String startMrtgServer() throws Exception { 
		log_info(Repo.getBanner("mrtgServer"));
		 String[] acceptedClients = new String[]{};
		//jrobin/mrtg/server/Server
		try {
			if (!isMRTGEnabled())return NOT_ENABLED;
			Server.main(acceptedClients);
			
			checkAutodiscoveringRequestSNMP();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
			throw e;
		}
		return SUCCESSFUL;
	}
	
	private void initAutoDiscover(String hostPar, String communityPar, String numericOid, String ifDescr) throws IOException{
		log_info("SNMP autodiscovery started for: host{},community{} from OID:{} || {}" ,new Object[]{hostPar,  communityPar,  numericOid,  ifDescr });
		log_info("SNMP autodiscovery started for: host{},community{} from OID:{} || {}" ,new Object[]{hostPar,  communityPar,  numericOid,  ifDescr });
		log_info("SNMP autodiscovery started for: host{},community{} from OID:{} || {}" ,new Object[]{hostPar,  communityPar,  numericOid,  ifDescr });
		log_info("SNMP autodiscovery started for: host{},community{} from OID:{} || {}" ,new Object[]{hostPar,  communityPar,  numericOid,  ifDescr });
		log_info("SNMP autodiscovery started for: host{},community{} from OID:{} || {}" ,new Object[]{hostPar,  communityPar,  numericOid,  ifDescr });
		log_info("SNMP autodiscovery started for: host{},community{} from OID:{} || {}" ,new Object[]{hostPar,  communityPar,  numericOid,  ifDescr });
		log_info("SNMP autodiscovery started for: host{},community{} from OID:{} || {}" ,new Object[]{hostPar,  communityPar,  numericOid,  ifDescr });
		log_info("SNMP autodiscovery started for: host{},community{} from OID:{} || {}" ,new Object[]{hostPar,  communityPar,  numericOid,  ifDescr });
		IfDsicoverer.startDiscoverer(ServletListener.getDefaultThreadGroup(), hostPar, communityPar, numericOid, ifDescr);
	}




	/**
	 * @author vipup
	 * @return 
	 */
	private String startCollectdWorker() { 
		//rrdDataWorker.dat
		log_info(Repo.getBanner( "rrdDataWorker"));
			worker = new DataWorker(); 
			Thread t1 = new Thread(ServletListener.getDefaultThreadGroup(), worker, "rrd DataWorker");
			t1.setDaemon(true);
			t1.start();
		return SUCCESSFUL; 	
	}

	
	/**
	 * @author vipup
	 * @param arg0
	 */
	private String startCollectdServer(final String[] arg0) {
		String retval = SUCCESSFUL;
		log_info(Repo.getBanner( "collectServer"));
		serverLauncher = new ServerLauncher(arg0);
		ThreadGroup dtgTmp = ServletListener.getDefaultThreadGroup();
		Thread t1 = new Thread ( dtgTmp , serverLauncher, "jcollectd_Server");
		t1.setDaemon(true);
		t1.start();	
		return retval;
	}

	private void log_info(String s) {
		if (log!=null) log.info(s);
		else System.out.println(s);
	}

	private void log_info(String string, Object[] objects) {
		if (log!=null) log.info(string,objects);
		else System.out.println(string+":::[]{}"+objects);
	}
	//TODO the only one ??
	ClientLauncher clientLauncher;
	/**
	 * @author vipup
	 * @return 
	 */
	private String startColelctdClient() {
		log_info(Repo.getBanner( "collectClient"));
		clientLauncher = new ClientLauncher() ;
		ThreadGroup dtgTmp = ServletListener.getDefaultThreadGroup();
		Thread t1 = new Thread ( dtgTmp , clientLauncher, "collectdCLIENTstater.TMP");
		t1.setDaemon(true);
		t1.start();
		return SUCCESSFUL;
	}

    /**
	 * This method seths a ShutdownHook to the system
	 *  This traps the CTRL+C or kill signal and shutdows 
	 * Correctly the system.
     * @return 
	 * @throws Exception
	 */ 
	 public String initShutdownHook() throws Exception {
		Runtime.getRuntime().addShutdownHook(new Thread("rrd.ShutdownHook") {
			public void run() {
				doStop();
			}
		});
		return SUCCESSFUL;
	}
     
    public void destroy() {
        doStop();
    }
    
    public void doStop() {
		log_info("Shutting down...");
		serverLauncher.destroyServer();
		// close all RRDs..
		RrdDbPool instance;
		try {
			instance = RrdDbPool.getInstance();
			instance.reset();

			worker.kill();
		} catch (RrdException e1) {
			log.error("doStop() failed", e1);
		} 
 	 
		try {
			Server.getInstance().stop();
		} catch (MrtgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			ThreadGroup mythreads = ServletListener.getDefaultThreadGroup();
			AlertCaptain.getInstance(mythreads ).setAlive(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			clientLauncher.killProcessTree();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		try {
			RrdKeeper.getInstance().destroy();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		log_info(Repo.getBanner( "+rrdws"));
		log_info("Stoped");
	}


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.getWriter().write(""+this.status);
	}
}
