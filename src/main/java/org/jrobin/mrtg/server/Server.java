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
 

import org.jrobin.GraphInfo;
import org.jrobin.cmd.RrdCommander;
import org.jrobin.core.RrdDbPool;
import org.jrobin.core.RrdException;
import org.jrobin.mrtg.Debug;
import org.jrobin.mrtg.MrtgException;
import org.jrobin.mrtg.MrtgConstants; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ws.rrd.csv.Registry; 

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileWriter;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

public class Server implements MrtgConstants {
	private static Server instance;

	private DeviceList deviceList;
	private Date startDate;

	private Timer timer;
	//private RrdWriter rrdWriter;
	private Listener listener;

	private boolean active = false;

	private static final Logger log = LoggerFactory.getLogger("org.jrobin.mrtg.server.Server");

	public synchronized static Server getInstance() throws MrtgException {
		if (instance == null) {
			try {
				instance = new Server();
			} catch (RrdException e) {
				throw new MrtgException(e);
			}
		}
		return instance;
	}

	private Server() throws RrdException {
		//TODO RrdDb.setLockMode(RrdDb.NO_LOCKS);
		//RrdDbPool instance2 = RrdDbPool.getInstance();
		//instance2.setCapacity(POOL_CAPACITY);
	}

	public synchronized void start(String[] acceptedClients) throws MrtgException {
		if(active) {
			throw new MrtgException("Cannot start Server, already started");
		}
		// set default backend factory
//		try {
//			RrdDb.setDefaultFactory(BACKEND_FACTORY_NAME);
//		} catch (RrdException e) {
//			throw new MrtgException("Inavlide backend factory (" + BACKEND_FACTORY_NAME + ")");
//		}
		// create template files
		try {
			String rrdTemplateFile = Config.getRrdTemplateFile();
			createXmlTemplateIfNecessary(rrdTemplateFile, RRD_TEMPLATE_STR);
			String graphTemplateFile = Config.getGraphTemplateFile();
			createXmlTemplateIfNecessary(graphTemplateFile, GRAPH_TEMPLATE_STR);
		}
		catch(IOException ioe) {
			throw new MrtgException(ioe);
		}
		// load configuration
		String hwFile = Config.getHardwareFile();
		File fileTmp = new File(hwFile);
		if(fileTmp.exists()) {
			loadHardware();
		}
		else {
			saveHardware();
		}
		// create threads

		timer = new Timer();
		
		listener = new Listener(acceptedClients);
		startDate = new Date();
		active = true;
	}

	private void createXmlTemplateIfNecessary(String filePath, String fileContent)
		throws IOException {
		File fileTmp = new File(filePath);
		if(!fileTmp.exists()) {
			if (fileTmp.canWrite() && fileTmp.getParentFile().isDirectory()){
				FileWriter writer = new FileWriter(filePath, false); 
				writer.write(fileContent);
				writer.flush();
				writer.close();
			}else{ // TODO assumes we are in the ... sandbox...
				throw new IOException( "assumes we are in the ... sandbox...");
			}
		}
	}

	public synchronized void stop() throws MrtgException {
		if(!active) {
			throw new MrtgException("Cannot stop Server, not started");
		}

		try {
			timer.terminate();
		} catch (Exception e) {e.printStackTrace();		}
		try {
			listener.terminate();
		} catch (Exception e) {e.printStackTrace();		}		
		
		active = false;
		try {
			RrdDbPool pool = RrdDbPool.getInstance();
			pool.reset (); 
		} catch (RrdException e) {
			throw new MrtgException(e);
		}
	}
	private static int revisionCout = 0;
	private static long lastCommit = -1;
	
