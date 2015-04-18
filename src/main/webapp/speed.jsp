<%@page  contentType="image/gif"%><%@page errorPage="gif.gif"%><%@page import="java.io.InputStream"%><%@page import="java.io.FileInputStream"%><%
//OutputStream outTmp = response.getOutputStream();
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
InputStream inTmp = null;
try{
	String realPathTmp = org.jrobin.core.RrdFileBackend.getCanonicalPath("speed.gif");
	inTmp = new FileInputStream(realPathTmp);
}catch(Exception e){e.printStackTrace();}
if (inTmp == null){
	inTmp = this.getClass().getClassLoader().getResourceAsStream("speed.gif"); 
}
int b = 0;

try{
	HttpSession sTmp = request.getSession(true); 
}catch(Throwable e){ 
	e.printStackTrace();//response.getWriter()
}
try{
	for( b = inTmp.read();b>=0;b = inTmp.read()){
		out.write(b);
	}
	inTmp.close();
	out.flush();
	out.close();
	
}catch(Exception e){e.printStackTrace();}

%>
