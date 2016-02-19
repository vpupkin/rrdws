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

import org.jrobin.mrtg.MrtgConstants;

import java.io.File;

public class Config implements MrtgConstants {
	// various paths
	public static final String DELIM = System.getProperty("file.separator");  
	// basic File-Structure is:
	// ${user.dir}/
	// ${user.dir}/mrtg
	// ${user.dir}/mrtg/rrd
	// ${user.dir}/mrtg/conf	
	private static final String MRTG = "mrtg";
	public static final String RRD = "rrd";
	public static final String CONF = "conf";
	
	private static String HOME_DIR  =
			CALC_DEFAULT_WORKDIR()
			+DELIM + MRTG;

	public static final String GRAPH_TEMPLATE_XML = "graph_template.xml";
	public static final String RRD_TEMPLATE_XML = "rrd_template.xml";
	public static final String MRTG_DAT = "mrtg.dat";

	
	static boolean isInited = false;

//	
//	 
//	public static final String RRD_DIR  = HOME_DIR + RRD + DELIM;
//	public static final String HARDWARE_FILE = CONF_DIR + MRTG_DAT;
//	public static final String RRD_DEF_TEMPLATE_FILE = CONF_DIR + RRD_TEMPLATE_XML;
//	public static final String RRD_GRAPH_DEF_TEMPLATE_FILE = CONF_DIR + GRAPH_TEMPLATE_XML;
//
//	
	static {
		// create directories if not found
		try {
			new File(getConfDir()).mkdirs();
			try {
				new File(getRrdDir()).mkdirs();
				isInited = true;
			} catch (Exception e) {
				e.printStackTrace();
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}

	static String getHomeDir() { 
		return HOME_DIR;
	}

	public static final String CALC_DEFAULT_WORKDIR() {
		return  System.getProperty( "rrd.home",
				System.getProperty("catalina.base", 
				System.getProperty("user.dir")))
				+ (System.getProperty("catalina.base") == null ? null
						: "/work/Catalina/localhost/rrdsaas");
	}

	static String getConfDir() {
		return HOME_DIR + DELIM + CONF;
	}

	static String getRrdDir() {
		return  HOME_DIR + DELIM + RRD ;
	}

	static String getHardwareFile() {
        return getConfDir() + DELIM + MRTG_DAT;
	}

	static String getRrdTemplateFile() {
		return getConfDir() + DELIM + RRD_TEMPLATE_XML;
	}

	static String getGraphTemplateFile() {
		return getConfDir() + DELIM + GRAPH_TEMPLATE_XML;
	}
}
