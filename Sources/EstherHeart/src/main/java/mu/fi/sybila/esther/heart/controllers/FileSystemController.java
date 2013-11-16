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
import java.util.LinkedHashMap;
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
import mu.fi.sybila.esther.heart.database.forms.FileForm;
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

/**
 * Controller for the file system manipulation
 * 
 * @author George Kolcak
 */
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
     * Handler method for listing file system root.
     * 
     * @return File system root page.
     */
    @RequestMapping(value = "/Files/Root", method = RequestMethod.GET)
    public String fileSystemRoot()
    {
        return "filesystem/root";
    }
    
    /**
     * Handler method for listing private files.
     * 
     * @param map The map of UI properties.
     * @return File list page.
     */
//    @RequestMapping(value = "/Files/My", method = RequestMethod.GET)
//    public String myFiles(ModelMap map)
//    {
//        List<EstherFile> files = new ArrayList<>();
//        
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication.isAuthenticated())
//        {
//            String username = authentication.getName();
//            
//            files = fileSystemManager.getRootFilesOwnedBy(getUserId(username));
//        }
//        
//        map.addAttribute("privacy", "private");
//        map.addAttribute("files", files);
//        
//        return "filesystem/list";
//    }
    
    /**
     * Handler method for listing public files.
     * 
     * @param map The map of UI properties.
     * @return File list page.
     */
//    @RequestMapping(value = "/Files/Public", method = RequestMethod.GET)
//    public String publicFiles(ModelMap map)
//    {
//        List<EstherFile> files = new ArrayList<>();
//        
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication.isAuthenticated())
//        {
//            String username = authentication.getName();
//            
//            UserInformation information = userManager.getUserInformation(getUserId(username));
//
//            files = fileSystemManager.getPublicRootFiles(information);
//        }
//        
//        map.addAttribute("privacy", "public");
//        map.addAttribute("files", files);
//        
//        return "filesystem/list";
//    }
    
    /**
     * Handler method for listing subfiles of the given file.
     * 
     * @param map The map of UI properties.
     * @param fileId The ID of the file whose subfiles are to be listed.
     * @param privacy Value indicating whether the list was opened in 'public' or 'private' subtree.
     * @return File list page.
     */
    @RequestMapping(value = "/Files/List", method = RequestMethod.GET)
    public String subfiles(ModelMap map, @RequestParam(value = "file", required = false) Long fileId,
        @RequestParam("privacy") String privacy)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName()))
        {
            return "redirect:/login";
        }
        
        Long user = getUserId(authentication.getName());
        
        Boolean published = ("public".equals(privacy));
        
        List<EstherFile> rawFiles;
        
        if (fileId == null)
        {
            if (published)
            {
                UserInformation information = userManager.getUserInformation(user);

                rawFiles = fileSystemManager.getPublicRootFiles(information);
            }
            else
            {
                rawFiles = fileSystemManager.getRootFilesOwnedBy(user);
            }
        }
        else
        {
            if (published)
            {
                UserInformation information = userManager.getUserInformation(user);

                rawFiles = fileSystemManager.getPublicSubfiles(fileSystemManager.getFileById(fileId), information);
            }
            else
            {
                rawFiles = fileSystemManager.getSubfilesOwnedBy(fileSystemManager.getFileById(fileId), user);
            }          
        }
        
        List<FileForm> files = new ArrayList<>();
        
        for (EstherFile file : rawFiles)
        {
            FileForm form = FileForm.fromFile(file);
            
            form.setParentId(fileId);
            form.setLocked(published && !file.getPublished() /*&& (user != file.getOwner())*/);
            
            files.add(form);
        }
        
        map.addAttribute("privacy", privacy);
        map.addAttribute("files", files);
        
        return "filesystem/list";
    }
    
