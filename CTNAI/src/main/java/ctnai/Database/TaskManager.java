package ctnai.Database;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.sql.DataSource;

public class TaskManager
{
    private DataSource dataSource;
    private String dataLocation;
    private static final Logger logger = Logger.getLogger(FileSystemManager.class.getName());
    
    //private static final Map<Long, Executor> tasks = new HashMap<>();
    private static final Map<Long, Process> tasks = new HashMap<>();
    
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
    
    public Long createTask(Task task, String[] cmdArgs)
    {
        if (task == null)
        {
            throw new NullPointerException("Task");
        }
        
        if (task.getId() != null)
        {
            throw new IllegalArgumentException("Task already has ID.");
        }
        
        if ((task.getFile() == null) || (task.getOwner() == null) || (task.getActive() == null) ||
            (task.getType() == null) || (task.getResult() == null))
        {
            throw new IllegalArgumentException("Task misses required attributes.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("INSERT INTO TASKS (file, owner, result, type, active, progress, error, information) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            
            statement.setLong(1, task.getFile());
            statement.setLong(2, task.getOwner());
            statement.setLong(3, task.getResult());
            statement.setString(4, task.getType());
            statement.setBoolean(5, task.getActive());
            statement.setInt(6, 0);
            statement.setString(7, null);
            statement.setString(8, null);
            
            statement.executeUpdate();
            
            Long id = DBUtils.getID(statement.getGeneratedKeys());
            task.setId(id);
            
//            Executor executor = new DefaultExecutor();
//            
//            executor.getStreamHandler().setProcessOutputStream(new PipedInputStream());
//            
//            tasks.put(id, executor);
//            task.setExecutor(executor);
//         
//            executor.execute(cmdLine, new DefaultExecuteResultHandler());
            
            Process process = new ProcessBuilder(cmdArgs).start();
            
            tasks.put(id, process);
            
            return id;
        }
        catch(IOException | SQLException e)
        {
            logger.log(Level.SEVERE, ("Error creating " + task), e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    public void removeTask(Task task)
    {
        if (task == null)
        {
            throw new NullPointerException("Task");
        }
        
        if (task.getId() == null)
        {
            throw new IllegalArgumentException("Cannot remove Task with NULL ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("DELETE FROM TASKS WHERE id=?;");
            
            statement.setLong(1, task.getId());
            
            statement.executeUpdate();
            
            tasks.remove(task.getId());
            
            logger.log(Level.INFO, ("Succesfully deleted: " + task));
        }
        catch (NullPointerException | SQLException e)
        {
            logger.log(Level.SEVERE, ("Error deleting " + task), e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
    }
    
    public void updateTask(Task task)
    {
        if (task == null)
        {
            throw new NullPointerException("Task");
        }
        
        if (task.getId() == null)
        {
            throw new IllegalArgumentException("Cannot update Task with NULL ID.");
        }
        
        if ((task.getFile() == null) || (task.getOwner() == null) || (task.getActive() == null) ||
            (task.getType() == null) || (task.getResult() == null))
        {
            throw new IllegalArgumentException("Task misses required attributes.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("UPDATE TASKS SET file=?, owner=?, result=?, type=?, active=?, progress=?, error=?, information=? WHERE id=?");
            
            statement.setLong(1, task.getFile());
            statement.setLong(2, task.getOwner());
            statement.setLong(3, task.getResult());
            statement.setString(4, task.getType());
            statement.setBoolean(5, task.getActive());
            statement.setString(6, task.getProgress());
            statement.setString(7, task.getError());
            statement.setString(8, task.getInformation());
            statement.setLong(9, task.getId());
            
            statement.executeUpdate();
            
            logger.log(Level.INFO, ("Succesfully updated: " + task));
        }
        catch(IOException | SQLException e)
        {
            logger.log(Level.SEVERE, ("Error updating " + task), e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
    }
    
    public Task getTask(Long id)
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
            statement = connection.prepareStatement("SELECT * FROM TASKS WHERE id=?");
            
            statement.setLong(1, id);
            
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next())
            {
                Task result = getTaskFromResultSet(resultSet);
                
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
        catch(SQLException e)
        {
            logger.log(Level.SEVERE, ("Error retrieving task ID: " + id), e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    public List<Task> getTasksRunBy(User owner)
    {
        if (owner == null)
        {
            throw new NullPointerException("Owner");
        }
        
        if (owner.getId() == null)
        {
            throw new IllegalArgumentException("Cannot return Tasks of User with NULL ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("SELECT * FROM TASKS WHERE owner=?");
            
            statement.setLong(1, owner.getId());
            
            ResultSet resultSet = statement.executeQuery();
            
            return getTasksFromResultSet(resultSet);
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Error selecting multiple tasks from the database.", e);
        }
        finally
        {
            DBUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    public int getActiveTaskCount(User user)
    {
        int count = 0;
        
        for (Task task : getTasksRunBy(user))
        {
            if (task.getActive())
            {
                count++;
            }
        }
        
        return count;
    }
    
    private List<Task> getTasksFromResultSet(ResultSet resultSet) throws SQLException
    {
        List<Task> resultTasks = new ArrayList<>();
        
        while (resultSet.next())
        {
            resultTasks.add(getTaskFromResultSet(resultSet));
        }
        
        return resultTasks;
    }
    
    private Task getTaskFromResultSet(ResultSet resultSet) throws SQLException
    {
        Task task = new Task();

        task.setId(resultSet.getLong("id"));
        task.setOwner(resultSet.getLong("owner"));
        task.setFile(resultSet.getLong("file"));
        task.setType(resultSet.getString("type"));
        task.setActive(resultSet.getBoolean("active"));
        task.setResult(resultSet.getLong("result"));
        task.setProgress(resultSet.getString("progress"));
        task.setError(resultSet.getString("error"));
        task.setInformation(resultSet.getString("information"));
        
        //task.setExecutor(tasks.get(task.getId()));
        task.setProcess(tasks.get(task.getId()));
        
        return task;
    }
}
