package mu.fi.sybila.esther.controllers;

import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import mu.fi.sybila.esther.heart.database.UserManager;
import mu.fi.sybila.esther.heart.database.entities.User;
import mu.fi.sybila.esther.heart.database.entities.UserInformation;
import mu.fi.sybila.esther.heart.database.forms.InformationForm;
import mu.fi.sybila.esther.heart.database.forms.UserForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author George Kolcak
 * 
 * Controller for profile management.
 */
@Controller
public class ProfileController
{
    
    @Autowired
    @Qualifier("authenticationManager")
    private AuthenticationManager authenticationManager;
    
    private UserManager userManager = new UserManager();
    public static final Logger logger = Logger.getLogger(ProfileController.class.getName());
    
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
    
    @RequestMapping(value = "/Profile", method = RequestMethod.GET)
    public String viewProfile(ModelMap map, @RequestParam("user") Long id)
    {
        User user = userManager.getUserById(id);
        
        InitialiseProfile(map, user);
        
        return "frontpage";
    }
    
    /**
     * Handler method for opening profile for edit.
     * 
     * @param map The map of UI properties.
     * @return Profile page.
     */
    @RequestMapping(value = "/Profile/Edit", method = RequestMethod.GET)
    public String editProfile(ModelMap map)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
        {
            return null;
        }
        String username = authentication.getName();
        
        User user = userManager.getUserByUsername(username);
        
        InitialiseProfile(map, user);
        
        map.addAttribute("edit", true);
        
