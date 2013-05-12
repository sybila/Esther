package mu.fi.sybila.esther.heart.widget;

import java.util.List;
import org.springframework.ui.ModelMap;

public interface EstherWidget
{
    public String getIdentifier();
    
    public void checkDependencies(List<EstherWidget> widgets);
    
    public boolean opensFile(String type);
    
    public String[] allowedChildren(String type);
    
    public String startWidget(ModelMap map, Long id, Long parent);
}
