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
import mu.fi.sybila.esther.heart.database.forms.InformationForm;
import mu.fi.sybila.esther.heart.database.forms.UserForm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author George Kolcak
 * 
 * Controller for account management.
 */
@Controller
public class AccountController
{
    
    public static final String[] countries = new String[]
    {
        "Afganistan",
        "Albania",
        "Algeria",
        "Andorra",
        "Angola",
        "Antigua and Barbuda",
        "Argentina",
        "Armenia",
        "Australia",
        "Austria",
        "Azerbaijan",
        "Bahrain",
        "Bangladesh",
        "Barbados",
        "Belarus",
        "Belgium",
        "Belize",
        "Benin",
        "Bhutan",
        "Bolivia",
        "Bosnia and Herzegovina",
        "Botswana",
        "Brazil",
        "Bulgiaria",
        "Burkina Faso",
        "Burundi",
        "Cambodia",
        "Cameroon",
        "Canada",
        "Cape Verde",
        "Central African Republic",
        "Chad",
        "Chile",
        "China",
        "Columbia",
        "Comoros",
        "Congo",
        "Costa Rica",
        "Croatia",
        "Cuba",
        "Cyprus",
        "Czech Republic",
        "Democratic Republic of the Congo",
        "Denmark",
        "Djibouti",
        "Dominica",
        "Dominican Republic",
        "East Timor",
        "Egypt",
        "Equador",
        "Equatorial Guinea",
        "Eritrea",
        "Estonia",
        "Ethiopia",
        "Fiji",
        "Finland",
        "France",
        "Gabon",
        "Gambia",
        "Georgia",
        "Germany",
        "Ghana",
        "Greece",
        "Grenada",
        "Guatemala",
        "Guayana",
        "Guinea",
        "Guinea-Bissau",
        "Haiti",
        "Honduras",
        "Hungary",
        "Iceland",
        "India",
        "Indonesia",
        "Iran",
        "Iraq",
        "Ireland",
        "Italy",
        "Ivory Coast",
        "Izrael",
        "Jamaica",
        "Japan",
        "Jordan",
        "Kazakhstan",
        "Kenya",
        "Kiribato",
        "Kuwait",
        "Kyrgyzstan",
        "Laos",
        "Latvia",
        "Lebanon",
        "Lesotho",
        "Liberia",
        "Libya",
        "Lichtenstein",
        "Lithuania",
        "Luxembourg",
        "Macedonia",
        "Madagascar",
        "Malawi",
        "Mali",
        "Malta",
        "Marshall Islands",
        "Mauretania",
        "Mauritius",
        "Mexico",
        "Micronesia",
        "Moldova",
        "Monaco",
        "Mongolia",
        "Montenegro",
        "Morocco",
        "Mozambique",
        "Myanmar",
        "Namibia",
        "Nauru",
        "Nepal",
        "Netherlands",
        "New Zealand",
        "Nicaragua",
        "Niger",
        "Nigeria",
        "North Korea",
        "Norway",
        "Oman",
        "Pakistan",
        "Palau",
        "Panama",
        "Papua New Guinea",
        "Paraguay",
        "Peru",
        "Philippines",
        "Poland",
        "Portugal",
        "Qatar",
        "Romania",
        "Russia",
        "Rwanda",
        "Salvador",
        "Samoa",
        "San Marino",
        "Saudi Arabia",
        "Sierra Leona",
        "Singapore",
        "Senegal",
        "Serbia",
        "Seychelles",
        "Slovakia",
        "Slovenia",
        "Solomon Islands",
        "Somalia",
        "South Africa",
        "South Korea",
        "South Sudan",
        "Spain",
        "St. Kitts and Nevis",
        "St. Lucia",
        "São Tomé and Príncipe",
        "St. Vincent and Grenadines",
        "Sudan",
        "Suriname",
        "Swaziland",
        "Sweden",
        "Syria",
        "Tajikistan",
        "Tanzania",
        "Taiwan",
        "Thailand",
        "Togo",
        "Tonga",
        "Trinidad and Tobago",
        "Tunisia",
        "Turkey",
        "Turkmenistan",
        "Tuvalu",
        "Uganda",
        "Ukraine",
        "United Arab Emirates",
        "United Kingdom",
        "United States of America",
        "Uruguay",
        "Uzbekistan",
        "Vanuatu",
        "Vatican",
        "Venezuela",
        "Vietnam",
        "Yemen",
        "Zambia",
        "Zimbabwe",
    };
    
