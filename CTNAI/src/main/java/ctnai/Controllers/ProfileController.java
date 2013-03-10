package ctnai.Controllers;

import Forms.UserForm;
import ctnai.Database.User;
import ctnai.Database.UserInformation;
import ctnai.Database.UserManager;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
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

@Controller
public class ProfileController
{
    @Autowired
    @Qualifier("authenticationManager")
    private AuthenticationManager authenticationManager;
    
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
    
    @RequestMapping(value = "/Profile", method = RequestMethod.GET)
    public String openProfile(ModelMap map)
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
        
        map.addAttribute("hide_public_owned", information.getHidePublicOwned());
        
        map.addAttribute("page", "profile");
        
        return "frontpage";
    }
    
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
        
        map.addAttribute("email", user.getEmail());
        map.addAttribute("page", "editCoreProfile");
        
        return "frontpage";
    }
    
    @RequestMapping(value = "/Profile/Edit/Core", method = RequestMethod.POST)
    public String submitCoreProfile(ModelMap map, @RequestParam("email") String email)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
        {
            return null;
        }
        String currentUserName = authentication.getName();
        
        User user = userManager.getUserByUsername(currentUserName);
        
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
            map.addAttribute("page", "editCoreProfile");
        }
        else
        {
            user.setEmail(updatedUser.getEmail());
            
            userManager.updateUser(user);
            
            map.addAttribute("page", "editProfileSuccess");
        }
        
        return "frontpage";
    }
    
    @RequestMapping(value = "/Profile/Edit/Password", method = RequestMethod.GET)
    public String changePassword(ModelMap map)
    {        
        map.addAttribute("page", "changePassword");
        
        return "frontpage";
    }
    
    @RequestMapping(value = "/Profile/Edit/Password", method = RequestMethod.POST)
    public String submitNewPassword(ModelMap map,@RequestParam("oldPS") String oldPassword, 
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
            map.addAttribute("page", "changePassword");
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
            
            map.addAttribute("page", "changePasswordSuccess");
        }
        
        return "frontpage";
    }
    
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
        map.addAttribute("page", "editPreferencesProfile");
        
        return "frontpage";
    }
    
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

        map.addAttribute("page", "editProfileSuccess");
        
        return "frontpage";
    }
}
