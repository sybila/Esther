package mu.fi.sybila.esther.heart.database;

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
import mu.fi.sybila.esther.heart.database.entities.User;
import mu.fi.sybila.esther.heart.database.entities.UserInformation;
import org.apache.commons.lang.NullArgumentException;

public class UserManager
{
    private DataSource dataSource;
    public static final Logger logger = Logger.getLogger(UserManager.class.getName());
    
    /**
     * Method for setting up the manager.
     * 
     * @param dataSource The DataSource used for database connection.
     */
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }
    
    /**
     * Method for setting up the manager.
     * 
     * @param fs The output stream for logger output.
     */
    public void setLogger(FileOutputStream fs)
    {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
    }
    
    /**
     * Adds new User to the database.
     * 
     * @param user The User to be added.
     * @return The ID of the new User.
     *         Null if the creation fails.
     */
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
            
            Long id = DatabaseUtils.getID(statement.getGeneratedKeys());
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
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Updates the given User in the database.
     * 
     * @param user The User to be updated.
     */
    public void updateUser(User user)
    {
        if (user == null)
        {
            throw new NullArgumentException("User");
        }
        
        if (user.getId() == null)
        {
            throw new IllegalArgumentException("User ID is NULL.");
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
                .prepareStatement("UPDATE USERS SET username=?, password=?, email=?, enabled=? WHERE id=?",
                    Statement.RETURN_GENERATED_KEYS);
            
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.setBoolean(4, user.getEnabled());
            statement.setLong(5, user.getId());
            
            statement.executeUpdate();
            
            logger.log(Level.INFO, ("Succesfully updated: " + user));
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error updating " + user), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    /**
     * Retrieves a user by ID.
     * 
     * @param id The ID of the coveted user.
     * @return The User with the specified ID.
     */
    public User getUserById(Long id)
    {
        if (id == null)
        {
            throw new NullArgumentException("ID");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT * FROM USERS WHERE id=?");
            
            statement.setLong(1, id);
            
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next())
            {
                User result = getUserFromResultSet(resultSet);
                
                if (resultSet.next())
                {
                    logger.log(Level.SEVERE, "Error selecting user ID: {0} from database. Several users with the same ID found.", id);
                
                    return null;
                }
                
                return result;
            }
            else
            {
                logger.log(Level.SEVERE, "Error selecting user ID: {0} from database. No such user found.", id);
            }
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting user ID: " + id + " from database.", e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Retrieves a user by username.
     * 
     * @param username the username of the coveted user.
     * @return The User with the specified username.
     */
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
                    logger.log(Level.SEVERE, "Error selecting user: {0} from database. Several users with the same username found.", username);
                    
                    return null;
                }
                
                return result;
            }
            else
            {
                logger.log(Level.SEVERE, "Error selecting user: {0} from database. No such user found.", username);
            }
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting user: " + username + " from database.", e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Retrieves a user by e-mail.
     * 
     * @param email The e-mail of the coveted user.
     * @return The User with the specified email.
     */
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
                    logger.log(Level.SEVERE, "Error selecting user with E-Mail: {0} from database. Several users found for the same e-mail.", email);
                    
                    return null;
                }
                
                return result;
            }
            else
            {
                logger.log(Level.SEVERE, "Error selecting user with E-Mail: {0} from database. No such user found.", email);
            }
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting user with E-Mail: " + email + " from database.", e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Sets the activation token for the given user.
     * 
     * @param user The User whose token is to be set.
     * @param token The activation token for the specified user.
     */
    public void setActivationToken(User user, String token)
    {
        if (user == null)
        {
            throw new NullArgumentException("User");
        }
        
        if (user.getId() == null)
        {
            throw new IllegalArgumentException("Cannot set activation token for User without ID.");
        }
        
        if (token == null)
        {
            throw new NullArgumentException("Token");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("INSERT INTO TOKENS (user, token) VALUES (?, ?)");
            
            statement.setLong(1, user.getId());
            statement.setString(2, token);
            
            statement.executeUpdate();
            
            logger.log(Level.INFO, ("Succesfully set activation token for " + user));
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error setting activation token for " + user), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    /**
     * Sets activation token for a disabled user.
     * 
     * @param user The disabled User whose token is to be set.
     * @param token the reactivation token for the specified user.
     */
    public void setReactivationToken(User user, String token)
    {
        if (user == null)
        {
            throw new NullArgumentException("User");
        }
        
        if (user.getId() == null)
        {
            throw new IllegalArgumentException("Cannot set activation token for User without ID.");
        }
        
        if (token == null)
        {
            throw new NullArgumentException("Token");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("UPDATE TOKENS SET token=? WHERE user=?");
            
            statement.setString(1, token);
            statement.setLong(2, user.getId());
            
            statement.executeUpdate();
            
            logger.log(Level.INFO, ("Succesfully set reactivation token for " + user));
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error setting reactivation token for " + user), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    /**
     * Removes the activation token for a user.
     * 
     * @param user The User whose token is to be deleted.
     */
    public void deactivateTokenForUser(User user)
    {
        if (user == null)
        {
            throw new NullArgumentException("User");
        }
        
        if (user.getId() == null)
        {
            throw new IllegalArgumentException("Cannot deactivation token for User without ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("UPDATE TOKENS SET token=NULL WHERE user=?");
            
            statement.setLong(1, user.getId());
            
            statement.executeUpdate();
            
            logger.log(Level.INFO, ("Succesfully deactivated token for " + user));
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error deactivating token for " + user), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    /**
     * Retrieves the activation token of the given User.
     * 
     * @param user The User whose token is coveted.
     * @return The activation token of the specified user.
     *         Null if no token is found for the specified user.
     */
    public String getActivationToken(User user)
    {
        if (user == null)
        {
            throw new NullArgumentException("User");
        }
        
        if (user.getId() == null)
        {
            throw new IllegalArgumentException("Cannot get activation token for User without ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("SELECT * FROM TOKENS WHERE user=? AND token IS NOT NULL");
            
            statement.setLong(1, user.getId());
            
            ResultSet resultSet = statement.executeQuery();
            
            String token = null;
            
            if (resultSet.next())
            {
                token = resultSet.getString("token");
                
                if (resultSet.next())
                {
                    logger.log(Level.WARNING, (user + "has multiple activation tokens specified."));
                }
            }
            else
            {
                logger.log(Level.SEVERE, "Error getting activation token for {0}. No token found for the specified User.", user);
            }
            
            logger.log(Level.INFO, ("Succesfully retrieved activation token for " + user));
                
            return token;
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error getting activation token for " + user), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Saves the user information into the database.
     * 
     * @param information The user information to be saved.
     */
    public void setUserInformation(UserInformation information)
    {
        if (information == null)
        {
            throw new NullArgumentException("Information");
        }
        
        if ((information.getId() == null) || (information.getHidePublicOwned() == null))
        {
            throw new IllegalArgumentException("User Information misses required attributes.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("INSERT INTO INFORMATION (id, hide_public_owned, country, organization) VALUES (?, ?, ?, ?)");
            
            statement.setLong(1, information.getId());
            statement.setBoolean(2, information.getHidePublicOwned());
            statement.setString(3, information.getCountry());
            statement.setString(4, information.getOrganization());
            
            statement.executeUpdate();
            
            logger.log(Level.INFO, ("Succesfully set user information for User ID: " + information.getId()));
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error setting user information for User ID: " + information.getId()), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    /**
     * Updates the user information in the database.
     * 
     * @param information The user information to be updated.
     */
    public void updateUserInformation(UserInformation information)
    {
        if (information == null)
        {
            throw new NullArgumentException("Information");
        }
        
        if ((information.getId() == null) || (information.getHidePublicOwned() == null))
        {
            throw new IllegalArgumentException("User Information misses required attributes.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("UPDATE INFORMATION SET hide_public_owned=?, country=?, organization=? WHERE id=?");
            
            statement.setBoolean(1, information.getHidePublicOwned());
            statement.setString(2, information.getCountry());
            statement.setString(3, information.getOrganization());
            statement.setLong(4, information.getId());
            
            statement.executeUpdate();
            
            logger.log(Level.INFO, ("Succesfully updated user information for User ID: " + information.getId()));
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error updating user information for User ID: " + information.getId()), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    /**
     * Retrieves the user information from the database.
     * 
     * @param id The ID of the user whose information id coveted.
     * @return The user information of the user with the specified ID.
     */
    public UserInformation getUserInformation(Long id)
    {
        if (id == null)
        {
            throw new NullArgumentException("ID");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT * FROM INFORMATION WHERE id=?");
            
            statement.setLong(1, id);
            
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next())
            {
                UserInformation result = getUserInformationFromResultSet(resultSet);
                
                if (resultSet.next())
                {
                    logger.log(Level.SEVERE, "Error selecting information of user ID: {0} from database. More than one entry found.", id);
                    
                    return null;
                }
                
                return result;
            }
            else
            {
                logger.log(Level.SEVERE, "Error selecting information of user ID: {0} from database. No data found.", id);
            }
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting information of user ID: " + id + " from database.", e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Parses the user from the result set.
     * 
     * @param resultSet The result set with the user data.
     * @return The obtained user,
     * @throws SQLException If the reading of the result set fails.
     */
    private User getUserFromResultSet(ResultSet resultSet) throws SQLException
    {
        User user = new User();

        user.setId(resultSet.getLong("id"));
        user.setUsername(resultSet.getString("username"));
        user.setEncryptedPassword(resultSet.getString("password"));
        user.setEmail(resultSet.getString("email"));
        user.setEnabled(resultSet.getBoolean("enabled"));

        return user;
    }
    
    /**
     * Parses the user information from the result set.
     * 
     * @param resultSet The result set with the user information.
     * @return The obtained user information.
     * @throws SQLException If the reading of the result set fails.
     */
    private UserInformation getUserInformationFromResultSet(ResultSet resultSet) throws SQLException
    {
        UserInformation information = new UserInformation();
        
        information.setId(resultSet.getLong("id"));
        information.setCountry(resultSet.getString("country"));
        information.setOrganization(resultSet.getString("organization"));
        information.setHidePublicOwned(resultSet.getBoolean("hide_public_owned"));
        
        return information;
    }
    
    /**
     * Sets access authority for the given user.
     * 
     * @param user The user whose role is to be set.
     * @param role The access role of the specified user.
     */
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
                .prepareStatement("INSERT INTO AUTHORITIES (user, authority) VALUES (?, ?)");
            
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
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
}
