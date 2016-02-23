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
import org.jrobin.mrtg.MrtgException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 

import java.io.IOException;

class SnmpReader   {
	static final int RECONFIGURE_RETRIES = 3;

	private Device router;
	private Port link;

	private Poller comm;

	private static final Logger log = LoggerFactory.getLogger("org.jrobin.mrtg.server.SnmpReader");

	SnmpReader(Device router, Port link) {
		
		link.setSampling(true);
		this.router = router;
		this.link = link;
	}

	/**
	 * @deprecated
	 * @author vipup
	 */
	public void run() {
		this.run(Thread.currentThread().getThreadGroup());
	}
	public void run(ThreadGroup ownTG) {
		String community = null;
		String host = null;
		String ifDescr = null;
		int ifIndex = -1 ;
		
		try {
			community = router.getCommunity();
			host = router.getHost();
			comm = new Poller(host, community);
			ifIndex = link.getIfIndex();
			if(ifIndex < 0) {
				findIfIndex();
			}
			if(ifIndex >= 0) {
				String oidTmp = link.getDescr();
				ifDescr = link.getIfDescr();
				if (oidTmp .indexOf("/")>=0){ // have to be 1.3.6.1.4.1.42.2.145.3.163.1.1.3.10.1.8.0.0.0.0.0.0.6.88
					oidTmp = comm.toNumericOID(ifDescr);
				} 
				
				
				Debug.print("Sampling: " + ifDescr + "@" + host + 	" [" + ifIndex + "]{"+oidTmp+"}"); 
				 
				String value  = null;
				if ("DEV_MOD".length()==7){
					value  = comm.getNextSNMPv2(oidTmp );
					RawSample sample = createRawSample(value);
					link.processSample(sample);
				}
				if (ifDescr == null)  throw new IllegalArgumentException("ifDescr IS NULL");
				if (link.getSnmpVersion() == 1)
					value  = comm.getSNMPv2(oidTmp );//TODO hotfixed into v2 !check v.1 value  = comm.get(ifDescr, ifIndex);
				else
					value  = comm.getSNMPv2(oidTmp );
				RawSample sample = createRawSample(value);
				link.processSample(sample);
			}
		}
		catch (IOException e) {
			Debug.print("IOException on " + getLabel() + ": " + e);
			deactivateLink(e, ownTG);
		} catch (MrtgException e) {
			deactivateLink(e, ownTG);			
			Debug.print("MrtgException on " + getLabel() + ": " + e);
		} catch (ArrayIndexOutOfBoundsException e) {
			deactivateLink(e,ownTG);
			Debug.print("MrtgException on " + getLabel() + ": " + e);
		} catch (java.lang.IllegalArgumentException	 e) {
			deactivateLink(e,ownTG);
			Debug.print("java.lang.IllegalArgumentException	" + getLabel() + ": " + e);
			
			
		} finally {
			if(comm != null) {
				comm.close();
			}
			link.setSampling(false);
		}
	}

	private void deactivateLink(Exception e, ThreadGroup ownTG) {
		String mesTmp = e.getMessage();
		String ifDescr= link.getIfDescr() ;
		String host = router.getHost();
		String community = router.getCommunity() ;
		
		try {
			link.error(e);
 				
				if (
						link.getErrorCount()>5 && 
						mesTmp .indexOf( "timed out")>0|| 
						mesTmp.indexOf("Bad OID")>=0 || 
						mesTmp.indexOf("No such instance")>=0 ||
						mesTmp.indexOf("No such name error")>=0 ||
						mesTmp .indexOf( "not available for retrieval")>0
					){ // autodiscover	
					String numericOid = link.getIfAlias(); 
					if (numericOid == comm.toNumericOID(numericOid) && numericOid .indexOf(".")==-1) {
						int endOfAlias = numericOid.lastIndexOf("/");
						String theOldOIDTmp = numericOid.substring(0,endOfAlias);
						numericOid = comm.toNumericOID(theOldOIDTmp); 	
					} 
					link.deactivate();
					// TODO should it be rediscovered on any SNMP error??
					if (2=="DO".length())
						IfDsicoverer.startDiscoverer(ownTG, host, community, numericOid, ifDescr);
					
				}else  if (link.getErrorCount()>1 && (mesTmp .indexOf( "timed out")>0 || mesTmp.indexOf("No such instance")>=0)){ // 2nd try via ver2
					link.setSnmpVersion(2);
					String oid = link.getIfAlias();
					String theNext = comm.getNextSNMPv2(oid); //oid = 	comm.toNumericOID(oid)			
					String descr = comm.getLastSymbol().getName();
					descr = descr==null?comm.getLastSymbol().getName()+"!"+link.getErrorCount()+"]":descr ;
					int samplingInterval= 60;
					boolean active = true;
					Server instanceTmp = Server.getInstance();
					int retcode = instanceTmp.addLink(host, ifDescr, 2, descr, samplingInterval, active );
					if (retcode == -2 ){ // already existing interface
						retcode = instanceTmp.addLink(host, ifDescr+"/"+descr, 2, oid, samplingInterval, active );
					}
					System.out.println("added??:::"+retcode);

				}else  if (link.getErrorCount()>32){
					link.deactivate();
					Debug.print(".. deactivated" + getLabel() + ": " + e);
				}
		 		
		} catch (MrtgException e1) {
			log.error("deactivateLink(Exception e)" ,  e1);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			log.error("deactivateLink(IOException e)" ,  e2);
		}catch (Throwable  e2) {
			// TODO Auto-generated catch block
			// e2. printStackTrace();
			log.error("deactivateLink(IOException e)" ,  e2);
		}
	}

  
	private void findIfIndex() throws MrtgException {
		for(int i = 0; i < RECONFIGURE_RETRIES; i++) {
			try {
				String ifDescr = link.getIfDescr();
				int ifIndex = comm.getIfIndexByIfDescr(ifDescr);
				if(ifIndex >= 0) {
					// new port number found
					String alias = comm.toNumericOID(  ifDescr ) +"."+ ifIndex ;
					link.setIfAlias(alias);
					link.switchToIfIndex(ifIndex);
					return;
				}
				else {
					// definitely no such interface
					break;
				}
			}
			catch(IOException ioe) {
				log.error("IOError while reconfiguring " + getLabel() + ": " , ioe);
			}
		}
		// new interface number not found after several retries
		link.deactivate();
		Debug.print("Link " + getLabel() + " not found, link deactivated");
	}

	private RawSample createRawSample(String value ) {
		RawSample sample = new RawSample();
		String host = router.getHost();
		sample.setHost(host);
		if(value != null) {
			sample.setValue(value) ;
			sample.setIfDescr(link.getIfDescr());
		}
 
		return sample;
	}

	String getLabel() {
		return link.getIfDescr() + "@" + router.getHost();
	}
}
