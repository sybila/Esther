package ctnai.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController
{
    @RequestMapping(value = "/Login", method = RequestMethod.GET)
    public String getLoginPage(ModelMap model)
    {
        model.addAttribute("page", "login");
        return "frontpage";
    }
    
    @RequestMapping(value = "/LoginFailure", method = RequestMethod.GET)
    public String loginError(ModelMap model)
    {
        model.addAttribute("error", "true");
        
        model.addAttribute("page", "login");
        return "frontpage";
    }
    
    @RequestMapping(value="/Logout", method = RequestMethod.GET)
    public String logout(ModelMap model)
    {
        model.addAttribute("page", "logout");
        return "frontpage";
    }
}
