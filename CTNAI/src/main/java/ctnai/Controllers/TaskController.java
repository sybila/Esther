package ctnai.Controllers;

import ctnai.Database.CTNAIFile;
import ctnai.Database.FileSystemManager;
import ctnai.Database.Task;
import ctnai.Database.TaskManager;
import ctnai.Database.User;
import ctnai.Database.UserManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.sql.DataSource;
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

@Controller
public class TaskController
{
    private FileSystemManager fileSystemManager = new FileSystemManager();
    private TaskManager taskManager = new TaskManager();
    private UserManager userManager = new UserManager();
    private static final Logger logger = Logger.getLogger(ParsyboneController.class.getName());
    
    @Resource
    public void setDataSource(DataSource dataSource)
    {
        fileSystemManager.setDataSource(dataSource);
        taskManager.setDataSource(dataSource);
        userManager.setDataSource(dataSource);
    }
    
    @Value("${data_location}")
    public void setDataLocation(String dataLocation)
    {
        fileSystemManager.setDataLocation(dataLocation);
    }
    
    public void setLogger(FileOutputStream fs)
    {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
        fileSystemManager.setLogger(fs);
        taskManager.setLogger(fs);
        userManager.setLogger(fs);
    }
    
    @Autowired
    FileSystemController fileSystemController;
    
    @RequestMapping(value = "/Tasks", method = RequestMethod.GET)
    public String getTasks(ModelMap map)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
        {
            return null;
        }
        String userName = authentication.getName();
        
        User user = userManager.getUserByUsername(userName);
        
        List<Task> tasks = taskManager.getTasksRunBy(user);
        
        for (int i = 0; i < tasks.size(); i++)
        {
            try
            {
                tasks.get(i).getInformation();
                tasks.get(i).getError();
                
                taskManager.updateTask(tasks.get(i));
            }
            catch (IOException e)
            {
                logger.log(Level.SEVERE, ("Error refreshing Task information."), e);
            }
        }
        
        map.addAttribute("tasks", tasks);
        
        return "taskList";
    }
    
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
        
        CTNAIFile result = fileSystemManager.getFileById(task.getResult());
        
        try
        {
            if (task.getActive() || (task.getError() != null))
            {
                task.cancel();
                
                fileSystemManager.deleteFile(result);
            }
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, ("Error cleaning up after Task ID: " + task.getId()), e);
        }
        
        taskManager.removeTask(task);
    }
    
    @RequestMapping(value = "/Task/Save", method = RequestMethod.POST)
    @ResponseBody
    public String finishTask(@RequestParam("task") Long id)
    {
        if (id == null)
        {
            return null;
        }
        
        Task task = taskManager.getTask(id);
        
        if (task == null)
        {
            return null;
        }
        
        try
        {
            if (task.getFinished() && (task.getError() == null))
            {
                task.setActive(false);
                taskManager.updateTask(task);
                
                CTNAIFile result = fileSystemManager.getFileById(task.getResult());
                File resultFile = fileSystemManager.getSystemFileById(result.getId());
                
                if (fileSystemController.exceedsAllowedSpace())
                {
                    return "LIMIT_REACHED=5Gb";
                }
                
                result.setSize(resultFile.getTotalSpace());
                result.setBlocked(false);
                
                fileSystemManager.updateFile(result);
                
                return (result.getName() + "." + result.getType());
            }
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, ("Error unblocking result of Task ID: " + task.getId()), e);
        }
        
        return null;
    }
}
