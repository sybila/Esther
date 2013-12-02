package mu.fi.sybila.esther.heart.database;

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
import mu.fi.sybila.esther.heart.database.entities.Task;
import mu.fi.sybila.esther.heart.database.entities.User;

/**
 * Manager of the Tasks Database.
 * 
 * @author George Kolcak
 */
public class TaskManager
{
    private DataSource dataSource;
    private static final Logger logger = Logger.getLogger(FileSystemManager.class.getName());
    
    //private static final Map<Long, Process> tasks = new HashMap<>();
    
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
    
    private void startTask(Long id, String type, String[] cmdArgs) throws IOException
    {
        Process process = new ProcessBuilder(cmdArgs).start();
        
        class TaskOutputReader implements Runnable
        {
            private Process process;
            
            private Long id;
            private String type;
            
            TaskOutputReader(Long id, String type, Process process)
            {
                this.id= id;
                this.type = type;
                
                this.process = process;
            }

            @Override
            public void run()
            {
                String outputResidue = null;
                Boolean finished = false;

                try
                {    
                    while (!finished)
                    {
                        String error = null;
                        String outputInformation = null;
                        String progress = null;
                        
                        int errorLength = process.getErrorStream().available();
                        int outputLength = process.getInputStream().available();
                        
                        if (errorLength != 0)
                        {
                            byte[] buffer = new byte[errorLength];
                            process.getErrorStream().read(buffer, 0, errorLength);

                            error = new String(buffer).substring(1).trim();
                        }
                        
                        if (outputLength != 0)
                        {
                            byte[] buffer = new byte [outputLength];
                            process.getInputStream().read(buffer, 0, outputLength);

                            String output = "";

                            if (outputResidue != null)
                            {
                                output = outputResidue;
                                outputResidue = null;
                            }

                            output = output.concat(new String(buffer));

                            String[] lines = output.split("[\n\r]");

                            String lastProgressLine = null;

                            for (String line : lines)
                            {
                                if (line.trim().isEmpty())
                                {
                                    continue;
                                }

                                if (!line.trim().endsWith("."))
                                {
                                    outputResidue = line;
                                    break;
                                }

                                String trimmedLine = line.trim().substring(2).trim();

                                if (line.trim().startsWith("*"))
                                {
                                    if (outputInformation == null)
                                    {
                                        outputInformation = trimmedLine;
                                    }
                                    else
                                    {
                                        outputInformation += ("</BR>" + trimmedLine);
                                    }
                                }
                                else if (line.trim().startsWith("#"))
                                {
                                    lastProgressLine = trimmedLine;
                                }
                            }

                            if (lastProgressLine != null)
                            {
                                String[] parts = lastProgressLine.split(":");

                                String operation = parts[0].trim();

                                StringBuilder progressBuilder = new StringBuilder();

                                String[] taskOperations = null;
                                
                                switch (type)
                                {
                                    case "parsybone":
                                    {
                                        taskOperations = Task.PARSYBONE_OPERATIONS;
                                        
                                        break;
                                    }
                                    case "behaviour_mapper":
                                    {
                                        taskOperations = Task.BEHAVIOUR_MAPPER_OPEARTIONS;
                                        
                                        break;
                                    }
                                    default: break;
                                }
                                
                                if (taskOperations != null)
                                {
                                    for (int i = 0; i < taskOperations.length; i++)
                                    {
                                        if (taskOperations[i].equals(operation))
                                        {
                                            progressBuilder.append("[");
                                            progressBuilder.append(i + 1);
                                            progressBuilder.append("/");
                                            progressBuilder.append(taskOperations.length);
                                            progressBuilder.append("] ");

                                            progressBuilder.append(operation);
                                            progressBuilder.append(": ");

                                            break;
                                        }
                                    }
                                }

                                String[] nums = parts[1].trim().split("/");

                                long round = Long.parseLong(nums[0]);
                                long total = Long.parseLong(nums[1].substring(0, (nums[1].length() - 1)));

                                progressBuilder.append((100 * round) / total);
                                progressBuilder.append("%");

                                progress = progressBuilder.toString();
                            }
                        }
                        
                        try
                        {
                            process.exitValue();
                            
                            finished = true;
                        }
                        catch (IllegalThreadStateException e)
                        {
                            finished = false;
                        }

                        Task task = getTask(id);
                        
                        String errorMsg = task.getError();
                        
                        if (errorMsg != null)
                        {
                            if (error == null)
                            {
                                error = errorMsg;
                            }
                            else
                            {
                                error = (errorMsg + error);
                            }
                        }
                            
                        task.setError(error);
                        
                        task.setFinished(finished);
                        
                        if (outputInformation != null)
                        {
                            String info = task.getInformation();
                            if (info != null)
                            {
                                outputInformation = (info + "</BR>" + outputInformation);
                            }

                            task.setInformation(outputInformation);
                        }
                        
                        if (progress != null)
                        {
                            task.setProgress(progress);
                        }
                        
                        updateTask(task);
                        
                        //Thread.sleep(256);
                    }
                }
                catch (/*InterruptedException |*/ IOException e)
                {
                    logger.log(Level.SEVERE, ("Error reading process output for Task ID: " + id), e);
                    
                    Task task = getTask(id);
                    
                    task.setFinished(true);
                    
                    task.setError("Ooops! We're terribily sorry to announce that something went wrong with the execution of this task. Please retry and if the problem persists contact one of the system administrators. Thank you and sorry for the inconvenience caused.");
                    task.setInformation(null);
                    
                    updateTask(task);
                }
                finally
                {
                    process.destroy();
                }
            }
        }
        
        new Thread(new TaskOutputReader(id, type, process)).start();
    }
    