	synchronized void  saveHardware() throws MrtgException {
		if ((System.currentTimeMillis()-lastCommit  )>60*1000){// onece per min
			revisionCout ++;
			lastCommit = System.currentTimeMillis();
		}else{
			return ; 
		}
		if(deviceList == null) {
			deviceList = new DeviceList();
		}
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element root = doc.createElement("mrtg");
			doc.appendChild(root);
			Vector routers = deviceList.getRouters();
			for(int i = 0; i < routers.size(); i++) {
				Device router = (Device) routers.get(i);
				router.appendXml(root);
			}
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
			transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
			DOMSource source = new DOMSource(root);
			
			synchronized (File.class) {
				File fout = new File(Config.getHardwareFile());
				File foutTmp = File.createTempFile("mrtgstage", ".xml", fout.getParentFile());
				String msg = "store mrtg.conf into {"+fout.getAbsolutePath()+"}";
				log.debug( "saveHardware into ["+msg +"].."); 
				FileOutputStream destination = new FileOutputStream(foutTmp);
				StreamResult result = new StreamResult(destination);
				transformer.transform(source, result);
				destination.close();
				log.debug( ".done.");
			
				if (fout.exists()){// make backup
					File dest = new File (fout.getAbsolutePath()+".bak"+((System.currentTimeMillis()/1000)%24*60*60 ));
					fout.renameTo(dest );
				}
				foutTmp.renameTo(fout) ;
			}

		} catch (Exception e) {
			throw new MrtgException(e);
		}
	}

	private void loadHardware() throws MrtgException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			String hardwareFile = Config.getHardwareFile();
			log.debug("USING hardware file:"+hardwareFile);
			Document doc = builder.parse(new File(hardwareFile));
			Element root = doc.getDocumentElement();
			NodeList nodes = root.getElementsByTagName("router");
			deviceList = new DeviceList();
			Vector routers = deviceList.getRouters();
			for(int i = 0; i < nodes.getLength(); i++) {
				Node item = nodes.item(i);
				Device e = new Device(item);
				routers.add(e);
				log.debug("added device:"+e);
			}
		} catch (Exception e) {
			throw new MrtgException(e);
		}
	}

	public String toString() {
		return deviceList.toString();
	}

	synchronized int addRouter(String host, String community, String descr, boolean active)
		throws MrtgException {
		int retCode = deviceList.addRouter(host, community, descr, active);
		if(retCode == 0) {
			saveHardware();
		}
		return retCode;
	}

	synchronized int updateRouter(String host, String community, String descr, boolean active)
		throws MrtgException {
		int retCode = deviceList.updateRouter(host, community, descr, active);
		if(retCode == 0) {
			saveHardware();
		}
		return retCode;
	}

	synchronized int removeRouter(String host) throws MrtgException {
		int retCode = deviceList.removeRouter(host);
		if(retCode == 0) {
			saveHardware();
		}
		return retCode;
	}

	/**
	 * @deprecated
	 * 
	 * @author vipup
	 * @param host
	 * @param ifDescr
	 * @param descr
	 * @param samplingInterval
	 * @param active
	 * @return
	 * @throws MrtgException
	 */
	synchronized int addLink(String host, String ifDescr, String descr, int samplingInterval, boolean active)throws MrtgException{
		return addLink(host, ifDescr, 2, descr, samplingInterval, active);
	}
	
	synchronized int addLink(String host, String ifDescr, int snmpVer, String descr, int samplingInterval, boolean active)
		throws MrtgException {
		int retCode = deviceList.addLink(host, ifDescr, snmpVer, descr, samplingInterval, active);
		if(retCode == 0) {
			saveHardware();
		}
		return retCode;
	}

	synchronized int updateLink(String host, String ifDescr, String descr,
								int samplingInterval, boolean active)
		throws MrtgException {
		int retCode = deviceList.updateLink(host, ifDescr, descr, samplingInterval, active);
		if(retCode == 0) {
			saveHardware();
		}
		return retCode;
	}

	synchronized int removeLink(String host, String ifDescr) throws MrtgException {
		int retCode = deviceList.removeLink(host, ifDescr);
		if(retCode == 0) {
			saveHardware();
			if(REMOVE_RRD_FOR_DEACTIVATED_LINK) {
				// remove the underlying RRD file
        		String rrdFile = RrdWriter.getRrdFilename(host, ifDescr);
				new File(rrdFile).delete();
			}
		}
		return retCode;
	}

	synchronized byte[] getPngGraph(String host, String ifDescr, long start, long stop)
		throws MrtgException {
		byte[] graph = new byte[0];
		int _w = 480;
		String _t = ""+host+":"+ifDescr;
		String _v = "-";
	    
	    Registry reg = Registry.getInstance(); 
	    String path2RRDb = calPath2RRDb(host, ifDescr);
		String dbName = reg.getPath2db().get(path2RRDb);
		String _end = ""+stop;
		String _start = ""+start;
		int _h = 200;
		String cmdTmp = "rrdtool graph - -v '"+_v+"' -t '"+_t+"'  -h "+ _h +" -w  "+_w+" --start="+_start+"   --end="+_end+"  DEF:dbdata="+dbName+":data:AVERAGE  LINE2:dbdata#44EE4499  LINE1:dbdata#003300AA ";
		// bikoz of '-' in the filename :
		GraphInfo img;
		try {
			img = (GraphInfo)RrdCommander.execute(cmdTmp);
			graph = img.getBytes(); 
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (RrdException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
//		response.setHeader("Pragma","no-cache"); //HTTP 1.0
//		response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
//		response.setHeader("Content-Disposition", "inline;filename="+dbName+".gif");
//		%><%
		
		Debug.print("Graph for interface " + ifDescr + "@" + host +
			" generated [" + graph.length + " bytes]");
		return graph;
 	}

	public static final String calPath2RRDb(String host, String ifDescr) {
		String addrTmp =host .replace(":", "/");
		return "mrtg4j/"+ addrTmp +"/" +ifDescr;
	}

	synchronized Device[] getRouters() {
		Vector routers = deviceList.getRouters();
		return (Device[]) routers.toArray(new Device[0]);
	}

	String[] getAvailableLinks(String host) throws MrtgException {
		Device router = deviceList.getRouterByHost(host);
		try {
			if(router != null) {
				return router.getAvailableLinks();
			}
			else {
				return null;
			}
		} catch (IOException e) {
			if (2==2) e.printStackTrace();
			throw new MrtgException(e);
		}
	}

    DeviceList getDeviceList() {
		return deviceList;
	}

	RrdWriter getRrdWriter() {
		return timer.getRrdWriter();
	}

	Date getStartDate() {
		return startDate;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	Hashtable getServerInfo() {
		Hashtable hash = new Hashtable();
		RrdWriter rrdWriter = timer.getRrdWriter();
		hash.put("sampleCount", new Integer(rrdWriter.getSampleCount()));
		hash.put("savesCount", new Integer(rrdWriter.getSavesCount()));
		hash.put("goodSavesCount", new Integer(rrdWriter.getGoodSavesCount()));
		hash.put("badSavesCount", new Integer(rrdWriter.getBadSavesCount()));
		hash.put("startDate", startDate);
		//TODO new Double(RrdDbPool.getInstance().getPoolEfficency())
		hash.put("poolEfficency", new Double(Math.PI));
		return hash;
	}

	public static void main(String[] acceptedClients) throws Exception {
        Server s = Server.getInstance();
        acceptedClients = acceptedClients==null?new String[]{}:acceptedClients;
		s.start(acceptedClients);
	}

}