        return "frontpage";
    }
    
    private void InitialiseProfile(ModelMap map, User user)
    {
        UserInformation information = userManager.getUserInformation(user.getId());
        
        UserForm userForm = UserForm.extractFromUser(user);
        InformationForm informationForm = InformationForm.extractFromUserInformation(information);
        
        map.addAttribute("user", userForm);
        
        map.addAttribute("information", informationForm);
        
        map.addAttribute("page", "profile/profile");
    }
    
    /**
     * Hander method for core profile information editing.
     * 
     * @param map The map of UI properties.
     * @return Core profile information edit page.
     */
    @RequestMapping(value = "/Profile/Edit/Core", method = RequestMethod.GET)
    public String editCoreProfile(ModelMap map)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
        {
            return null;
        }
        String username = authentication.getName();
        
        User user = userManager.getUserByUsername(username);
        UserInformation information = userManager.getUserInformation(user.getId());
        
        map.addAttribute("email", user.getEmail());
        map.addAttribute("show_email", information.getShowEmail());
        map.addAttribute("page", "profile/editCore");
        
        return "frontpage";
    }
    
    /**
     * Handler method for new core profile information submission.
     * 
     * @param map The map of UI properties.
     * @param email The new e-mail.
     * @return Profile edit success page if the profile update succeeds. Core profile information edit page with the appropriate error otherwise.
     */
    @RequestMapping(value = "/Profile/Edit/Core", method = RequestMethod.POST)
    public String submitCoreProfile(ModelMap map, @RequestParam("email") String email,
            @RequestParam(value = "show_email", required = false) Boolean showEmail)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
        {
            return null;
        }
        String currentUserName = authentication.getName();
        
        User user = userManager.getUserByUsername(currentUserName);
        UserInformation information = userManager.getUserInformation(user.getId());
        
        UserForm updatedUser = new UserForm();
        updatedUser.setEmail(email);
        
        StringBuilder errorBuilder = new StringBuilder();
        
        if (updatedUser.validateEmail(errorBuilder))
        {            
            User emailCheck = userManager.getUserByEmail(updatedUser.getEmail());
            
            if ((emailCheck != null) && !emailCheck.equals(user))
            {
                errorBuilder.append("E-mail already in use.</br>");
            }
        }
        
        if (errorBuilder.length() > 0)
        {
            map.addAttribute("email", updatedUser.getEmail());
            map.addAttribute("error", errorBuilder.toString());
            map.addAttribute("page", "profile/editCore");
        }
        else
        {
            user.setEmail(updatedUser.getEmail());
            information.setShowEmail((showEmail != null) && showEmail);
            
            userManager.updateUser(user);
            userManager.updateUserInformation(information);
            
            map.addAttribute("page", "profile/editSuccess");
        }
        
        return "frontpage";
    }
    
    /**
     * Handler method for changing password.
     * 
     * @param map The map of UI properties.
     * @return Change password page.
     */
    @RequestMapping(value = "/Profile/Edit/Password", method = RequestMethod.GET)
    public String changePassword(ModelMap map)
    {        
        map.addAttribute("page", "profile/changePassword");
        
        return "frontpage";
    }
    
    /**
     * Handler method for password submission.
     * 
     * @param map The map of UI properties.
     * @param oldPassword Current password for verification.
     * @param newPassword The new password.
     * @param cNewPassword The new password copy used for confirmation.
     * @param request The HTTP request.
     * @return Password change success page if changing the password succeeds. Change password page with the appropriate error otherwise.
     */
    @RequestMapping(value = "/Profile/Edit/Password", method = RequestMethod.POST)
    public String submitNewPassword(ModelMap map,@RequestParam("oldPassword") String oldPassword, 
        @RequestParam("password") String newPassword, @RequestParam("cPassword") String cNewPassword,
        HttpServletRequest request)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
        {
            return null;
        }
        String currentUserName = authentication.getName();
        
        User user = userManager.getUserByUsername(currentUserName);
        
        UserForm updatedUser = new UserForm();
        updatedUser.setPassword(newPassword);
        updatedUser.setcPassword(cNewPassword);
        
        StringBuilder errorBuilder = new StringBuilder();
        
        String oldPass = user.getPassword();
        
        try
        {
            user.setPassword(oldPassword);
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
        {
            return null;
        }
        
        if (oldPass.equals(user.getPassword()))
        {
            updatedUser.validatePassword(errorBuilder);
        }
        else
        {
            errorBuilder.append("Wrong password!</br>");
        }
                
        if (errorBuilder.length() > 0)
        {
            map.addAttribute("error", errorBuilder.toString());
            map.addAttribute("page", "profile/changePassword");
        }
        else
        {
            try
            {
                user.setPassword(newPassword);
            }
            catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
            {
                return null;
            }
            
            userManager.updateUser(user);
            
            SecurityContextHolder.clearContext();
            
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getUsername(), newPassword);
            token.setDetails(new WebAuthenticationDetails(request));
            Authentication auth = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            map.addAttribute("page", "profile/changePasswordSuccess");
        }
        
        return "frontpage";
    }
    
    /**
     * Handler method for editing personal information.
     * 
     * @param map The map of UI properties.
     * @return Edit personal information page.
     */
    @RequestMapping(value = "/Profile/Edit/Personal", method = RequestMethod.GET)
    public String changePersonal(ModelMap map)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
        {
            return null;
        }
        String userName = authentication.getName();
        
        User user = userManager.getUserByUsername(userName);
        
        UserInformation information = userManager.getUserInformation(user.getId());
        
        map.addAttribute("countries", AccountController.countries);
        map.addAttribute("information", information);
        map.addAttribute("page", "profile/editPersonal");
        
        return "frontpage";
    }
    
    /**
     * Handler method for new personal information submission.
     * 
     * @param map The map of UI properties.
     * @param request The HTTP request.
     * @return Profile edit success page if the preferences update succeeds. Personal information edit page with the appropriate error otherwise.
     */
    @RequestMapping(value = "/Profile/Edit/Personal", method = RequestMethod.POST)
    public String submitPersonal(ModelMap map, HttpServletRequest request)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
        {
            return null;
        }
        String currentUserName = authentication.getName();
        
        StringBuilder errorBuilder = new StringBuilder();
        
        User user = userManager.getUserByUsername(currentUserName);
        UserInformation information = userManager.getUserInformation(user.getId());
        
        InformationForm infoForm = InformationForm.extractFromRequest(request);
        UserInformation newInformation = infoForm.validate(errorBuilder);
        
        information.setCountry(newInformation.getCountry());
        information.setOrganization(newInformation.getOrganization());
        
        userManager.updateUserInformation(information);

        if (errorBuilder.length() == 0)
        {
            map.addAttribute("page", "profile/editSuccess");
        }
        else
        {
            map.addAttribute("countries", AccountController.countries);
            map.addAttribute("error", errorBuilder.toString());
            map.addAttribute("information", information);
            map.addAttribute("page", "profile/editPersonal");
        }
        
        return "frontpage";
    }
    
    /**
     * Handler method for editing profile preferences.
     * 
     * @param map The map of UI properties.
     * @return Edit profile preferences page.
     */
    @RequestMapping(value = "/Profile/Edit/Preferences", method = RequestMethod.GET)
    public String changePreferences(ModelMap map)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
        {
            return null;
        }
        String userName = authentication.getName();
        
        User user = userManager.getUserByUsername(userName);
        
        UserInformation information = userManager.getUserInformation(user.getId());
        
        map.addAttribute("hide_public_owned", information.getHidePublicOwned());
        map.addAttribute("page", "profile/editPreferences");
        
        return "frontpage";
    }
    
    /**
     * Handler method for new profile preferences submission.
     * 
     * @param map The map of UI properties.
     * @param hidePublicOwned The selection to hide or display one's own public files.
     * @return Profile edit success page if the preferences update succeeds. Profile preferences edit page with the appropriate error otherwise.
     */
    @RequestMapping(value = "/Profile/Edit/Preferences", method = RequestMethod.POST)
    public String submitPreferences(ModelMap map, @RequestParam(value = "hide_public_owned", required = false) Boolean hidePublicOwned)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
        {
            return null;
        }
        String currentUserName = authentication.getName();
        
        User user = userManager.getUserByUsername(currentUserName);
        UserInformation information = userManager.getUserInformation(user.getId());
        
        information.setHidePublicOwned((((hidePublicOwned != null) && hidePublicOwned.booleanValue()) ? true : false));

        userManager.updateUserInformation(information);

        map.addAttribute("page", "profile/editSuccess");
        
        return "frontpage";
    }
    
}
