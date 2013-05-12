package mu.fi.sybila.esther.parameterviewwidget.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.sql.DataSource;
import mu.fi.sybila.esther.heart.database.FileSystemManager;
import mu.fi.sybila.esther.sqlitemanager.SQLiteException;
import mu.fi.sybila.esther.sqlitemanager.SQLiteManager;
import mu.fi.sybila.esther.sqlitemanager.parameterfilter.ParameterFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ParameterFilterController
{
    
    private FileSystemManager fileSystemManager = new FileSystemManager();
    private SQLiteManager sqliteManager = new SQLiteManager();
    public static final Logger logger = Logger.getLogger(ParameterFilterController.class.getName());
    
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
    
    @RequestMapping(value = "/Widget/Parameters/Filter", method = RequestMethod.GET)
    public String filterParameterView(ModelMap model, @RequestParam("source") Long id, @RequestParam("filter") String filter)
    {
        if (id == null)
        {
            return null;
        }
        
        File file = fileSystemManager.getSystemFileById(id);
        
        List<Map<String, Object>> rows;
        
        try
        {
            rows = sqliteManager.generateRows(file, (filter.isEmpty() ? null : new ParameterFilter(filter)), null);
        }
        catch (SQLiteException e)
        {
            logger.log(Level.SEVERE, ("Error reading parameter database ID: " + id), e);
            
            return null;
        }
    
        model.addAttribute("rows", rows);
        
        return "widget/parameterView/list";
    }
    
}
