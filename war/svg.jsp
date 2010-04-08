<%@page import="java.io.ByteArrayInputStream"%><%@page  contentType="image/svg+xml"%><%
response.setContentType("image/svg+xml;charset=UTF-8");
%><?xml version="1.0" standalone="no"?>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN"
"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<%
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
response.setHeader("Content-Disposition", "inline;filename=svg.svg");
%>
<%@page import="ws.rrd.Chk"%> 
 
<%
try{
	 	if (session.getAttribute("svg")!=null){
	 		out.write("<svg  version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\">");
	 		out.write(new String(  (byte[])session.getAttribute("svg")));
	 		out.write("</svg>");
	 		out.flush();
	 		
	 	}else{
	 		out.write("<svg width=\"100%\" height=\"100%\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\"><text x=\"20\" y=\"20\">empty</text></svg>");
	 	}
	
	}catch(Throwable e){
		Chk.chk(e);
		e.printStackTrace();
}
%>