    private static final Random rnd = new Random();
    
    private UserManager userManager = new UserManager();
    public static final Logger logger = Logger.getLogger(AccountController.class.getName());
    
    @Value("${web_url}")
    private String estherURL;

    @Value("${email_address}")
    private String emailAddress;
    @Value("${email_host}")
    private String emailHost;
    private static final String estherSignature = "<p>=== THIS IS AN AUTO-GENERATED E-MAIL, RESPONDING IS FUTILE ===</p><br/><p>Yours sincerely, Esther mail bot</p>";
    
    /**
     * Method for setting up the controller.
     * 
     * @param dataSource The DataSource used for database connection.
     */
    @Resource
    public void setDataSource(DataSource dataSource)
    {
        userManager.setDataSource(dataSource);
    }
    
    /**
     * Method for setting up the controller.
     * 
     * @param fs The output stream for logger output.
     */
    public void setLogger(FileOutputStream fs)
    {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
        userManager.setLogger(fs);
    }
    
    /**
     * Handler method for logging in.
     * 
     * @param map The map of UI properties.
     * @return Login page.
     */
    @RequestMapping(value = "/Login", method = RequestMethod.GET)
    public String getLoginPage(ModelMap map)
    {
        map.addAttribute("page", "security/login");
        
        return "frontpage";
    }
    
    /**
     * Handler method for login failure.
     * 
     * @param map The map of UI properties.
     * @return Login page with the appropriate error.
     */
    @RequestMapping(value = "/LoginFailure", method = RequestMethod.GET)
    public String loginError(ModelMap map)
    {
        map.addAttribute("error", true);
        
        map.addAttribute("page", "security/login");
        
        return "frontpage";
    }
    
    /**
     * Handler method for logging out.
     * 
     * @param map The map of UI properties.
     * @return Logout page.
     */
    @RequestMapping(value="/Logout", method = RequestMethod.GET)
    public String logout(ModelMap map)
    {
        map.addAttribute("page", "security/logout");
        return "frontpage";
    }
    
    /**
     * Handler method for registration.
     * 
     * @param map
     * @return Registration page.
     */
    @RequestMapping(value = "/Registration", method = RequestMethod.GET)
    public String getRegistrationPage(ModelMap map)
    {
        map.addAttribute("countries", countries);
        map.addAttribute("user", new UserForm());
        map.addAttribute("page", "security/registration");
        return "frontpage";
    }
    
    /**
     * Handler method for registration form submission.
     * 
     * @param map The map of UI properties.
     * @param request The HTTP request.
     * @return Registration success page is the registration is completed successfully. Registration page with the appropriate error otherwise.
     */
    @RequestMapping(value = "/Register", method = RequestMethod.POST)
    public String register(ModelMap map, HttpServletRequest request)
    {
        UserForm userForm = UserForm.extractFromRequest(request);
        InformationForm informationForm = InformationForm.extractFromRequest(request);
        
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
        
        UserInformation userInformation = informationForm.validate(errorBuilder);
        
        User nameCheck = userManager.getUserByUsername(newUser.getUsername());
        User mailCheck = userManager.getUserByEmail(newUser.getEmail());
        
        if (nameCheck != null)
        {
            errorBuilder.append("Username: ");
            errorBuilder.append(newUser.getUsername());
            errorBuilder.append(" is alrady in use.");
            errorBuilder.append("<br/>\n");
            
            map.addAttribute("passRecPrompt", true);
        }
        
        if (mailCheck != null)
        {
            errorBuilder.append("E-Mail: ");
            errorBuilder.append(newUser.getEmail());
            errorBuilder.append(" is alrady in use.");
            errorBuilder.append("<br/>\n");
            
            map.addAttribute("nameRecPrompt", true);
        }
        
        if (errorBuilder.length() == 0)
        {
            Long id;
            if ((id = userManager.registerUser(newUser)) != null)
            {                
                userManager.setUserRole(newUser, "user");
                
                userInformation.setId(id);
                
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
                
                map.addAttribute("email", newUser.getEmail());
                map.addAttribute("page", "security/registrationSuccess");
            }
        }
        else
        {
            map.addAttribute("countries", countries);
            map.addAttribute("error", errorBuilder.toString());
            map.addAttribute("user", userForm);
            map.addAttribute("information", informationForm);
            map.addAttribute("page", "security/registration");
        }
        
        return "frontpage";
    }
    
