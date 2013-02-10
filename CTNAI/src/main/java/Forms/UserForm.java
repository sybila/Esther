package Forms;

import ctnai.Database.User;
import org.apache.commons.validator.EmailValidator;

public class UserForm
{
    private String username;
    private String password;
    private String cPassword;
    private String email;
    
    public UserForm()
    {
        username = "";
        password = "";
        cPassword = "";
        email = "@";
    }
    
    public UserForm(String username, String password, String cPassword, String email)
    {
        this.username = username;
        this.password = password;
        this.cPassword = cPassword;
        this.email = email;
    }
    
    public User validate(StringBuilder errorOutbut)
    {
        User user = new User();
        
        user.setEnabled(false);
        
        if ((username == null) || username.isEmpty())
        {
            errorOutbut.append("Username cannot be blank.");
            errorOutbut.append("<br/>\n");
        }
        else if (username.length() < 3)
        {
            errorOutbut.append("Username must be at least 3 characters long.");
            errorOutbut.append("<br/>\n");
        }
        else if (username.length() > 64)
        {
            errorOutbut.append("Username cannot be more than 64 characters long.");
            errorOutbut.append("<br/>\n");
        }
        else
        {
            user.setUsername(username);
        }
        
        if ((email == null) || email.isEmpty())
        {
            errorOutbut.append("Please provide an E-Mail address.");
            errorOutbut.append("<br/>\n");
        }
        else if (!EmailValidator.getInstance().isValid(email))
        {
            errorOutbut.append("The E-Mail address specified is invalid.");
            errorOutbut.append("<br/>\n");
        }
        else if (email.length() > 128)
        {
            errorOutbut.append("E-Mail addresses longer than 128 characters are not supported.");
            errorOutbut.append("<br/>\n");
        }
        else
        {
            user.setEmail(email);
        }
        
        if ((password == null) || password.isEmpty())
        {
            errorOutbut.append("Password cannot be blank.");
            errorOutbut.append("<br/>\n");
        }
        else if (password.length() < 6)
        {
            errorOutbut.append("Password must be at least 6 characters long.");
            errorOutbut.append("<br/>\n");
        }
        else if (password.length() > 64)
        {
            errorOutbut.append("Password cannot be more than 64 characters long.");
            errorOutbut.append("<br/>\n");
        }
        else if (!password.equals(cPassword))
        {
            errorOutbut.append("Passwords do not match.");
            errorOutbut.append("<br/>\n");
        }
        else
        {
            user.setPassword(password);
        }
        
        return user;
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

    public String getcPassword()
    {
        return cPassword;
    }

    public void setcPassword(String cPassword)
    {
        this.cPassword = cPassword;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }
}
