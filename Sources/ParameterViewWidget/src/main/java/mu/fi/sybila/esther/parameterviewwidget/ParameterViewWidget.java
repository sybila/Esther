package mu.fi.sybila.esther.parameterviewwidget;

import java.io.File;
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
import mu.fi.sybila.esther.heart.database.UserManager;
import mu.fi.sybila.esther.heart.database.entities.EstherFile;
import mu.fi.sybila.esther.heart.database.entities.User;
import mu.fi.sybila.esther.heart.widget.EstherWidget;
import mu.fi.sybila.esther.sqlitemanager.SQLiteException;
import mu.fi.sybila.esther.sqlitemanager.SQLiteManager;
import mu.fi.sybila.esther.sqlitemanager.parameterfilter.ParameterFilter;
import mu.fi.sybila.esther.sqlitemanager.parameterfilter.ParameterFilterException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;

/**
 * The Parameter View Widget class.
 * 
 * @author George Kolcak
 */
public class ParameterViewWidget implements EstherWidget
{
    
    private FileSystemManager fileSystemManager = new FileSystemManager();
    private UserManager userManager = new UserManager();
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
        userManager.setDataSource(dataSource);
    }
    
    public void setLogger(FileOutputStream fs)
    {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
        fileSystemManager.setLogger(fs);
        userManager.setLogger(fs);
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated() || authentication.getName().equals("anonymousUser"))
        {
            return "redirect:/login";
        }
        
        Long user = getUserId(authentication.getName());
            
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
            
            File source = fileSystemManager.getSystemFileById(fileId);
            List<Map<Integer, Object>> rows = sqliteManager.generateRows(source, filter, contextMasks, columnNames);
            
            for (int i : columnNames.keySet())
            {
                if (columnNames.get(i).startsWith("Robust"))
                {
                    String[] robustProp = columnNames.get(i).split("_");
                    Long propId;
                    
                    try
                    {
                        propId = Long.parseLong(robustProp[1]);
                    }
                    catch (NumberFormatException e)
                    {
                        propId = null;
                    }
                    
                    EstherFile prop = fileSystemManager.getParent(fileSystemManager.getFileById(fileId));
                    
                    if ((propId == null) || (propId != prop.getId()))
                    {
                        sqliteManager.refactorTable(source, prop.getId());
                    }
                    
                    columnNames.put(i, ("Robustness: " + prop.getName()));
                    break;
                }
            }
            
            Map<String, Map<Integer, String>> columnDisplayOptions = GenerateColumnDisplayOptions(columnNames);
            
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

            if (fileSystemManager.getFileById(fileId).getOwner() != user)
            {
                map.addAttribute("readonly", true);
            }
            
            map.addAttribute("context_masks", contextMasks);
            map.addAttribute("column_names", columnNames);
            map.addAttribute("display_settings", columnDisplayOptions);
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
    
    /**
     * Returns ID of the user with the specified username.
     * 
     * @param username The username associated with the coveted ID.
     * @return The ID of the user. Null if no such user exists.
     */
    private Long getUserId(String username)
    {
        User user = userManager.getUserByUsername(username);
        if ((user != null) && (user.getId() != null))
        {
            return user.getId();
        }
        
        return null;
    }
    
    private Map<String, Map<Integer, String>> GenerateColumnDisplayOptions(Map<Integer, String> columnNames)
    {
        Map<String, Map<Integer, String>> columnDisplayOptions = new LinkedHashMap<>();
        
        for (int i : columnNames.keySet())
        {
            String specieName = null;
            
            if (!columnNames.get(i).startsWith("Cost") && !columnNames.get(i).startsWith("Robustness"))
            {
                specieName = columnNames.get(i).split("\\{")[0];
            }
            
            if (!columnDisplayOptions.containsKey(specieName))
            {
                columnDisplayOptions.put(specieName, new LinkedHashMap<Integer, String>());
            }
            
            columnDisplayOptions.get(specieName).put(i, columnNames.get(i));
        }
        
        return columnDisplayOptions;
    }
}
