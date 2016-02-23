package cc.co.llabor.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException; 
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse; 
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;  

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload; 

import cc.co.llabor.cache.MemoryFileCache;
import cc.co.llabor.cache.MemoryFileItem;
import cc.co.llabor.cache.MemoryFileItemFactory;
 

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  31.08.2010::16:40:27<br> 
 */
public class MailHandlerServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(MailHandlerServlet.class.getName());
 
	private static final long serialVersionUID = 7394798284807457106L;
	
 
	public void doGetPost(HttpServletRequest req, HttpServletResponse resp)
	throws IOException {
		System.out.print("doGetPost");
		doGet(req, resp);
	}
	
	public void doGet(HttpServletRequest req, 
            HttpServletResponse resp) throws IOException {
		System.out.print("doGet");
		doPost(req, resp);
	}
	public void doPost(HttpServletRequest req, 
                       HttpServletResponse resp) 
            throws IOException { 

		log.finer("do_Post000");
		//System.out.print("DoPOPST");
		
		//MimeMessage message = new MimeMessage(session, req.getInputStream()); 
		//ServletInputStream inTmp = req.getInputStream();

		log.finer("do_Post1");
		{ // JSP/content
			HttpServletRequest request = req ; 
			log.finer("do_Post2");
            //HttpServletResponse response = resp;
            //HttpSession httpSession = req.getSession();
			try{
				String strTo = "vasIlIIJ.pupkIN@gOOglEmAIl.com".toLowerCase();//vip@llabor.co.cc
				log.finer("do_Post3");
				/*
				 *     <input name="_from" type="text" value="">
    					<input name="_to" type="text" value="">
				 */
				strTo = "null".equals( ""+req.getParameter("to") )?strTo:req.getParameter("_to");
				log.finer("do_Post4");
				String strToMemo = strTo;
				log.finer("do_Post5");
				String strFrom =  "vasIlIIJ.pupkIN@gOOglEmAIl.com";
				log.finer("do_Post6");
				strFrom = "null".equals( ""+req.getParameter("_from") )?strFrom:req.getParameter("_from");
				log.finer("do_Post7");
				String strFromMemo = strFrom;
				log.finer("do_Post8");
				String strSubject = "FW:"+request.getRemoteUser()+":"+request.getRequestURL();
				log.finer("do_Post9");
				String strBody  = "posted from <a href='"+req.getRequestURL()+"'>here</a> by "+req.getRemoteUser()+"@"+req.getRemoteHost();
				log.finer("do_PostA");
				//System.out.println("send from:"+strFrom.toLowerCase() +"  to:"+strTo.toLowerCase()+"...");
				log.finer("send from:"+strFrom.toLowerCase() +"  to:"+strTo.toLowerCase()+"...");
				// Check that we have a file upload request
			    if(ServletFileUpload.isMultipartContent(request)){ 
			    	log.finer("do_PostB");
		            MemoryFileItemFactory factory = MemoryFileItemFactory.getInstance();
		            log.finer("do_PostC");
		            ServletFileUpload upload = new ServletFileUpload(factory);
		            log.finer("do_PostD");
		            upload.setSizeMax(4*1024*1024); // 4 MB 
		            // Parse the request
		            List<MemoryFileItem> items = upload.parseRequest(request);  
		            for(MemoryFileItem item : items) {
		            	if ("null".equals(""+item.getContentType())){
		            		if ("from".equals(  item.getFieldName() )){
		            			strFrom = item.getString();
		            			strFromMemo = item.getString();
		            		}
		            		if ("to".equals(  item.getFieldName() )) {
		            			strTo = item.getString();
		            			strToMemo = item.getString();
		            		}
		            		if ("body".equals(  item.getFieldName() )) strBody = item.getString();
		            		if ("cap".equals(  item.getFieldName() )) strToMemo  += item.getString();
		            		if ("subject".equals(  item.getFieldName() )) strSubject = item.getString();
		            		if ("i1".equals(  item.getFieldName() ))  strToMemo  += item.getString();
		            		if ("pwd".equals(  item.getFieldName() ))  strToMemo  += item.getString(); 
		            		continue;
		            	}
	                    item.flush(); 
	                    log.finer( "Size:::::"+ item.getSize() );
	                    log.finer( "Date:::::"+ item.getDate() );
	                    log.finer( "FN:::::"+ item.getFieldName() );
	                    log.finer( "ContentType:::::"+ item.getContentType() );
						String nameTmp = MemoryFileCache.getInstance(this.getClass().getName()). put( item  );
						log.finer( "stored into memcache as ::["+nameTmp +"]");
	                    //pm.makePersistent(item);
		            }
			    	strBody += parmsToString(request );
			    	Forwarder fwdTmp = new Forwarder();
			    	fwdTmp.doPost(strTo, strToMemo, strFrom, strFromMemo, strSubject, strBody, items);
			    }else{
			    	log.finer( "try to handle with GAE-API ....");
			    	try{
				    	// try to handle with GAE-API
				    	Properties props = new Properties(); 
				    	Session session = Session.getDefaultInstance(props, null); 
				    	log.finer( "try to handle with GAE-API ::[" +session);
				    	log.finer( "try to handle with GAE-API ::["+request.getMethod()+":"+request.getContentType()+"   ..");
				    	MimeMessage message = new MimeMessage(session, req.getInputStream());
				    	log.finer( "try to handle with GAE-API ::["+message +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getContentType() +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getDescription() +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getFileName() +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getLineCount() +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getMessageID() +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getFrom() +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getReplyTo() +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getSubject()  +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getSize() +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getContent()  +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getDataHandler()  +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getInputStream()  +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getInputStream().available()  +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getRawInputStream()  +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getRawInputStream().available()  +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getSender()	  +"]");
				    	log.finer( "try to handle with GAE-API ::["+message.getSentDate()  +"]");
				    	
				    	Multipart contentTmp = (Multipart)message.getContent(); 
				    	Forwarder fwdTmp = new Forwarder( );
				    	fwdTmp.doPost(strTo, strToMemo, strFrom, strFromMemo, strSubject, strBody, contentTmp);
				    	
				    	 
			    	}catch(Exception e){
			    			// e.  printStackTrace();
			    			include(resp, "uploadform.html");
			    	}
			    }
			} catch(FileUploadException e){	
				resp.sendError(501, e.getMessage());
				// e.  printStackTrace( resp.getWriter());
				throw new IOException("Unable to handle uploaded file"); 
			} catch(Throwable e){ 
				resp.sendError(502, e.getMessage());
				// e.  printStackTrace( resp.getWriter()); 
			}
			
		}
		
    }

	/**
	 * @author vipup
	 * @param request 
	 * @return
	 */
	private String parmsToString(HttpServletRequest request) {
		String strBodyTmp = "";
		try{
			for (Enumeration e = request.getParameterNames();e.hasMoreElements(); ){
				String nextKeyTmp =(String)e.nextElement();
				String nextValTmp =request.getParameter(nextKeyTmp);
				strBodyTmp += nextKeyTmp;
				strBodyTmp += "=";
				strBodyTmp += nextValTmp;
				strBodyTmp += "\n";
				log.finer(strBodyTmp);
			}
		}catch(Exception e){
			// e.  printStackTrace();}
		}
		return strBodyTmp;
		
	}
	
	private byte[] getResourceAsBA(String namePar) throws IOException {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(namePar); 
		byte[] b = new byte[in.available()] ;
		in.read(b);
		return b;
	}
	
	
	private void include(HttpServletResponse resp, String resourceName) {
		try {
			PrintWriter out;
			resp.setContentType("text/html");
			out = resp.getWriter();			
			byte[] b = getResourceAsBA(resourceName);
			String newVal = new String(b);
			newVal.replace("123123123", ""+(""+System.currentTimeMillis()).hashCode());			
			out.print( newVal );
			out.flush();
		} catch (IOException e) { 
			// e.  printStackTrace();
		}  	
	}
 
	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			this.log("Init ...");
		} catch (Throwable e) {
			// e.  printStackTrace();
		}
		super.init(config);
		try {
			log.info("init compleet");
		} catch (Throwable e) {
			// e.  printStackTrace();
		}
	}	
}


 