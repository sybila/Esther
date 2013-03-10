package ctnai.Database;

public class UserInformation
{
    private Long id;
    private Boolean hidePublicOwned;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
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
