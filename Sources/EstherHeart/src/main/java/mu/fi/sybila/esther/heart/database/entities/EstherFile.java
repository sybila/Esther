package mu.fi.sybila.esther.heart.database.entities;

import java.util.Objects;

/**
 * Database entity representing a file.
 * 
 * @author George Kolcak
 */
public class EstherFile
{
    
    private Long id;
    
    private String name;
    private String type;
    
    private Long owner;

    private Boolean published;
    
    private Long size;
    
    private Boolean blocked;
    
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

    public Boolean getBlocked()
    {
        return blocked;
    }

    public void setBlocked(Boolean blocked)
    {
        this.blocked = blocked;
    }
    
    /**
     * Returns a new file with the specified attributes.
     * 
     * @param name The name of the file.
     * @param type The extension of the file.
     * @param ownerId The ID of the User who owns the file.
     * @param published Whether the file is public for the other users.
     * @param size Size of the file in bytes.
     * @param blocked Whether the file is visible and accessible.
     * @return The created EstherFile.
     */
    public static EstherFile newFile(String name, String type, Long ownerId, Boolean published, Long size, Boolean blocked)
    {
        EstherFile file = new EstherFile();
        
        file.setName(name);
        file.setType(type);
        file.setOwner(ownerId);
        file.setPublished(published);
        file.setSize(size);
        file.setBlocked(blocked);
        
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
        
        final EstherFile other = (EstherFile)obj;
        
        if (!Objects.equals(this.id, other.id))
        {
            return false;
        }
        
        return true;
    }    
    
}
