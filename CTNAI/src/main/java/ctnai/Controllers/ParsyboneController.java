package ctnai.Controllers;

import ctnai.Database.CTNAIFile;
import ctnai.Database.FileSystemManager;
import ctnai.Database.Task;
import ctnai.Database.TaskManager;
import ctnai.Database.User;
import ctnai.Database.UserManager;
import java.io.File;
import java.io.FileOutputStream;
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
        taskManager.setDataLocation(dataLocation);
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
    
    @RequestMapping(value = "/Parsybone", method = RequestMethod.POST)
    @ResponseBody
    public String runParsybone(@RequestParam("file") Long id)
    {
        if (id == null)
        {
            return null;
        }
        
        CTNAIFile file = fileSystemManager.getFileById(id);
        
        if (file == null)
        {
            return null;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
        {
            return null;
        }
        String userName = authentication.getName();
        
        User user = userManager.getUserByUsername(userName);
        
        if (user == null)
        {
            return null;
        }
        
        if (taskManager.getActiveTaskCount(user) >= 2)
        {
            return "REACHED_LIMIT=2";
        }
        
        Long resultID = Long.parseLong(fileSystemController.createFile(file.getName(),
            "sqlite", new Long[] { file.getId() }, true));
        
        File result = fileSystemManager.getSystemFileById(resultID);
        
        Task task = Task.newTask(user.getId(), file.getId(), resultID, "Parsybone");
        
        Long taskId = taskManager.createTask(task, new String[] { parsyboneLocation,
                fileSystemManager.getSystemFileById(file.getId()).getAbsolutePath(),
                "-vrW", "--data", result.getAbsolutePath() });
        
        return taskId.toString();
        
//        Long targetId = Long.parseLong(fileSystemController.createFile(file.getName(), "sqlite", new Long[] { file.getId() }));
//        
//        StringBuilder outputBuilder = new StringBuilder();
//        
//        try
//        {
//            File newFile = fileSystemManager.getSystemFileById(targetId);
//            
//            StringBuilder commandBuilder = new StringBuilder();
//            
//            commandBuilder.append('"');
//            commandBuilder.append(parsyboneLocation);
//            commandBuilder.append("\" ");
//            commandBuilder.append("-vrW ");
//            commandBuilder.append('"');
//            commandBuilder.append(fileSystemManager.getSystemFileById(file.getId()).getAbsolutePath());
//            commandBuilder.append('"');
//            commandBuilder.append(" --data \"");
//            commandBuilder.append(newFile.getAbsolutePath());
//            commandBuilder.append('"');
//            
//            CommandLine cmdLine = CommandLine.parse(commandBuilder.toString());
//            DefaultExecutor executor = new DefaultExecutor();
//            
//            ProcessBuilder procBuilder = new ProcessBuilder(parsyboneLocation,
//                fileSystemManager.getSystemFileById(file.getId()).getAbsolutePath(),
//                "-vrW", "--data", newFile.getAbsolutePath());
//            
//            procBuilder.redirectOutput(Redirect.PIPE);
//            procBuilder.redirectError(Redirect.PIPE);
//            
//            Process process = procBuilder.start();
//            
//            ExecuteStreamHandler esHandler = new PumpStreamHandler();
//            
//            InputStream parsyboneOutput = new PipedInputStream();
//            
//            esHandler.setProcessOutputStream(parsyboneOutput);
//            
//            executor.setStreamHandler(esHandler);
//            
//            int exitValue = executor.execute(cmdLine);
//            int exitValue = process.waitFor();
//            
//            int c;
//            while ((c = process.getInputStream().read()) != (-1))
//            {
//                outputBuilder.append((char)c);
//            }
//            
//            while ((c = process.getErrorStream().read()) != (-1))
//            {
//                outputBuilder.append((char)c);
//            }
//            
//            char c;
//            while ((c = (char)parsyboneOutput.read()) != (-1))
//            {
//                outputBuilder.append(c);
//            }
//            
//            logger.log(Level.INFO, ("Parsybone exited with value: " + exitValue + "\n" + outputBuilder.toString()));
//            
//            CTNAIFile dbFile = fileSystemManager.getFileById(targetId);
//            dbFile.setSize(newFile.getTotalSpace());
//            fileSystemManager.updateFile(dbFile);
//        }
//        catch (InterruptedException | IOException e)
//        {
//            logger.log(Level.SEVERE, "Error executing Parsybone binary.", e);
//        }
//        
//        return outputBuilder.toString();
    }
}
