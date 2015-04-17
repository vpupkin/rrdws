package ws.rrd.csv;

import java.io.IOException;

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
public class CSVParserTest extends TestCase{
	

	public CSVParserTest() {
		System.setProperty("net.sf.jsr107cache.CacheFactory",
				BasicCacheFactory.class.getName());
	}
 
	public void testCSVParserToLowErr() throws IOException {
		CSVParser p = new CSVParser(this.getClass().getClassLoader()
				.getResourceAsStream("testErr45.csv"));
		// testdrive into System.out
		Action a = new SystemOutPrintlnAction(); 
		try{  
			p.perform(a);
			fail();
		}catch (Exception e) {
			assertTrue( e instanceof ArrayIndexOutOfBoundsException);
		}
	}
	 
	public void testCSVTXT() throws IOException {
		CSVParser p = new CSVParser(this.getClass().getClassLoader()
				.getResourceAsStream("testCSV.txt"));
		// testdrive into System.out
		Action a = new ToStringPrintlnAction(); 
		try{//p.setIgnoreWrongLine(true);
			p.perform(a);
			fail();
			String out = a.toString();
			System.out.println(out);
			assertTrue( out.indexOf("\\10.253.24.80\\Prozessor(_Total)\\Prozessorzeit (%)-->\nrrdtool create X-1979395149.rrd")>0);
			assertTrue(out.indexOf("rrdtool update X-1979395149.rrd 1272616451:8.207780")>0);
			assertTrue(out.indexOf("rrdtool update X-1132348867.rrd 1272616691:22361.069101")>0);
		}catch(Exception e){
			assertTrue(a.toString(), e instanceof ArrayIndexOutOfBoundsException);
		}
	}
	
	public void testNumOnHead() throws IOException {
		CSVParser p = new CSVParser(this.getClass().getClassLoader()
				.getResourceAsStream("testNumOnHead.txt"));
		// testdrive into System.out
		Action a = new ToStringPrintlnAction(); 
		p.setIgnoreWrongLine(true);
		try{
			p.perform(a);
			fail( "number at headers error expected!");
		}catch(Throwable e){
			
			String ch = "number at headers";
			assertTrue(e.getMessage(),e.getMessage().indexOf(ch )>=0);
		}
		
				
	}
	public void testCSVTXT_ignoreErrors() throws IOException {
		CSVParser p = new CSVParser(this.getClass().getClassLoader()
				.getResourceAsStream("testCSV.txt"));
		// testdrive into System.out
		Action a = new ToStringPrintlnAction(); 
		p.setIgnoreWrongLine(true);
		p.perform(a);
		String out = a.toString();
		System.out.println(out);
		//TOODO 		assertTrue( out.indexOf("\\10.253.24.80\\Prozessor(_Total)\\Prozessorzeit (%)-->\nrrdtool create X-1979395149.rrd")>0);
		assertTrue(out.indexOf(".rrd 1272616451:8.207780")>0);
		assertTrue(out.indexOf(".rrd 1272616691:22361.069101")>0);
				
	}
	 
	public void testCSV () throws IOException {
		CSVParser p = new CSVParser(this.getClass().getClassLoader()
				.getResourceAsStream("test.csv"));
		// testdrive into System.out
		Action a = new ToStringPrintlnAction(); 
		p.perform(a);
		String out = a.toString();
		System.out.println(out);
		//TODO assertTrue(out,out.indexOf("SMSvcHost 3.0.0.0\\Protocol Failures over net.tcp-->\nrrdtool create X-857541619.rrd --start 1271697051")>0);
		assertTrue(out,out.indexOf("rrdtool update")>=0);
		assertTrue(out,out.indexOf(".rrd 1271697061:23.531515422440769")>0);
		assertTrue(out,out.indexOf(".rrd 1271697089:23.531515422440769")>0);
				
	}
 
	public void testCSVParserToMuchErr() throws IOException {
		CSVParser p = new CSVParser(this.getClass().getClassLoader()
				.getResourceAsStream("testErr55.csv"));
		// testdrive into System.out
		Action a = new SystemOutPrintlnAction();
		try{
			p.perform(a);
			fail();
		}catch (Exception e) {
			assertTrue(e.getMessage(), e.getMessage().indexOf("header")>0 );
		}
	}
 
	public void testCSVParser() throws IOException {
		CSVParser p = new CSVParser(this.getClass().getClassLoader()
				.getResourceAsStream("test.csv"));
		// testdrive into System.out
		Action a = new SystemOutPrintlnAction(); 
		p.perform(a);
	}
 
	public void testExecuteUpdate() throws IOException {
		CSVParser p = new CSVParser(this.getClass().getClassLoader()
				.getResourceAsStream("test.csv"));
		// RrdCommander.execute(cmdTmp);
		Action a = new RrdUpdateAction();
		Object o = p.perform(a);
		System.err.println(("" + o).replace(", \\\\", ",\n \\\\"));
	}	
	 
	
	public void testExecuteUpdateInMEM() throws IOException {
		System.setProperty("RrdMemoryBackendFactory","ON");
		
		CSVParser p = new CSVParser(this.getClass().getClassLoader()
				.getResourceAsStream("test.csv"));
		// RrdCommander.execute(cmdTmp);
		Action a = new RrdUpdateAction();
		Object o = p.perform(a);
		System.err.println(("" + o).replace(", \\\\", ",\n \\\\"));
	} 
}
