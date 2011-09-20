<%@page 
	import="net.sf.jsr107cache.Cache"%><%@page 
	import="cc.co.llabor.cache.Manager"%><%@page 
	import="java.util.List"%><%@page 
	import="cc.co.llabor.cache.MemoryFileItem"%><%@page 
	import="cc.co.llabor.cache.MemoryFileItemFactory"%><%@page 
	import="org.apache.commons.fileupload.servlet.ServletFileUpload"%><%@page
	import="cc.co.llabor.cache.MemoryFileItemFactory"%><%@page 
    contentType="application/json"%><%

//	response.setContentType("application/json");		
//	response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
//	response.setHeader("Content-Disposition", "attachment;filename=anno.json");	

String uCase = request.getRequestURI();
String cxType = request.getContentType();
String methTmp = (request.getMethod().toLowerCase()); 
String idTmp = null;
int cmdID = -1;
for (String caseTmp: new String[]{"annoCREATE","annoUPDATE","annoREAD","annoDESTROY","annoSEARCH"}){
	cmdID ++;
	int posTmp =uCase.indexOf(caseTmp);
	if(posTmp>0){
		try{
			idTmp = uCase.substring(posTmp+caseTmp.length()+1);
		}catch(Throwable e){e.printStackTrace();}
		break;
	}
}
System.out.println("cmd = "+cmdID +" ID=["+idTmp+"]" );
Cache anno = Manager.getCache("Annotations");


System.out.println("cmd = "+cmdID +" ID=["+idTmp+"]" );
byte[] buf = new byte[request.getInputStream().available()];
request.getInputStream().read(buf);
String theA = new String(buf);
System.out.println("---therow----"+theA);

String theB = null;

if(ServletFileUpload.isMultipartContent(request)){ 
	 System.out.println("do_PostB");
    MemoryFileItemFactory factory = MemoryFileItemFactory.getInstance();
    System.out.println("do_PostC");
    ServletFileUpload upload = new ServletFileUpload(factory);
    System.out.println("do_PostD");
    upload.setSizeMax(4*1024*1024); // 4 MB 
    // Parse the request
    List<MemoryFileItem> items = upload.parseRequest(request);  
    for(MemoryFileItem item : items) {
   	 System.out.println(item);
    }
}else{
	 byte[] buf2 = new byte[request.getInputStream().available()];
	 request.getInputStream().read(buf2);
	 theB = new String(buf2);
	 System.out.println("---theroMULT----\n"+theB+"\n---theroMULT----");
}

String storedAnn = null;
String allKeys  = null;
switch(cmdID){
	case 0: 
		System.out.println("annoCREATE");
		idTmp = ""+System.currentTimeMillis();
		//"id": "cccccccccccccccccccccccccccccccc",  
		theB = "{\"id\":\""+idTmp+"\","  +theB.trim().substring(1);
		//break;
	case 1: 
		System.out.println("annoUPDATE");
		
		allKeys = (String)anno.get("ALL");
		allKeys = allKeys ==null? "":allKeys ;
		allKeys +=",";
		allKeys += idTmp;
		anno.put("ALL", allKeys);
		anno.put(idTmp, ""+theB);
		 
		break;
	case 2: 
		System.out.println("annoREAD");
		break;
	case 3: 
		System.out.println("annoDESTROY");
		allKeys = (String)anno.get("ALL");
		allKeys = allKeys ==null? "":allKeys ;
		allKeys = allKeys.indexOf(idTmp)>=0?allKeys.replace(idTmp,",").replace(",,",","):allKeys;		
		anno.put("ALL", allKeys);
		anno.remove(idTmp);
		break;
	case 4: 
		System.out.println("annoSEARCH");
		allKeys = (String)anno.get("ALL");
		
		if (allKeys != null){
			storedAnn = "{\"rows\":[\n";
			int cTmp = 0;
			for (String keyTmp:allKeys.split(",")){
				String rowTmp = (String)anno.get(keyTmp);
				
				if (null!= rowTmp ){
					if (cTmp>0){
						storedAnn +="\n,\n";
					}
					storedAnn +=rowTmp;
					cTmp++;
				}
			}
			storedAnn += "],\n \"total\": "+cTmp+" }";
		}
		break;
	default:
		System.out.println("___________");
		break;
	
}

HttpSession sTmp = request.getSession(true);

System.out.println("reqURL :"+request.getRequestURL()+":["+uCase+"]");
System.out.println(" METHOD:"+  methTmp +"::"+cxType );
 for (Object par:request.getParameterMap().keySet().toArray()){
 	System.out.println(":"+par +" =="+request.getParameter(""+par )+" :@"+sTmp.getId());
 } 


 // String storedAnn = null;""+session.getAttribute( "__ann__");
 //System.out.println("replace :"+storedAnn +" ==>> ["+theA+"]");
 //session.setAttribute( "__ann__",theA);

%>
<%if ("null".equals(""+storedAnn)   ){
//	{ "user":"Mark","text":"SFG SFG","ranges":[{"start":"/div[2]/p[2]","startOffset":10,"end":"/div[2]/p[2]","endOffset":274}], }
// {"permissions":{"read":[],"update":[],"delete":[],"admin":[]},"user":"Mark","text":"333","ranges":[{"start":"/h2","startOffset":4,"end":"/h2","endOffset":17}],"quote":"Comedy of Err"}
//http://c0824ce0:8080/examples/openshakespeare/GESHA/

	%> {
  "rows": [
	{
      "id": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa9",  
      "text": "hallo s0fcsse!",  
      "uri": "http://c0824ce0:8080/examples/openshakespeare/GESHA/",         	  
      "ranges":[{"start":"/div[2]/h2","end":"/div[2]/h2","startOffset":0,"endOffset":19}] ,  
	  "tags":["jazz"],
	  "quote":"The Comedy of Error" ,  
      "type": "Annotation" 
    },
	{
		"text":"222","tags":[],"ranges":[{"start":"/div[2]/h2","startOffset":6,"end":"/div[2]/h2","endOffset":20}],"quote":"medy of Errors","uri":"http://c0824ce0:8080/examples/openshakespeare/GESHA/",
      "id": "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",        
	  "tags":["jazz","box"],
      "uri": "http://c0824ce0:8080/examples/openshakespeare/GESHA/",         	  
	  "quote":"The Comedy of Error" ,  
      "type": "Annotation" 
    },
	{
		"text":"333","tags":[],"ranges":[{"start":"/div[2]/h2","startOffset":4,"end":"/div[2]/h4","endOffset":30}],"quote":"Comedy of ErrorsACT ISCENE I.  A hall in DUKE SOLIN","uri":"http://c0824ce0:8080/examples/openshakespeare/GESHA/"	,
      "id": "cccccccccccccccccccccccccccccccc",        
      "uri": "http://c0824ce0:8080/examples/openshakespeare/GESHA/",         	  
      "quote":"The Comedy of Error" ,  
	  "tags":["jazz","box","sex"],
      "type": "Annotation" 
    } 
 
  ], 
  "total": 3
}<%
}else{ 	
	%>  <%=storedAnn  %> <%
}%>
 