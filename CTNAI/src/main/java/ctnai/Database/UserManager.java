package ctnai.Database;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.sql.DataSource;
import org.apache.commons.lang.NullArgumentException;

public class UserManager
{
    private DataSource dataSource;
    public static final Logger logger = Logger.getLogger(FileSystemManager.class.getName());
    
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }
    
    public void setLogger(FileOutputStream fs)
    {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
    }
    
    public Long registerUser(User user)
    {
        if (user == null)
        {
            throw new NullArgumentException("User");
        }
        
        if (user.getId() != null)
        {
            throw new IllegalArgumentException("User is already registered an ID.");
        }
        
        if ((user.getUsername() == null) || (user.getPassword() == null) ||
                (user.getEmail() == null) || (user.getEnabled() == null))
        {
            throw new IllegalArgumentException("User misses required attributes.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("INSERT INTO USERS (username, password, email, enabled) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.setBoolean(4, user.getEnabled());
            
            statement.executeUpdate();
            
            Long id = DBUtils.getID(statement.getGeneratedKeys());
            user.setId(id);
            
            logger.log(Level.INFO, ("Succesfully registered: " + user));
            
            return id;
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error registering " + user), e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    public User getUserByUsername(String username)
    {
        if (username == null)
        {
            throw new NullArgumentException("Username");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT * FROM USERS WHERE username=?");
            
            statement.setString(1, username);
            
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next())
            {
                User result = getUserFromResultSet(resultSet);
                
                if (resultSet.next())
                {
                    return null; //TODO
                }
                
                return result;
            }
            else
            {
                return null;
            }
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting user: " + username + " from database.", e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    public User getUserByEmail(String email)
    {
        if (email == null)
        {
            throw new NullArgumentException("Username");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT * FROM USERS WHERE email=?");
            
            statement.setString(1, email);
            
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next())
            {
                User result = getUserFromResultSet(resultSet);
                
                if (resultSet.next())
                {
                    return null; //TODO
                }
                
                return result;
            }
            else
            {
                return null;
            }
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting user with E-Mail: " + email + " from database.", e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    private User getUserFromResultSet(ResultSet resultSet) throws SQLException
    {
        User user = new User();

        user.setId(resultSet.getLong("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        user.setEmail(resultSet.getString("email"));
        user.setEnabled(resultSet.getBoolean("enabled"));

        return user;
    }
    
    public void setUserRole(User user, String role)
    {
        if (user == null)
        {
            throw new NullArgumentException("User");
        }
        
        if (user.getId() == null)
        {
            throw new IllegalArgumentException("Cannot set user rolu for user with NULL ID.");
        }
        
        if (role == null)
        {
            throw new NullArgumentException("Role");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("INSERT INTO AUHTORITIES (user, authority) VALUES (?, ?)");
            
            statement.setLong(1, user.getId());
            statement.setString(2, role);
            
            statement.executeUpdate();
            
            logger.log(Level.INFO, ("Succesfully set user role for: " + user));
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error setting rol for " + user), e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
    }
}
