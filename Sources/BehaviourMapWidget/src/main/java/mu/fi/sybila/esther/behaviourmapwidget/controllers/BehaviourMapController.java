package mu.fi.sybila.esther.behaviourmapwidget.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import mu.fi.sybila.esther.behaviourmapwidget.behaviourmapper.BehaviourMapper;
import mu.fi.sybila.esther.heart.controllers.FileSystemController;
import mu.fi.sybila.esther.heart.database.FileSystemManager;
import mu.fi.sybila.esther.heart.database.UserManager;
import mu.fi.sybila.esther.heart.database.entities.EstherFile;
import mu.fi.sybila.esther.heart.database.entities.User;
import mu.fi.sybila.esther.sqlitemanager.SQLiteManager;
import mu.fi.sybila.esther.sqlitemanager.parameterfilter.ParameterFilter;
import mu.fi.sybila.esther.sqlitemanager.parameterfilter.ParameterFilterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for behaviour mapping.
 * 
 * @author George Kolcak
 */
@Controller
public class BehaviourMapController
{
    private FileSystemManager fileSystemManager = new FileSystemManager();
    private UserManager userManager = new UserManager();
    private SQLiteManager sqliteManager = new SQLiteManager();
    public static final Logger logger = Logger.getLogger(BehaviourMapController.class.getName());
    
    @Value("${allowed_storage_space}")
    private Long maxStorageSpace;
    private static final Long gigabyte = 1073741824l;
    
    @Autowired
    private FileSystemController fileSystemController;
    
    /**
     * Method for setting up the controller.
     * 
     * @param dataSource The DataSource used for database connection.
     */
    @Resource
    public void setDataSource(DataSource dataSource)
    {
        fileSystemManager.setDataSource(dataSource);
        userManager.setDataSource(dataSource);
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
        userManager.setLogger(fs);
    }
    
    /**
     * Handler method for behaviour map creation.
     * 
     * @param sourceId The ID of the parameter set files to use as an input.
     * @param filterId The ID of the filter file to restrain the output with.
     * @return The ID of the created behaviour map file.
     *         Error message if behaviour map creation fails.
     *         Limit reached message if the behaviour map exceeds the allowed storage space.
     */
    @RequestMapping(value = "Widget/BehaviourMap", method = RequestMethod.POST)
    @ResponseBody
    public String generateBehaviourMap(@RequestParam("file") Long sourceId,
        @RequestParam(value = "filter", required = false) Long filterId)
    {
        if (sourceId == null)
        {
            return "ERROR=Invalid data specified.";
        }
        
        ParameterFilter filter = null;
        
        EstherFile source = fileSystemManager.getFileById(sourceId);
        File file = fileSystemManager.getSystemFileById(sourceId);
        
        EstherFile parentFile = fileSystemManager.getParent(source);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        User user = userManager.getUserByUsername(authentication.getName());
        
        if (!authentication.isAuthenticated() || (user == null))
        {
            return "ERROR=You are not loggen in.";
        }
        
        if (user.getId() != source.getOwner())
        {
            return "ERROR=Cannot create Behaviour Map from public file you do not own.";
        }
        
        Long targetId;
        
        try
        {
            if (filterId != null)
            {
                EstherFile filterFile = fileSystemManager.getFileById(filterId);
                
                if (user.getId() != filterFile.getOwner())
                {
                    return "ERROR=You cannot use public filter you do not own to create Behaviour Map.";
                }

                filter = new ParameterFilter(fileSystemManager.getSystemFileById(filterFile.getId()));

                targetId = Long.parseLong(fileSystemController.createFile(filterFile.getName(), "xgmml", filterId, false));
            }
            else
            {
                targetId = Long.parseLong(fileSystemController.createFile(source.getName(), "xgmml", source.getId(), false));
            }
        }
        catch (ParameterFilterException e)
        {
            logger.log(Level.SEVERE, ("Error reading filter ID: " + filterId), e);
            
            return "ERROR=Failed to read filter.";
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try
        {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            statement = sqliteManager.constructSQLQuery(filter, connection, false);
            resultSet = statement.executeQuery();
            
            File newFile = fileSystemManager.getSystemFileById(targetId);
            
            BehaviourMapper.behaviourMap(resultSet, parentFile.getId().toString(), newFile.getAbsolutePath());
            
            EstherFile bmFile = fileSystemManager.getFileById(targetId);
            bmFile.setSize(newFile.length());
            fileSystemManager.updateFile(bmFile);
            
            if (fileSystemController.exceedsAllowedSpace())
            {
                return ("LIMIT_REACHED=" + (maxStorageSpace / gigabyte) + "GB=" + bmFile.getId());
            }
            
            return (bmFile.getId().toString() + "=" + bmFile.getName());
        }
        catch (ClassNotFoundException | ParserConfigurationException | SQLException | TransformerException e)
        {
            logger.log(Level.SEVERE, ("Error building behaviour map for file ID: " + sourceId), e);
            
            return "ERROR=Failed to create behaviour map.";
        }
        finally
        {
            try
            {
                if (resultSet != null)
                {
                    resultSet.close();
                }
                if (statement != null)
                {
                    statement.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            }
            catch (SQLException e)
            {
                logger.log(Level.SEVERE, ("Error closing connection to parameter database ID: " + sourceId), e);
            }
        }
    }
}
