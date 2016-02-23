package cc.co.llabor.mail;
import java.io.IOException; 
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;  
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart; 
import javax.mail.util.ByteArrayDataSource;

import cc.co.llabor.cache.MemoryFileItem;
import cc.co.llabor.cache.MemoryFileItemFactory;;
/** 
 * <b>Description:TODO</b>
 * @author      vipup<br>
 * <br>
 * <b>Copyright:</b>     Copyright (c) 2006-2008 Monster AG <br>
 * <b>Company:</b>       Monster AG  <br>
 * 
 * Creation:  31.08.2010::16:52:27<br> 
 */
public class Forwarder {
	private static final Logger log = Logger.getLogger(""+Forwarder.class.getName());
 
	/**
	 * 
	 * 
	 * 
	 * @author vipup
	 * @param strTo
	 * @param strToMemo
	 * @param strFrom
	 * @param strFromMemo
	 * @param strSubject
	 * @param strBody
	 * @throws IOException
	 * @throws MessagingException
	 */
	public void doPost(
			String strTo, String strToMemo, 
			String strFrom, String strFromMemo, 
			String strSubject, 
			String strBody
			
			 ) throws IOException, MessagingException {
		Properties props = new Properties(); // 
		// props.put("mail.smtp.host", "mail.host");
		// props.put("mail.smtp.port", "" + 25);
		props.put("mail.debug", "true" );

		Authenticator fakeAuth = null;// new FakeAuthenticator();
		Session session = Session.getDefaultInstance(props, fakeAuth);
		 		
		Message msg = new MimeMessage(session); 
		 //setup message (from, to, subject, etc)
		msg.setFrom(new InternetAddress(strFrom, strFromMemo));
		msg.addRecipient(Message.RecipientType.TO, new InternetAddress( strTo ,  strToMemo ));
		// CC
		//msg.addRecipient(Message.RecipientType.CC, new InternetAddress(md.getEmail(), "Dr. " + md.getNickname()));
		msg.setText(strBody);
		msg.saveChanges();
		log.warning(" sending...");
		
		Transport.send(msg); // Transport.class.getClassLoader().getParent().getParent().getParent().getParent()
		log.warning(" done.");
		
	}
	
