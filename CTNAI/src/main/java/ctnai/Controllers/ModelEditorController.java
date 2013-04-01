package ctnai.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ModelEditorController
{
    @Autowired
    private FileSystemController fileSystemController;
    
    @RequestMapping(value = "Model", method = RequestMethod.GET)
    public String getModelEditor(@RequestParam("file") Long id, ModelMap map)
    {
        if (id == null)
        {
            return null;
        }
        
        map.addAttribute("model", fileSystemController.readFile(id));
        map.addAttribute("file", id);
        
        return "modelEditor";
    }
}
