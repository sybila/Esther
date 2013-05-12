package mu.fi.sybila.esther.controllers;

import java.util.List;
import javax.annotation.Resource;
import mu.fi.sybila.esther.heart.widget.EstherWidget;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WidgetController
{
    
    @Resource
    private List<EstherWidget> widgetList;
    
    @RequestMapping(value = "Widget/Start", method = RequestMethod.GET)
    public String startWidget(ModelMap map, @RequestParam("type") String fileType,
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