//    @RequestMapping(value = "/Files/Sub", method = RequestMethod.GET)
//    public String subfiles(ModelMap map, @RequestParam("file") Long parentId,
//        @RequestParam("privacy") String privacy)
//    {
//        List<EstherFile> files = new ArrayList<>();
//        
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication.isAuthenticated())
//        {
//            String username = authentication.getName();
//            
//            switch (privacy)
//            {
//                case "private":
//                    {
//                        files = fileSystemManager.getSubfilesOwnedBy(fileSystemManager.getFileById(parentId), getUserId(username));
//                        break;
//                    }
//                case "public":
//                    {
//                        UserInformation information = userManager.getUserInformation(getUserId(username));
//                        
//                        files = fileSystemManager.getPublicSubfiles(fileSystemManager.getFileById(parentId), information);
//                        break;
//                    }
//            }
//        }
//        
//        map.addAttribute("privacy", privacy);
//        map.addAttribute("files", files);
//        
//        return "filesystem/list";
//    }
    
    /**
     * Handler method for right click file menu.
     * 
     * @param map The map of UI properties.
     * @param id The ID of the file whose menu is to be opened.
     * @return File menu page.
     */
    @RequestMapping(value = "/File/Menu", method = RequestMethod.GET)
    public String getFileMenu(ModelMap map, @RequestParam("file") Long id)
    {
        Map<String, String> links = new LinkedHashMap<>();
        
        if (id == null)
        {
            links.put("new_model", "New Model");
            links.put("upload", "Upload");
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

                    if (file.getPublished())
                    {
                        links.put("privatize", "Make Private");
                    }
                    else
                    {
                        links.put("publish", "Make Public");
                    }
                    
                    links.put("upload", "Upload");
                    links.put("copy", "Copy");
                }
                else if (fileSystemManager.getParent(file) == null)
                {
                    links.put("copy", "Copy");
                }

                links.put("download", "Download");
            }
        }
        
        map.addAttribute("links", links);
        
        return "filesystem/menu";
    }
    
    @RequestMapping(value = "/File/DragMenu", method = RequestMethod.GET)
    public String getDragMenu(ModelMap map, @RequestParam("file") Long id, @RequestParam("source") Long sourceId)
    {
        Map<String, String> links = new LinkedHashMap<>();
        
        if ((id != null) && (sourceId != null))
        {
            EstherFile file = fileSystemManager.getFileById(id);
            EstherFile source = fileSystemManager.getFileById(sourceId);
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if ((file != null) && (source != null) && authentication.isAuthenticated())
            {
                Long userId = getUserId(authentication.getName());

                if (file.getOwner() == userId)
                {
                    boolean compatibleChild = false;
                    
                    for (EstherWidget widget : widgetList)
                    {
                        if (widget.opensFile(file.getType()))
                        {
                            String[] allowedChildren = widget.allowedChildren(file.getType());
                            
                            for (int i = 0; i < allowedChildren.length; i++)
                            {
                                if (allowedChildren[i].equals(source.getType()))
                                {
                                    compatibleChild = true;
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (compatibleChild)
                    {
                        links.put("copy_as_child", "Copy as subfile");
                        
                        if (source.getOwner() == userId)
                        {
                            links.put("move_as_child", "Move as subfile");
                        }
                    }
                }
            }
        }
        
        map.addAttribute("links", links);
        
        return "filesystem/menu";
    }
    
    /**
     * Handler method for creating files.
     * 
     * @param name The name of the file to be created.
     * @param type The extension of the file to be created.
     * @param parents List of IDs of the parent files of the created file. Empty if not specified.
     * @param blocked Value indicating whether the file access to the created file is to be blocked. False by default.
     * @return The file ID of the created file.
     *         Error message if creating of the file fails.
     */
    @RequestMapping(value = "/File/Create", method = RequestMethod.POST)
    @ResponseBody
    public String createFile(@RequestParam("name") String name, @RequestParam("type") String type,
        @RequestParam(value="parent", required = false) Long parentId,
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
        
        EstherFile parent = null;
        
        if (parentId != null)
        {
            parent = fileSystemManager.getFileById(parentId);
        }
        
        Long userId = getUserId(username);
        
        if((parent != null) && (parent.getOwner() != userId))
        {
            return "ERROR=Cannot create file as a subfile of public file you not own. Please copy it first.";
        }
            
        EstherFile file = EstherFile.newFile(name, type, userId, false, new Long(0),
            ((blocked == null) ? false : blocked));
        
        Long id = fileSystemManager.createFile(file);
        
        if (parent != null)
        {
            fileSystemManager.setParent(fileSystemManager.getFileById(id), parent);
        }
        
        return id.toString();
    }
    
    /**
     * Handler method for file deleting.
     * 
     * @param id ID of the file to be deleted.
     */
    @RequestMapping(value = "/File/Delete", method = RequestMethod.POST)
    @ResponseBody
    public void deleteFile(@RequestParam("file") Long id)
    {
        if (id == null)
        {
            return;
        }
        
        EstherFile file = fileSystemManager.getFileById(id);
        
        List<EstherFile> children = fileSystemManager.getAllSubfiles(file);
        
        fileSystemManager.deleteFile(file);
        
        for (EstherFile child : children)
        {
            deleteFile(child.getId());
        }
    }
    
    /**
     * Handler method for file renaming.
     * 
     * @param id The ID of the file to be renamed.
     * @param name The new name of the file.
     */
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
    
    /**
     * Handler method for rendering files public.
     * 
     * @param id The ID of the file to make public.
     */
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
    
    /**
     * Handler method for rendering files private.
     * 
     * @param id The ID of the file to make private.
     */
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
    
    /**
     * Handler method for file downloading.
     * 
     * @param id The ID of the file to be downloaded.
     * @param response The HTTP Response.
     */
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
    
    /**
     * Handler method for file uploading.
     * 
     * @param file The multipart data containing the uploaded file.
     * @param parent The ID of the parent of the file to upload.
     * @return ID of the uploaded file.
     *         Error message if uploading of the file fails.
     *         Limit reached message if the file exceeds the storage capacity.
     */
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
        
        Long id = Long.parseLong(createFile(name, type, parent, true));
        
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
    
    /**
     * Handler method for emergency downloading of files over the storage space capacity.
     * 
     * @param id The ID of the file to be rescued.
     * @param response The HTML Response.
     */
    @RequestMapping(value = "/File/Rescue", method = RequestMethod.GET)
    public void rescueFile(@RequestParam("file") Long id, HttpServletResponse response)
    {
        downloadFile(id, response);
        
        deleteFile(id);
    }
    
    /**
     * Handler method for file copying.
     * 
     * @param id The ID of the file the copy.
     * @param name Name of the copy.
     * @return ID of the new file.
     *         Error message if the copying failed.
     *         Limit reached message if the new file exceeds the allowed storage space.
     */
    @RequestMapping(value = "/File/Copy", method = RequestMethod.POST)
    @ResponseBody
    public String copyFile(@RequestParam("file") Long id, @RequestParam("name") String name,
        @RequestParam(value = "parent", required = false) Long parentId)
    {
        if ((id == null) || (name == null) || name.isEmpty())
        {
            return "ERROR=Invalid data specified.";
        }
        
        EstherFile file = fileSystemManager.getFileById(id);
        EstherFile parent;
        
        if (parentId != null)
        {
            parent = fileSystemManager.getFileById(parentId);
        }
        else
        {
            parent = fileSystemManager.getParent(file);
        }
        
        File sourceFile = fileSystemManager.getSystemFileById(file.getId());
        
        if (exceedsAllowedSpace(file.getSize()))
        {
            return "LIMIT_REACHED=" + (maxStorageSpace / gigabyte) + "GB";
        }
        
        String createMessage = createFile(name, file.getType(), ((parent == null) ? null : parent.getId()), false);
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
    
    @RequestMapping(value = "/File/Move", method = RequestMethod.POST)
    @ResponseBody
    public String moveFile(@RequestParam("file") Long id, @RequestParam("parent") Long parentId)
    {
        if ((id == null) || (parentId == null))
        {
            return "ERROR=Invalid data specified.";
        }
        
        EstherFile file = fileSystemManager.getFileById(id);
        EstherFile parent = fileSystemManager.getFileById(parentId);
        
        fileSystemManager.removeParent(file);
        fileSystemManager.setParent(file, parent);
        
        return null;
    }
    
    /**
     * Handler method for file reading.
     * 
     * @param id The ID of the file to be read.
     * @return File contents.
     *         Error message if reading of the file fails.
     */
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
    
    /**
     * Handler method for file writing.
     * 
     * @param id The ID of the file to be rewritten.
     * @param data The new file contents.
     * @return The file ID.
     *         Error message if the file exceeds the allowed storage space.
     */
    @RequestMapping(value = "/File/Write", method = RequestMethod.POST)
    @ResponseBody
    public String writeFile(@RequestParam("file") Long id, @RequestParam("data") String data)
    {
        if ((id == null) || (data == null))
        {
            return "ERROR=Invalid data specified.";
        }
        
        EstherFile file = fileSystemManager.getFileById(id);
        
        if (file == null)
        {
            return "ERROR=Invalid data specified.";
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated())
        {
            return "ERROR=You are not logged in.";
        }
        String username = authentication.getName();
            
        if (file.getOwner() != getUserId(username))
        {
            return "ERROR=You cannot edit files of another user.";
            
//            List<EstherFile> parents = fileSystemManager.getFileParents(file);
//            Long[] parentIds = new Long[parents.size()];
//            for (int i = 0; i < parents.size(); i++)
//            {
//                parentIds[i] = parents.get(i).getId();
//            }
//            
//            String createMessage = createFile(file.getName(), file.getType(), parentIds, false);
//            
//            Long newId;
//            
//            try
//            {
//                newId = Long.parseLong(createMessage);
//            }
//            catch (NumberFormatException e)
//            {
//                return createMessage;
//            }
//            
//            file = fileSystemManager.getFileById(newId);
        }
        
        File sysFile = fileSystemManager.getSystemFileById(file.getId());
        
        BufferedWriter bw = null;

        try
        {
            bw = new BufferedWriter(new FileWriter(sysFile));

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
        
        file.setSize(sysFile.length());
        fileSystemManager.updateFile(file);
        
        if (exceedsAllowedSpace())
        {
            return "LIMIT_REACHED=" + (maxStorageSpace / gigabyte) + "GB";
        }
        else
        {
            return id.toString();
        }
    }
    
    /**
     * Checks whether the storage space limit is met.
     * 
     * @return True if the limit is exceeded. False otherwise.
     */
    public Boolean exceedsAllowedSpace()
    {
        return exceedsAllowedSpace(0l);
    }
    
    /**
     * Checks whether the storage space limit will be met after addition of a new file.
     * 
     * @param newFileSize The size of the new file.
     * @return True if the limit will be exceeded. False otherwise.
     */
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
    
    /**
     * Returns ID of the user with the specified username.
     * 
     * @param username The username associated with the coveted ID.
     * @return The ID of the user. Null if no such user exists.
     */
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
