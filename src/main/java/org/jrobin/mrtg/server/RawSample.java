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
class RawSample {
	private String host;
	private String ifDescr = null;
	private String value = null;
	public void setHost(String host2) {
		this.host = host2;
	}
	public String getIfDescr() { 
			return ifDescr;
	}
	public void setIfDescr(String ifDescr) {
		this.ifDescr = ifDescr;
	}
	public String getValue() { 
			return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getHost() { 
			return host;
	}
 
	public String toString(){
		//[Thu Mar 31 16:54:07 CEST 2011] c1263ce0/java.lang/hudson-Memory/gauge/Heap_init=[value=1.6777216E7]gauge.Heap_init := 1.6777216E7
		return ""+host+":"+ifDescr+"=[value="+value+"]";
	}
 }
