package ctnai.Controllers;

import ctnai.Database.CTNAIFile;
import ctnai.Database.FileSystemManager;
import ctnai.Database.User;
import ctnai.Database.UserManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.sql.DataSource;
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
public class FileSystemController
{
    private FileSystemManager fileSystemManager = new FileSystemManager();
    private UserManager userManager = new UserManager();
    public static final Logger logger = Logger.getLogger(FileSystemController.class.getName());
    
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
    
    @RequestMapping(value = "/FileSystemRoot", method = RequestMethod.GET)
    public String fileSystemRoot()
    {
        return "fileSystemRoot";
    }
    
    @RequestMapping(value = "/MyFiles", method = RequestMethod.GET)
    public String myFiles(ModelMap model)
    {
        List<CTNAIFile> files = new ArrayList<>();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            String username = authentication.getName();
            
            files = fileSystemManager.getRootFilesOwnedBy(getUserId(username));
        }
        
        model.addAttribute("privacy", "private");
        model.addAttribute("files", files);
        
        return "fileList";
    }
    
    @RequestMapping(value = "/PublicFiles", method = RequestMethod.GET)
    public String publicFiles(ModelMap model)
    {
        List<CTNAIFile> files = fileSystemManager.getPublicRootFiles();
        
        model.addAttribute("privacy", "public");
        model.addAttribute("files", files);
        
        return "fileList";
    }
    
    @RequestMapping(value = "/Subfiles", method = RequestMethod.GET)
    public String subfiles(ModelMap model, @RequestParam("file") Long parentId,
        @RequestParam("privacy") String privacy)
    {
        List<CTNAIFile> files = new ArrayList<>();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            String username = authentication.getName();
            switch (privacy)
            {
                case "private":
                    {
                        files = fileSystemManager.getSubfilesOwnedBy(parentId, getUserId(username));
                        break;
                    }
                case "public":
                    {
                        files = fileSystemManager.getPublicSubfiles(parentId);
                        break;
                    }
            }
        }
        
        model.addAttribute("privacy", privacy);
        model.addAttribute("files", files);
        
        return "fileList";
    }
    
    @RequestMapping(value = "/CreateFile", method = RequestMethod.POST)
    @ResponseBody
    public Long createFile(@RequestParam("name") String name, @RequestParam("type") String type,
            @RequestParam(value="parent", required = false) Long parent)
    {
        if ((name == null) || (type == null) || name.isEmpty() || type.isEmpty())
        {
            return null;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
        {
            return null;
        }
        String username = authentication.getName();
        
        CTNAIFile file = CTNAIFile.newFile(parent, name, type, getUserId(username), false);
        
        return fileSystemManager.createFile(file);
    }
    
    @RequestMapping(value = "/ReadFile", method = RequestMethod.GET)
    @ResponseBody
    public String readFile(@RequestParam("file") Long id)
    {
        if (id == null)
        {
            return null;
        }
        
        String data = "";
        
        File file = fileSystemManager.getSystemFileById(id);
        
        if (file == null)
        {
            return null;
        }
        
        BufferedReader br = null;
        
        try
        {
            br = new BufferedReader(new FileReader(file));

            StringBuilder dataBuilder = new StringBuilder();
            
            String line;
            while ((line = br.readLine()) != null)
            {
                dataBuilder.append(line);
                dataBuilder.append('\n');
            }
        
            data = dataBuilder.toString();
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, ("Error reading file ID: " + id), e);
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    logger.log(Level.SEVERE, ("Error closing file ID: " + id), e);
                }
            }
        }
        
        return data;
    }
    
    @RequestMapping(value = "/WriteFile", method = RequestMethod.POST)
    @ResponseBody
    public void writeFile(@RequestParam("file") Long id, @RequestParam("data") String data)
    {
        if ((id == null) || (data == null))
        {
            return;
        }
        
        File file = fileSystemManager.getSystemFileById(id);
        
        if (file == null)
        {
            return;
        }
        
        BufferedWriter bw = null;

        try
        {
            bw = new BufferedWriter(new FileWriter(file));

            bw.write(data);
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, ("Error writing file ID: " + id), e);
        }
        finally
        {
            if (bw != null)
            {
                try 
                {
                    bw.close();
                }
                catch (IOException e)
                {
                    logger.log(Level.SEVERE, ("Error closing file ID: " + id), e);
                }
            }
        }
    }
    
    private Long getUserId(String username)
    {
        User user = userManager.getUserByUsername(username);
        if ((user != null) && (user.getId() != null))
        {
            return user.getId();
        }
        
        return null;
    }
}
