package mu.fi.sybila.esther.heart.database.entities;

/**
 * Database entity representing user information.
 * 
 * @author George Kolcak
 */
public class UserInformation
{
    
    private Long id;
    
    private String country;
    private String organization;
    
    private Boolean hidePublicOwned;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    public String getOrganization()
    {
        return organization;
    }

    public void setOrganization(String organization)
    {
        this.organization = organization;
    }

    public Boolean getHidePublicOwned()
    {
        return hidePublicOwned;
    }

    public void setHidePublicOwned(Boolean hidePublicOwned)
    {
        this.hidePublicOwned = hidePublicOwned;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = ((61 * hash) + (int)(this.id ^ (this.id >>> 32)));
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((obj == null) || (getClass() != obj.getClass()))
        {
            return false;
        }
        
        final UserInformation other = (UserInformation)obj;
        return (this.id == other.id);
    }
    
}
