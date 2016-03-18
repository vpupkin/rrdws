package cc.co.llabor.script;

import java.io.ByteArrayOutputStream;
import java.io.IOException; 
import java.io.PrintWriter;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import ws.rrd.server.LServlet;

/** 
 * <b>Description:TODO</b>
 * @author      gennady<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 OXSEED AG <br>
 * <b>Company:</b>       OXSEED AG  <br>
 * 
 * Creation:  Sep 8, 2010::8:11:04 PM<br> 
 */
public class Beauty {
	
	static String beautyScript = "";
	static String cssScript = "";
	
	public Beauty(){
		System.out.println("js:IN::"+jsInBytes+"/OUT:: "+jsOutBytes+"["+jsMs+"] ms ==~"+ (jsOutBytes/(.001*jsMs+1)+" bps"));
		System.out.println("css:IN::"+cssInBytes+"/OUT:: "+cssOutBytes+"["+cssMs+"] ms ==~"+ (cssOutBytes/(.001*cssMs+1)+" bps"));
	}
	
	static {
		try {
			// js
			byte[] buf = "".getBytes();
			buf = LServlet.getResourceAsBA("beautifyALL.js");
			beautyScript = new String(buf);
			// css
			buf = LServlet.getResourceAsBA("tabifier.js");
			cssScript = new String(buf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
		}
	}
	private static long jsInBytes = 0;
	private static long jsOutBytes = 0;
	volatile private static long jsMs = 0;
	
	
	public  String fire(String scriptIn) throws IOException, ScriptException{
			long start = System.currentTimeMillis();
			String retval = "";
	        ScriptEngineManager manager = new ScriptEngineManager();
	        ScriptEngine engine = manager.getEngineByName("JavaScript");   
	        
			engine.put("x", beautyScript); 
	        ByteArrayOutputStream bout = new ByteArrayOutputStream();
	        ByteArrayOutputStream berr = new ByteArrayOutputStream();
	        engine.getContext().setWriter(new PrintWriter(bout, true));
	        engine.getContext().setErrorWriter(new PrintWriter(berr,true)); 
	        retval = bout.toString();
	        engine.put("b", scriptIn  );        
	        bout.reset();
	        engine.eval("eval(x);");
	        retval = bout.toString();
			long stop = System.currentTimeMillis();
			jsInBytes+= scriptIn.length();
			jsOutBytes+= retval.length();
			jsMs += (stop-start);

	        return retval;
	}
	
	/**
	 * almost easiesy way to beautify the CSS - format it by JS-Engine-Call ;)  
	 * 
	 * @param styleIn
	 * @return
	 * @throws IOException
	 * @throws ScriptException
	 */
	public  String fireCSS(String styleIn) throws IOException, ScriptException{
		
		String retval = "";
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");   
        
		engine.put("x", cssScript + "" +
				"\n cleanCSS(code);" +
				"\n print(separator);" +
				"\n print(code);" +
				"\n print(separator);" +
				"\n print(out);" +
				"\n print(separator);"); 
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayOutputStream berr = new ByteArrayOutputStream();
        engine.getContext().setWriter(new PrintWriter(bout, true));
        engine.getContext().setErrorWriter(new PrintWriter(berr,true)); 
        retval = bout.toString();
        String outTmp = "";//new StringBuilder();
        String comment =  "\n/* --- */\n";
        engine.put("code", styleIn  ); 
        engine.put("separator", comment  ); 
        engine.put("out", outTmp ); 
        
        bout.reset();
        engine.eval("eval(x);");
        retval = bout.toString();
        return retval;
}	
	
	protected void _todo() throws ScriptException{
		ScriptEngineManager manager = new ScriptEngineManager();
		        
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		// Now, pass a different script context
        ScriptContext newContext = new SimpleScriptContext();
        Bindings engineScope = newContext.getBindings(ScriptContext.ENGINE_SCOPE);

        // add new variable "x" to the new engineScope        
        engineScope.put("x", "world");

        // execute the same script - but this time pass a different script context
        engine.eval("println(x);", newContext);
         
	}
	
	



	String tabs(int level) {
		String  s="";
		for (int j=0; j<level; j++) s+=' ';
		return s;
	}	
	//int level = 0;
	int LOOP_SIZE=64 * 1024;
	String out = "";
	String cleanAsync(int i, String code, int level) {
		String out = "";
		int iStart=i;
		for (; i<code.length() && i<iStart+LOOP_SIZE; i++) {
			if ('{'==code.charAt(i)) {
				level++;
				out+=" {\n"+tabs(level);
			} else if ('}'==code.charAt(i)) {
				out=out.replace("/*$", "");
				level--;
				out+='\n'+tabs(level)+"}\n"+tabs(level);
			} else if (';'==code.charAt(i)) {
				out+=";\n"+tabs(level);
			} else if ('\n'==code.charAt(i)) {
				out+='\n'+tabs(level);
			} else {
				out+=code.charAt(i);
			}
		}

		//showProgress(i, code.length);
		if (i<code.length()) {
			//setTimeout(cleanAsync, 0);
			out += cleanAsync(i,code, level+1);
		} else {
			//level=li;
			//out=out.replace(/[\s\n]*$/, '');
			//finishTabifier(out);
			out = out ;
		}
		return out;
	}

	private static long cssInBytes = 0;
	private static long cssOutBytes = 0;
	volatile private static long cssMs = 0;
	
	public String cleanCSS(String code) {
//		int  i=0;

//		if ('\n'==code[0]) code=code.substr(1);
//		code=code.replace(/([^\/])?\n*/g, '$1');
//		code=code.replace(/\n\s+/g, '\n');
//		code=code.replace(/[	 ]+/g, ' ');
//		code=code.replace(/\s?([;:{},+>])\s?/g, '$1');
//		code=code.replace(/\{(.*):(.*)\}/g, '{$1: $2}');
		
		long start = System.currentTimeMillis();
		 
		String out=tabs(0);// li=level;
		out = cleanAsync(0, code, 0);
		long stop = System.currentTimeMillis();
		cssInBytes+= code.length();
		cssOutBytes+= out.length();
		cssMs += (stop-start);
		//System.out.println(out);
		
		return out;
	}


}


 