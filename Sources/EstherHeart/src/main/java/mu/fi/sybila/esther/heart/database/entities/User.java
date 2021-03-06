package mu.fi.sybila.esther.heart.database.entities;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Database entity representing a User.
 * 
 * @author George Kolcak
 */
public class User
{
    
    private Long id;
    private String username;
    private String password;
    private String email;
    private Boolean enabled;

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

    /**
     * Sets the password encrypting it in the process.
     * 
     * @param password The not encrypted password.
     * @throws NoSuchAlgorithmException If the password encryption fails.
     * @throws UnsupportedEncodingException If the password encryption fails.
     */
    public void setPassword(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        StringBuilder hashBuilder = new StringBuilder();
        
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] digest = sha.digest(password.getBytes("UTF-8"));
        
        for (byte b : digest)
        {
            hashBuilder.append(String.format("%02x", b));
        }
        
        this.password = hashBuilder.toString();
    }
    
    /**
     * Sets the password without encryption.
     * 
     * @param hash The encrypted password.
     */
    public void setEncryptedPassword(String hash)
    {
        password = hash;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
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
        return ((enabled ? "Enabled" : "Disabled") + " user " + username + " ID: " + id + ", E-Mail: " + email);
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
