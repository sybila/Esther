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
                (file.getOwner() == null) || (file.getPublished() == null) ||
                (file.getSize() == null))
        {
            throw new IllegalArgumentException("File misses required attributes.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("INSERT INTO FILES (name, type, owner, public, size) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            
            statement.setString(1, file.getName());
            statement.setString(2, file.getType());
            statement.setLong(3, file.getOwner());
            statement.setBoolean(4, file.getPublished());
            statement.setLong(5, file.getSize());
            
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
                    .prepareStatement("UPDATE FILES SET name=?, type=?, owner=?, public=?, size=? WHERE id=?");
            
            statement.setString(1, file.getName());
            statement.setString(2, file.getType());
            statement.setLong(3, file.getOwner());
            statement.setBoolean(4, file.getPublished());
            statement.setLong(5, file.getSize());
            statement.setLong(6, file.getId());
            
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
                .prepareStatement("SELECT f.* FROM FILES f LEFT OUTER JOIN " +
                    "SPECIFICATIONS s ON f.id = s.child LEFT OUTER JOIN " +
                    "FILES i ON s.parent = i.id LEFT OUTER JOIN " +
                    "EQUIVALENCES e ON s.parent = e.original LEFT OUTER JOIN " +
                    "FILES l ON e.copy = l.id LEFT OUTER JOIN " +
                    "EQUIVALENCES q ON s.parent = q.copy LEFT OUTER JOIN " +
                    "FILES g ON q.original = g.id WHERE (s.child IS NULL OR " +
                    "(i.public = FALSE AND (e.copy IS NULL OR l.public = FALSE) AND " +
                    "(q.original IS NULL OR g.public = FALSE))) AND f.public = TRUE");
            
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
                .prepareStatement("SELECT f.* FROM FILES f LEFT OUTER JOIN " +
                    "SPECIFICATIONS s ON f.id = s.child LEFT OUTER JOIN " +
                    "FILES i ON s.parent = i.id LEFT OUTER JOIN " +
                    "EQUIVALENCES e ON s.parent = e.original LEFT OUTER JOIN " +
                    "FILES l ON e.copy = l.id LEFT OUTER JOIN " +
                    "EQUIVALENCES q ON s.parent = q.copy LEFT OUTER JOIN " +
                    "FILES g ON q.original = g.id WHERE (s.child IS NULL OR " +
                    "(i.owner != ? AND (e.copy IS NULL OR l.owner != ?) AND " +
                    "(q.original IS NULL OR g.owner != ?))) AND f.owner = ?");
            
            statement.setLong(1, ownerId);
            statement.setLong(2, ownerId);
            statement.setLong(3, ownerId);
            statement.setLong(4, ownerId);
            
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
    
    public List<CTNAIFile> getPublicSubfiles(CTNAIFile parent)
    {
        if (parent == null)
        {
            throw new NullPointerException("Parent ID");
        }
        
        if (parent.getId() == null)
        {
            throw new IllegalArgumentException("Cannot find subfiles of file with NULL ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            List<CTNAIFile> equivalents = getEquivalentFiles(parent);
            
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT f.* FROM FILES f JOIN SPECIFICATIONS s ON f.id = s.child WHERE (s.parent = ?");
            
            for (int i = 0; i < equivalents.size(); i++)
            {
                queryBuilder.append(" OR s.parent = ?");
            }
            
            queryBuilder.append(") AND f.public = TRUE");
            
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement(queryBuilder.toString());
            
            statement.setLong(1, parent.getId());
            
            for (int i = 0; i < equivalents.size(); i++)
            {
                statement.setLong((i + 2), equivalents.get(i).getId());
            }
            
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
    
    public List<CTNAIFile> getSubfilesOwnedBy(CTNAIFile parent, Long ownerId)
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
            List<CTNAIFile> equivalents = getEquivalentFiles(parent);
            
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT f.* FROM FILES f JOIN SPECIFICATIONS s ON f.id = s.child WHERE (s.parent = ?");
            
            for (int i = 0; i < equivalents.size(); i++)
            {
                queryBuilder.append(" OR s.parent = ?");
            }
            
            queryBuilder.append(") AND f.owner = ?");
            
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement(queryBuilder.toString());
            
            statement.setLong(1, parent.getId());
            
            for (int i = 0; i < equivalents.size(); i++)
            {
                statement.setLong((i + 2), equivalents.get(i).getId());
            }
            
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
        file.setSize(resultSet.getLong("size"));
        
        return file;
    }
    
    public List<CTNAIFile> getFileParents(CTNAIFile file)
    {
        if (file == null)
        {
            throw new NullPointerException("File");
        }
        
        if (file.getId() == null)
        {
            throw new IllegalArgumentException("Cannot access parent of file with NULL ID.");
        }
        
        List<CTNAIFile> parents = new ArrayList<>();
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT * FROM SPECIFICATIONS where child=?");
            
            statement.setLong(1, file.getId());
            
            ResultSet resultSet = statement.executeQuery();
            
            while (resultSet.next())
            {
                Long parentId = resultSet.getLong("parent");
                CTNAIFile parent = getFileById(parentId);
                parents.add(parent);
            }
            
            return parents;
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error reading parents of " + file), e);
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
    
    public void createEquivalence(CTNAIFile file, CTNAIFile copy)
    {
        if (file == null)
        {
            throw new NullPointerException("File");
        }
        
        if (file.getId() == null)
        {
            throw new IllegalArgumentException("Cannot crate equivalence of file with NULL ID.");
        }
        
        if (copy == null)
        {
            throw new NullPointerException("Copy");
        }
        
        if (copy.getId() == null)
        {
            throw new IllegalArgumentException("File with NULL ID cannot be made equivalent.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("INSERT INTO EQUIVALENCES (original, copy) VALUES (?, ?)");
            
            statement.setLong(1, file.getId());
            statement.setLong(2, copy.getId());
            
            statement.executeUpdate();
            
            logger.log(Level.INFO, ("Successfully created equivalence between files: " + file + " and: " + copy));
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error creating equivalence between files: " + file + " and: " + copy), e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
    }
    
    public void breakEquivalences(CTNAIFile file)
    {
        if (file == null)
        {
            throw new NullPointerException("File");
        }
        
        if (file.getId() == null)
        {
            throw new IllegalArgumentException("Cannot break equivalences of file with NULL ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("DELETE FROM EQUIVALENCES WHERE original=? or copy=?");
            
            statement.setLong(1, file.getId());
            statement.setLong(2, file.getId());
            
            statement.executeUpdate();
            
            logger.log(Level.INFO, ("Successfully breaked equivalences of " + file));
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error breaking equivalences of " + file), e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
    }
    
    public List<CTNAIFile> getEquivalentFiles(CTNAIFile file)
    {
        if (file == null)
        {
            throw new NullPointerException("File");
        }
        
        if (file.getId() == null)
        {
            throw new IllegalArgumentException("Cannot list equivalences of file with NULL ID.");
        }
        
        List<CTNAIFile> equivalents = new ArrayList<>();
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT copy AS id FROM EQUIVALENCES WHERE original=? UNION SELECT original AS id FROM EQUIVALENCES WHERE copy=?");
            
            statement.setLong(1, file.getId());
            statement.setLong(2, file.getId());
            
            ResultSet resultSet = statement.executeQuery();
            
            while (resultSet.next())
            {
                Long id = resultSet.getLong("id");
                CTNAIFile equivalent = getFileById(id);
                equivalents.add(equivalent);
            }
            
            return equivalents;
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error reading equivalences of " + file), e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
}