    /**
     * Handler method for account activation.
     * 
     * @param map The map of UI properties.
     * @param id The ID of the user account to be activated.
     * @param token The activation token.
     * @return Activation success page if the activation is successful. Activation failure page otherwise.
     */
    @RequestMapping(value = "/Activate", method = RequestMethod.GET)
    public String activateUser(ModelMap map, @RequestParam("user") Long id, @RequestParam("token") String token)
    {
        User user = userManager.getUserById(id);
        
        if (token.equals(userManager.getActivationToken(user)))
        {
            user.setEnabled(true);
            
            userManager.updateUser(user);
            
            userManager.deactivateTokenForUser(user);
            
            map.addAttribute("page", "security/activationSuccess");
        }
        else
        {
            map.addAttribute("page", "security/activationFailure");
        }
        
        return "frontpage";
    }
    
    /**
     * Handler method for username recovery.
     * 
     * @param map The map of UI properties.
     * @return Username recovery page.
     */
    @RequestMapping(value="/Recover/Username", method = RequestMethod.GET)
    public String recoverUsername(ModelMap map)
    {
        map.addAttribute("page", "security/usernameRecovery");
        
        return "frontpage";
    }
    
    /**
     * Handler method for username recovery form submission.
     * 
     * @param map The map of UI properties.
     * @param email The e-mail associated with the coveted username.
     * @return Username recovery success page if the recovery is successful. Username recovery page with the appropriate error otherwise.
     */
    @RequestMapping(value="/Recover/Username", method = RequestMethod.POST)
    public String emailUsername(ModelMap map, @RequestParam("email") String email)
    {
        if ((email == null) || email.isEmpty())
        {
            map.addAttribute("error", "You have to enter your e-mail to proceed.");
            map.addAttribute("page", "security/usernameRecovery");
        }
        else
        {
            User user = userManager.getUserByEmail(email);
            
            if (user == null)
            {
                map.addAttribute("error", "The e-mail " + email + " doesn't belong to any user.");
                map.addAttribute("registerPrompt", true);
                map.addAttribute("page", "security/usernameRecovery");
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
                
                map.addAttribute("email", user.getEmail());
                map.addAttribute("page", "security/usernameRecoverySuccess");
            }
        }
        
        return "frontpage";
    }
    
    /**
     * Handler method for password recovery.
     * 
     * @param map The map of UI properties.
     * @return Password recovery page.
     */
    @RequestMapping(value="/Recover/Password", method = RequestMethod.GET)
    public String recoverPassword(ModelMap map)
    {
        map.addAttribute("page", "security/passwordRecovery");
        
        return "frontpage";
    }
    
    /**
     * Handler method for password recovery form submission.
     * 
     * @param map The map of UI properties.
     * @param email The e-mail associated with the coveted password.
     * @param username The username associated with coveted password.
     * @return Password recovery success page is the recovery is successful. Password recovery page with the appropriate error otherwise.
     */
    @RequestMapping(value="/Recover/Password", method = RequestMethod.POST)
    public String emailPassword(ModelMap map, @RequestParam("email") String email, @RequestParam("username") String username)
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

                map.addAttribute("registerPrompt", true);
            }
            else if (!user.getEmail().equals(email))
            {
                errorBuilder.append("The username and e-mail don't match!");
            }
        }
        
        if (errorBuilder.length() > 0)
        {
            map.addAttribute("error", errorBuilder.toString());
            map.addAttribute("page", "security/passwordRecovery");
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

            map.addAttribute("email", user.getEmail());
            map.addAttribute("page", "security/passwordRecoverySuccess");
        }
        
        return "frontpage";
    }
    
    /**
     * Method for random password generation.
     * 
     * @return Random String of 8 hexadecimal digits.
     */
    private String generateRandomPassword()
    {   
        return Long.toHexString(rnd.nextLong());
    }
    
    /**
     * Method for random activation token generation
     * 
     * @return Random String of 64 hexadecimal digits.
     */
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
