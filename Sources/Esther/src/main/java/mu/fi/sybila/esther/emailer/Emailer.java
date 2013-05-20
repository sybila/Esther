package mu.fi.sybila.esther.emailer;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author George Kolcak
 * 
 * Esther class used for sending e-mails.
 */
public class Emailer
{
    
    private String senderAddress;
    private String host;
    
    private String signature;
    
    /**
     * Emailer constructor
     * 
     * @param address The sender e-mail address.
     * @param host Host server for e-mail sending.
     */
    public Emailer(String address, String host)
    {
        this.senderAddress = address;
        this.host = host;
        
        signature = "";
    }

    /**
     * method for setting signature of the messages.
     * 
     * @param signature The signature to be attached to the e-mails. HTML form is expected.
     */
    public void setSignature(String signature)
    {
        this.signature = signature;
    }
    
    /**
     * method for sending an e-mail message to the specified recipient.
     * 
     * @param recipient The e-mail address of the recipient.
     * @param subject The subject of the e-mail.
     * @param content The message of the e-mail. The message is expected in HTML format.
     * @throws MessagingException If sending the e-mail fails.
     */
    public void sendMail(String recipient, String subject, String content) throws MessagingException
    {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        
        Session session = Session.getDefaultInstance(properties);
        
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderAddress));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        
        message.setSubject(subject);
        message.setContent((content + "<BR/><BR/>" + signature), "text/html");
        
        Transport.send(message);
    }
    
}
