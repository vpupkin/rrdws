package org.jrobin.mrtg.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibValueSymbol;

import org.jrobin.mrtg.MrtgException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  22.07.2011::15:19:37<br> 
 */
public class IfDsicoverer implements Runnable{
	private String host;
	private String community;
	private String numOID;
	private String ifDescr;
	private int id;
	private String key;
	private static  Logger log = LoggerFactory.getLogger("org.jrobin.mrtg.server.IfDsicoverer");
	
	//new Poller(host, community)
	java.util.Queue<Poller> queue;

	/**
	 * @author vipup
	 * @param snmpReader
	 * @throws IOException 
	 */
	IfDsicoverer(String hostPar, String communityPar, String numericOid,
			String ifDescr) throws IOException {
		queue = new LinkedList<Poller>();
		
		this.host = hostPar;
		this.community = communityPar;
		this.numOID = numericOid;
		this.ifDescr = ifDescr;
		
		addTraper(this.host, this.community );
	}
	private void addTraper(String hostPar, String communityPar) throws IOException {
		Poller theFirstOne = new Poller(hostPar, communityPar);
		queue.add(theFirstOne);
	} 
	public final static int MAX_CHECK_QUEUE = 123;// (sec) 2 Minutes should be enough for 1 try?
	private int checkCounter = 0;
	
	/**
	 * gives back progress state in percents
	 * 
	 * @return
	 */
	public int getProgressStatus(){
		return checkCounter / MAX_CHECK_QUEUE * 100;
	}
	
