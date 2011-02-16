<%@page import="org.jrobin.GraphInfo"
%><%@page import="java.io.File"
%><%@page import="java.io.OutputStream"
%><%@page import="org.jrobin.cmd.RrdCommander"
%><%@page import="java.io.FileInputStream"
%><%@page  contentType="image/gif"
%><%
response.setContentType("image/gif");
%><%
// init tmDIR
 
// gen.jsp generates gif.preview by RRD-name. 
// known usage: list.jsp
String dbParName = request.getParameter("db");
String dbName = dbParName==null?"X-1979395149":dbParName;
String EXT = ".rrd";
dbName = dbName.toLowerCase().indexOf(EXT)>0?dbName.substring(0,dbName.length()-EXT.length()):dbName; 
String _h = request.getParameter("_h");
_h = _h == null? "132":_h;
String _w = request.getParameter("_w");
_w = _w == null? "164":_w;
String _end = request.getParameter("_end");
_end  = _end  == null? "end-1hour":_end ;

String cmdTmp = "rrdtool graph - -h "+ _h +" -w  "+_w+" --start="+_end+"  DEF:dbdata="+dbName+".rrd:data:AVERAGE  LINE2:dbdata#44EE4499  LINE1:dbdata#003300AA ";
// bikoz of '-' in the filename :
GraphInfo img = (GraphInfo)RrdCommander.execute(cmdTmp);
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
response.setHeader("Content-Disposition", "inline;filename="+dbName+".gif");
%><%
try{
	byte[]buf = img.getBytes();
	OutputStream respOutTmp = response.getOutputStream();
	respOutTmp.write(buf);
}catch(Throwable e){ 
		e.printStackTrace(response.getWriter());
}
%>