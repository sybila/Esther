package mu.fi.sybila.esther.parsybonewidget.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.sql.DataSource;
import mu.fi.sybila.esther.heart.controllers.FileSystemController;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for Parsybone execution.
 * 
 * @author George Kolcak
 */
@Controller
public class ParsyboneController
{
    
    private FileSystemManager fileSystemManager = new FileSystemManager();
    private TaskManager taskManager = new TaskManager();
    private UserManager userManager = new UserManager();
    
    @Value("${allowed_parallel_tasks}")
    private Integer maxTasks;
    
    @Value("${parsybone_location}")
    private String parsyboneLocation;
    private static final Logger logger = Logger.getLogger(ParsyboneController.class.getName());
    
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
        taskManager.setLogger(fs);
        userManager.setLogger(fs);
    }
    
    @Autowired
    private FileSystemController fileSystemController;
    
    /**
     * Handler method for Parsybone execution.
     * 
     * @param id The ID of the model file.
     * @return The ID of the started Task.
     *         Error message if starting Parsybone fails.
     *         Limit reached message if the maximal number of active tasks has been reached.
     */
    @RequestMapping(value = "/Widget/Parsybone", method = RequestMethod.POST)
    @ResponseBody
    public String runParsybone(@RequestParam("model") Long model_id, @RequestParam("property") Long property_id,
        @RequestParam(value="compute_robustness", required=false) Boolean robustness,
        @RequestParam(value="compute_witnesses", required=false) Boolean witnesses)
    {
        if ((model_id == null) || (property_id == null))
        {
            return "ERROR=Invalid data specified.";
        }
        
        EstherFile model = fileSystemManager.getFileById(model_id);
        EstherFile property = fileSystemManager.getFileById(property_id);
        
        if (model == null)
        {
            return "ERROR=Model file not found.";
        }
        
        if (property == null)
        {
            return "ERROR=Property file not found.";
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            String userName = authentication.getName();
        
            User user = userManager.getUserByUsername(userName);
            
            if (taskManager.getActiveTaskCount(user) >= maxTasks)
            {
                return "LIMIT_REACHED=" + maxTasks;
            }

            Long resultID = Long.parseLong(fileSystemController.createFile(property.getName(),
                "sqlite", property.getId(), true));

            File result = fileSystemManager.getSystemFileById(resultID);

            Task task = Task.newTask(user.getId(), model.getId(), property.getId(), resultID, "parsybone");

            List<String> taskArgs = new ArrayList<>();
            
            taskArgs.add(parsyboneLocation);
            taskArgs.add(fileSystemManager.getSystemFileById(model.getId()).getAbsolutePath());
            taskArgs.add(fileSystemManager.getSystemFileById(property.getId()).getAbsolutePath());
            taskArgs.add("-v");
            taskArgs.add("--data");
            taskArgs.add(result.getAbsolutePath());
            
            if ((robustness != null) && robustness.booleanValue())
            {
                taskArgs.add("-r");
            }
            
            if ((witnesses != null) && witnesses.booleanValue())
            {
                taskArgs.add("-W");
            }
            
            Long taskId = taskManager.createTask(task, taskArgs.toArray(new String[] { }));

            return taskId.toString();
        }
        
        return "ERROR=You are not logged in.";
    }
    
}
