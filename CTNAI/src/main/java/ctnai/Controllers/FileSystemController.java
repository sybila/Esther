package ctnai.Controllers;

import ctnai.Database.CTNAIFile;
import ctnai.Database.FileSystemManager;
import ctnai.Database.User;
import ctnai.Database.UserManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
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
    
    @RequestMapping(value = "/Files/Root", method = RequestMethod.GET)
    public String fileSystemRoot()
    {
        return "fileSystemRoot";
    }
    
    @RequestMapping(value = "/Files/My", method = RequestMethod.GET)
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
    
    @RequestMapping(value = "/Files/Public", method = RequestMethod.GET)
    public String publicFiles(ModelMap model)
    {
        List<CTNAIFile> files = fileSystemManager.getPublicRootFiles();
        
        model.addAttribute("privacy", "public");
        model.addAttribute("files", files);
        
        return "fileList";
    }
    
    @RequestMapping(value = "/Files/Sub", method = RequestMethod.GET)
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
                        files = fileSystemManager
                            .getSubfilesOwnedBy(fileSystemManager.getFileById(parentId),
                                getUserId(username));
                        break;
                    }
                case "public":
                    {
                        files = fileSystemManager
                            .getPublicSubfiles(fileSystemManager.getFileById(parentId));
                        break;
                    }
            }
        }
        
        model.addAttribute("privacy", privacy);
        model.addAttribute("files", files);
        
        return "fileList";
    }
    
    @RequestMapping(value = "/File/Menu", method = RequestMethod.GET)
    public String getFileMenu(ModelMap model, @RequestParam("file") Long id)
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
        
        Map<String, String> links = new HashMap<>();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
        {
            return null;
        }
        String username = authentication.getName();
        
        if (getUserId(username).equals(file.getOwner()))
        {
            links.put("delete", "Delete");
            links.put("rename", "Rename");
            
            if (file.getPublished())
            {
                links.put("privatize", "Make Private");
            }
            else
            {
                links.put("publish", "Make Public");
            }
        }
        
        links.put("copy", "Copy");
        
        model.addAttribute("links", links);
        
        return "fileMenu";
    }
    
    @RequestMapping(value = "/File/Create", method = RequestMethod.POST)
    @ResponseBody
    public String createFile(@RequestParam("name") String name, @RequestParam("type") String type,
        @RequestParam(value="parents[]", required = false) Long[] parents)
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
        
        CTNAIFile file = CTNAIFile.newFile(name, type, getUserId(username), false, new Long(0));
        
        Long id = fileSystemManager.createFile(file);
        
        if (parents != null)
        {
            for (Long parent : parents)
            {
                fileSystemManager.setParent(fileSystemManager.getFileById(id),
                    fileSystemManager.getFileById(parent));
            }
        }
        
        return id.toString();
    }
    
    @RequestMapping(value = "/File/Delete", method = RequestMethod.POST)
    @ResponseBody
    public void deleteFile(@RequestParam("file") Long id)
    {
        if (id == null)
        {
            return;
        }
        
        CTNAIFile file = fileSystemManager.getFileById(id);
        
        fileSystemManager.deleteFile(file);
    }
    
    @RequestMapping(value = "/File/Rename", method = RequestMethod.POST)
    @ResponseBody
    public void renameFile(@RequestParam("file") Long id, @RequestParam("name") String name)
    {
        if (id == null)
        {
            return;
        }
        
        if ((name == null) || name.isEmpty())
        {
            return;
        }
        
        CTNAIFile file = fileSystemManager.getFileById(id);
        
        file.setName(name);
        
        fileSystemManager.updateFile(file);
    }
    
    @RequestMapping(value = "/File/Publish", method = RequestMethod.POST)
    @ResponseBody
    public void publishFile(@RequestParam("file") Long id)
    {
        if (id == null)
        {
            return;
        }
        
        CTNAIFile file = fileSystemManager.getFileById(id);
        
        file.setPublished(true);
        
        fileSystemManager.updateFile(file);
    }
    
    @RequestMapping(value = "/File/Privatize", method = RequestMethod.POST)
    @ResponseBody
    public void privatizeFile(@RequestParam("file") Long id)
    {
        if (id == null)
        {
            return;
        }
        
        CTNAIFile file = fileSystemManager.getFileById(id);
        
        file.setPublished(false);
        
        fileSystemManager.updateFile(file);
    }
    
    @RequestMapping(value = "/File/Download", method = RequestMethod.POST)
    @ResponseBody
    public FileSystemResource downloadFile(@RequestParam("file") Long id)
    {
        if (id == null)
        {
            return null;
        }
        
        File file = fileSystemManager.getSystemFileById(id);
        
        if (file == null)
        {
            return null;
        }
        
        return new FileSystemResource(file);
    }
    
    @RequestMapping(value = "/File/Copy", method = RequestMethod.POST)
    @ResponseBody
    public String copyFile(@RequestParam("file") Long id, @RequestParam("name") String name)
    {
        if (id == null)
        {
            return null;
        }
        
        if ((name == null) || name.isEmpty())
        {
            return null;
        }
        
        CTNAIFile file = fileSystemManager.getFileById(id);
        List<CTNAIFile> parents = fileSystemManager.getFileParents(file);
        
        Long[] parentIds = new Long[parents.size()];
        for (int i = 0; i < parents.size(); i++)
        {
            parentIds[i] = parents.get(i).getId();
        }
        
        Long newId = Long.parseLong(createFile(name, file.getType(), parentIds));
        
        CTNAIFile newFile = fileSystemManager.getFileById(newId);
        newFile.setSize(file.getSize());
        fileSystemManager.updateFile(newFile);
        
        for (CTNAIFile child : fileSystemManager.getAllSubfiles(file))
        {
            fileSystemManager.setParent(child, newFile);
        }
        
        File sourceFile = fileSystemManager.getSystemFileById(file.getId());
        File destinationFile = fileSystemManager.getSystemFileById(newFile.getId());
        
        FileChannel source = null;
        FileChannel destination = null;

        try
        {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destinationFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, "Error copying file ID: " + id, e);
        }
        finally
        {
            try
            {
                if(source != null)
                {
                    source.close();
                }
                if(destination != null)
                {
                    destination.close();
                }
            }
            catch (IOException e)
            {
                logger.log(Level.SEVERE, "Error copying file ID: " + id, e);
            }
        }
        
        return newId.toString();
    }
    
    @RequestMapping(value = "/File/Read", method = RequestMethod.GET)
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
    
    @RequestMapping(value = "/File/Write", method = RequestMethod.POST)
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
        
        CTNAIFile ctnaiFile = fileSystemManager.getFileById(id);
        ctnaiFile.setSize(file.getTotalSpace());
        fileSystemManager.updateFile(ctnaiFile);
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
