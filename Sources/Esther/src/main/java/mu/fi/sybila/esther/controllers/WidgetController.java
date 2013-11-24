package mu.fi.sybila.esther.controllers;

import java.util.List;
import javax.annotation.Resource;
import mu.fi.sybila.esther.heart.widget.EstherWidget;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author George Kolcak
 * 
 * Controller for loading widgets.
 */
@Controller
public class WidgetController
{
    
    @Resource
    private List<EstherWidget> widgetList;
    
    /**
     * Handler method for opening files inside appropriate widgets.
     * 
     * @param map The map of UI properties.
     * @param fileType The file extension of the opened file. Used for identification of the proper widget.
     * @param id The ID of the opened file.
     * @param parent The ID of the parent file of the one opened. Or null if the file is root.
     * @return Path to the proper widget UI.
     */
    @RequestMapping(value = "Widget/Open", method = RequestMethod.GET)
    public String openWidget(ModelMap map, @RequestParam("type") String fileType,
        @RequestParam("file") Long id, @RequestParam(value = "parent", required = false) Long parent)
    {
        map.addAttribute("type", fileType);
        
        for (EstherWidget widget : widgetList)
        {
            if (widget.opensFile(fileType))
            {
                widget.checkDependencies(widgetList);
                
                return widget.startWidget(map, id, parent);
            }
        }
        
        return "widget/unknown";
    }
    
}
