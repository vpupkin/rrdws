package cc.co.llabor.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.proxy.ProxyInfo;
import org.jivesoftware.smack.proxy.ProxyInfo.ProxyType;

/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  29.09.2011::00:07:53<br> 
 */
public class GtalkAppender extends AppenderSkeleton {
    private String user;
    public String getUser() { 
			return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	private String password;
    public String getPassword() { 
			return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	private String receiver; 

    public String getReceiver() { 
			return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	private boolean isFirst = true;
    XMPPConnection connection;
    ConnectionConfiguration connConfig;

//    public void sendMessage(String content) {
//      try {
//        Skype.chat(receiver).send(content);
//      } catch (SkypeException ex) {
//
//      }
//    }

    @Override
    protected void append(LoggingEvent event) {
    	//ProxyInfo proxy = new ProxyInfo(ProxyType.HTTP, "proxy.host", 8080, null, null);
    	ProxyInfo proxy = new ProxyInfo(ProxyType.HTTP, "127.0.0.1", 9666, null, null);
      if (isFirst) {
        connConfig = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com",proxy);
        //connConfig.setServiceName("googlemail.com");
        //connConfig.setSASLAuthenticationEnabled(false);
        // You have to put this code before you login

        SASLAuthentication.supportSASLMechanism("PLAIN", 0);
        connection = new XMPPConnection(connConfig);
        try {
          connection.connect();
        } catch (XMPPException ex) {
        	// ex. printStackTrace();
        }
        while (!connection.isConnected()) {
        	try {
                connection.login(user, password);

        		connection.connect();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.  printStackTrace();
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				// e.  printStackTrace();
			}
        }
        isFirst = !isFirst;
      }
      Message msg = new Message(receiver, Message.Type.chat);
      msg.setBody(getLayout().format(event));
      connection.sendPacket(msg);
    }

    @Override
    public boolean requiresLayout() {
      return true;
    }

    @Override
    public void close() {
    }
  }


 