package ctnai.Controllers;

import ctnai.Database.CTNAIFile;
import ctnai.Database.FileSystemManager;
import ctnai.Database.UserManager;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ParsyboneController
{
    private FileSystemManager fileSystemManager = new FileSystemManager();
    private UserManager userManager = new UserManager();
    
    @Value("${parsybone_location}")
    private String parsyboneLocation;
    public static final Logger logger = Logger.getLogger(ParsyboneController.class.getName());
    
    @Resource
    public void setDataSource(DataSource dataSource)
    {
        fileSystemManager.setDataSource(dataSource);
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
        userManager.setLogger(fs);
    }
    
    @Autowired
    private FileSystemController fileSystemController;
    
    @RequestMapping(value = "/Parsybone", method = RequestMethod.POST)
    @ResponseBody
    public String runParsybone(@RequestParam("file") Long id)
    {
        CTNAIFile file = fileSystemManager.getFileById(id);
        
        if (file == null)
        {
            return null;
        }
        
        Long target_id = fileSystemController.createFile(file.getName(), "sqlite", file.getId());
        
        StringBuilder outputBuilder = new StringBuilder();
        
        try
        {
            String[] cmdArray = new String[]
            {
                parsyboneLocation,
                ("\"" + fileSystemManager.getSystemFileById(file.getId()).getAbsolutePath() + "\""),
                "-rW",
                ("--data \"" + fileSystemManager.getSystemFileById(target_id).getAbsolutePath() + "\"")
            };
            
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec(cmdArray);

            int i;
            while ((i = proc.getInputStream().read()) >= 0)
            {
                outputBuilder.append((char)i);
            }
            
            while ((i = proc.getErrorStream().read()) >= 0)
            {
                outputBuilder.append((char)i);
            }
            
            int exitValue = proc.waitFor();
            
            proc.destroy();
            
            logger.log(Level.INFO, ("Parsybone exited with value: " + exitValue));
        }
        catch (InterruptedException | IOException e)
        {
            logger.log(Level.SEVERE, "Error executing Parsybone binary.", e);
        }
        
        return outputBuilder.toString();
    }
}