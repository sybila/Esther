package ctnai.Database;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class DBUtils
{
    private static final Logger logger = Logger.getLogger(DBUtils.class.getName());
    
    public static void closeQuietly(Connection connection, Statement... statements)
    {
        for (Statement statement : statements)
        {
            if (statement != null)
            {
                try
                {
                    statement.close();
                }
                catch (SQLException e)
                {
                    logger.log(Level.SEVERE, ("Error when closing statement: " + e.getLocalizedMessage()));
                }
            }
        }
        if (connection != null)
        {
            try
            {
                connection.close();
            }
            catch (SQLException e)
            {
                logger.log(Level.SEVERE, ("Error when closing connection: " + e.getLocalizedMessage()));
            }
        }
    }

    public static void tryCreateTables(DataSource dataSource) throws SQLException
    {
        try
        {
            createTables(dataSource);
            logger.info("Tables created");
        }
        catch (SQLException e)
        {
            if (!"XOY32".equals(e.getSQLState())) //Derby specific - tables already exists
            {
                throw e;
            }
        }
    }

    public static Long getID(ResultSet keys) throws SQLException
    {
        if (1 != keys.getMetaData().getColumnCount())
        {
            throw new IllegalArgumentException("Given ResultSet contains more columns");
        }
        
        if (keys.next())
        {
            Long result = keys.getLong(1);
            
            if (keys.next())
            {
                throw new IllegalArgumentException("Given ResultSet contains more rows");
            }
            
            return result;
        }
        else
        {
            throw new IllegalArgumentException("Given ResultSet contains no rows");
        }
    }

    private static String[] readDataSourceSqlStatements(URL url)
    {
        try
        {
            char buffer[] = new char[256];
            StringBuilder result = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(url.openStream(), "UTF-8");
            
            int count;
            while ((count = reader.read(buffer)) >= 0)
            {
                result.append(buffer, 0, count);
            }
            
            return result.toString().split(";");  
        }
        catch (IOException e)
        {
            throw new RuntimeException("Cannot read " + url, e);
        }
    }

    public static void createTables(DataSource dataSource) throws SQLException
    {
        executeSqlScript(dataSource, "createTables.sql");
    }

    public static void dropTables(DataSource dataSource) throws SQLException
    {
        executeSqlScript(dataSource, "dropTables.sql");
    }

    private static void executeSqlScript(DataSource dataSource, String scriptName)
    {
        Connection connection = null;
        try
        {
            connection = dataSource.getConnection();
            URL url = DBUtils.class.getResource(scriptName);
            for (String sqlStatement : readDataSourceSqlStatements(url))
            {
                if (!sqlStatement.trim().isEmpty())
                {
                    connection.prepareStatement(sqlStatement).executeUpdate();
                }
            }
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, ("Failed to execute SQL script" + scriptName + ": " + e.getLocalizedMessage()));
        }
        finally
        {
            closeQuietly(connection);
        }
    }
}