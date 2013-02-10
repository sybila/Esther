package ctnai.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController
{
    @RequestMapping(value = "/Home", method = RequestMethod.GET)
    public String getHomepage(ModelMap model)
    {
        model.addAttribute("page", "home");
        return "frontpage";
    }
    
    @RequestMapping(value = "*", method = RequestMethod.GET)
    public String redirectDefault(ModelMap model)
    {
        return "redirect:/Home";
    }
    
    @RequestMapping(value = "/Analysis", method = RequestMethod.GET)
    public String getAnalysisPage(ModelMap model)
    {
        model.addAttribute("page", "analysis");
        return "frontpage";
    }
}
