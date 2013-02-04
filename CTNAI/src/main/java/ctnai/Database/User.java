package ctnai.Database;

import java.util.Objects;

public class User
{
    private Long id;
    private String username;
    private String password;
    private Boolean enabled;
    
    public User() { }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public Boolean getEnabled()
    {
        return enabled;
    }

    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return ((enabled ? "Enabled" : "Disabled") + " user " + username + " ID: " + id);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = ((67 * hash) + Objects.hashCode(this.id));
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((obj == null) || (getClass() != obj.getClass()))
        {
            return false;
        }
        
        final User other = (User)obj;
        
        if (!Objects.equals(this.id, other.id))
        {
            return false;
        }
        
        return true;
    }
}
