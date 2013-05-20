package mu.fi.sybila.esther.heart.widget;

import java.util.List;
import org.springframework.ui.ModelMap;

/**
 * @author George Kolcak
 * 
 * Interface for Esther Widgets.
 */
public interface EstherWidget
{
    
    /**
     * Returns unique identifier of the Esther Widget.
     * 
     * @return Esther Widget identifier.
     */
    public String getIdentifier();
    
    /**
     * Checks the provided widgets for possible dependencies.
     * 
     * @param widgets The list of Esther Widgets present in the system.
     */
    public void checkDependencies(List<EstherWidget> widgets);
    
    /**
     * Determines whether the specified file type is opened by the Esther Widget.
     * 
     * @param type The extension of the file in question.
     * @return True if the specified file is opened by the Esther Widget. False otherwise.
     */
    public boolean opensFile(String type);
    
    /**
     * Specifies the possible subfile types of the file in question.
     * 
     * @param type The extension of the file in question.
     * @return List of file extensions of the allowed subfiles for the given file.
     */
    public String[] allowedChildren(String type);
    
    /**
     * Opens the given file in the widget.
     * 
     * @param map The map of UI properties.
     * @param id The ID of the file to be opened.
     * @param parent The Parent of the file to be opened. Or null if the file is root.
     * @return The Esther Widget UI page.
     */
    public String startWidget(ModelMap map, Long id, Long parent);
    
}
