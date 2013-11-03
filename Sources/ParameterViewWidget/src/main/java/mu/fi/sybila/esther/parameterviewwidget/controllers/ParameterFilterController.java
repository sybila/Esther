package mu.fi.sybila.esther.parameterviewwidget.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.sql.DataSource;
import mu.fi.sybila.esther.heart.database.FileSystemManager;
import mu.fi.sybila.esther.heart.database.entities.EstherFile;
import mu.fi.sybila.esther.sqlitemanager.SQLiteException;
import mu.fi.sybila.esther.sqlitemanager.SQLiteManager;
import mu.fi.sybila.esther.sqlitemanager.parameterfilter.ParameterFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for parameter filter and view manipulation.
 * 
 * @author George Kolcak
 */
@Controller
public class ParameterFilterController
{
    
    private FileSystemManager fileSystemManager = new FileSystemManager();
    private SQLiteManager sqliteManager = new SQLiteManager();
    public static final Logger logger = Logger.getLogger(ParameterFilterController.class.getName());
    
    /**
     * Method for setting up the controller.
     * 
     * @param dataSource The DataSource used for database connection.
     */
    @Resource
    public void setDataSource(DataSource dataSource)
    {
        fileSystemManager.setDataSource(dataSource);
    }
    
    /**
     * Method for setting up the controller.
     * 
     * @param dataLocation The location of the data in the local file system.
     */
    @Value("${data_location}")
    public void setDataLocation(String dataLocation)
    {
        fileSystemManager.setDataLocation(dataLocation);
    }
    
    /**
     * Method for setting up the controller.
     * 
     * @param fs The output stream for logger output.
     */
    public void setLogger(FileOutputStream fs)
    {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
        fileSystemManager.setLogger(fs);
    }
    
    /**
     * Handler method for filtering parameter view.
     * 
     * @param map The map of UI properties. 
     * @param id The id of the parameter set file.
     * @param filter The filter encoded in string.
     *               The string filter should have one constraint on every "line" (separated by \n).
     * @return Parameter list page.
     */
    @RequestMapping(value = "/Widget/Parameters/Filter", method = RequestMethod.GET)
    public String filterParameterView(ModelMap map, @RequestParam("source") Long id, @RequestParam("filter") String filter)
    {
        if (id == null)
        {
            return null;
        }
        
        File file = fileSystemManager.getSystemFileById(id);
        
        List<Map<Integer, Object>> rows;

        Map<Integer, String> columnNames = new LinkedHashMap<>();
        
        try
        {    
            rows = sqliteManager.generateRows(file, (filter.isEmpty() ? null : new ParameterFilter(filter)), null, columnNames);
            
            for (int i : columnNames.keySet())
            {
                if (columnNames.get(i).startsWith("Robust"))
                {
                    String[] robustProp = columnNames.get(i).split("_");
                    EstherFile prop = fileSystemManager.getFileById(Long.parseLong(robustProp[1]));
                    columnNames.put(i, ("Robustness: " + prop.getName()));
                    break;
                }
            }
        }
        catch (SQLiteException e)
        {
            logger.log(Level.SEVERE, ("Error reading parameter database ID: " + id), e);
            
            return null;
        }
    
        map.addAttribute("column_names", columnNames);
        map.addAttribute("rows", rows);
        
        return "widget/parameterView/list";
    }
    
}
