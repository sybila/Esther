package mu.fi.sybila.esther.heart.database.forms;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import javax.servlet.http.HttpServletRequest;
import mu.fi.sybila.esther.heart.database.entities.User;
import org.apache.commons.validator.EmailValidator;

/**
 * Data Model for User.
 * 
 * @author George Kolcak
 */
public class UserForm
{
    
    private String username;
    private String password;
    private String cPassword;
    private String email;
    
    /**
     * Static method for obtaining the user data from a HTTP request.
     * 
     * @param request The HTTP request with the user information.
     * @return The created user form.
     */
    public static UserForm extractFromRequest(HttpServletRequest request)
    {
        UserForm form = new UserForm();
        
        form.setUsername(request.getParameter("username"));
        form.setEmail(request.getParameter("email"));
        form.setPassword(request.getParameter("password"));
        form.setcPassword(request.getParameter("cPassword"));
        
        return form;
    }
    
    /**
     * Static method for converting user into user form.
     * 
     * @param user The user with the coveted information.
     * @return The created user form.
     */
    public static UserForm extractFromUser(User user)
    {
        UserForm form = new UserForm();
        
        form.setEmail(user.getEmail());
        form.setUsername(user.getUsername());
        
        return form;
    }
    
    /**
     * Validates user data.
     * 
     * @param errorOutput StringBuilder used as output in case of error in the data.
     * @return A new User with the validated data.
     * @throws NoSuchAlgorithmException In case the encoding of the password fails.
     * @throws UnsupportedEncodingException In case the encoding of the password fails.
     */
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
    
    /**
     * Validates the value of the username attribute.
     * 
     * @param errorOutput StringBuilder used as output in case of error in the data.
     * @return True if the username is valid. False otherwise.
     */
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

    /**
     * Validates the value of the email attribute.
     * 
     * @param errorOutput StringBuilder used as output in case of error in the data.
     * @return True if the e-mail is valid. False otherwise.
     */
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
    
    /**
     * Validates the value of password and confirmation password attributes.
     * 
     * @param errorOutput StringBuilder used as output in case of error in the data.
     * @return True if the passwords are valid. False otherwise.
     */
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

