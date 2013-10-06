package mu.fi.sybila.esther.parameterviewwidget;

import java.io.FileOutputStream;
import java.util.HashMap;
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
import mu.fi.sybila.esther.heart.widget.EstherWidget;
import mu.fi.sybila.esther.sqlitemanager.SQLiteException;
import mu.fi.sybila.esther.sqlitemanager.SQLiteManager;
import mu.fi.sybila.esther.sqlitemanager.parameterfilter.ParameterFilter;
import mu.fi.sybila.esther.sqlitemanager.parameterfilter.ParameterFilterException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.ModelMap;

/**
 * The Parameter View Widget class.
 * 
 * @author George Kolcak
 */
public class ParameterViewWidget implements EstherWidget
{
    
    private FileSystemManager fileSystemManager = new FileSystemManager();
    private SQLiteManager sqliteManager = new SQLiteManager();
    public static final Logger logger = Logger.getLogger(ParameterViewWidget.class.getName());

    @Override
    public String[] allowedChildren(String type) 
    {
        if (!opensFile(type))
        {
            return new String[] { };
        }
        
        if (type.equals("sqlite"))
        {
            return new String[] { "filter", "xgmml" };
        }
        else
        {
            return new String[] { "xgmml" };
        }
    }
    
    private boolean behaviourMapperPresent;

    public ParameterViewWidget()
    {
        behaviourMapperPresent = false;
    }
    
    @Value("${data_location}")
    public void setDataLocation(String dataLocation)
    {
        fileSystemManager.setDataLocation(dataLocation);
    }
    
    @Resource
    public void setDataSource(DataSource dataSource)
    {
        fileSystemManager.setDataSource(dataSource);
    }
    
    public void setLogger(FileOutputStream fs)
    {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
        fileSystemManager.setLogger(fs);
    }
    
    @Override
    public String getIdentifier()
    {
        return "Parameter View Widget";
    }

    @Override
    public void checkDependencies(List<EstherWidget> widgets)
    {
        for (EstherWidget widget : widgets)
        {
            if (widget.getIdentifier().equals("Behaviour Map Widget"))
            {
                behaviourMapperPresent = true;
            }
        }
    }

    @Override
    public boolean opensFile(String type)
    {
        if ((type != null) && (type.equals("sqlite") || type.equals("filter")))
        {
            return true;
        }
        
        return false;
    }
    
    @Override
    public String[] globalJavascripts()
    {
        return new String[] { "parameterView" };
    }

    @Override
    public String startWidget(ModelMap map, Long id, Long parent)
    {
        if (id == null)
        {
            return "widget/fileBroken";
        }
        
        if (behaviourMapperPresent)
        {
            map.addAttribute("behaviourMapper", "present");
        }
        
        EstherFile file = fileSystemManager.getFileById(id);
        
        if (file == null)
        {
            return "widget/fileBroken";
        }
        
        Long fileId = ((file.getType().equals("sqlite")) ? file.getId() : parent);
        Long filterId = ((file.getType().equals("sqlite")) ? null : file.getId());
        
        map.addAttribute("file", fileId);
        
        Map<String, String> contextMasks = new HashMap<>();
        
        ParameterFilter filter = null;
        
        Map<Integer, Object[]> filterProperties = new HashMap<>();
        
        try
        {
            if (filterId != null)
            {
                filter = new ParameterFilter(fileSystemManager.getSystemFileById(filterId));
                
                map.addAttribute("filter", filterId);
            }
            
            Map<Integer, String> columnNames = new LinkedHashMap<>();
            
            List<Map<Integer, Object>> rows = sqliteManager.generateRows(fileSystemManager.getSystemFileById(fileId), filter, contextMasks, columnNames);
            
            if (filterId != null)
            {
                for (int i = 1; i <= filter.getFilter().length; i++)
                {
                    String[] constraintProperties = filter.getFilter()[(i - 1)].split(";");
                    Object[] filterProp = new Object[6];

                    filterProp[0] = constraintProperties[0];

                    if (constraintProperties[0].startsWith("K_"))
                    {
                        filterProp[1] = contextMasks.get(constraintProperties[0]);
                    }
                    else
                    {
                        filterProp[1] = constraintProperties[0].substring(0, 1).toUpperCase().concat(constraintProperties[0].substring(1));
                    }
                    filterProp[2] = constraintProperties[1];
                    filterProp[3] = ParameterFilter.translateFilterType(constraintProperties[1]);
                    filterProp[4] = constraintProperties[2];
                    filterProp[5] = ((constraintProperties[0].equals("robustness") ? "%." : "."));

                    filterProperties.put(i, filterProp);
                }

                map.addAttribute("filter_properties", filterProperties);
            }

            map.addAttribute("context_masks", contextMasks);
            map.addAttribute("column_names", columnNames);
            map.addAttribute("rows", rows);

            return "widget/parameterView/view";
        }
        catch (ParameterFilterException e)
        {
            logger.log(Level.SEVERE, ("Error reading filter ID: " + filterId), e);
            
            return "widget/fileBroken";
        }
        catch (SQLiteException e)
        {
            logger.log(Level.SEVERE, ("Error reading parameter database ID: " + fileId), e);
            
            return "widget/fileBroken";
        }
    }
    
}
