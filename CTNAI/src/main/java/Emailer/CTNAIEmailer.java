package Emailer;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class CTNAIEmailer
{
    private static final String senderAddress = "ctnai@fi.muni.cz";
    private static final String host = "localhost";
    
    public static void sendMail(String recipient, String content) throws MessagingException
    {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        
        Session session = Session.getDefaultInstance(properties);
        
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderAddress));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        
        message.setSubject("CTNAI account activation");
        message.setContent(content, "text/html");
        
        Transport.send(message);
    }
}