	/**
	 * @deprecated
	 * @author vipup
	 * @param strTo
	 * @param strToMemo
	 * @param strFrom
	 * @param strFromMemo
	 * @param strSubject
	 * @param strBody
	 * @param attachmentName
	 * @param attachmentData
	 * @param strAttachContentType
	 */
	public void doPost(
			String strTo, String strToMemo, 
			String strFrom, String strFromMemo, 
			String strSubject, String strBody, 
			String attachmentName, byte[] attachmentData, String strAttachContentType   
		){
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
		
        try {
            Message msg = new MimeMessage(session);
			InternetAddress toAddress = new InternetAddress(strTo, strToMemo);
			InternetAddress fromAddress = new InternetAddress(strFrom, strFromMemo);
			msg.setFrom(fromAddress); 
			msg.addRecipient(Message.RecipientType.TO, toAddress);
			
			msg.setSubject(strSubject);
            msg.setText(strBody);

            Multipart mp = new MimeMultipart(); 
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(strBody, "text/html");
            mp.addBodyPart(htmlPart);

            MimeBodyPart attachment = new MimeBodyPart();
            attachment.setFileName(attachmentName);
            attachment.setContent(attachmentData, strAttachContentType);
            mp.addBodyPart(attachment); 
            msg.setContent(mp);	 
            Transport.send(msg);	
        } catch (UnsupportedEncodingException e) {
        	log.throwing(this.getClass().getName(), "doPost", e); 
			// e.  printStackTrace();
		} catch (MessagingException e) { 
			log.throwing(this.getClass().getName(), "doPost", e);
			// e.  printStackTrace();
		}finally{
			System.out.println("eOf"+strTo+strToMemo+strFrom+strFromMemo+strSubject+strBody);
        } 
	}
	
	
		public void doPost(
				String strTo, String strToMemo, 
				String strFrom, String strFromMemo, 
				String strSubject, 
				String strBody,
				List<MemoryFileItem> items) throws IOException, MessagingException {
		Properties props = new Properties(); // 
		// props.put("mail.smtp.host", "mail.host");
		// props.put("mail.smtp.port", "" + 25);
		props.put("mail.debug", "true" );

		Authenticator fakeAuth = null;// new FakeAuthenticator();
		Session session = Session.getDefaultInstance(props, fakeAuth);
		 
		try {
			Message msg = new MimeMessage(session); 
			Multipart mp = new MimeMultipart();
			//we'll have an htmlpart and an attachment partin our mimemultipart message
			MimeBodyPart htmlPart = new MimeBodyPart();
			 //setup message (from, to, subject, etc)
			msg.setFrom(new InternetAddress(strFrom,   strFromMemo));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress( strTo ,  strToMemo ));
			// CC
			//msg.addRecipient(Message.RecipientType.CC, new InternetAddress(md.getEmail(), "Dr. " + md.getNickname()));
			msg.setSubject(strSubject);
			//msg.setText(strBody); !!!
			htmlPart.setContent(strBody, "text/html");
			mp.addBodyPart(htmlPart);
			log.warning("" +
					"MESSAGE FROM::" + strFrom+"\n" +
					"MESSAGE TO::" + strTo+"\n" +
					"SUBJ: " + strSubject+"  \n" +
					"---{\n" + strBody +"\n}---");
			int i=0;
			if (items!=null)
			for (MemoryFileItem item : items) {
				String ctypeTmp = ""+item.getContentType();
				if ("null".equals( ctypeTmp)) continue;
				// z.B. multipart/alternative
				if (!isAllowed(ctypeTmp) ) continue;
				MimeBodyPart attachment = new MimeBodyPart();
                //prepare attachment using abytearraydatasource
				i++;
				byte[] attachmentData = item.get();
				DataSource dsTmp = new ByteArrayDataSource(attachmentData, item .getContentType());
				String namePrefix = "fw.#"+i;
				namePrefix = "";
				attachment.setFileName(namePrefix + item.getName());
				log.warning(">>>" + item.getName() + "::" + item.getContentType());
				DataHandler dhTmp = new DataHandler(dsTmp);
				attachment.setDataHandler(dhTmp);
                //put the parts together into a multipart 				
				mp.addBodyPart(attachment);
			} 
			msg.setContent(mp);
			msg.saveChanges();
			log.warning(" sending...");
			
			Transport.send(msg); // Transport.class.getClassLoader().getParent().getParent().getParent().getParent()
			log.warning(" done.");
			
		} catch (UnsupportedEncodingException e) { 
			log.throwing(this.getClass().getName(), "doPost", e);
			// e.  printStackTrace();
		} catch (MessagingException e) { 
			log.throwing(this.getClass().getName(), "doPost", e);
			// e.  printStackTrace();
		} catch (Exception e) { 
			log.throwing(this.getClass().getName(), "doPost", e);
			// e.  printStackTrace();
		} catch (Throwable e) { 
			log.throwing(this.getClass().getName(), "doPost", e);
			// e.  printStackTrace();
		} finally { // session.getTransport("smtp")// OK!!! :
					// session.getTransport("smtp")
			log.warning("--------------------eOf" + strTo + "|" + strToMemo
					+ "|" + strFrom + "|" + strFromMemo + "|" + strSubject
					+ "|" + strBody);
		}
	}
	
	private boolean isAllowed(String ctypeTmp) throws IOException {
		Properties gaeMimes = new Properties();
		gaeMimes.load(  this.getClass().getClassLoader().getResourceAsStream("gae_allowed_mimetypes.properties"));
		if (null != gaeMimes.getProperty(ctypeTmp)) return true;
		return false;	 
	}

	/**
	 * send as a mail multiparted HTTP-req-data
	 * 
	 * @author vipup
	 * @param toPar
	 * @param toMemPar
	 * @param fromPar
	 * @param fromMemPar
	 * @param subjPar
	 * @param bodyPar
	 * @param mp
	 * @throws IOException
	 * @throws MessagingException
	 */
	public void doPost(
				String toPar , String toMemPar , 
				String fromPar ,  String fromMemPar , 
				String subjPar, String bodyPar,
				Multipart mp) throws IOException, MessagingException {
			// transform GAE-MultiPart into List<MemoryFileItem> items
		List<MemoryFileItem> itemsTmp = new ArrayList<MemoryFileItem> ();
		for (int i=0;i<mp.getCount();i++){
			BodyPart part = mp.getBodyPart(i);
			String contentType = part.getContentType();
			boolean isFormField = false;
			String fileName = part.getFileName();
			String fieldName = part.getDescription();
			MemoryFileItem item = MemoryFileItemFactory.getInstance().createItem(fieldName , contentType , isFormField , fileName  );
			part.writeTo( item.getOutputStream() );
			item.flush();
			itemsTmp.add(item);
		}
		doPost( 
				toPar , toMemPar ,
				fromPar , fromMemPar , 
				subjPar, bodyPar,
				itemsTmp
		);
		 
	}

 

	
	       public void emailList(byte[] attachmentData) {
               Properties props = new Properties();
               Session session = Session.getDefaultInstance(props,null);
               session.setDebug(true);

//              String msgBody = "Here is an encrypted copy of thepatient scut list you requested.  Enter your password to open the  attached file.";
               String htmlBody = "<b>Hi</b>  Here is the encrypted ptscut list yourequested.  Please <b><i>enter your password</i></b> to open thedocument.\nCheers!";

               MyData md = new DefaultMe();
               try {

                       Message msg = new MimeMessage(session);
                       Multipart mp = new MimeMultipart();
                       //we'll have an htmlpart and an attachment partin our mimemultipart message
                       MimeBodyPart htmlPart = new MimeBodyPart();
                       MimeBodyPart attachment = new MimeBodyPart();

                       //setup message (from, to, subject, etc)
                       msg.setFrom(new InternetAddress(md.getEmail()));
                       msg.addRecipient(Message.RecipientType.TO, new InternetAddress(md.getEmail(), "Dr. " +md.getNickname()));
                       msg.setSubject("Here is the patient list yourequested.");
//                      msg.setText(msgBody);

                       //prepare html part
                       htmlPart.setContent(htmlBody, "text/html");

                       //prepare attachment using abytearraydatasource
                       DataSource src = new ByteArrayDataSource(attachmentData, "application/pdf");
                       attachment.setFileName("ptlist.pdf");
                       attachment.setDataHandler(new DataHandler(src));

                       //put the parts together into a multipart
                       mp.addBodyPart(htmlPart);
                       mp.addBodyPart(attachment);

                       //set the content of the message to be themultipart
                       msg.setContent(mp);
                       msg.saveChanges();//I think this is necessary,but, not sure....

                       Transport.send(msg);
               } catch (AddressException e) {
                       // e.  printStackTrace();
               } catch (MessagingException e) {
                       // e.  printStackTrace();
                       // c.sep("sfe: "+e.getInvalidAddresses()[0].toString());
               } catch (UnsupportedEncodingException e) { 
                       // e.  printStackTrace();
               }
       } 		
	
	       public void emailLists(byte[][] attachmentDatas) {
               Properties props = new Properties();
               Session session = Session.getDefaultInstance(props,null);
               session.setDebug(true);

//              String msgBody = "Here is an encrypted copy of thepatient scut list you requested.  Enter your password to open the  attached file.";
               String htmlBody = "<b>Hi</b>  Here is the encrypted ptscut list yourequested.  Please <b><i>enter your password</i></b> to open thedocument.\nCheers!";

               MyData md = new DefaultMe();
               try {

                       Message msg = new MimeMessage(session);
                       Multipart mp = new MimeMultipart();
                       //we'll have an htmlpart and an attachment partin our mimemultipart message
                       MimeBodyPart htmlPart = new MimeBodyPart();
                       //setup message (from, to, subject, etc)
                       msg.setFrom(new InternetAddress(md.getEmail()));
                       msg.addRecipient(Message.RecipientType.TO, new InternetAddress(md.getEmail(), "Dr. " +md.getNickname()));
                       msg.setSubject("Here is the patient list yourequested.");
//                      msg.setText(msgBody);

                       //prepare html part
                       htmlPart.setContent(htmlBody, "text/html");
                       mp.addBodyPart(htmlPart);
                       int i=0;
                       for (byte[]attachmentData:attachmentDatas){
                    	   final MimeBodyPart attachment = new MimeBodyPart();
                    	   i++;
	                       //prepare attachment using abytearraydatasource
	                       DataSource src = new ByteArrayDataSource(attachmentData, "application/pdf");
	                       attachment.setFileName("ptlist"+i+".pdf");
	                       attachment.setDataHandler(new DataHandler(src));
	
	                       //put the parts together into a multipart 
	                       mp.addBodyPart(attachment);
                       }
                       //set the content of the message to be themultipart
                       msg.setContent(mp);
                       msg.saveChanges();//I think this is necessary,but, not sure....

                       Transport.send(msg);
               } catch (AddressException e) {
                       // e.  printStackTrace();
               } catch (MessagingException e) {
                       // e.  printStackTrace();
                       // c.sep("sfe: "+e.getInvalidAddresses()[0].toString());
               } catch (UnsupportedEncodingException e) { 
                       // e.  printStackTrace();
               }
       } 		
 
}


 