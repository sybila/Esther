package ctnai.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class RegistrationController
{
    @RequestMapping(value = "/Registration", method = RequestMethod.GET)
    public String getRegistrationPage(ModelMap model)
    {
        model.addAttribute("Page", "registration");
        return "frontpage";
    }
}
