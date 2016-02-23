package cc.co.llabor.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;   
import java.util.ArrayList;
import java.util.Date; 
import java.util.List;
import java.util.Properties; 

import net.sf.jsr107cache.Cache; 
import net.sf.jsr107cache.CacheListener;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  14.04.2010::10:50:13<br> 
 */
public class MemoryFileCache {
	
	 public static final String CREATION_DATE = ".";

	private String cachename;
	 
	 private List<CacheListener> listeners = new ArrayList<CacheListener>();
	 
	 public MemoryFileCache(String cachename) {
		this.cachename = cachename;
	}
	 

	 

	public static MemoryFileCache getInstance(String cachename){
		return new MemoryFileCache( cachename );
	}
	
	 public Date getLastModified(String name) throws IOException{
		 MemoryFileItem f = get(name);
		 return f.date_created;
	 }
	 
	 public Date getCreationDate(String name) throws IOException{
		 if (isDir(name)){
				Cache cache = Manager.getCache(this.cachename);
				if (cache instanceof FileCache){
					String baseDir = ((FileCache)cache).getBaseDir();
					name = name.startsWith(baseDir)?name.substring(baseDir.length()):name;
				}			 
				Properties o = (Properties)cache.get(name+"/.!"+"");
				final String dateAsTimestamp = o.getProperty(CREATION_DATE);
				final long parseDateAsLong = Long.parseLong(dateAsTimestamp);
				Date retval = new Date(parseDateAsLong);
				return retval;
		 }
		 MemoryFileItem f = get(name);
		 return f.date_created;
	 }
	 
	public boolean isDir (String name) throws IOException{
		Cache cache = Manager.getCache(this.cachename);
		if (cache instanceof FileCache){
			String baseDir = ((FileCache)cache).getBaseDir();
			name = name.startsWith(baseDir)?name.substring(baseDir.length()):name;
		}
		 
		Object o = cache.get(name+"/.!"+"");
		return (o instanceof Properties );
	}
	
	public MemoryFileItem get (String name) throws IOException{
			Cache cache = Manager.getCache(this.cachename);
			if (cache instanceof FileCache){
				String baseDir = ((FileCache)cache).getBaseDir();
				name = name.startsWith(baseDir)?name.substring(baseDir.length()):name;
			}
			MemoryFileItem retval = null;
			Object o = cache.get(name);
			if (o instanceof String){
				retval = new MemoryFileItem (name,"text/plain",false,name, 0);
				retval.getOutputStream().write( ((String)o).getBytes());
				retval.flush();
			}
			if (o instanceof MemoryFileItem){
				retval = (MemoryFileItem) o; 
				retval = retval.getSize()==1?null:retval;
			}
			if (retval ==null){ // try to restore parts
				  for (int i=0;cache.get(name+"::"+i)!=null;){
					 MemoryFileItem next = (MemoryFileItem)cache.get(name+"::"+i);				 
					 if (i==0 ){
						 retval = new MemoryFileItem (next.fileName,next.contentType,next.isFormField(),next.fileName, 0);
					 }
					 retval.getOutputStream().write(next.get());
					 i++;
				 }
				 if (retval !=null)
					  retval.flush();
			 }
			
			if (o instanceof InputStream){  
				 InputStream in = (InputStream)o;
				 retval = new MemoryFileItem ( name,"text/plain",false,name, 0);
				 byte[] b = new byte[in.available()];
				 in.read(b);
				 retval.getOutputStream().write(b );
				 retval.flush();
			 }
			 return retval;
		 }	
             
	public String mkdir(MemoryFileItem item) throws IOException {
			Cache cache = Manager.getCache(cachename);
			String name = item.getName();
			name +="/.!";
			name = name.replace("//", "/");
			Object oldTmp = cache.get(name);
			if (oldTmp != null) {
				cache.remove(name);
			}else{
				oldTmp= new Properties(); 
			}
			if (!(oldTmp instanceof Properties)){
				oldTmp= new Properties();  // forget any kind of content and replace by empty DIR
			}
			Properties dir = (Properties) oldTmp;
			// creating date
			dir.setProperty(".",""+System.currentTimeMillis());
			cache.put(name, dir );
			int beginIndex = 0;
			
			int endIndex = name. substring(0,name.length()-3).lastIndexOf("/");
			// update parent
			String parentName = name.substring(beginIndex, endIndex)+"/.!";
			parentName =parentName.replace("//", "/");
			Properties parent = (Properties)cache.get(parentName);
			parent = parent==null?new Properties():parent;
			parent.put(name, ""+System.currentTimeMillis());
			cache.remove(parentName);
			cache.put(parentName, parent );
			
			return name;
	 }

	public String put(MemoryFileItem item) throws IOException {
		Cache cache = Manager.getCache(cachename);
		String name = item.getName();
		name = name.replace("//", "/");
		Object oldTmp = cache.get(name);
		if (oldTmp != null) {
			cache.remove(name);
		}
		byte[] bs = item.get();

		if (bs.length < MAX_SIZE) {
			cache.put(name, item);
		} else { // SPLIT
			int done = 0;
			for (int i = 0; done < bs.length; i++) {
				String nameTmp = item.getName() + "::" + i;
				MemoryFileItem itemNext = new MemoryFileItem(item.fileName,
						item.contentType, item.isFormField(), item.fileName, 0);
				OutputStream outputStream = itemNext.getOutputStream();
				outputStream.write(bs, done, Math.min(MAX_BUFF_SIZE, bs.length
						- done));
				itemNext.flush();
				done += MAX_BUFF_SIZE;
				try {
					cache.put(nameTmp, itemNext);
				} catch (Throwable e) {
					// e.  printStackTrace();
					throw new IOException(e.getMessage());
				}
			}
		}
		for (CacheListener l : listeners) {
			Object key = name;
			l.onPut(key);
		}
		addToList(name);
		return name;
	}

