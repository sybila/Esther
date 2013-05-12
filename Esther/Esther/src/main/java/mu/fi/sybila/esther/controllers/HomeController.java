package mu.fi.sybila.esther.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController
{
    
    @RequestMapping(value = "*", method = RequestMethod.GET)
    public String redirectDefault()
    {
        return "redirect:/Home";
    }
    
    @RequestMapping(value = "/Home", method = RequestMethod.GET)
    public String getHomepage(ModelMap map)
    {
        map.addAttribute("page", "home");
        
        return "frontpage";
    }
    
    @RequestMapping(value = "/Analysis", method = RequestMethod.GET)
    public String getAnalysisPage(ModelMap model)
    {
        model.addAttribute("page", "analysis");
        return "frontpage";
    }
    
}
