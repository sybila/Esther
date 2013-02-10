package ctnai.Database;

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

public class FileSystemManager
{
    private DataSource dataSource;
    private String dataLocation;
    public static final Logger logger = Logger.getLogger(FileSystemManager.class.getName());
    
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }
    
    public void setDataLocation(String dataLocation)
    { 
        this.dataLocation = dataLocation;
    }
    
    public void setLogger(FileOutputStream fs)
    {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
    }
    
    public Long createFile(CTNAIFile file)
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
                .prepareStatement("INSERT INTO FILES (name, type, owner, public) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            
            statement.setString(1, file.getName());
            statement.setString(2, file.getType());
            statement.setLong(3, file.getOwner());
            statement.setBoolean(4, file.getPublished());
            
            statement.executeUpdate();
            
            Long id = DBUtils.getID(statement.getGeneratedKeys());
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
            DBUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    public void deleteFile(CTNAIFile file)
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
            for (CTNAIFile child : getAllSubfiles(file.getId()))
            {
                deleteFile(child);
            }
            
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
            DBUtils.closeQuietly(connection, statement);
        }
    }
    
    public void updateFile(CTNAIFile file)
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
                    .prepareStatement("UPDATE FILES SET name=?, type=?, owner=?, public=? WHERE id=?");
            
            statement.setString(1, file.getName());
            statement.setString(2, file.getType());
            statement.setLong(3, file.getOwner());
            statement.setBoolean(4, file.getPublished());
            statement.setLong(5, file.getId());
            
            statement.executeUpdate();
            
            logger.log(Level.INFO, ("Succesfully updated: " + file));
        }
        catch(SQLException e)
        {
            logger.log(Level.SEVERE, ("Error updating " + file), e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
    }
    
    public CTNAIFile getFileById(Long id)
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
                CTNAIFile result = getFileFromResultSet(resultSet);
                
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
            logger.log(Level.SEVERE, "Error selecting file ID: " + id + " from database.", e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    public List<CTNAIFile> getAllFiles()
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
            DBUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    public List<CTNAIFile> getPublicRootFiles()
    {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("SELECT f.* FROM FILES f LEFT OUTER JOIN SPECIFICATIONS s ON f.id = s.child WHERE s.child IS NULL AND f.public = TRUE");
            
            ResultSet resultSet = statement.executeQuery();
            
            return getFilesFromResultSet(resultSet);
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting multiple files from the database.", e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    public List<CTNAIFile> getRootFilesOwnedBy(Long ownerId)
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
                .prepareStatement("SELECT f.* FROM FILES f LEFT OUTER JOIN SPECIFICATIONS s ON f.id = s.child WHERE s.child IS NULL AND f.owner = ?");
            
            statement.setLong(1, ownerId);
            
            ResultSet resultSet = statement.executeQuery();
            
            return getFilesFromResultSet(resultSet);
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting multiple files from the database.", e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    public List<CTNAIFile> getAllSubfiles(Long parentId)
    {
        if (parentId == null)
        {
            throw new NullPointerException("Parent ID");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("SELECT f.* FROM FILES f JOIN SPECIFICATIONS s ON f.id = s.child WHERE s.parent = ?");
            
            statement.setLong(1, parentId);
            
            ResultSet resultSet = statement.executeQuery();
            
            return getFilesFromResultSet(resultSet);
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting multiple files from the database.", e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    public List<CTNAIFile> getPublicSubfiles(Long parentId)
    {
        if (parentId == null)
        {
            throw new NullPointerException("Parent ID");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("SELECT f.* FROM FILES f JOIN SPECIFICATIONS s ON f.id = s.child WHERE s.parent = ? AND f.public = TRUE");
            
            statement.setLong(1, parentId);
            
            ResultSet resultSet = statement.executeQuery();
            
            return getFilesFromResultSet(resultSet);
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting multiple files from the database.", e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    public List<CTNAIFile> getSubfilesOwnedBy(Long parentId, Long ownerId)
    {
        if (parentId == null)
        {
            throw new NullPointerException("Parent ID");
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
                .prepareStatement("SELECT f.* FROM FILES f JOIN SPECIFICATIONS s ON f.id = s.child WHERE s.parent = ? AND f.owner = ?");
            
            statement.setLong(1, parentId);
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
            DBUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    public File getSystemFileById(Long id)
    {
        CTNAIFile result = getFileById(id);
        
        if (result != null)
        {
            return new File(dataLocation, (result.getId() + "." + result.getType()));
        }
        
        return null;
    }
    
    private List<CTNAIFile> getFilesFromResultSet(ResultSet resultSet) throws SQLException
    {
        List<CTNAIFile> files = new ArrayList<>();
        
        while (resultSet.next())
        {
            files.add(getFileFromResultSet(resultSet));
        }
        
        return files;
    }
    
    private CTNAIFile getFileFromResultSet(ResultSet resultSet) throws SQLException
    {
        CTNAIFile file = new CTNAIFile();

        file.setId(resultSet.getLong("id"));
        file.setName(resultSet.getString("name"));
        file.setType(resultSet.getString("type"));
        file.setOwner(resultSet.getLong("owner"));
        file.setPublished(resultSet.getBoolean("public"));
        
        return file;
    }
    
    public CTNAIFile getFileParent(CTNAIFile file)
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
            statement = connection.prepareStatement("SELECT * FROM SPECIFICATIONS where child=?");
            
            statement.setLong(1, file.getId());
            
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next())
            {
                Long parentId = resultSet.getLong("parent");
                
                if (resultSet.next())
                {
                    logger.log(Level.WARNING, ("Multiple parents specified for file ID: " + file.getId()));
                }
                
                return getFileById(parentId);
            }
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error reading parent of file ID: " + file.getId()), e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    public void setParent(CTNAIFile file, CTNAIFile parent)
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
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("INSERT INTO SPECIFICATIONS (parent, child) VALUES (?, ?)");
            
            statement.setLong(1, parent.getId());
            statement.setLong(2, file.getId());
            
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error setting file ID: " + parent.getId() + " as parent of file ID: " + file.getId()), e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
    }
}