	private synchronized void addToList(String name) {
 
		// update parent
		int beginIndex = 0;
		int endIndex = name.substring(0,name.length()-3).lastIndexOf("/");
		
		Cache cache = Manager.getCache(cachename);
		final String parentName = name.substring(beginIndex,  endIndex==-1?0:endIndex )+"/.!";
		Properties parent = (Properties)cache.get(parentName);
		parent = parent==null?new Properties():parent;
		String pureName = name.substring(endIndex+1);
		parent.put(pureName, ""+System.currentTimeMillis());
		// replace
		Properties toDel = (Properties)cache.remove(parentName);
		//System.out.println(toDel);
		// update Date for Dir
		parent.put(CREATION_DATE, ""+System.currentTimeMillis());
		cache.put(parentName, parent );
	}
	
	
	/**
	 * @deprecated use getInstance()
	 * 
	 * 
	 * @author vipup
	 * @param name
	 * @return
	 * @throws IOException
	 */
	 static MemoryFileItem _get (String name) throws IOException{
		Cache cache = getMyCache();
		MemoryFileItem retval = (MemoryFileItem) cache.get(name);
		 if (retval ==null){ // try to restore parts
			  for (int i=0;cache.get(name+"::"+i)!=null;){
				 MemoryFileItem next = (MemoryFileItem)cache.get(name+"::"+i);				 
				 if (i==0 ){
					 retval = new MemoryFileItem (next.fileName,next.contentType,next.isFormField(),next.fileName, 0);
				 }
				 retval.getOutputStream().write(next.get());
				 i++;
			 }
			 retval.flush();
		 }
		 return retval;
	 }

	private static Cache getMyCache() {
		return Manager.getCache();
	}
	 static int MAX_SIZE = 64*1024;
	 static int MAX_BUFF_SIZE = MAX_SIZE;

	 /**
	  * 
	  * @deprecated
	  * 
	  * @author vipup
	  * @param item
	  * @return
	  * @throws IOException
	  */
	 static String _put (MemoryFileItem  item) throws IOException{
		 Cache cache = getMyCache();
		 String name = item.getName();
		 byte[] bs = item.get();
		 
		if (bs.length < MAX_SIZE){
			 cache.put(name,item);
		 }else{ //SPLIT
			 int done = 0;
			 for (int i=0;done<bs.length ;i++){
				 String nameTmp = item.getName()+"::"+i;
				 MemoryFileItem itemNext =  new MemoryFileItem (item.fileName,item.contentType,item.isFormField(),item.fileName, 0);
				 OutputStream outputStream = itemNext.getOutputStream();
				 outputStream.write(bs, done,Math.min( MAX_BUFF_SIZE, bs.length-done ));
				 itemNext.flush();
				 done += MAX_BUFF_SIZE;
				 try{
					 cache.put(nameTmp,itemNext);
				 }catch(Throwable e){
					 // e.  printStackTrace();
					 throw new IOException (e.getMessage());
				 }
			 }
		 }
		 return name;
	 }

 

	public String[] list(String folderUri) {
		List<String> list = new ArrayList<String>();
		String dirTmp = folderUri + ".!";
		Properties dir = null;
		try {
			Cache cache = Manager.getCache(cachename); // Manager.getCache("SCRIPTSTORE/ABC")
			dir = (Properties) cache.get(dirTmp);// reserved for Dir-content
			for (String item : dir.keySet().toArray(new String[] {})) {
				if (item.endsWith("/.!")) {
					final String dirName = item;
					// dirName = dirName.substring(1,item.length()-3);
					list.add(dirName);
					continue;// fix for old bugs
				}
				list.add(item);
			}
		} catch (NullPointerException e) {
			initDir(dirTmp);
		} catch (Throwable e) {
			// e.  printStackTrace();
		}
		return list.toArray(new String[] {});
	}




	private void initDir(String dirTmp) {
		Properties dir;
		dir = new Properties();
		Cache cache = Manager.getCache(cachename);
		// creating date
		dir.setProperty(".", "" + System.currentTimeMillis());
		cache.put(dirTmp, dir);
	}

	public Object delete(MemoryFileItem toDel) {
		Cache cache = Manager.getCache(cachename);
		Object o = cache .remove(toDel);
		remFromList(toDel.getName());
		return o;
		 
	}

	private void remFromList(String name) {
 
		// update parent
		int beginIndex = 0;
		int endIndex = name.length()-1;
		try{
			name.substring(0,name.length()-3).lastIndexOf("/");
		}catch(Exception e){
			// e.  printStackTrace();
		}
		Cache cache = Manager.getCache(cachename);
		final String parentName = name.substring(beginIndex, endIndex)+"/.!";
		Properties parent = (Properties)cache.get(parentName);
		parent = parent==null?new Properties():parent;
		String pureName = name.substring(endIndex+1);
		parent.remove( pureName ); // "doc"
		parent.remove( name ); // /ffgtk-0.7.91/doc
		parent.remove( name +"/.!" ); // /ffgtk-0.7.91/doc
		// replace
		cache.remove(parentName);
		cache.put(parentName, parent );
	}

	public void delDir(String uri) {
		Cache cache = Manager.getCache(this.cachename);
		String name = uri;
		if (cache instanceof FileCache){
			String baseDir = ((FileCache)cache).getBaseDir();
			name  = name.startsWith(baseDir)?name.substring(baseDir.length()):name;
		} 
		System.out.println("<delete>\n\t<dir name=\'"+name+"\'/>... ");
		Object o = cache.remove(name+"/.!"+"");
		System.out.println(o+"\n</delete>");		
		remFromList(name);
		 
	}


}


 