package mu.fi.sybila.esther.controllers;

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
        map.addAttribute("page", "analysis");
        
        return "frontpage";
    }
    
    /**
     * Handler method for About page.
     * 
     * @param map The map of UI properties.
     * @return About page.
     */
    @RequestMapping(value = "/About", method = RequestMethod.GET)
    public String getAboutPage(ModelMap map)
    {
        map.addAttribute("page", "about");
        
        return "frontpage";
    }
    
}
