package mu.fi.sybila.esther.emailer;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Emailer
{
    
    private String senderAddress;
    private String host;
    
    private String signature;
    
    public Emailer(String address, String host)
    {
        this.senderAddress = address;
        this.host = host;
        
        signature = "";
    }

    public String getSignature()
    {
        return signature;
    }

    public void setSignature(String signature)
    {
        this.signature = signature;
    }
    
    public void sendMail(String recipient, String subject, String content) throws MessagingException
    {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        
        Session session = Session.getDefaultInstance(properties);
        
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderAddress));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        
        message.setSubject(subject);
        message.setContent((content + signature), "text/html");
        
        Transport.send(message);
    }
    
}
