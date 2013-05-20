package mu.fi.sybila.esther.parsybonewidget;

import java.util.List;
import mu.fi.sybila.esther.heart.widget.EstherWidget;
import org.springframework.ui.ModelMap;

/**
 * The Parsybone Widget class.
 * 
 * @author George Kolcak
 */
public class ParsyboneWidget implements EstherWidget
{

    @Override
    public String getIdentifier()
    {
        return "Parsybone Widget";
    }

    @Override
    public void checkDependencies(List<EstherWidget> widgets) { }

    @Override
    public boolean opensFile(String type)
    {
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
        return "widget/unknown";
    }
    
}
