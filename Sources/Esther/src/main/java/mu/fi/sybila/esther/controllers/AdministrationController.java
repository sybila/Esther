package mu.fi.sybila.esther.controllers;

import java.io.FileOutputStream;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.sql.DataSource;
import mu.fi.sybila.esther.heart.database.AdministrationManager;
import mu.fi.sybila.esther.heart.database.UserManager;
import mu.fi.sybila.esther.heart.database.entities.Post;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AdministrationController
{
    private AdministrationManager administrationManager = new AdministrationManager();
    private UserManager userManager = new UserManager();
    public static final Logger logger = Logger.getLogger(AdministrationController.class.getName());
    
    /**
     * Method for setting up the controller.
     * 
     * @param dataSource The DataSource used for database connection.
     */
    @Resource
    public void setDataSource(DataSource dataSource)
    {
        administrationManager.setDataSource(dataSource);
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
        administrationManager.setLogger(fs);
        userManager.setLogger(fs);
    }
    
    @RequestMapping(value = "/News/New", method = RequestMethod.GET)
    public String newNews(ModelMap map)
    {
        map.addAttribute("page", "administration/newNewsMessage");
        
        return "frontpage";
    }
    
    @RequestMapping(value = "/News/Post", method = RequestMethod.POST)
    public String newNewsPost(ModelMap map,
            @RequestParam("title") String title, @RequestParam("content") String content)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated() && IsAdmin())
        {
            String username = authentication.getName();
            
            Post post = new Post();
            
            post.setTitle(title);
            post.setContent(content);
            post.setCreator(userManager.getUserByUsername(username).getId());
            
            administrationManager.createPost(post);
            
            map.addAttribute("page", "administration/newsMessagePostSuccess");
        }
        else
        {
            map.addAttribute("page", "administration/newsMessagePostFailure");
        }
        
        return "frontpage";
    }
    
    @RequestMapping(value = "/News/Remove", method = RequestMethod.POST)
    @ResponseBody
    public String removeNewsPost(ModelMap map, @RequestParam("id") Long id)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated() && IsAdmin())
        {
            Post post = administrationManager.getPostById(id);
            
            administrationManager.removePost(post);
            
            return post.getId().toString();
        }
        else
        {
            return "ERROR=Only administrators can remove news posts.";
        }
    }
    
    private boolean IsAdmin()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        for(GrantedAuthority auth : authentication.getAuthorities())
        {
            if ("administrator".equals(auth.getAuthority()))
            {
                return true;
            }
        }
        
        return false;
    }
}
