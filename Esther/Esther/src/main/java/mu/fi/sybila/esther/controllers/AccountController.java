package mu.fi.sybila.esther.controllers;

import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import mu.fi.sybila.esther.emailer.Emailer;
import mu.fi.sybila.esther.heart.database.UserManager;
import mu.fi.sybila.esther.heart.database.entities.User;
import mu.fi.sybila.esther.heart.database.entities.UserInformation;
import mu.fi.sybila.esther.heart.database.forms.UserForm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountController
{
    
    private static final Random rnd = new Random();
    
    private UserManager userManager = new UserManager();
    public static final Logger logger = Logger.getLogger(AccountController.class.getName());
    
    @Value("${web_url}")
    private String estherURL;

    @Value("${email_address}")
    private String emailAddress;
    @Value("${email_host}")
    private String emailHost;
    private static final String estherSignature = "<br/><br/><p>=== THIS IS AN AUTO-GENERATED E-MAIL, RESPONDING IS FUTILE ===</p><br/><p>Yours sincerely, Esther mail bot</p>";
    
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
    
    @RequestMapping(value = "/Login", method = RequestMethod.GET)
    public String getLoginPage(ModelMap model)
    {
        model.addAttribute("page", "security/login");
        return "frontpage";
    }
    
    @RequestMapping(value = "/LoginFailure", method = RequestMethod.GET)
    public String loginError(ModelMap model)
    {
        model.addAttribute("error", true);
        
        model.addAttribute("page", "security/login");
        return "frontpage";
    }
    
    @RequestMapping(value="/Logout", method = RequestMethod.GET)
    public String logout(ModelMap model)
    {
        model.addAttribute("page", "security/logout");
        return "frontpage";
    }
    
    @RequestMapping(value = "/Registration", method = RequestMethod.GET)
    public String getRegistrationPage(ModelMap model)
    {
        model.addAttribute("user", new UserForm());
        model.addAttribute("page", "security/registration");
        return "frontpage";
    }
    
    @RequestMapping(value = "/Register", method = RequestMethod.POST)
    public String register(ModelMap model, HttpServletRequest request)
    {
        UserForm userForm = UserForm.extractFromRequest(request);
        
        StringBuilder errorBuilder = new StringBuilder();
        
        User newUser;
        
        try
        {
            newUser = userForm.validate(errorBuilder);
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
        {
            return null;
        }
        
        User nameCheck = userManager.getUserByUsername(newUser.getUsername());
        User mailCheck = userManager.getUserByEmail(newUser.getEmail());
        
        if (nameCheck != null)
        {
            errorBuilder.append("Username: ");
            errorBuilder.append(newUser.getUsername());
            errorBuilder.append(" is alrady in use.");
            errorBuilder.append("<br/>\n");
            
            model.addAttribute("passRecPrompt", true);
        }
        
        if (mailCheck != null)
        {
            errorBuilder.append("E-Mail: ");
            errorBuilder.append(newUser.getEmail());
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
                
                UserInformation userInformation = new UserInformation();
                userInformation.setId(id);
                userInformation.setHidePublicOwned(Boolean.TRUE);
                
                userManager.setUserInformation(userInformation);
                
                String token = generateToken();
                userManager.setActivationToken(newUser, token);
                
                try
                {
                    Emailer emailer = new Emailer(emailAddress, emailHost);
                    emailer.setSignature(estherSignature);
                    
                    emailer.sendMail(newUser.getEmail(), "Esther account activation", ("<h2>Welcome " +
                        newUser.getUsername() + "!</h2><p>Click the link below to complete the registration.</p>" +
                        "<p>" + estherURL + "Activate?user=" + id + "&token=" + token + "</p>"));
                    
                    logger.log(Level.INFO, ("Succesfuly sent activation e-mail to: " + newUser.getEmail()));
                }
                catch (MessagingException e)
                {
                    logger.log(Level.SEVERE, ("Failed sending activation e-mail to: " + newUser.getEmail()), e);
                }
                
                model.addAttribute("email", newUser.getEmail());
                model.addAttribute("page", "security/registrationSuccess");
            }
        }
        else
        {
            model.addAttribute("error", errorBuilder.toString());
            model.addAttribute("user", userForm);
            model.addAttribute("page", "security/registration");
        }
        
        return "frontpage";
    }
    
    @RequestMapping(value = "/Activate", method = RequestMethod.GET)
    public String activateUser(ModelMap model, @RequestParam("user") Long id, @RequestParam("token") String token)
    {
        User user = userManager.getUserById(id);
        
        if (token.equals(userManager.getActivationToken(user)))
        {
            user.setEnabled(true);
            
            userManager.updateUser(user);
            
            userManager.deactivateTokenForUser(user);
            
            model.addAttribute("page", "security/activationSuccess");
        }
        else
        {
            model.addAttribute("page", "security/activationFailure");
        }
        
        return "frontpage";
    }
    
    @RequestMapping(value="/Recover/Username", method = RequestMethod.GET)
    public String recoverUsername(ModelMap model)
    {
        model.addAttribute("page", "security/usernameRecovery");
        return "frontpage";
    }
    
    @RequestMapping(value="/Recover/Username", method = RequestMethod.POST)
    public String emailUsername(ModelMap model, @RequestParam("email") String email)
    {
        if ((email == null) || email.isEmpty())
        {
            model.addAttribute("error", "You have to enter your e-mail to proceed.");
            model.addAttribute("page", "security/usernameRecovery");
        }
        else
        {
            User user = userManager.getUserByEmail(email);
            
            if (user == null)
            {
                model.addAttribute("error", "The e-mail " + email + " doesn't belong to any user.");
                model.addAttribute("registerPrompt", true);
                model.addAttribute("page", "security/usernameRecovery");
            }
            else
            {
                try
                {
                    Emailer emailer = new Emailer(emailAddress, emailHost);
                    emailer.setSignature(estherSignature);
                    
                    emailer.sendMail(user.getEmail(), "Esther username recovery", ("<h2>Username recovery</h2>" +
                        "<p>Your username is " + user.getUsername() + "</p>" +
                        "<p>If you're not the one who asked for this information please ignore this e-mail and allow us to apologize for the inconvenience caused.</p>"));
                    
                    logger.log(Level.INFO, ("Succesfuly sent username recovery e-mail to: " + user.getEmail()));
                }
                catch (MessagingException e)
                {
                    logger.log(Level.SEVERE, ("Failed sending username recovery e-mail to: " + user.getEmail()), e);
                }
                
                model.addAttribute("email", user.getEmail());
                model.addAttribute("page", "security/usernameRecoverySuccess");
            }
        }
        
        return "frontpage";
    }
    
    @RequestMapping(value="/Recover/Password", method = RequestMethod.GET)
    public String recoverPassword(ModelMap model)
    {
        model.addAttribute("page", "security/passwordRecovery");
        return "frontpage";
    }
    
    @RequestMapping(value="/Recover/Password", method = RequestMethod.POST)
    public String emailPassword(ModelMap model, @RequestParam("email") String email, @RequestParam("username") String username)
    {
        StringBuilder errorBuilder = new StringBuilder();
        
        if ((email == null) || email.isEmpty())
        {
            errorBuilder.append("You have to fill in your e-mail to proceed.");
        }
        
        if ((username == null) || username.isEmpty())
        {
            errorBuilder.append("You have to fill in your username to proceed.");
        }
        
        User user = null;
        
        if (username != null)
        {
            user = userManager.getUserByUsername(username);

            if (user == null)
            {
                errorBuilder.append("There's no user named ");
                errorBuilder.append(username);
                errorBuilder.append('.');

                model.addAttribute("registerPrompt", true);
            }
            else if (!user.getEmail().equals(email))
            {
                errorBuilder.append("The username and e-mail don't match!");
            }
        }
        
        if (errorBuilder.length() > 0)
        {
            model.addAttribute("error", errorBuilder.toString());
            model.addAttribute("page", "security/passwordRecovery");
        }
        else
        {
            try
            {
                user.setPassword(generateRandomPassword());
            }
            catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
            {
                return null;
            }
            
            userManager.updateUser(user);
            
            try
            {
                Emailer emailer = new Emailer(emailAddress, emailHost);
                emailer.setSignature(estherSignature);
                
                emailer.sendMail(user.getEmail(), "Esther password recovery", ("<h2>Password recovery</h2>" +
                    "<p>Hello " + user.getUsername() + "!</br>" +
                    "Your new password is: " + user.getPassword() + "</p>" +
                    "<p>If you're not the one who asked for password recovery we're sorry for the inconvenience caused.</p>"));

                logger.log(Level.INFO, ("Succesfuly sent password recovery e-mail to: " + user.getEmail()));
            }
            catch (MessagingException e)
            {
                logger.log(Level.SEVERE, ("Failed sending password recovery e-mail to: " + user.getEmail()), e);
            }

            model.addAttribute("email", user.getEmail());
            model.addAttribute("page", "security/passwordRecoverySuccess");
        }
        
        return "frontpage";
    }
    
    private String generateRandomPassword()
    {   
        return Long.toHexString(rnd.nextLong());
    }
    
    private String generateToken()
    {
        StringBuilder tokenBuilder = new StringBuilder();
        
        for (int i = 0; i < 8; i++)
        {
            tokenBuilder.append(Integer.toHexString(rnd.nextInt()));
        }
        
        return tokenBuilder.toString();
    }
    
}
