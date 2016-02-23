package ws.rrd.csv;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import ws.rrd.collectd.TextLineIterator;

import cc.co.llabor.cache.BasicCacheFactory;

import junit.framework.TestCase;
 

/**
 * <b>Description:TODO</b>
 * 
 * @author vipup<br>
 *         <br>
 *         <b>Copyright:</b> Copyright (c) 2006-2008 Monster AG <br>
 *         <b>Company:</b> Monster AG <br>
 * 
 * Creation: 20.04.2010::11:59:24<br>
 */
public class CollectDTest extends TestCase{
	
 
	 
	public void testCSVTXT() throws IOException {
		ClassLoader classLoader = this.getClass().getClassLoader();
		InputStream resourceAsStream = classLoader .getResourceAsStream("collectd.txt");
		TextLineIterator p = new TextLineIterator(resourceAsStream);
		// testdrive into System.out
		DateFormat SDF = new SimpleDateFormat("MMM dd HH:mm:ss yyyy", Locale.US);
		Action a = new ToStringPrintlnAction(SDF); 
		try{//p.setIgnoreWrongLine(true);
			p.perform(a);
			String out = a.toString();
			 
			assertTrue( out, out.indexOf("rrdtool create X-1722861190.rrd --start 1301583237 --step 60")>0);
         	assertTrue(out, out.indexOf("rrdtool update X-1722861190.rrd 1301583247:1.6777216E7")>0);
			assertTrue(out, out.indexOf("rrdtool update X-779976225.rrd 1301583247:3.5815424E7")>0);
 
		}catch(Exception e){
			// e.  printStackTrace();
			assertTrue(a.toString(), e instanceof ArrayIndexOutOfBoundsException);
		}
	}
	 
	 
	public void testCSVTXT2sol() throws IOException {
		ClassLoader classLoader = this.getClass().getClassLoader();
		InputStream resourceAsStream = classLoader .getResourceAsStream("collectdSOL.txt");
		TextLineIterator p = new TextLineIterator(resourceAsStream);
		// testdrive into System.out
		DateFormat SDF = new SimpleDateFormat("MMM dd HH:mm:ss yyyy", Locale.US);
		Action a = new ToStringPrintlnAction(SDF); 
		try{//p.setIgnoreWrongLine(true);
			p.perform(a);
			String out = a.toString();
			 
			assertTrue(out,  out.indexOf("rrdtool create X-1722861190.rrd ")>0);
			assertTrue(out, out.indexOf("rrdtool update X-779614068.rrd 1302694515:1.5527576E7")>0);
			assertTrue(out, out.indexOf("rrdtool update X1191353274.rrd 1302694515:2.5952256E8")>0);
		}catch(Exception e){
			// e.  printStackTrace();
			assertTrue(a.toString(), e instanceof ArrayIndexOutOfBoundsException);
		}
	}
	 
	
	public void testExecuteUpdate() throws IOException {
 
		ClassLoader classLoader = this.getClass().getClassLoader();
		InputStream resourceAsStream = classLoader .getResourceAsStream("collectd.txt");
		TextLineIterator p = new TextLineIterator(resourceAsStream);
		// testdrive into System.out
		SimpleDateFormat SDF = new SimpleDateFormat("MMM dd HH:mm:ss yyyy", Locale.US);				
		Action a = new RrdUpdateAction(SDF);
		p.perform(a);
	}	
	 
}
