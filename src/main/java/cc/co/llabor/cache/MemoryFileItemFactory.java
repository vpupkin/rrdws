package cc.co.llabor.cache;
/** 
 * <b>Description:TODO</b>
 * @author      v.i.p.<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  13.04.2010::19:30:48<br> 
 */

import org.apache.commons.fileupload.FileItemFactory;

public class MemoryFileItemFactory implements FileItemFactory {

	private int maxSize;

	private static MemoryFileItemFactory myself = new MemoryFileItemFactory(
			4 * 1024 * 1024);
	public static MemoryFileItemFactory getInstance() {
		return myself;
	}
	MemoryFileItemFactory(int size) {
		maxSize = size;
	}

	public MemoryFileItem createItem(String fieldName, String contentType,
			boolean isFormField, String fileName) {

		MemoryFileItem item = new MemoryFileItem(fieldName, contentType,
				isFormField, fileName, maxSize);
		return item;
	}

	public void setMaxSize(int size) {
		maxSize = size;
	}
}
