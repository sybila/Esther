package mu.fi.sybila.esther.heart.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
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
import mu.fi.sybila.esther.heart.database.entities.EstherFile;
import mu.fi.sybila.esther.heart.database.entities.UserInformation;
import org.apache.commons.lang.NullArgumentException;

/**
 * 
 * 
 * @author jooji
 */
public class FileSystemManager
{
    
    private DataSource dataSource;
    private String dataLocation;
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
     * @param dataLocation The location of the data in the local file system.
     */
    public void setDataLocation(String dataLocation)
    { 
        this.dataLocation = dataLocation;
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
     * Creates a new file in both the database and in the local file system.
     * 
     * @param file The file to be created.
     * @return The ID of the created file.
     *         Null if file creation failed.
     */
    public Long createFile(EstherFile file)
    {
        if (file == null)
        {
            throw new NullPointerException("File");
        }
        
        if (file.getId() != null)
        {
            throw new IllegalArgumentException("File already has ID.");
        }
        
        if ((file.getName() == null) || (file.getType() == null) ||
                (file.getOwner() == null) || (file.getPublished() == null) ||
                (file.getSize() == null) || (file.getBlocked() == null))
        {
            throw new IllegalArgumentException("File misses required attributes.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("INSERT INTO FILES (name, type, owner, public, size, blocked) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            
            statement.setString(1, file.getName());
            statement.setString(2, file.getType());
            statement.setLong(3, file.getOwner());
            statement.setBoolean(4, file.getPublished());
            statement.setLong(5, file.getSize());
            statement.setBoolean(6, file.getBlocked());
            
            statement.executeUpdate();
            
            Long id = DatabaseUtils.getID(statement.getGeneratedKeys());
            file.setId(id);
            
            new File(dataLocation, (id.toString() + '.' + file.getType())).createNewFile();
            
            logger.log(Level.INFO, ("Succesfully created: " + file));
            
            return id;
        }
        catch(IOException | SQLException e)
        {
            logger.log(Level.SEVERE, ("Error creating " + file), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Deletes the file from both the database and the file system.
     * 
     * @param file The file to be deleted.
     */
    public void deleteFile(EstherFile file)
    {
        if (file == null)
        {
            throw new NullPointerException("File");
        }
        
        if (file.getId() == null)
        {
            throw new IllegalArgumentException("Cannot delete file with NULL ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("DELETE FROM FILES WHERE id=?;");
            
            statement.setLong(1, file.getId());
            
            statement.executeUpdate();
            
            new File(dataLocation, (file.getId().toString() + '.' + file.getType())).delete();
            
            logger.log(Level.INFO, ("Succesfully deleted: " + file));
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error deleting " + file), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    /**
     * Updates the file in the database.
     * 
     * @param file The file to be updated.
     */
    public void updateFile(EstherFile file)
    {
        if (file == null)
        {
            throw new NullPointerException("File");
        }
        
        if (file.getId() == null)
        {
            throw new IllegalArgumentException("Cannot update file with NULL ID.");
        }
        
        if ((file.getName() == null) || (file.getType() == null) ||
                (file.getOwner() == null) || (file.getPublished() == null))
        {
            throw new IllegalArgumentException("File misses required attributes.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                    .prepareStatement("UPDATE FILES SET name=?, type=?, owner=?, public=?, size=?, blocked=? WHERE id=?");
            
            statement.setString(1, file.getName());
            statement.setString(2, file.getType());
            statement.setLong(3, file.getOwner());
            statement.setBoolean(4, file.getPublished());
            statement.setLong(5, file.getSize());
            statement.setBoolean(6, file.getBlocked());
            statement.setLong(7, file.getId());
            
            statement.executeUpdate();
            
            logger.log(Level.INFO, ("Succesfully updated: " + file));
        }
        catch(SQLException e)
        {
            logger.log(Level.SEVERE, ("Error updating " + file), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    /**
     * Retrieves the file information from the database.
     * 
     * @param id The ID of the coveted file.
     * @return The file with he specified ID.
     *         Null if no such file is found.
     */
    public EstherFile getFileById(Long id)
    {
        if (id == null)
        {
            throw new NullPointerException("ID");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT * FROM FILES WHERE id=?");
            
            statement.setLong(1, id);
            
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next())
            {
                EstherFile result = getFileFromResultSet(resultSet);
                
                if (resultSet.next())
                {
                    logger.log(Level.SEVERE, "Error selecting file ID: {0} from database. Several Files with the same ID found.", id);
                    return null;
                }
                
                return result;
            }
            else
            {
                logger.log(Level.SEVERE, "Error selecting file ID: {0} from database. No such File found.", id);
            }
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting file ID: " + id + " from database.", e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Retrieve all files in the database.
     * 
     * @return List of all file entries in the database.
     */
    public List<EstherFile> getAllFiles()
    {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT * FROM FILES");
            ResultSet resultSet = statement.executeQuery();
            
            return getFilesFromResultSet(resultSet);
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting multiple files from the database.", e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Retrieve all public files in the database.
     * 
     * @return List of all file entries with public mark set to true in the database.
     */
    public List<EstherFile> getAllPublicFiles()
    {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT * FROM FILES WHERE public = TRUE");
            ResultSet resultSet = statement.executeQuery();
            
            return getFilesFromResultSet(resultSet);
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting multiple files from the database.", e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Returns all the root files that are public.
     * 
     * @param user The preferences of the logged in user.
     * @return List of all files that meet the criteria:<BR/>
     *             Every file must be publicly accessible.<BR/>
     *             Every file must be a root file with no parents.<BR/>
     *             Only files of different users are listed unless user preferences state otherwise.
     */
    public List<EstherFile> getPublicRootFiles(UserInformation user)
    {
        if (user == null)
        {
            throw new NullPointerException("User");
        }
        
        if (user.getId() == null)
        {
            throw new IllegalArgumentException("User information cannot have NULL ID.");
        }
        
        List<EstherFile> publicFiles = getAllPublicFiles();
        List<EstherFile> publicFileRoots = new ArrayList<>();
        
        for (EstherFile file : publicFiles)
        {
            if (user.getHidePublicOwned() && (file.getOwner() == user.getId()))
            {
                continue;
            }
            
            EstherFile root = file;
            EstherFile parent;
            
            while ((parent = getParent(root)) != null)
            {
                root = parent;
            }
            
            if (!publicFileRoots.contains(root))
            {
                publicFileRoots.add(root);
            }
        }
        
//        Connection connection = null;
//        PreparedStatement statement = null;
//        
//        try
//        {
//            StringBuilder queryBuilder = new StringBuilder();
//            
//            queryBuilder.append("SELECT f.* FROM FILES f LEFT OUTER JOIN ");
//            queryBuilder.append("SPECIFICATIONS s JOIN FILES g ON g.id = s.parent AND g.public = TRUE AND g.blocked = FALSE ");
//            queryBuilder.append("ON f.id = s.child WHERE s.child IS NULL AND f.public = TRUE AND f.blocked = FALSE");
//            
//            if (user.getHidePublicOwned())
//            {
//                queryBuilder.append(" AND f.owner != ?");
//            }
//            
//            connection = dataSource.getConnection();
//            statement = connection.prepareStatement(queryBuilder.toString());
//            
//            if (user.getHidePublicOwned())
//            {
//                statement.setLong(1, user.getId());
//            }
//            
//            ResultSet resultSet = statement.executeQuery();
//            
//            return getFilesFromResultSet(resultSet);
//        }
//        catch (SQLException e)
//        {
//            logger.log(Level.SEVERE, "Error selecting multiple files from the database.", e);
//        }
//        finally
//        {
//            DatabaseUtils.closeQuietly(connection, statement);
//        }
        
        return publicFileRoots;
    }
    
    /**
     * Returns all root files owned by the specified user.
     * 
     * @param ownerId The ID of the user whose files are coveted.
     * @return List of all files that meet the criteria:<BR/>
     *             Every file must be owned by the specified user.<BR/>
     *             Every file must a root file with no parents.
     */
    public List<EstherFile> getRootFilesOwnedBy(Long ownerId)
    {
        if (ownerId == null)
        {
            throw new NullPointerException("Owner ID");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("SELECT f.* FROM FILES f LEFT OUTER JOIN " +
                    "SPECIFICATIONS s JOIN FILES g ON g.id = s.parent AND g.owner = ? AND g.blocked = FALSE " +
                    "ON f.id = s.child WHERE s.child IS NULL AND f.owner = ? AND f.blocked = false");
            
            statement.setLong(1, ownerId);
            statement.setLong(2, ownerId);
            
            ResultSet resultSet = statement.executeQuery();
            
            return getFilesFromResultSet(resultSet);
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting multiple files from the database.", e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Returns all subfiles of the specified file.
     * 
     * @param parent The parent file whose subfiles are to be listed.
     * @return List of all subfiles of the specified file.
     */
    public List<EstherFile> getAllSubfiles(EstherFile parent)
    {
        if (parent == null)
        {
            throw new NullArgumentException("Parent");
        }
        
        if (parent.getId() == null)
        {
            throw new IllegalArgumentException("Cannot list subfiles of file with NULL ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("SELECT f.* FROM FILES f JOIN SPECIFICATIONS s ON f.id = s.child WHERE s.parent = ?");
            
            statement.setLong(1, parent.getId());
            
            ResultSet resultSet = statement.executeQuery();
            
            return getFilesFromResultSet(resultSet);
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error selecting subfiles of file: " + parent), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Returns list of all subfiles that are public.
     * 
     * @param parent The parent file whose subfiles are to be listed.
     * @param user The preferences of the logged in user.
     * @return List of all files that meet the criteria:<BR/>
     *             Every file must be publicly available.<BR/>
     *             Every file must be child of the specified parent file.<BR/>
     *             Only files of different users are listed unless user preferences state otherwise.
     */
    public List<EstherFile> getPublicSubfiles(EstherFile parent, UserInformation user)
    {
        if (parent == null)
        {
            throw new NullPointerException("Parent");
        }
        
        if (parent.getId() == null)
        {
            throw new IllegalArgumentException("Cannot find subfiles of file with NULL ID.");
        }
        
        if (user == null)
        {
            throw new NullPointerException("User");
        }
        
        if (user.getId() == null)
        {
            throw new IllegalArgumentException("User information cannot have NULL ID.");
        }
        
        List<EstherFile> subfiles = getAllSubfiles(parent);
        List<EstherFile> publicSubfileRoots = new ArrayList<>();
        
        for (EstherFile file : subfiles)
        {
            if (file.getPublished() && user.getHidePublicOwned() && (file.getOwner() == user.getId()))
            {
                continue;
            }
            
            if (file.getPublished() || !getPublicSubfiles(file, user).isEmpty())
            {
                publicSubfileRoots.add(file);
            }            
        }
        
//        Connection connection = null;
//        PreparedStatement statement = null;
//        
//        try
//        {
//            StringBuilder queryBuilder = new StringBuilder();
//            
//            queryBuilder.append("SELECT f.* FROM FILES f JOIN SPECIFICATIONS s ON f.id = s.child WHERE s.parent = ? AND f.public = TRUE AND f.blocked = FALSE");
//            
//            if (user.getHidePublicOwned())
//            {
//                queryBuilder.append(" AND f.owner != ?");
//            }
//            
//            connection = dataSource.getConnection();
//            statement = connection.prepareStatement(queryBuilder.toString());
//            
//            statement.setLong(1, parent.getId());
//            
//            if (user.getHidePublicOwned())
//            {
//                statement.setLong(2, user.getId());
//            }
//            
//            ResultSet resultSet = statement.executeQuery();
//        }
//        catch (SQLException e)
//        {
//            logger.log(Level.SEVERE, "Error selecting multiple files from the database.", e);
//        }
//        finally
//        {
//            DatabaseUtils.closeQuietly(connection, statement);
//        }
        
        return publicSubfileRoots;
    }
    
    /**
     * Returns all subfiles owned by the specified user.
     * 
     * @param parent The parent file whose subfiles are to be listed.
     * @param ownerId The ID of the user whose files are coveted.
     * @return List of all files that meet the criteria:<BR/>
     *             Every file must be owned by the specified user.<BR/>
     *             Every file must be a child of the specified parent file.
     */
    public List<EstherFile> getSubfilesOwnedBy(EstherFile parent, Long ownerId)
    {
        if (parent == null)
        {
            throw new NullPointerException("Parent ID");
        }
        
        if (parent.getId() == null)
        {
            throw new IllegalArgumentException("Cannot find subfiles of file with NULL ID.");
        }
        
        if (ownerId == null)
        {
            throw new NullPointerException("Owner ID");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("SELECT f.* FROM FILES f JOIN SPECIFICATIONS s ON f.id = s.child WHERE s.parent = ? AND f.owner = ? AND f.blocked = FALSE");
            
            statement.setLong(1, parent.getId());
            statement.setLong(2, ownerId);
            
            ResultSet resultSet = statement.executeQuery();
            
            return getFilesFromResultSet(resultSet);
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting multiple files from the database.", e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Returns parent file of the specified file.
     * 
     * @param file The file whose parent files are coveted.
     * @return The file that has the specified file set as a child.
     */
    public EstherFile getParent(EstherFile file)
    {
        if (file == null)
        {
            throw new NullPointerException("File");
        }
        
        if (file.getId() == null)
        {
            throw new IllegalArgumentException("Cannot access parent of file with NULL ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT * FROM FILES f JOIN SPECIFICATIONS s ON f.id = s.parent where s.child=?");
            
            statement.setLong(1, file.getId());
            
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next())
            {
                EstherFile result = getFileFromResultSet(resultSet);
                
                if (resultSet.next())
                {
                    logger.log(Level.SEVERE, ("Error reading parent of " + file + ". Multiple parents found."));
                    return null;
                }
                
                return result;
            }
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error reading parent of " + file), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Puts two files into a parent-child relationship.
     * 
     * @param file The file to take the position of the child.
     * @param parent The file to take the position of the parent.
     */
    public void setParent(EstherFile file, EstherFile parent)
    {
        if (file == null)
        {
            throw new NullPointerException("File");
        }
        
        if (file.getId() == null)
        {
            throw new IllegalArgumentException("Cannot set parent of file with NULL ID.");
        }
        
        if (parent == null)
        {
            throw new NullPointerException("Parent");
        }
        
        if (parent.getId() == null)
        {
            throw new IllegalArgumentException("Cannot set file with NULL ID as parent.");
        }
        
        if (getParent(file) != null)
        {
            throw new IllegalArgumentException("Parent already set.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("INSERT INTO SPECIFICATIONS (parent, child) VALUES (?, ?)");
            
            statement.setLong(1, parent.getId());
            statement.setLong(2, file.getId());
            
            statement.executeUpdate();
            
            logger.log(Level.INFO, ("Successfully set file ID: " + parent.getId() + " as parent of file ID: " + file.getId()));
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error setting file ID: " + parent.getId() + " as parent of file ID: " + file.getId()), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    public void removeParent(EstherFile file)
    {
        if (file == null)
        {
            throw new NullPointerException("File");
        }
        
        if (file.getId() == null)
        {
            throw new IllegalArgumentException("Cannot remove parent of file with NULL ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("DELETE FROM SPECIFICATIONS WHERE child=?");
            
            statement.setLong(1, file.getId());
            
            statement.executeUpdate();
            
            logger.log(Level.INFO, ("Successfully removed the parent of file ID: " + file.getId()));
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error removing the parent of file ID: " + file.getId()), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    /**
     * Returns the size of all files owned by the specified user.
     * 
     * @param ownerId The user whose files' size is to be counted.
     * @return The sum of sizes of all files owned by the specified user.
     */
    public Long getTotalSize(Long ownerId)
    {
        if (ownerId == null)
        {
            throw new NullPointerException("Owner ID");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("SELECT SUM(size) FROM FILES WHERE owner=?");
            
            statement.setLong(1, ownerId);
            
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next())
            {
                Long result = resultSet.getLong(1);
                
                if (resultSet.next())
                {
                    return null;
                }
                
                return result;
            }
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error counting total size of files of owner ID: " + ownerId), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Returns the file information from the file system.
     * 
     * @param id The ID of the coveted file.
     * @return The system file information of the file with the specified ID.
     */
    public File getSystemFileById(Long id)
    {
        EstherFile result = getFileById(id);
        
        if (result != null)
        {
            return new File(dataLocation, (result.getId() + "." + result.getType()));
        }
        
        return null;
    }
    
    /**
     * Parses the file information from the result set.
     * 
     * @param resultSet The result set with the file information.
     * @return The obtained file.
     * @throws SQLException If the reading of the result set fails.
     */
    private EstherFile getFileFromResultSet(ResultSet resultSet) throws SQLException
    {
        EstherFile file = new EstherFile();

        file.setId(resultSet.getLong("id"));
        file.setName(resultSet.getString("name"));
        file.setType(resultSet.getString("type"));
        file.setOwner(resultSet.getLong("owner"));
        file.setPublished(resultSet.getBoolean("public"));
        file.setSize(resultSet.getLong("size"));
        file.setBlocked(resultSet.getBoolean("blocked"));
        
        return file;
    }
    
    /**
     * Parses the files information from the result set.
     * 
     * @param resultSet The result set with the files information.
     * @return List of the obtained files.
     * @throws SQLException If the reading of the result set fails.
     */
    private List<EstherFile> getFilesFromResultSet(ResultSet resultSet) throws SQLException
    {
        List<EstherFile> files = new ArrayList<>();
        
        while (resultSet.next())
        {
            files.add(getFileFromResultSet(resultSet));
        }
        
        return files;
    }
    
}
