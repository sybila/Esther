package ctnai.Controllers;

import Emailer.CTNAIEmailer;
import Forms.UserForm;
import ctnai.Database.User;
import ctnai.Database.UserManager;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.sql.DataSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController
{
    private UserManager userManager = new UserManager();
    public static final Logger logger = Logger.getLogger(FileSystemController.class.getName());
    
    @Resource
    public void setDataSource(DataSource dataSource)
    {
        userManager.setDataSource(dataSource);
    }
    
    public void setLogger(FileOutputStream fs)
    {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
        userManager.setLogger(fs);
    }
    
    @RequestMapping(value = "/Registration", method = RequestMethod.GET)
    public String getRegistrationPage(ModelMap model)
    {
        model.addAttribute("user", new UserForm());
        model.addAttribute("page", "registration");
        return "frontpage";
    }
    
    @RequestMapping(value = "/Register", method = RequestMethod.POST)
    public String register(ModelMap model, @RequestParam("username") String username,
        @RequestParam("password") String password, @RequestParam("cPassword") String cPassword,
        @RequestParam("email") String email)
    {
        UserForm userForm = new UserForm(username, password, cPassword, email);
        
        StringBuilder errorBuilder = new StringBuilder();
        
        User newUser = userForm.validate(errorBuilder);
        
        User nameCheck = userManager.getUserByUsername(username);
        User mailCheck = userManager.getUserByEmail(email);
        
        if (nameCheck != null)
        {
            errorBuilder.append("Username: ");
            errorBuilder.append(username);
            errorBuilder.append(" is alrady in use.");
            errorBuilder.append("<br/>\n");
            
            model.addAttribute("passRecPrompt", true);
        }
        
        if (mailCheck != null)
        {
            errorBuilder.append("E-Mail: ");
            errorBuilder.append(email);
            errorBuilder.append(" is alrady in use.");
            errorBuilder.append("<br/>\n");
            
            model.addAttribute("nameRecPrompt", true);
        }
        
        if (errorBuilder.length() == 0)
        {
            Long id;
            if ((id = userManager.registerUser(newUser)) != null)
            {
                userManager.setUserRole(newUser, "user");
                
                try
                {
                    CTNAIEmailer.sendMail(newUser.getEmail(), ("<h2>Welcome " +
                        newUser.getUsername() + "!</h2><p>Click the link below to complete the registration.</p>" +
                        "<p>localhost:8084/CNTAI/ConfirmEmailuser=" + id + "</p>"));
                    
                    logger.log(Level.INFO, ("Succesfuly sent e-mail to: " + newUser.getEmail()));
                }
                catch (MessagingException e)
                {
                    logger.log(Level.SEVERE, ("Failed sending confirmation e-mail to: " + newUser.getEmail()), e);
                }
                
                model.addAttribute("email", newUser.getEmail());
                model.addAttribute("page", "registrationSuccess");
            }
        }
        else
        {
            model.addAttribute("error", errorBuilder.toString());
            model.addAttribute("user", userForm);
            model.addAttribute("page", "registration");
        }
        
        return "frontpage";
    }
}
