package mu.fi.sybila.esther.heart.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class with methods for smoother database manipulation.
 * 
 * @author George Kolcak
 */
public class DatabaseUtils
{
    private static final Logger logger = Logger.getLogger(DatabaseUtils.class.getName());
    
    /**
     * Closes the connection and statements without firing exceptions.
     * 
     * @param connection The database connection to be closed.
     * @param statements Array of Statements to be closed.
     */
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

    /**
     * Retrieves the ID stored in the result set.
     * 
     * @param keys The result set with the ID.
     * @return The obtained ID.
     * @throws SQLException If the reading of the reading of the result set fails.
     */
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
}
