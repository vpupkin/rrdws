package eu.blky.logparser;

import java.io.BufferedReader; 
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import ws.rrd.csv.Action;
import ws.rrd.csv.RrdUpdateAction;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  14.09.2012::09:46:26<br> 
 */
public class LogChecker implements Runnable{
	 
	public LogChecker(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		try{
			LogChecker chTmp = new LogChecker(args[0]);
			Thread checkerTh = new Thread (chTmp);
			System.out.println("start processing file {"+args[0]+"}. Press any key to interrupt.");
			checkerTh.start(); 
			// wait any console input for interrupt the reading-loop
			System.in.read();
			chTmp.interrupted = true;
			System.out.println("-- stopped --");
		}catch(Exception e){
			// e.  printStackTrace();
			System.out.println("Missing parameter!?\nUsage: java eu.blky.logparser.LogChecker fileName");
		}
				
	}
	long sleepTime = 1000;
	boolean interrupted = false;
	String fileName;
	public void run() {  
		BufferedReader input;
		try {
			// INPUT
			File fin = new File(fileName);
			// PROCESS
			Parser parserTmp = new Parser( "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) (\\d+) \"(.+?)\" ");
			// OUTPUT
			SimpleDateFormat SDF = new SimpleDateFormat("MMM dd HH:mm:ss yyyy", Locale.US);
			Action a = new RrdUpdateAction(SDF);
			 
			FileReader in = new FileReader(fin );
			in.skip(fin.length()-4000);
			input = new BufferedReader(in); 
		      String currentLine = null; 
		      while (!interrupted) { 
		        if ((currentLine = input.readLine()) != null) {
		          //System.out.println(currentLine);
		          EventLog e = parserTmp.parse9(currentLine);
				  String xpath = "accessLog";
				  long timestamp = e.getStartTime();
				  long sizeTmp = e.getHttpSize();
				  String data = ""+sizeTmp;
				  a.perform(xpath, timestamp, data);	          
		          continue;
		        }
	
		        try { 
				Thread.sleep(sleepTime);
		        } catch (InterruptedException e) {
		          Thread.currentThread().interrupt();
		          break;
		        } 
		      }
		      input.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			// e1. printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
		}		      
	}
 
}


 