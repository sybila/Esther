package Forms;

import ctnai.Database.User;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
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
        email = "";
    }
    
    public UserForm(String username, String password, String cPassword, String email)
    {
        this.username = username;
        this.password = password;
        this.cPassword = cPassword;
        this.email = email;
    }
    
    public User validate(StringBuilder errorOutput) throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        User user = new User();
        
        user.setEnabled(false);
        
        if (validateUsername(errorOutput))
        {
            user.setUsername(username);
        }
        
        if (validateEmail(errorOutput))
        {
            user.setEmail(email);
        }
        
        if (validatePassword(errorOutput))
        {
            user.setPassword(password);
        }
        
        return user;
    }
    
    public boolean validateUsername(StringBuilder errorOutput)
    {
        if ((username == null) || username.isEmpty())
        {
            errorOutput.append("Username cannot be blank.");
            errorOutput.append("<br/>\n");
            
            return false;
        }
        else if (username.length() < 3)
        {
            errorOutput.append("Username must be at least 3 characters long.");
            errorOutput.append("<br/>\n");
            
            return false;
        }
        else if (username.length() > 64)
        {
            errorOutput.append("Username cannot be more than 64 characters long.");
            errorOutput.append("<br/>\n");
            
            return false;
        }
        
        return true;
    }

    public boolean validateEmail(StringBuilder errorOutput)
    {
        if ((email == null) || email.isEmpty())
        {
            errorOutput.append("Please provide an E-Mail address.");
            errorOutput.append("<br/>\n");
            
            return false;
        }
        else if (!EmailValidator.getInstance().isValid(email))
        {
            errorOutput.append("The E-Mail address specified is invalid.");
            errorOutput.append("<br/>\n");
            
            return false;
        }
        else if (email.length() > 128)
        {
            errorOutput.append("E-Mail addresses longer than 128 characters are not supported.");
            errorOutput.append("<br/>\n");
            
            return false;
        }
        
        return true;
    }
    
    public boolean validatePassword(StringBuilder errorOutput)
    {
        if ((password == null) || password.isEmpty())
        {
            errorOutput.append("Password cannot be blank.");
            errorOutput.append("<br/>\n");
            
            return false;
        }
        else if (password.length() < 6)
        {
            errorOutput.append("Password must be at least 6 characters long.");
            errorOutput.append("<br/>\n");
            
            return false;
        }
        else if (password.length() > 64)
        {
            errorOutput.append("Password cannot be more than 64 characters long.");
            errorOutput.append("<br/>\n");
            
            return false;
        }
        else if (!password.equals(cPassword))
        {
            errorOutput.append("Passwords do not match.");
            errorOutput.append("<br/>\n");
            
            return false;
        }
        
        return true;
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
