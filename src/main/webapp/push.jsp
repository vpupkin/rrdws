<?xml version="1.0"?>
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<!-- % response.setContentType("application/vnd.wap.xhtml+xml"); %> -->
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload"%> 
<%@page import="java.util.List"%> 
<%@page import="ws.rrd.mem.MemoryFileCache"%>
<%@page import="ws.rrd.mem.MemoryFileItemFactory"%>
<%@page import="ws.rrd.mem.MemoryFileItem"%>
<%@page import="org.apache.commons.fileupload.FileUploadException"%>
<%@page import="java.io.IOException"%> 
<html xmlns="http://www.w3.org/1999/xhtml"> <head> <title>RDD UPDATE PAGE</title> </head> 
<%
try{
    // Check that we have a file upload request
    if(ServletFileUpload.isMultipartContent(request)){
            
            MemoryFileItemFactory factory = MemoryFileItemFactory.getInstance();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(4*1024*1024); // 4 MB
  
            // Parse the request
            List<MemoryFileItem> items = upload.parseRequest(request); 
            for(MemoryFileItem item : items) {
                    item.flush(); 
                    response.getWriter().append( "<br>Name:::"+ item.getName() );
                    response.getWriter().append( "<br>Size:::::"+ item.getSize() );
                    response.getWriter().append( "<br>Date:::::"+ item.getDate() );
                    response.getWriter().append( "<br>FN:::::"+ item.getFieldName() );
                    response.getWriter().append( "<br>ContentType:::::"+ item.getContentType() );
                    session.setAttribute(item.getName(),item );
					String nameTmp = MemoryFileCache. put( item  );
					System.out.println( "stored into memcache as ::["+nameTmp +"]");
                    //pm.makePersistent(item);
            }
    }else{
%>
<form  method="post" enctype="multipart/form-data">
  <p>Select CSV File for update:<br>
    <input name="Datei" type="file" size="50" maxlength="100000" accept="text/*">
    <input type="submit"/>
  </p>
</form>
<%     	
    }
} catch(FileUploadException e){ throw new IOException("Unable to handle uploaded file"); 
} catch(Throwable e){ e.printStackTrace(response.getWriter()); }

%> 
  </body>
</html>