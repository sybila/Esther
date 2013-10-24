package mu.fi.sybila.esther.heart.database.forms;

import mu.fi.sybila.esther.heart.database.entities.EstherFile;

/**
 *
 * @author jooji
 */
public class FileForm
{
    
    private Long id;
    private Long parentId;
    
    private String name;
    private String type;
    
    private Boolean published;
    private Boolean locked;
    
    public static FileForm fromFile (EstherFile file)
    {
        FileForm form = new FileForm();
        
        form.setId(file.getId());
        
        form.setName(file.getName());
        form.setType(file.getType());
        
        form.setPublished(file.getPublished());
        
        return form;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Boolean getPublished()
    {
        return published;
    }

    public void setPublished(Boolean published)
    {
        this.published = published;
    }

    public Boolean getLocked()
    {
        return locked;
    }

    public void setLocked(Boolean locked)
    {
        this.locked = locked;
    }
    
}
