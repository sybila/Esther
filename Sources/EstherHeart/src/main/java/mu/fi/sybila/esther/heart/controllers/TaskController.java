package mu.fi.sybila.esther.heart.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.sql.DataSource;
import mu.fi.sybila.esther.heart.database.FileSystemManager;
import mu.fi.sybila.esther.heart.database.TaskManager;
import mu.fi.sybila.esther.heart.database.UserManager;
import mu.fi.sybila.esther.heart.database.entities.EstherFile;
import mu.fi.sybila.esther.heart.database.entities.Task;
import mu.fi.sybila.esther.heart.database.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for long-term task manipulation.
 * 
 * @author George Kolcak
 */
@Controller
public class TaskController
{
    
    private FileSystemManager fileSystemManager = new FileSystemManager();
    private TaskManager taskManager = new TaskManager();
    private UserManager userManager = new UserManager();
    private static final Logger logger = Logger.getLogger(TaskController.class.getName());
    
    @Value("${allowed_storage_space}")
    private Long maxStorageSpace;
    private static final Long gigabyte = 1073741824l;
    
    /**
     * Method for setting up the controller.
     * 
     * @param dataSource The DataSource used for database connection.
     */
    @Resource
    public void setDataSource(DataSource dataSource)
    {
        fileSystemManager.setDataSource(dataSource);
        taskManager.setDataSource(dataSource);
        userManager.setDataSource(dataSource);
    }
    
    /**
     * Method for setting up the controller.
     * 
     * @param dataLocation The Location of the data in the local file system.
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
        taskManager.setLogger(fs);
        userManager.setLogger(fs);
    }
    
    @Autowired
    FileSystemController fileSystemController;
    
    /**
     * Handler method for Task listing.
     * 
     * @param map The map of UI properties.
     * @return Task list page.
     */
    @RequestMapping(value = "/Tasks", method = RequestMethod.GET)
    public String getTasks(ModelMap map)
    {
        List<Task> tasks = new ArrayList<>();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            String userName = authentication.getName();
        
            User user = userManager.getUserByUsername(userName);
            
            tasks = taskManager.getTasksRunBy(user);
            
            for (Task task : tasks)
            {
                EstherFile model = fileSystemManager.getFileById(task.getModel());
                EstherFile property = fileSystemManager.getFileById(task.getProperty());
                
                task.setModelName(model.getName() + "." + model.getType());
                task.setPropertyName(property.getName() + "." + property.getType());
                
                if (task.getDatabases().size() > 0)
                {
                    List<String> databaseNames = new ArrayList<>();
                    for (Long id : task.getDatabases())
                    {
                        EstherFile db = fileSystemManager.getFileById(id);

                        databaseNames.add(db.getName() + "." + db.getType());
                    }

                    task.setDatabaseNames(databaseNames);
                }
                
                if (task.getFilters().size() > 0)
                {
                    List<String> filterNames = new ArrayList<>();
                    for (Long id : task.getFilters())
                    {
                        EstherFile filter = fileSystemManager.getFileById(id);

                        filterNames.add(filter.getName() + "." + filter.getType());
                    }

                    task.setFilterNames(filterNames);
                }
            }
            
//            for (int i = 0; i < tasks.size(); i++)
//            {
//                try
//                {
//                    tasks.get(i).getInformation();
//                    tasks.get(i).getError();
//
//                    taskManager.updateTask(tasks.get(i));
//                }
//                catch (IOException e)
//                {
//                    logger.log(Level.SEVERE, ("Error refreshing Task information."), e);
//                }
//            }
        }
        
        ArrayList<Task> orderedTasks = new ArrayList<>();
        
        for (int i = (tasks.size() - 1); i >= 0; i--)
        {
            orderedTasks.add(tasks.get(i));
        }
        
        map.addAttribute("tasks", orderedTasks);
        
        return "tasks/list";
    }
    
    /**
     * Handler method for task cancellation.
     * 
     * @param id The ID of the task to be cancelled.
     */
    @RequestMapping(value = "/Task/Cancel", method = RequestMethod.POST)
    @ResponseBody
    public void cancelTask(@RequestParam("task") Long id)
    {
        if (id == null)
        {
            return;
        }
        
        Task task = taskManager.getTask(id);
        
        if (task == null)
        {
            return;
        }
        
        EstherFile result = fileSystemManager.getFileById(task.getResult());
        
        try
        {
            if (task.getActive() || (task.getError() != null))
            {
                //task.cancel();
                
                fileSystemManager.deleteFile(result);
            }
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, ("Error cleaning up after Task ID: " + task.getId()), e);
        }
        
        taskManager.removeTask(task);
    }
    
    /**
     * Handler method for task output saving.
     * 
     * @param id The ID of the task to be finished.
     * @return The name of the output file.
     *         Error if saving of the task output fails.
     *         Limit reached message if the output file exceeds the allowed storage space.
     */
    @RequestMapping(value = "/Task/Save", method = RequestMethod.POST)
    @ResponseBody
    public String finishTask(@RequestParam("task") Long id)
    {
        if (id == null)
        {
            return "ERROR=Invalid data specified.";
        }
        
        Task task = taskManager.getTask(id);
        
        if (task == null)
        {
            return "ERROR=The desired Task no longer exists.";
        }
        
        try
        {
            if (task.getFinished() && (task.getError() == null))
            {
                task.setActive(false);
                taskManager.updateTask(task);
                
                EstherFile result = fileSystemManager.getFileById(task.getResult());
                File resultFile = fileSystemManager.getSystemFileById(result.getId());
                
                result.setSize(resultFile.length());
                result.setBlocked(false);
                
                fileSystemManager.updateFile(result);
                
                if (fileSystemController.exceedsAllowedSpace())
                {
                    return "LIMIT_REACHED=" + (maxStorageSpace / gigabyte) + "GB";
                }
                
                return (result.getName() + "." + result.getType() + "=" + fileSystemManager.getParent(result).getId());
            }
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, ("Error unblocking result of Task ID: " + task.getId()), e);
        }
        
        return "ERROR=Failed to retrieve the task result.";
    }
    
}
