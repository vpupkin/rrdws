package cc.co.llabor.threshold.nagios; 

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;  
import java.util.LinkedList; 
 
 
import cc.co.llabor.props.CommentedProperties;
import cc.co.llabor.props.StringWithComments;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  16.02.2012::13:16:21<br> 
 */
public abstract class CfgReader extends LinkedList<CfgReader> {
	private static final String EOL = "\n";
	CfgReader theNext =null;

	

	
	private static final String STD_ENCODING = "UTF-8";
 




	static LinkedList<Cfg> readFromCfg(Cfg cfgPar) throws IOException { 
		return readFromCfg(cfgPar.getCfgName());
	}
	static LinkedList<Cfg> readFromCfg(String cfgName ) throws IOException { 
		
		LinkedList<Cfg> retval=new LinkedList<Cfg>();
	
		InputStream in = Cfg.class.getClassLoader().getResourceAsStream("nagios/"+cfgName  );
		BufferedReader inBuf = new BufferedReader(new InputStreamReader(in, STD_ENCODING)   );
	
		for (StringWithComments line = readln(inBuf); line  != null; line = readln(inBuf)) {
			// define XXX{
			if (line.getLine().startsWith("define ")) { // accumulate props for one
				String sTitle = line.getComments();
				int beginIndex ="define ".length();
				String sClass = line.getLine().substring(beginIndex ) .replace("{", "").replace(" ", "");
				Cfg e = new GenCfg(sClass );
				CommentedProperties  theP = e.isInited()?e.toProperties():new CommentedProperties(sTitle);
				
 							  // section... 
				// TODO assert line has a CfgName after define like  services.cfg @define service{@				 
				while (((line = readln(inBuf)) !=null)){
					if ("}".equals(line.getLine())){
						break;
					}else{
						//System.out.println(line.getLine());						
					}
					//(!"}".equals(line = readln(inBuf)) )&& 
					String lTmp = line.getLine();
					String key_val []= lTmp.replace(" ", "\t").replace("\t\t", "\t").replace("\t\t", "\t").replace("\t\t", "\t").replace("\t\t", "\t").replace("\t\t", "\t").replace("\t\t", "\t").replace("\t\t", "\t").replace("\t", ";").split(";");
					String keyTmp = key_val [0];
					String valTmp= line.getLine().substring(keyTmp.length()).trim();
					String commentTmp = line.getComments();
					commentTmp = "}".equals( commentTmp)?"":commentTmp;
					theP.setProperty(keyTmp, valTmp, commentTmp);
					
				}
				e.flush(theP);
				retval.add(e); 
			} 
		}  
		 
		return retval;
	}

	//final static Hashtable<String,CfgChain> cfgRepo = new Hashtable<String, CfgChain>();

	/**
	 * search the key in the sub-Cfg on position == index
	 * @author vipup
	 * @param key
	 * @param index
	 * @param cfgPar 
	 * @return
	 * @throws NullPointerException
	 */
	public String get(String key, int index, Cfg cfgPar) {
		LinkedList<Cfg> cfgTmp = null;
		try {
			cfgTmp = readFromCfg( cfgPar);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
		}
		String retval = null;   
		//retval = cfgRepo.get(  cfgPar.getCfgName() ).get( index ).get(key);
		retval = cfgTmp.get( index ).get(key);
		return retval;
	} 
	
	/**
	 * return trimmed, uncommented, non-empty line of CFG-File
	 * @author vipup
	 * @param inBuf 
	 * @return
	 * @throws IOException
	 */
	private static StringWithComments readln(BufferedReader inBuf) throws IOException {
		String retval = inBuf.readLine();

		String skipped = "";
		String prefix = "";
		try{
			retval = retval.trim();
			// #
			while (retval.startsWith("#")||"".equals( retval) ){
				skipped += prefix ;
				skipped += retval;
				prefix  = EOL;
				retval = inBuf.readLine();
				retval = retval.trim();
				
			}
		}catch(NullPointerException e){
			//// e.  printStackTrace();
			//throw new IOException("EOF at line "+linecount);
			return null;
		}
		//System.out.println(skipped);
		// http://nagios.sourceforge.net/docs/3_0/objectdefinitions.html#contactgroup
		// ....
		// 3.Characters that appear after a semicolon (;) in configuration lines are treated as comments and are not processed
		if (retval.indexOf(";")>=0){
			int beginIndex = retval.indexOf(";");
			skipped +=prefix;
			skipped +="# "+ retval.substring(beginIndex+1);
			retval = retval.substring(0,beginIndex);
		}
		
		return new StringWithComments(retval, skipped);
	}


}


 