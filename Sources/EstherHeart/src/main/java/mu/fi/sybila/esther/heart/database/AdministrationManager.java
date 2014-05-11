package mu.fi.sybila.esther.heart.database;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.sql.DataSource;
import mu.fi.sybila.esther.heart.database.entities.Post;
import org.apache.commons.lang.NullArgumentException;

public class AdministrationManager
{
    private DataSource dataSource;
    private static final Logger logger = Logger.getLogger(FileSystemManager.class.getName());
    
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
    
    public Long createPost(Post post)
    {
        if (post == null)
        {
            throw new NullArgumentException("Post");
        }
        
        if (post.getId() != null)
        {
            throw new IllegalArgumentException("Post already has an ID.");
        }
        
        if ((post.getContent() == null) || (post.getCreator() == null) ||
                (post.getTitle() == null))
        {
            throw new IllegalArgumentException("Post misses required attributes.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("INSERT INTO POSTS (title, content, creator, date) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getContent());
            statement.setLong(3, post.getCreator());
            statement.setDate(4, new Date((new java.util.Date()).getTime()));
            
            statement.executeUpdate();
            
            Long id = DatabaseUtils.getID(statement.getGeneratedKeys());
            post.setId(id);
            
            logger.log(Level.INFO, ("Succesfully created: " + post));
            
            return id;
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error creating " + post), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    public void removePost(Post post)
    {
        if (post == null)
        {
            throw new NullPointerException("Post");
        }
        
        if (post.getId() == null)
        {
            throw new IllegalArgumentException("Cannot remove Post with NULL ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("DELETE FROM POSTS WHERE id=?;");
            
            statement.setLong(1, post.getId());
            
            statement.executeUpdate();
            
            //tasks.remove(task.getId());
            
            logger.log(Level.INFO, ("Succesfully removed: " + post));
        }
        catch (NullPointerException | SQLException e)
        {
            logger.log(Level.SEVERE, ("Error removing " + post), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    public void updatePost(Post post)
    {
        if (post == null)
        {
            throw new NullArgumentException("Post");
        }
        
        if (post.getId() == null)
        {
            throw new IllegalArgumentException("Post ID is NULL.");
        }
        
        if ((post.getContent() == null) || (post.getCreator() == null) ||
                (post.getTitle() == null))
        {
            throw new IllegalArgumentException("Post misses required attributes.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("UPDATE POSTS SET title=?, content=?, creator=?, date=? WHERE id=?",
                    Statement.RETURN_GENERATED_KEYS);
            
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getContent());
            statement.setLong(3, post.getCreator());
            statement.setDate(4, post.getDate());
            statement.setLong(5, post.getId());
            
            statement.executeUpdate();
            
            logger.log(Level.INFO, ("Succesfully updated: " + post));
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error updating " + post), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    public Post getPostById(Long id)
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
            statement = connection.prepareStatement("SELECT * FROM POSTS WHERE id=?");
            
            statement.setLong(1, id);
            
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next())
            {
                Post result = getPostFromResultSet(resultSet);
                
                if (resultSet.next())
                {
                    logger.log(Level.SEVERE, "Error selecting post ID: {0} from database. Several posts with the same ID found.", id);
                
                    return null;
                }
                
                return result;
            }
            else
            {
                logger.log(Level.SEVERE, "Error selecting post ID: {0} from database. No such post found.", id);
            }
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting post ID: " + id + " from database.", e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    public List<Post> getAllPosts()
    {        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("SELECT * FROM POSTS");
            
            ResultSet resultSet = statement.executeQuery();
            
            return getPostsFromResultSet(resultSet);
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting all posts from the database.", e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    private Post getPostFromResultSet(ResultSet resultSet) throws SQLException
    {
        Post post = new Post();

        post.setId(resultSet.getLong("id"));
        post.setTitle(resultSet.getString("title"));
        post.setContent(resultSet.getString("content"));
        post.setCreator(resultSet.getLong("creator"));
        post.setDate(resultSet.getDate("date"));

        return post;
    }
    
    private List<Post> getPostsFromResultSet(ResultSet resultSet) throws SQLException
    {
        List<Post> resultTasks = new ArrayList<>();
        
        while (resultSet.next())
        {
            resultTasks.add(getPostFromResultSet(resultSet));
        }
        
        return resultTasks;
    }
}
