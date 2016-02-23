/*
 * SNMP Inquisitor
 *
 * Copyright (C) 2002, Jonathan Sevy <jsevy@mcs.drexel.edu>
 *
 * This is free software. Redistribution and use in source and binary forms, with
 * or without modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation 
 *     and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products 
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package snmp;

import java.util.*;
import java.math.*;
import java.net.*;



public class TestSNMP
{

	public static void main(String args[]) 
	{

		try
		{

			byte[] encoding;
			
			// instantiate and check out BER encodings of various types
			SNMPObject snmpObject;
			
			snmpObject = new SNMPCounter32(127);
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			snmpObject =SNMPBERCodec.extractEncoding(SNMPBERCodec.extractNextTLV(snmpObject.getBEREncoding(),0));
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			printBEREncoding(snmpObject);
			
			snmpObject = new SNMPCounter64(128*128);
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			snmpObject =SNMPBERCodec.extractEncoding(SNMPBERCodec.extractNextTLV(snmpObject.getBEREncoding(),0));
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			printBEREncoding(snmpObject);
			
			snmpObject = new SNMPGauge32(1024*1024*1024*3);
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			snmpObject =SNMPBERCodec.extractEncoding(SNMPBERCodec.extractNextTLV(snmpObject.getBEREncoding(),0));
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			printBEREncoding(snmpObject);
			
			snmpObject = new SNMPInteger(128);
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			snmpObject =SNMPBERCodec.extractEncoding(SNMPBERCodec.extractNextTLV(snmpObject.getBEREncoding(),0));
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			printBEREncoding(snmpObject);
			
			snmpObject = new SNMPIPAddress("128.20.255.13");
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			snmpObject =SNMPBERCodec.extractEncoding(SNMPBERCodec.extractNextTLV(snmpObject.getBEREncoding(),0));
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			printBEREncoding(snmpObject);
			
			snmpObject = new SNMPNSAPAddress("12.34.56.78.90.AB");
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			snmpObject =SNMPBERCodec.extractEncoding(SNMPBERCodec.extractNextTLV(snmpObject.getBEREncoding(),0));
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			printBEREncoding(snmpObject);
			
			snmpObject = new SNMPObjectIdentifier("1.3.2.4.8.16.32.64.128.256.512.1024");
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			snmpObject =SNMPBERCodec.extractEncoding(SNMPBERCodec.extractNextTLV(snmpObject.getBEREncoding(),0));
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			printBEREncoding(snmpObject);
			
			
			snmpObject = new SNMPOctetString("Howdy doody!");
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			snmpObject =SNMPBERCodec.extractEncoding(SNMPBERCodec.extractNextTLV(snmpObject.getBEREncoding(),0));
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			printBEREncoding(snmpObject);
			
			snmpObject = new SNMPTimeTicks(12345);
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			snmpObject =SNMPBERCodec.extractEncoding(SNMPBERCodec.extractNextTLV(snmpObject.getBEREncoding(),0));
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			printBEREncoding(snmpObject);
			
			snmpObject = new SNMPUInteger32(12345);
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			snmpObject =SNMPBERCodec.extractEncoding(SNMPBERCodec.extractNextTLV(snmpObject.getBEREncoding(),0));
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			printBEREncoding(snmpObject);
			
			
			// now play with a sequence
			SNMPSequence snmpSequence = new SNMPSequence();
			snmpSequence.addSNMPObject(new SNMPCounter32(127));
			snmpSequence.addSNMPObject(new SNMPOctetString("abc"));
			snmpSequence.addSNMPObject(new SNMPNSAPAddress("12.34.56.78.90.AB"));
			snmpObject = snmpSequence;
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			snmpObject =SNMPBERCodec.extractEncoding(SNMPBERCodec.extractNextTLV(snmpObject.getBEREncoding(),0));
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			printBEREncoding(snmpObject);
			
			
			// create a PDU
			SNMPPDU pdu = new SNMPPDU(SNMPBERCodec.SNMPGETREQUEST, 64, 0, 0, snmpSequence);
			snmpObject = pdu;
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			snmpObject =SNMPBERCodec.extractEncoding(SNMPBERCodec.extractNextTLV(snmpObject.getBEREncoding(),0));
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			printBEREncoding(snmpObject);
			
			
			// create a message
			SNMPMessage message = new SNMPMessage(0, "community", pdu);
			snmpObject = message;
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			snmpObject =SNMPBERCodec.extractEncoding(SNMPBERCodec.extractNextTLV(snmpObject.getBEREncoding(),0));
			System.out.println("Object: " + snmpObject.getClass().toString() + ", value " + snmpObject.toString());
			printBEREncoding(snmpObject);
			
			
			/* */
			
			// create a communications interface to a remote SNMP-capable device;
			// need to provide the remote host's InetAddress and the community
			// name for the device; in addition, need to  supply the version number
			// for the SNMP messages to be sent (the value 0 corresponding to SNMP
			// version 1)
			String INTERFACE = "127.0.0.1";//"10.0.1.1"
			int RANDOMPORT =  (int)(162+61000 + System.currentTimeMillis()%999 );
			{
				String remoteJVMwithSNMP = "java -Dcom.sun.management.snmp.port="+RANDOMPORT+" -Dcom.sun.management.snmp.interface="+INTERFACE+"  -Dcom.sun.management.snmp.acl=false YourMainClass";
				Process o = Runtime.getRuntime().exec(remoteJVMwithSNMP );
				System.out.println(o); 
			}
			
			InetAddress hostAddress = InetAddress.getByName(INTERFACE);
			String community = "public";
			int version = 0;	// SNMPv1
			
			SNMPv1CommunicationInterface comInterface = new SNMPv1CommunicationInterface(version, hostAddress, RANDOMPORT ,community);
			
			
			
			// now send an SNMP GET request to retrieve the value of the SNMP variable
			// corresponding to OID 1.3.6.1.2.1.2.1.0; this is the OID corresponding to
			// the device identifying string, and the type is thus SNMPOctetString
			String itemID = "1.3.6.1.2.1.1.1.0";
			
			System.out.println("Retrieving value corresponding to OID " + itemID);
			
			// the getMIBEntry method of the communications interface returns an SNMPVarBindList
			// object; this is essentially a Vector of SNMP (OID,value) pairs. In this case, the
			// returned Vector has just one pair inside it.
			SNMPVarBindList newVars = comInterface.getMIBEntry(itemID);
			
			// extract the (OID,value) pair from the SNMPVarBindList; the pair is just a two-element
			// SNMPSequence
			SNMPSequence pair = (SNMPSequence)(newVars.getSNMPObjectAt(0));
			
			// extract the object identifier from the pair; it's the first element in the sequence
			SNMPObjectIdentifier snmpOID = (SNMPObjectIdentifier)pair.getSNMPObjectAt(0);
			System.out.println(snmpOID);
			
			// extract the corresponding value from the pair; it's the second element in the sequence
			SNMPObject snmpValue = pair.getSNMPObjectAt(1);
			
			// print out the String representation of the retrieved value
			System.out.println("Retrieved value: type " + snmpValue.getClass().getName() + ", value " + snmpValue.toString());
			
			// the retrieved value can be obtained from the SNMPObject using the getValue method;
			// the return type of the method is the generic base class Object, and must be cast to 
			// the appropriate actual Java type; in this case, for an SNMPOctetString, the underlying
			// Java type is a byte array[]
			Object javaByteArrayValue =  snmpValue.getValue();
			System.out.println(javaByteArrayValue);
			
			
			// now send an SNMP GET request to retrieve the value of the SNMP variable
			// corresponding to OID 1.3.6.1.2.1.1.3.0; this is the OID corresponding to
			// the uptime of the device, and the return type is thus SNMPTimeTicks
			itemID = "1.3.6.1.2.1.1.3.0";
			
			System.out.println("Retrieving value corresponding to OID " + itemID);
			
			// the getMIBEntry method of the communications interface returns an SNMPVarBindList
			// object; this is essentially a Vector of SNMP (OID,value) pairs. In this case, the
			// returned Vector has just one pair inside it.
			newVars = comInterface.getMIBEntry(itemID);
			
			// extract the (OID,value) pair from the SNMPVarBindList; the pair is just a two-element
			// SNMPSequence
			pair = (SNMPSequence)(newVars.getSNMPObjectAt(0));
			
			// extract the object identifier from the pair; it's the first element in the sequence
			snmpOID = (SNMPObjectIdentifier)pair.getSNMPObjectAt(0);
			
			// extract the corresponding value from the pair; it's the second element in the sequence
			snmpValue = pair.getSNMPObjectAt(1);
			
			// print out the String representation of the retrieved value
			System.out.println("Retrieved value: type " + snmpValue.getClass().getName() + ", value " + snmpValue.toString());
			
			// the retrieved value can be obtained from the SNMPObject using the getValue method;
			// the return type of the method is the generic base class Object, and must be cast to 
			// the appropriate actual Java type; in this case, for SNMPTimeTicks, which is a subclass
			// of SNMPInteger, the actual type is BigInteger (which permits arbitrarily large values to 
			// be represented).
			BigInteger javaIntegerValue = (BigInteger)snmpValue.getValue();
			System.out.println(javaIntegerValue);
			
			
			// now send an SNMP SET request to set the value of the SNMP variable
			// corresponding to OID 1.3.6.1.2.1.1.1.0; this is the OID corresponding to
			// the device identifying string, and the type is thus SNMPOctetString;
			// to set a new value, a string is supplied
			itemID = "1.3.6.1.2.1.1.1.0";	
			
			SNMPOctetString newValue = new SNMPOctetString("New device name");
			
			System.out.println("Setting value corresponding to OID " + itemID);
			System.out.println("New value: " + newValue.toString());
			
			// the setMIBEntry method of the communications interface returns the SNMPVarBindList
			// corresponding to the supplied OID and value
			// This call will probably cause an SNMPSetException to be thrown, since the
			// community name "public" is probably not the read/write password of the device 
			newVars = comInterface.setMIBEntry(itemID, newValue);
			
			
			
				
		}
		catch(Exception e)
		{
			// e.  printStackTrace();
			System.out.println("Exception during SNMP operation:  " + e + "\n");
		}
		
	}
	
	
	private static void printBEREncoding(SNMPObject object)
	{
		byte[] encoding = object.getBEREncoding();
		
		System.out.println("BER encoding:");
		for (int i = 0; i < encoding.length; ++i)
		{
			System.out.print(hexByte(encoding[i]) + " ");
		}
		System.out.println("\n");
	}
	
	
	
	private static String hexByte(byte b)
	{
		int pos = b;
		if (pos < 0)
			pos += 256;
		String returnString = new String();
		returnString += Integer.toHexString(pos/16);
		returnString += Integer.toHexString(pos%16);
		return returnString;
	}

}