    /**
     * Starts a new Task and stores it in the database.
     * 
     * @param task The Task to be started.
     * @param cmdArgs The arguments for the Task.
     * @return The ID of the new Task.
     *         Null if the Task creation failed.
     */
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
        
        if ((task.getModel() == null) || (task.getProperty() == null) || (task.getOwner() == null) ||
            (task.getActive() == null) || (task.getType() == null) || (task.getResult() == null))
        {
            throw new IllegalArgumentException("Task misses required attributes.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("INSERT INTO TASKS (model, property, owner, result, type, finished, active, progress, error, information) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            
            statement.setLong(1, task.getModel());
            statement.setLong(2, task.getProperty());
            statement.setLong(3, task.getOwner());
            statement.setLong(4, task.getResult());
            statement.setString(5, task.getType());
            statement.setBoolean(6, task.getFinished());
            statement.setBoolean(7, task.getActive());
            statement.setString(8, "0%");
            statement.setString(9, null);
            statement.setString(10, null);
            
            statement.executeUpdate();
            
            Long id = DatabaseUtils.getID(statement.getGeneratedKeys());
            task.setId(id);
            
            if (task.getDatabases().size() > 0)
            {
                addTaskDatabases(task);
            }
            
            if (task.getFilters().size() > 0)
            {
                addTaskFilters(task);
            }
            
//            Process process = new ProcessBuilder(cmdArgs).start();
            
//            tasks.put(id, process);
            
            logger.log(Level.INFO, "Successfully created {0}", task);
            
            startTask(id, task.getType(), cmdArgs);
            
            return id;
        }
        catch(IOException | SQLException e)
        {
            logger.log(Level.SEVERE, ("Error creating " + task), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Removes the Task from the database.
     * 
     * @param task The Task to be removed.
     */
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
            
            //tasks.remove(task.getId());
            
            logger.log(Level.INFO, ("Succesfully deleted: " + task));
        }
        catch (NullPointerException | SQLException e)
        {
            logger.log(Level.SEVERE, ("Error deleting " + task), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    /**
     * Updates the given Task.
     * 
     * @param task The Task to be updated.
     */
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
        
        if ((task.getModel() == null) || (task.getProperty() == null) || (task.getOwner() == null) ||
            (task.getActive() == null) || (task.getType() == null) || (task.getResult() == null))
        {
            throw new IllegalArgumentException("Task misses required attributes.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection
                .prepareStatement("UPDATE TASKS SET model=?, property=?, owner=?, result=?, type=?, finished=?, active=?, progress=?, error=?, information=? WHERE id=?");
            
            statement.setLong(1, task.getModel());
            statement.setLong(2, task.getProperty());
            statement.setLong(3, task.getOwner());
            statement.setLong(4, task.getResult());
            statement.setString(5, task.getType());
            statement.setBoolean(6, task.getFinished());
            statement.setBoolean(7, task.getActive());
            statement.setString(8, task.getProgress());
            statement.setString(9, task.getError());
            statement.setString(10, task.getInformation());
            statement.setLong(11, task.getId());
            
            statement.executeUpdate();
            
            logger.log(Level.INFO, ("Succesfully updated: " + task));
        }
        catch(IOException | SQLException e)
        {
            logger.log(Level.SEVERE, ("Error updating " + task), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    /**
     * Retrieves the Task from the database.
     * 
     * @param id The ID of the coveted Task.
     * @return The Task with the specified ID.
     *         Null id no such Task is found.
     */
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
                    logger.log(Level.SEVERE, "Error retrieving task ID: {0}. Several Tasks with the same ID found.", id);
                    return null;
                }
                
                return result;
            }
            else
            {
                logger.log(Level.SEVERE, "Error retrieving task ID: {0}. No such Task found.", id);
            }
        }
        catch(SQLException e)
        {
            logger.log(Level.SEVERE, ("Error retrieving task ID: " + id), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    /**
     * Returns all Tasks of the given user.
     * 
     * @param owner The User whose Tasks are to be retrieved.
     * @return List of Tasks run by the User specified.
     */
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
            DatabaseUtils.closeQuietly(connection, statement);
        }
        
        return null;
    }
    
    private void addTaskDatabases(Task task)
    {
        if (task == null)
        {
            throw new NullPointerException("Task");
        }
        
        if (task.getId() == null)
        {
            throw new IllegalArgumentException("Cannot add databases for Task with NULL ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            
            for (Long id : task.getDatabases())
            {
                statement = connection.prepareStatement("INSERT INTO TASK_DBS (task, sqlitedb) VALUES (?, ?)");

                statement.setLong(1, task.getId());
                statement.setLong(2, id);
                
                statement.execute();
                
                statement.close();
            }
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error adding databases for " + task), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    private void removeTaskDatabases(Task task)
    {
        if (task == null)
        {
            throw new NullPointerException("Task");
        }
        
        if (task.getId() == null)
        {
            throw new IllegalArgumentException("Cannot remove databases for Task with NULL ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("DELETE FROM TASK_DBS WHERE task=?");

            statement.setLong(1, task.getId());

            statement.execute();
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error removing databases for " + task), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    public void updateTaskDatabases(Task task)
    {
        removeTaskDatabases(task);
        
        addTaskDatabases(task);
    }
    
    private void getTaskDatabases(Task task)
    {
        if (task == null)
        {
            throw new NullPointerException("Task");
        }
        
        if (task.getId() == null)
        {
            throw new IllegalArgumentException("Cannot get databases for Task with NULL ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT *  FROM TASK_DBS WHERE task=?");

            statement.setLong(1, task.getId());
            
            resultSet = statement.executeQuery();
            
            getTaskDatabasesFromResultSet(task, resultSet);
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error retrieving databases for " + task), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    private void addTaskFilters(Task task)
    {
        if (task == null)
        {
            throw new NullPointerException("Task");
        }
        
        if (task.getId() == null)
        {
            throw new IllegalArgumentException("Cannot add filters for Task with NULL ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            
            for (Long id : task.getFilters())
            {
                statement = connection.prepareStatement("INSERT INTO TASK_FILTERS (task, filter) VALUES (?, ?)");

                statement.setLong(1, task.getId());
                statement.setLong(2, id);
                
                statement.execute();
                
                statement.close();
            }
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error adding filters for " + task), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    private void removeTaskFilters(Task task)
    {
        if (task == null)
        {
            throw new NullPointerException("Task");
        }
        
        if (task.getId() == null)
        {
            throw new IllegalArgumentException("Cannot remove filters for Task with NULL ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("DELETE FROM TASK_FILTERS WHERE task=?");

            statement.setLong(1, task.getId());

            statement.execute();
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error removing filters for " + task), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    public void updateTaskFilters(Task task)
    {
        removeTaskFilters(task);
        
        addTaskFilters(task);
    }
    
    private void getTaskFilters(Task task)
    {
        if (task == null)
        {
            throw new NullPointerException("Task");
        }
        
        if (task.getId() == null)
        {
            throw new IllegalArgumentException("Cannot get filters for Task with NULL ID.");
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet;
        
        try
        {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement("SELECT *  FROM TASK_FILTERS WHERE task=?");

            statement.setLong(1, task.getId());
            
            resultSet = statement.executeQuery();
            
            getTaskFiltersFromResultSet(task, resultSet);
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Error retrieving filters for " + task), e);
        }
        finally
        {
            DatabaseUtils.closeQuietly(connection, statement);
        }
    }
    
    /**
     * Parses the Task from the result set.
     * 
     * @param resultSet The result set containing the task information.
     * @return The Task obtained.
     * @throws SQLException If the reading of the result set fails.
     */
    private Task getTaskFromResultSet(ResultSet resultSet) throws SQLException
    {
        Task task = new Task();

        task.setId(resultSet.getLong("id"));
        task.setOwner(resultSet.getLong("owner"));
        task.setModel(resultSet.getLong("model"));
        task.setProperty(resultSet.getLong("property"));
        task.setType(resultSet.getString("type"));
        task.setFinished(resultSet.getBoolean("finished"));
        task.setActive(resultSet.getBoolean("active"));
        task.setResult(resultSet.getLong("result"));
        task.setProgress(resultSet.getString("progress"));
        task.setError(resultSet.getString("error"));
        task.setInformation(resultSet.getString("information"));
                
        getTaskDatabases(task);
        getTaskFilters(task);
        
        //task.setProcess(tasks.get(task.getId()));
        
        return task;
    }
    
    private void getTaskDatabasesFromResultSet(Task task, ResultSet resultSet) throws SQLException
    {
        while (resultSet.next())
        {
            task.addDatabase(resultSet.getLong("sqlitedb"));
        }
    }
    
    private void getTaskFiltersFromResultSet(Task task, ResultSet resultSet) throws SQLException
    {
        while (resultSet.next())
        {
            task.addFilter(resultSet.getLong("filter"));
        }
    }
    
    /**
     * returns all the Tasks in the result set.
     * 
     * @param resultSet The result set with tasks information.
     * @return List of the Tasks obtained.
     * @throws SQLException If the reading of the result set fails.
     */
    private List<Task> getTasksFromResultSet(ResultSet resultSet) throws SQLException
    {
        List<Task> resultTasks = new ArrayList<>();
        
        while (resultSet.next())
        {
            resultTasks.add(getTaskFromResultSet(resultSet));
        }
        
        return resultTasks;
    }
    
    /**
     * Returns the count of all the active tasks of a user.
     * 
     * @param user The User whose Tasks are to be counted.
     * @return The count of all active Tasks run by the specified user.
     */
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
}
