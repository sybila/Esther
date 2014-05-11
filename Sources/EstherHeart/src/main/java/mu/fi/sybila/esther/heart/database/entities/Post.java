package mu.fi.sybila.esther.heart.database.entities;

import java.sql.Date;
import java.util.Objects;

public class Post
{
    private Long id;
    private String title;
    private String content;
    private Long creator;
    private Date date;
    
    private String creatorNick;
    
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Long getCreator()
    {
        return creator;
    }

    public void setCreator(Long creator)
    {
        this.creator = creator;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public String getCreatorNick()
    {
        return creatorNick;
    }

    public void setCreatorNick(String creatorNick)
    {
        this.creatorNick = creatorNick;
    }
     
    @Override
    public String toString() {
        return ("Post \"" + title + "\" by user ID: " + creator + " on " + date);
    }

    @Override
    public int hashCode()
    {
        int hash = 1997;
        hash = (hash + (41 * Objects.hashCode(this.id)));
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((obj == null) || (getClass() != obj.getClass()))
        {
            return false;
        }
        
        final Post other = (Post)obj;
        
        if (!Objects.equals(this.id, other.id))
        {
            return false;
        }
        
        return true;
    }
}
