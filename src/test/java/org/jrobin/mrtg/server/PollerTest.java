package org.jrobin.mrtg.server;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.SortedMap;

import net.percederberg.mibble.MibType;

import org.junit.Test;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  08.07.2011::13:44:07<br> 
 */
public class PollerTest {

	@Test
	public void testWalk() throws IOException {
		Poller p = new Poller("localhost:1616","public");
		String base = "jvmMgtMIB";
		SortedMap oTmp = p.walk(base );
		assertNotNull(oTmp);
		
		String numericOid = ""+oTmp.firstKey();
		assertNotNull(numericOid);
		String retvLTmp = null;
		try{
			retvLTmp = p.getSNMPv1(numericOid);
			fail("root of jvmMgtMIB have to throw at least NoNumberException");
		}catch(Exception e){
			retvLTmp = p.getNextSNMPv1(numericOid);
			
		}
		assertNotNull("NULL by retrieve {"+numericOid+"}!",retvLTmp );
		System.out.println(p.getLastSymbol() +" == "+retvLTmp);
		
	}
	
	@Test
	public void testWalk_v2() throws IOException {
		Poller p = new Poller("localhost:1616","public");
		String base = "jvmMgtMIB";
		SortedMap oTmp = p.walk(base );
		assertNotNull(oTmp);
		
		String numericOid = ""+oTmp.firstKey();
		assertNotNull(numericOid);
		String retvLTmp = null;
		try{
			retvLTmp = p.getSNMPv2(numericOid);
			fail("root of jvmMgtMIB have to throw at least NoNumberException for :"+numericOid+" == "+retvLTmp);
		}catch(Exception e){
			//// e.  printStackTrace();
			retvLTmp = p.getNextSNMPv2(numericOid);
			
		}
		assertNotNull("NULL by retrieve {"+numericOid+"}!",retvLTmp );
		System.out.println(p.getLastSymbol() +" == "+retvLTmp);
		
	}
	
	@Test
	public void testWalkTree() throws IOException{
		Poller p = new Poller("localhost:1616","public");
		System.out.println("SnmpPoolWalk  JUNit output.");
		String base = "jvmMgtMIB";
		SortedMap oTmp = p.walk(base );		
		String numericOid = ""+oTmp.firstKey();
		// getting full tree...
		String lastKey = null;
		for (String retvLTmp = p.getNextSNMPv1(numericOid);lastKey !=""+p.getLastOID();numericOid = ""+p.getLastOID()){
			MibType type = p.getLastSymbol().getType();
			String typeName = type.toString().split(" ")[6] ;//getName();
			// OctetString
			typeName = typeName.equals("OCTET")? "OctetString":typeName;
			// Type=OID
			typeName = typeName.equals("OBJECT")?type.toString().split(" ")[7]:typeName;
			typeName = typeName.equals("IDENTIFIER\n")?"OID":typeName;
			
			
			System.out.println("OID=."+numericOid +", Type="+typeName+", Value="+retvLTmp );
			try{
				retvLTmp = p.getNextSNMPv1(numericOid);
			}catch(Throwable e){
				break;
			}
		}

	}


	@Test
	public void testWalkTree_v2() throws IOException{
		Poller p = new Poller("localhost:1616","public");
		System.out.println("SnmpPoolWalk V2 V2 V2 V2 V2 V2 V2 JUNit output.");
		String base = "jvmMgtMIB";
		SortedMap oTmp = p.walk(base );		
		String numericOid = ""+oTmp.firstKey();
		// getting full tree...
		String lastKey = null;
		String lastName = null;
		try{
			for (String retvLTmp = p.getNextSNMPv2( numericOid);lastKey !=""+p.getLastOID();numericOid = ""+p.getLastOID()){
				MibType type = p.getLastSymbol().getType();
				String typeName = type.toString().split(" ")[6] ;//getName();
				// OctetString
				typeName = typeName.equals("OCTET")? "OctetString":typeName;
				// Type=OID
				typeName = typeName.equals("OBJECT")?type.toString().split(" ")[7]:typeName;
				typeName = typeName.equals("IDENTIFIER\n")?"OID":typeName;
				
				if (lastName != p.getLastSymbol().getName()){
					lastName = p.getLastSymbol().getName();
					System.out.println(  lastName);
				}
				System.out.println("OID=."+numericOid +", Type="+typeName+", Value="+retvLTmp );
				try{
					retvLTmp = p.getNextSNMPv2( numericOid);
				}catch(Throwable e){
	//				/// e.  printStackTrace();
					break;
				}
				System.out.print("");
			}
		}catch(Throwable e){
			// e.  printStackTrace();
		}

	}
	
	@Test
	public void testWalkIfDescr() throws IOException {
		Poller p = new Poller("localhost:1616","public");
		SortedMap oTmp = p.walkIfDescr();
		assertNotNull(oTmp);		
	}

	@Test
	public void testWalkANDGETTree_v2() throws IOException{
		Poller p = new Poller("localhost:1616","public");
		System.out.println("SnmpPoolWalk V2 V2 V2 V2 V2 V2 V2 JUNit output.");
		String base = "jvmMgtMIB";
		SortedMap oTmp = p.walk(base );		
		String numericOid = ""+oTmp.firstKey();
		// getting full tree...
		String lastKey = null;
		String lastName = null;
		try{
			for (String retvLTmp = p.getNextSNMPv2( numericOid);lastKey !=""+p.getLastOID();numericOid = ""+p.getLastOID()){
				MibType type = p.getLastSymbol().getType();
				String typeName = type.toString().split(" ")[6] ;//getName();
				// OctetString
				typeName = typeName.equals("OCTET")? "OctetString":typeName;
				// Type=OID
				typeName = typeName.equals("OBJECT")?type.toString().split(" ")[7]:typeName;
				typeName = typeName.equals("IDENTIFIER\n")?"OID":typeName;
				
				if (lastName != p.getLastSymbol().getName()){
					lastName = p.getLastSymbol().getName();
					System.out.println(  lastName);
				}
				System.out.println("OID=."+numericOid +", Type="+typeName+", Value="+retvLTmp );
				
				String justGetVal = p.getSNMPv2( ""+ p.getLastOID() );//p.getSNMPv2(numericOid+".0");
				System.out.println("#2D=."+numericOid +", Type="+typeName+", Value="+justGetVal );
				
				//assertEquals(retvLTmp, justGetVal);
				
				try{
					retvLTmp = p.getNextSNMPv2(numericOid);
				}catch(Throwable e){
	//				/// e.  printStackTrace();
					break;
				}
				System.out.print("");
			}
		}catch(Throwable e){
			// e.  printStackTrace();
		}
	
	}

}


 