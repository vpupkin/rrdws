package cc.co.llabor.mail;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  29.09.2011::11:47:29<br> 
 */
public class SMTPTransport extends com.sun.mail.smtp.SMTPTransport{

	public SMTPTransport(Session session, URLName urlname) {
		super(session, urlname);
		// TODO Auto-generated constructor stub
	}
    /**
     * Performs the actual protocol-specific connection attempt.
     * Will attempt to connect to "localhost" if the host was null. <p>
     *
     * Unless mail.smtp.ehlo is set to false, we'll try to identify
     * ourselves using the ESMTP command EHLO.
     *
     * If mail.smtp.auth is set to true, we insist on having a username
     * and password, and will try to authenticate ourselves if the server
     * supports the AUTH extension (RFC 2554).
     *
     * @param	host		  the name of the host to connect to
     * @param	port		  the port to use (-1 means use default port)
     * @param	user		  the name of the user to login as
     * @param	passwd	  	  the user's password
     * @return	true if connection successful, false if authentication failed
     * @exception MessagingException	for non-authentication failures
     */
    protected boolean protocolConnect(String host, int port, String user,
			      String passwd) throws MessagingException {
    	try{
    		Properties p = this.session.getProperties();
    		host = host==null? p.getProperty("mail.smtp.host"):host;
    		//port = port== -1 ? Integer.parseInt( p.getProperty("mail.smtp.port")):port;
    	}catch(Exception e){
    		// e.  printStackTrace();
    	}
    	//host = host==null? p.getProperty("mail.smtp.host"):host;
    	return super.protocolConnect(host, port, user, passwd);
    }
}


 