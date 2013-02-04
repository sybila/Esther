package ctnai.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LoginController
{
    @RequestMapping(value = "/Login", method = RequestMethod.GET)
    public String getLoginPage(ModelMap model)
    {
        model.addAttribute("Page", "login");
        return "frontpage";
    }
    
    @RequestMapping(value = "/LoginFailure", method = RequestMethod.GET)
    public String loginError(ModelMap model)
    {
        model.addAttribute("Error", "true");
        
        model.addAttribute("Page", "login");
        return "frontpage";
    }
    
    @RequestMapping(value="/Logout", method = RequestMethod.GET)
    public String logout(ModelMap model)
    {
        model.addAttribute("Page", "logout");
        return "frontpage";
    }
}
