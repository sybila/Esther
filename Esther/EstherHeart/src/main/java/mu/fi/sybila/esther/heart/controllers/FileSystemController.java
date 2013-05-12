package mu.fi.sybila.esther.heart.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import mu.fi.sybila.esther.heart.database.FileSystemManager;
import mu.fi.sybila.esther.heart.database.UserManager;
import mu.fi.sybila.esther.heart.database.entities.EstherFile;
import mu.fi.sybila.esther.heart.database.entities.User;
import mu.fi.sybila.esther.heart.database.entities.UserInformation;
import mu.fi.sybila.esther.heart.widget.EstherWidget;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FileSystemController
{
    
    private FileSystemManager fileSystemManager = new FileSystemManager();
    private UserManager userManager = new UserManager();
    public static final Logger logger = Logger.getLogger(FileSystemController.class.getName());
    
    @Value("${allowed_storage_space}")
    private Long maxStorageSpace;
    private static final Long gigabyte = 1073741824l;
    
    @Resource
    private List<EstherWidget> widgetList;
    
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
        return "filesystem/root";
    }
    
    @RequestMapping(value = "/Files/My", method = RequestMethod.GET)
    public String myFiles(ModelMap model)
    {
        List<EstherFile> files = new ArrayList<>();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            String username = authentication.getName();
            
            files = fileSystemManager.getRootFilesOwnedBy(getUserId(username));
        }
        
        model.addAttribute("privacy", "private");
        model.addAttribute("files", files);
        
        return "filesystem/list";
    }
    
    @RequestMapping(value = "/Files/Public", method = RequestMethod.GET)
    public String publicFiles(ModelMap model)
    {
        List<EstherFile> files = new ArrayList<>();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            String username = authentication.getName();
            
            UserInformation information = userManager.getUserInformation(getUserId(username));

            files = fileSystemManager.getPublicRootFiles(information);
        }
        
        model.addAttribute("privacy", "public");
        model.addAttribute("files", files);
        
        return "filesystem/list";
    }
    
    @RequestMapping(value = "/Files/Sub", method = RequestMethod.GET)
    public String subfiles(ModelMap model, @RequestParam("file") Long parentId,
        @RequestParam("privacy") String privacy)
    {
        List<EstherFile> files = new ArrayList<>();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated())
        {
            String username = authentication.getName();
            
            switch (privacy)
            {
                case "private":
                    {
                        files = fileSystemManager.getSubfilesOwnedBy(fileSystemManager.getFileById(parentId), getUserId(username));
                        break;
                    }
                case "public":
                    {
                        UserInformation information = userManager.getUserInformation(getUserId(username));
                        
                        files = fileSystemManager.getPublicSubfiles(fileSystemManager.getFileById(parentId), information);
                        break;
                    }
            }
        }
        
        model.addAttribute("privacy", privacy);
        model.addAttribute("files", files);
        
        return "filesystem/list";
    }
    
    @RequestMapping(value = "/File/Menu", method = RequestMethod.GET)
    public String getFileMenu(ModelMap model, @RequestParam("file") Long id)
    {
        Map<String, String> links = new HashMap<>();
        
        if (id == null)
        {
            links.put("upload", "Upload");
            
            model.addAttribute("ext", "dbm");
        }
        else
        {
            EstherFile file = fileSystemManager.getFileById(id);
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if ((file != null) && authentication.isAuthenticated())
            {
                String username = authentication.getName();

                if (getUserId(username).equals(file.getOwner()))
                {
                    links.put("delete", "Delete");
                    links.put("rename", "Rename");

                    switch(file.getType())
                    {
                        case "dbm":
                        {
                            links.put("upload", "Upload");
                            model.addAttribute("ext", "sqlite");
                            break;
                        }
                        case "sqlite":
                        {

                        }
                        case "filter":
                        {
                            links.put("upload", "Upload");
                            model.addAttribute("ext", "xgmml");
                            break;
                        }
                        default:
                        {
                            break;
                        }
                    }

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
                links.put("download", "Download");
            }
        }
        
        model.addAttribute("links", links);
        
        return "filesystem/menu";
    }
    
    @RequestMapping(value = "/File/Create", method = RequestMethod.POST)
    @ResponseBody
    public String createFile(@RequestParam("name") String name, @RequestParam("type") String type,
        @RequestParam(value="parents[]", required = false) Long[] parents,
        @RequestParam(value="blocked", required = false) Boolean blocked)
    {
        if ((name == null) || (type == null) || name.isEmpty() || type.isEmpty())
        {
            return "ERROR=Invalid data specified.";
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
        {
            return "ERROR=You are not logged in.";
        }
        String username = authentication.getName();
        
        EstherFile file = EstherFile.newFile(name, type, getUserId(username), false, new Long(0),
            ((blocked == null) ? false : blocked));
        
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
        
        EstherFile file = fileSystemManager.getFileById(id);
        
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
        
        EstherFile file = fileSystemManager.getFileById(id);
        
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
        
        EstherFile file = fileSystemManager.getFileById(id);
        
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
        
        EstherFile file = fileSystemManager.getFileById(id);
        
        file.setPublished(false);
        
        fileSystemManager.updateFile(file);
    }
    
    @RequestMapping(value = "/File/Download", method = RequestMethod.GET)
    public void downloadFile(@RequestParam("file") Long id, HttpServletResponse response)
    {
        if (id == null)
        {
            return;
        }
        
        EstherFile estherFile = fileSystemManager.getFileById(id);
        File file = fileSystemManager.getSystemFileById(estherFile.getId());
        
        response.setContentType("application/downloadable");
        response.setContentLength(estherFile.getSize().intValue());
        response.setHeader("Content-Disposition", ("attachment; filename=\"" + estherFile.getName() + "." + estherFile.getType() + "\""));
        
        try
        {
            InputStream is = new FileInputStream(file);

            IOUtils.copy(is, response.getOutputStream());

            response.flushBuffer();
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, ("Error sending contents of file ID: " + id));
        }
    }
    
    @RequestMapping(value = "/File/Upload", method = RequestMethod.POST)
    @ResponseBody
    public String uploadFile(@RequestParam("file") MultipartFile file,
        @RequestParam(value = "parent", required = false) Long parent)
    {
        String name = file.getOriginalFilename().split("\\.")[0];
        String type = file.getOriginalFilename().split("\\.")[1];
        
        if (parent != null)
        {
            EstherFile parentFile = fileSystemManager.getFileById(parent);
            
            for (EstherWidget widget : widgetList)
            {
                if (widget.opensFile(parentFile.getType()))
                {
                    boolean valid = false;
                    
                    StringBuilder errorBuilder = new StringBuilder("ERROR=Invalid file specified! ");
                    
                    String[] allowedChildren = widget.allowedChildren(parentFile.getType());
                    
                    if (allowedChildren.length > 0)
                    {
                        errorBuilder.append("Please select one of the following:");
                        
                        for (int i = 0; i < allowedChildren.length; i++)
                        {
                            if (allowedChildren[i].equals(type))
                            {
                                valid = true;
                            }
                            
                            if (i != 0)
                            {
                                errorBuilder.append(',');
                            }
                            
                            errorBuilder.append(" .");
                            errorBuilder.append(allowedChildren[i]);
                        }
                    }
                    else
                    {
                        errorBuilder.append("No file can be appended as a child of ");
                        errorBuilder.append(parentFile.getName());
                        errorBuilder.append('.');
                        errorBuilder.append(parentFile.getType());
                    }
                    
                    if (!valid)
                    {
                        return errorBuilder.toString();
                    }
                }
            }
        }
        
        if (exceedsAllowedSpace(file.getSize()))
        {
            return "LIMIT_REACHED=" + (maxStorageSpace / gigabyte) + "GB";
        }
        
        Long[] parents = new Long[] { };
        if (parent != null)
        {
            parents = new Long[] { parent };
        }
        
        Long id = Long.parseLong(createFile(name, type, parents, true));
        
        EstherFile newFile = fileSystemManager.getFileById(id);
        File uploadedFile = fileSystemManager.getSystemFileById(id);
        
        FileOutputStream output = null;
        
        try
        {
            byte[] content = file.getBytes();
            
            output = new FileOutputStream(uploadedFile);
            output.write(content, 0, content.length);
            
            newFile.setSize(uploadedFile.length());
            newFile.setBlocked(false);
            fileSystemManager.updateFile(newFile);
            
            return id.toString();
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, ("Error uploading file " + file.getOriginalFilename()), e);
        }
        finally
        {
            if (output != null)
            {
                try
                {
                    output.close();
                }
                catch (IOException e)
                {
                    logger.log(Level.SEVERE, ("Error uploading file " + file.getOriginalFilename()), e);
                }
            }
        }
        
        return "ERROR=The file could not be uploaded.";
    }
    
    @RequestMapping(value = "/File/Resque", method = RequestMethod.GET)
    public void resqueFile(@RequestParam("file") Long id, HttpServletResponse response)
    {
        downloadFile(id, response);
        
        deleteFile(id);
    }
    
    @RequestMapping(value = "/File/Copy", method = RequestMethod.POST)
    @ResponseBody
    public String copyFile(@RequestParam("file") Long id, @RequestParam("name") String name)
    {
        if ((id == null) || (name == null) || name.isEmpty())
        {
            return "ERROR=Invalid data specified.";
        }
        
        EstherFile file = fileSystemManager.getFileById(id);
        List<EstherFile> parents = fileSystemManager.getFileParents(file);
        
        File sourceFile = fileSystemManager.getSystemFileById(file.getId());
        
        if (exceedsAllowedSpace(file.getSize()))
        {
            return "LIMIT_REACHED=" + (maxStorageSpace / gigabyte) + "GB";
        }
        
        Long[] parentIds = new Long[parents.size()];
        for (int i = 0; i < parents.size(); i++)
        {
            parentIds[i] = parents.get(i).getId();
        }
        
        String createMessage = createFile(name, file.getType(), parentIds, false);
        Long newId;
                
        try
        {
            newId = Long.parseLong(createMessage);
        }
        catch (NumberFormatException e)
        {
            return createMessage;
        }
        
        EstherFile newFile = fileSystemManager.getFileById(newId);
        newFile.setSize(file.getSize());
        fileSystemManager.updateFile(newFile);
        
        for (EstherFile child : fileSystemManager.getAllSubfiles(file))
        {
            fileSystemManager.setParent(child, newFile);
        }
        
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
            return "ERROR=Invalid data specified.";
        }
        
        File file = fileSystemManager.getSystemFileById(id);
        
        if (file == null)
        {
            return "ERROR=File not found.";
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
        
            return dataBuilder.toString();
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
        
        return "ERROR=Failed to read file.";
    }
    
    @RequestMapping(value = "/File/Write", method = RequestMethod.POST)
    @ResponseBody
    public String writeFile(@RequestParam("file") Long id, @RequestParam("data") String data)
    {
        if ((id == null) || (data == null))
        {
            return null;
        }
        
        File file = fileSystemManager.getSystemFileById(id);
        
        if (file == null)
        {
            return null;
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
        
        EstherFile estherFile = fileSystemManager.getFileById(id);
        estherFile.setSize(file.length());
        fileSystemManager.updateFile(estherFile);
        
        if (exceedsAllowedSpace())
        {
            return "LIMIT_REACHED=" + (maxStorageSpace / gigabyte) + "GB";
        }
        else
        {
            return id.toString();
        }
    }
    
    public Boolean exceedsAllowedSpace()
    {
        return exceedsAllowedSpace(0l);
    }
    
    private Boolean exceedsAllowedSpace(long newFileSize)
    {
        Long size = newFileSize;
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
        {
            return null;
        }
        Long userId = getUserId(authentication.getName());
        
        size += fileSystemManager.getTotalSize(userId);
        
        return (size > maxStorageSpace);
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
