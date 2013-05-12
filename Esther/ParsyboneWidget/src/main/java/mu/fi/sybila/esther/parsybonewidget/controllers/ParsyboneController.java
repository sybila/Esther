package mu.fi.sybila.esther.parsybonewidget.controllers;

import java.io.File;
import java.io.FileOutputStream;
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
    private FileSystemController fileSystemController;
    
    @RequestMapping(value = "/Widget/Parsybone", method = RequestMethod.POST)
    @ResponseBody
    public String runParsybone(@RequestParam("file") Long id)
    {
        if (id == null)
        {
            return "ERROR=Invalid data specified.";
        }
        
        EstherFile file = fileSystemManager.getFileById(id);
        
        if (file == null)
        {
            return "ERROR=File not found.";
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

            Long resultID = Long.parseLong(fileSystemController.createFile(file.getName(),
                "sqlite", new Long[] { file.getId() }, true));

            File result = fileSystemManager.getSystemFileById(resultID);

            Task task = Task.newTask(user.getId(), file.getId(), resultID, "Parsybone");

            Long taskId = taskManager.createTask(task, new String[] { parsyboneLocation,
                    fileSystemManager.getSystemFileById(file.getId()).getAbsolutePath(),
                    "-vrW", "--data", result.getAbsolutePath() });

            return taskId.toString();
        }
        
        return "ERROR=You are not logged in.";
    }
    
}
