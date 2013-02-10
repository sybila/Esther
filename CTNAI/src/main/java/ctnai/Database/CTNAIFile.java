package ctnai.Database;

import java.util.Objects;

public class CTNAIFile
{
    private Long id;
    
    private String name;
    private String type;
    
    private Long owner;

    private Boolean published;
    
    private Long size;
    
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
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

    public Long getOwner()
    {
        return owner;
    }

    public void setOwner(Long owner)
    {
        this.owner = owner;
    }

    public Boolean getPublished()
    {
        return published;
    }

    public void setPublished(Boolean published)
    {
        this.published = published;
    }

    public Long getSize()
    {
        return size;
    }

    public void setSize(Long size)
    {
        this.size = size;
    }
    
    public static CTNAIFile newFile(String name, String type, Long ownerId, Boolean published, Long size)
    {
        CTNAIFile file = new CTNAIFile();
        
        file.setName(name);
        file.setType(type);
        file.setOwner(ownerId);
        file.setPublished(published);
        file.setSize(size);
        
        return file;
    }

    @Override
    public String toString()
    {
       return ((published ? "Public" : "Private") + " file of owner ID: " + owner +
               ", named: " + name + '.' + type + " (ID: " + id + "), (Size: " + size + " bytes)");
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = ((13 * hash) + Objects.hashCode(this.id));
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((obj == null) || (getClass() != obj.getClass()))
        {
            return false;
        }
        
        final CTNAIFile other = (CTNAIFile)obj;
        
        if (!Objects.equals(this.id, other.id))
        {
            return false;
        }
        
        return true;
    }    
}
