package cc.co.llabor.cache.js; 
import java.io.BufferedInputStream;
import java.io.IOException;  
import java.io.InputStream;
import java.io.ObjectInputStream;

import cc.co.llabor.cache.Manager;
import cc.co.llabor.script.Beauty;
 
import net.sf.jsr107cache.Cache; 
  
/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  26.07.2010::11:42:57<br> 
 */ 
public class JSStore { 
	 
	private static final String SCRIPTSTORE = "SCRIPTSTORE";
	private static final JSStore me = new JSStore();
	volatile Cache  scriptStore  = null;
	JSStore() { 
		scriptStore = Manager.getCache(SCRIPTSTORE);	
	}
  

	public static JSStore getInstanse() {
		me.scriptStore = Manager.getCache(SCRIPTSTORE);		
		return me;
	}

	/**
	 * @deprecated
	 * @author vipup
	 * @param scriptValue
	 * @return
	 */
	public Item _getByValue(String scriptValue) { 
		return (Item) scriptStore.get(scriptValue);
	}
	
	public Item getByURL(String scriptURL) { 
		String key = scriptURL;
		Item retval = null;
		Object objectTmp = scriptStore.get(key );
		retval = chekOut(scriptURL, retval, objectTmp);
		return  retval;
	}


	/**
	 * try to load Item from Cache by URL as a Key.
	 * 1) as a stored Obj:Item
	 * 2) as a plain bytes->new Item(${bytes}). 
	 * 
	 * @author vipup
	 * @param scriptURL
	 * @param retval
	 * @param objectTmp
	 * @return
	 */
	private Item chekOut(String scriptURL, Item retval, Object objectTmp) {
		if (objectTmp ==null)return null;
		if (objectTmp instanceof Item)return (Item)objectTmp;
		
		BufferedInputStream bin  = null;
		try{
			bin = new BufferedInputStream ((InputStream)objectTmp );
			bin .mark(Integer.MAX_VALUE); 
			ObjectInputStream oin = new ObjectInputStream(bin); 
			objectTmp = oin.readObject();
			oin.close();
			bin.close();
			 
		}catch(IOException e){
			try { // go back and read from start
				bin.reset();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				//e1. printStackTrace();
			} 
			String value =  readFully((InputStream)bin);
//			/String cacheKey = scriptURL;
			String refPar = null;
			//objectTmp =  putOrCreate(  cacheKey ,   value,   refPar  ) ;
			retval  = new Item(value);
			retval  .addReffer(refPar);  
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
		} 	 
		if (objectTmp instanceof Item){ 
			retval =(Item ) objectTmp;
		}
		return retval;
	}

	private String readFully(InputStream inPar) {
		StringBuffer sb = new StringBuffer();
		byte[] buf = new byte[1024];
		int l = -1;
		try {
			for (l = inPar.read(buf);l>=0;l = inPar.read(buf)){
				String s = new String(buf, 0, l);
				sb.append(s );
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.  printStackTrace();
		}
		return sb.toString();
	}

	
	/**
	 * store the Script-Item as a CacheItem into JSCacheStore.
	 * -before store value will be checked/transformed for links in the :
	 *    a) header
	 * -stored value will be reformatted ( 2nd pass) by beautifyer   
	 * 
	 * @author vipup
	 * @param cacheKey
	 * @param value
	 * @param refPar
	 * @return
	 */
	public Item putOrCreate(String cacheKey, String value, String refPar ) { 
		Object jsItemTmp = scriptStore.get(cacheKey);
		Item jsItem = null;
		if (jsItemTmp instanceof Item){
			jsItem = (Item)jsItemTmp;
		}else{
			// TODO
			System.out.println(jsItemTmp);
		}
		try{ 
			if (jsItem == null){  
				value = checkHeadAndFoot(value);			
				jsItem = new Item(value); 
				jsItem.addReffer(refPar); 
				scriptStore.put(cacheKey, jsItem );
			}  else{  // only for existing entries 
				jsItem.addReffer(refPar); 
				if (!jsItem.isReadOnly()){
					jsItem.setValue(value); 
					reformat(cacheKey, jsItem);						
					jsItem.setReadOnly(true);
				}
			}
			// TODO Refactor for gae-cache
			chekIn(cacheKey, jsItem);
		}catch(Throwable e){
			// e.  printStackTrace();
		}
		return jsItem;
	}


	/**
	 * synchronized store into Cache-Facility
	 *.. just avoid storing the same. The price for PUT is much more as for GET==THIS?IGNORE:PUT;
	 * 
	 * @author vipup
	 * @param cacheKey
	 * @param jsItem
	 */
	private void chekIn(String cacheKey, Item jsItem) {
		synchronized (SCRIPTSTORE) { 
			{ 
				Object o = scriptStore.peek(cacheKey);
				o = chekOut(cacheKey, jsItem, o);
				boolean changed = false;
				if (jsItem != o) {// check similarity 
					if (o != null)
						try {
							final String valTmp = jsItem.getValue();
							final String cachedVTmp = ((Item) o).getValue();
							changed = !cachedVTmp.equals(valTmp);
						} catch (NullPointerException e) {
							// e.  printStackTrace();
						}
					if (changed) {
						scriptStore.remove(cacheKey);
						scriptStore.put(cacheKey, jsItem);
					}
				}
			}
		}/* synchronized (SCRIPTSTORE) */
	}

	/**
	 * @author vipup
	 * @param value
	 * @return
	 */
	private String checkHeadAndFoot(String value) {
		if (value.trim().toLowerCase().startsWith("<script")){
			String newVal = "";
			value = value.replace(">", ">\n");
			String XDATATMP = "XCDATAXCDATAX"+System.currentTimeMillis()+"CDATAXCDATA";
			value = value.replace("<![CDATA[", XDATATMP). replace("<", "\n<").replace(XDATATMP, "<![CDATA[");
			String[] lines =value.split("\n");
			int i=0;
			for (String sTmp :lines){
				String trimTmp = sTmp.toLowerCase().trim();
				boolean shouldBeWraped = trimTmp.startsWith("<") 
					&& (trimTmp.replace(" ", ""). startsWith("<script") ||
						trimTmp.replace(" ", ""). startsWith("</script")) ;
				if (shouldBeWraped)
					newVal += "/*";
				newVal+= sTmp;
				if (shouldBeWraped)
					newVal += "*/";
				newVal +="\n";
				i++;
			}
			value = newVal;
		}
		return value;
	}


	private void reformat(String cacheKey, Item jsItem) {
		try{
			if (jsItem.isReadOnly()) return;  
			String jsValue = jsItem.getValue();
			jsValue = checkHeadAndFoot(jsValue);	
			// http://stackoverflow.com/questions/18985/javascript-beautifier
			jsValue  =  performFormatJS(cacheKey, jsValue ); 
			jsItem.setValue( jsValue );
		}catch(IOException e){
			// e.  printStackTrace();
		} catch (Throwable e) {
			// e.  printStackTrace();
		}
	}


	public static String performFormatJS(String uriTmp, String scriptValue) throws IOException  {//, <-- ScriptItem scriptTmp
		Item jsTmp = getInstanse().getByURL(uriTmp);
		String formattedJS = scriptValue;
		if (jsTmp!=null && jsTmp.readOnly ) {
			// already formatted
			formattedJS = jsTmp.getValue();
		}else{
			synchronized (uriTmp) { 
				Beauty b = new Beauty();  
				try{
					formattedJS = b.fire(scriptValue);
				}catch (Exception e) {
					// TODO: handle exception
					// e.  printStackTrace();
				}
			}
		}
	
		return formattedJS;
	}
	
}


 