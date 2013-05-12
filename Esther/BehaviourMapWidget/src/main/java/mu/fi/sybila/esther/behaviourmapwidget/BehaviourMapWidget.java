package mu.fi.sybila.esther.behaviourmapwidget;

import java.util.List;
import mu.fi.sybila.esther.heart.widget.EstherWidget;
import org.springframework.ui.ModelMap;

public class BehaviourMapWidget implements EstherWidget
{

    @Override
    public String getIdentifier()
    {
        return "Behaviour Map Widget";
    }

    @Override
    public void checkDependencies(List<EstherWidget> widgets) { }

    @Override
    public boolean opensFile(String type)
    {
        if ((type != null) && type.equals("xgmml"))
        {
            return true;
        }
        
        return false;
    }

    @Override
    public String[] allowedChildren(String type)
    {
        return new String[] { };
    }

    @Override
    public String startWidget(ModelMap map, Long id, Long parent)
    {
        map.addAttribute("file", id);
        
        return "widget/behaviourMap/interface";
    }
    
}
