package ctnai.Database;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    
    private User getUserFromResultSet(ResultSet resultSet) throws SQLException
    {
        User user = new User();

        user.setId(resultSet.getLong("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        user.setEnabled(resultSet.getBoolean("enabled"));

        return user;
    }
}
