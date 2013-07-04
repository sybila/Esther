package mu.fi.sybila.esther.modeleditorwidget;

import java.util.List;
import mu.fi.sybila.esther.heart.controllers.FileSystemController;
import mu.fi.sybila.esther.heart.widget.EstherWidget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;

/**
 * The Model Editor Widget class.
 * 
 * @author George Kolcak
 */
public class ModelEditorWidget implements EstherWidget
{
    @Autowired
    private FileSystemController fileSystemController;

    private Boolean parsybonePresent = false;
    
    @Override
    public String getIdentifier()
    {
        return "Model Editor Widget";
    }

    @Override
    public void checkDependencies(List<EstherWidget> widgets)
    {
        for (EstherWidget widget : widgets)
        {
            if (widget.getIdentifier().equals("Parsybone Widget"))
            {
                parsybonePresent = true;
            }
        }
    }

    @Override
    public boolean opensFile(String type)
    {
        if ((type != null) && type.equals("dbm"))
        {
            return true;
        }
        
        return false;
    }

    @Override
    public String[] allowedChildren(String type)
    {
        if (opensFile(type))
        {
            return new String[] { "sqlite" };
        }
        
        return new String[] { };
    }

    @Override
    public String[] globalJavascripts()
    {
        return new String[] { "modelEditor" };
    }
    
    @Override
    public String startWidget(ModelMap map, Long id, Long parent)
    {
        if (parsybonePresent)
        {
            map.addAttribute("parsybone", "present");
        }
        
        if (id != null)
        {
            String data = fileSystemController.readFile(id);
            
            if (!data.startsWith("ERROR"))
            {
                map.addAttribute("model", data);
                map.addAttribute("file", id);

                return "widget/modelEditor/editor";
            }
        }
        
        return "widget/fileBroken";
    }
    
}
