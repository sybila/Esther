package mu.fi.sybila.esther.controllers;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.sql.DataSource;
import mu.fi.sybila.esther.heart.database.AdministrationManager;
import mu.fi.sybila.esther.heart.database.UserManager;
import mu.fi.sybila.esther.heart.database.entities.Post;
import mu.fi.sybila.esther.heart.widget.EstherWidget;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author George Kolcak
 * 
 * Controller for main menu.
 */
@Controller
public class HomeController
{
    private AdministrationManager administrationManager = new AdministrationManager();
    private UserManager userManager = new UserManager();
    public static final Logger logger = Logger.getLogger(HomeController.class.getName());
    
    @Resource
    private List<EstherWidget> widgetList;
    
    @Resource
    public void setDataSource(DataSource dataSource)
    {
        administrationManager.setDataSource(dataSource);
        userManager.setDataSource(dataSource);
    }
    
    public void setLogger(FileOutputStream fs)
    {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
        administrationManager.setLogger(fs);
        userManager.setLogger(fs);
    }
    
    /**
     * Handler method for unrecognised request.
     * 
     * @return Redirect to Home.
     */
    @RequestMapping(value = "*", method = RequestMethod.GET)
    public String redirectDefault()
    {
        return "redirect:/Home";
    }
    
    /**
     * Handler method for Home page.
     * 
     * @param map The map of UI properties.
     * @return Home page.
     */
    @RequestMapping(value = "/Home", method = RequestMethod.GET)
    public String getHomepage(ModelMap map)
    {
        List<Post> posts = administrationManager.getAllPosts();
        
        List<Post> orderedPosts = new ArrayList<>();
        
        for (int i = (posts.size() - 1); i >= 0; i--)
        {
            Post post = posts.get(i);
            
            post.setCreatorNick(userManager.getUserById(post.getCreator()).getUsername());
            
            orderedPosts.add(post);
        }
        
        map.addAttribute("news", orderedPosts);
        
        map.addAttribute("page", "home");
        
        return "frontpage";
    }
    
    /**
     * Handler method for Analysis page.
     * 
     * @param map The map of UI properties.
     * @return Analysis page.
     */
    @RequestMapping(value = "/Analysis", method = RequestMethod.GET)
    public String getAnalysisPage(ModelMap map)
    {
        List<String> globalJavascripts = new ArrayList<>();
        
        for (EstherWidget widget : widgetList)
        {
            globalJavascripts.addAll(Arrays.asList(widget.globalJavascripts()));
        }
        
        map.addAttribute("global_js", globalJavascripts);
        
        map.addAttribute("page", "analysis");
        
        return "frontpage";
    }
    
    /**
     * Handler method for Guide page.
     * 
     * @param map The map of UI properties.
     * @return Guide page.
     */
    @RequestMapping(value = "/Guide", method = RequestMethod.GET)
    public String getGuidePage(ModelMap map)
    {
        map.addAttribute("page", "guide");
        
        return "frontpage";
    }
    
}