	boolean isAlive = true;
	/**
	 * finish discovering automatically after fixed number of tries..
	 */
	public void run() {
		while(isAlive ){ 
			if (queue.isEmpty()){
				try {
					Thread.sleep(1001);
					checkCounter ++;
					isAlive = isAlive && checkCounter <MAX_CHECK_QUEUE;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{ 
				try{
					Poller commTmp = queue.poll();
					performDiscovery(commTmp);
					
				}catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}	
			}
		}
		// clean up the pool
		discovererPool.remove(this.key);		
	}
	private void performDiscovery(Poller commPar ) {	
		
		System.out.println("##"+id+"##"+key+":: Discovering started for:"+commPar);

		try {
			
			Server instanceTmp = Server.getInstance();
			MibValueSymbol lastSymbol = null;
			try{
				lastSymbol  = commPar.getLastSymbol();
			}catch(Throwable e){
				//e.printStackTrace();
			}
			if (lastSymbol==null){
				try{// TODO lets statrt from start ?
					commPar.getNextSNMPv2( commPar.getNumericOid( "jvmMgtMIB"));
					lastSymbol = commPar.getLastSymbol();
				}catch(Throwable e){e.printStackTrace();}
			}
			String descr = lastSymbol.getName();
			descr = descr==null?lastSymbol.getName()+"!"+"]":descr ;
			int samplingInterval= 60;
			boolean active = true;					
			String lastKey = null;
			int retcode = -1;
			for (String retvLTmp = commPar.getNextSNMPv2( numOID);lastKey !=""+commPar.getLastOID();numOID = ""+commPar.getLastOID()){
				String iPrefixTmp = ifDescr.substring(0, ifDescr.lastIndexOf("/"));
				String ifPathTmp = iPrefixTmp+"/"+descr;
				String chk_OID_tmp = commPar.toNumericOID(ifPathTmp);
				numOID = ""+commPar.getLastOID();
				try{ // add only Digital-metricas
					double valTmp = Double.parseDouble( retvLTmp );
					String pathToAddTmp = toXName(commPar);
					// store discovered link
					retcode = instanceTmp.addLink(host, pathToAddTmp, 2, numOID, samplingInterval, active );
					
					while (retcode ==-255){// // there are ling with path BUT! diff OID
					// try to combite path+OID as a new path...
						DeviceList deviceList = instanceTmp.getDeviceList();
						Device routerByHost = deviceList.getRouterByHost(host);
						Port pTmp = routerByHost.getLinkByIfDescr(pathToAddTmp);
						String pOIDTmp = pTmp.getIfAlias();
						// logical numOID - pOIDTmp:
						Mib mib = commPar.getLastSymbol().getMib();
						MibValueSymbol symbolByOidTmp = mib.getSymbolByOid(numOID);
						String prefixTmp = ""+symbolByOidTmp.getValue();
						int pLengthTmp = prefixTmp.length();
						String suffixTmp = numOID.substring(pLengthTmp) ;
						pathToAddTmp = pathToAddTmp +"/["+suffixTmp+"]"; 
						retcode = instanceTmp.addLink(host, pathToAddTmp, 2, numOID, samplingInterval, active ); 
					}
					if (retcode ==0)
						log.debug("+++ chkOID:{}=={}::{}[5:{}]4:{}3:{}2:{}1:{} ",new Object[]{chk_OID_tmp,retcode,retvLTmp, valTmp, numOID ,ifPathTmp});
					else
						log.trace( "... numOID:{} = {}" , numOID, retcode  );
					
				}catch(Throwable eDDD){
					log.trace( chk_OID_tmp, eDDD );					
				}
				// skip (1)
				retvLTmp = commPar.getNextSNMPv2( numOID);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MrtgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private String toXName(Poller commPar) throws IOException {
		MibValueSymbol lastSymbol;
		String pathToAdd = "";
		lastSymbol = commPar.getLastSymbol() ;
		for (String nextNameTmp = lastSymbol.getName();lastSymbol!=null;lastSymbol= lastSymbol.getParent()){
			nextNameTmp = lastSymbol.getName();
			pathToAdd = "/"+nextNameTmp+pathToAdd;
			
		}
		return pathToAdd;
	}
	
	
	private static int dCounter = 0;

	final static Map<String, IfDsicoverer > discovererPool = new HashMap<String, IfDsicoverer>();
	/**
	 * initiate the new Discovery-Thread for given host/comunity
	 * 
	 * @author vipup
	 * @param tgPar
	 * @param hostPar
	 * @param communityPar
	 * @param numericOid
	 * @param ifDescrPar
	 * @throws IOException
	 */
	public static void startDiscoverer(ThreadGroup tgPar, String hostPar, String communityPar,	String numericOid, String ifDescrPar) throws IOException {
		String keyTmp = hostPar +"::"+communityPar;
		synchronized (discovererPool) {
			IfDsicoverer theT  =  discovererPool.get(keyTmp );
			if (theT  == null){
				IfDsicoverer newDiscoverer = new IfDsicoverer(hostPar, communityPar, numericOid, ifDescrPar);
				dCounter++;
				newDiscoverer.setId(dCounter);
				newDiscoverer.setKey(keyTmp);
				String sDiscNameTmp = "Discoverer#"+newDiscoverer.getId()+" :"+hostPar;
				Thread t2Run = new Thread(tgPar, newDiscoverer, sDiscNameTmp);
				t2Run.setDaemon(true);
				discovererPool.put(keyTmp, newDiscoverer );
				synchronized (keyTmp) {
					t2Run.start();
				}
			}else{
				log.debug("duplicate SNMP-Discovery-request //["+keyTmp+"]// will be ignored.");
			}
		}		
	}
	
	public static void stopAll(){
		for(IfDsicoverer next:discovererPool.values()){
			next.terminate();
		}
	}
	
	private void terminate() {
		 this.isAlive = false;
	}
	
	
	public static String[] listDiscoverer(){
		ArrayList<String> list = new ArrayList<>();
		for(IfDsicoverer next:discovererPool.values()) {
			String e = next.host+":"+next.community+":"+next.ifDescr+":"+next.numOID+":::"+next.getProgressStatus() +"%";
			list.add(e );
		}
		return list.toArray(new String  []{});
	}
	
	private void setKey(String key) {
		this.key = key;
	}
	private int getId() { 
		return id;
		 
	}
	private void setId(int i) {
		id = i;
	}
	
}

 