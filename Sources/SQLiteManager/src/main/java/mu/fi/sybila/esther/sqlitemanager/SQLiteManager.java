package mu.fi.sybila.esther.sqlitemanager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mu.fi.sybila.esther.sqlitemanager.parameterfilter.ParameterFilter;

/**
 * Manager for SQLite databases.
 * 
 * @author George Kolcak
 */
public class SQLiteManager
{
    
    /**
     * Obtains the database contents and saves them as a two dimensional list with column names.
     * 
     * @param file The file containing the database.
     * @param filter The filter to use for restraining the data returned.
     * @param contextMasks The map in which names of columns specifying the individual regulatory contexts are to be saved,
     * @return List of rows in form of maps of column name and contents.
     * @throws SQLiteException If reading the database fails.
     */
    public List<Map<Integer, Object>> generateRows(File file, ParameterFilter filter, Map<String, String> contextMasks, Map<Integer, String> columnNames) throws SQLiteException
    {
        List<Map<Integer, Object>> rows = new ArrayList<>();
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
    
        try
        {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            statement = constructSQLQuery(filter, connection, true);
            resultSet = statement.executeQuery();

            ResultSetMetaData tableData = resultSet.getMetaData();

            if (contextMasks != null)
            {
                for (int i = 1; i <= tableData.getColumnCount(); i++)
                {
                    String columnName = tableData.getColumnName(i);
                    if (columnName.startsWith("K"))
                    {
                        contextMasks.put(columnName, transformColumnName(columnName, connection));
                    }
                    
                    columnNames.put(i, transformColumnName(columnName, connection));
                }
            }
            
            while (resultSet.next())
            {
                Map<Integer, Object> cells = new LinkedHashMap<>();

                for (int i = 1; i <= tableData.getColumnCount(); i++)
                {
                    cells.put(i, resultSet.getObject(i));
                }

                rows.add(cells);
            }
        }
        catch (ClassNotFoundException | SQLException e)
        {
            throw new SQLiteException(e);
        }
        finally
        {
            try
            {
                if (resultSet != null)
                {
                    resultSet.close();
                }
                if (statement != null)
                {
                    statement.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            }
            catch (SQLException e)
            {
                throw new SQLiteException(e);
            }
        }
        
        return rows;
    }
    
    /**
     * Constructs a complex SQL query based on the filter.
     * 
     * @param filter The filter to use when restraining the coveted data.
     * @param connection The connection to the database.
     * @param limit Whether to limit the maximal number of rows obtained by 128.
     * @return The Statement with the SQL query.
     * @throws SQLException If construction of the statement fails.
     */
    public PreparedStatement constructSQLQuery(ParameterFilter filter, Connection connection, boolean limit) throws SQLException
    {
        StringBuilder queryBuilder = new StringBuilder();
        List<Object> constraintValues = new ArrayList<>();
        List<Integer> doubleValuePositions = new ArrayList<>();
        
        queryBuilder.append("SELECT * FROM PARAMETRIZATIONS");
        
        if ((filter != null) && (filter.getFilter().length > 0))
        {
            queryBuilder.append(" WHERE");
        
            for (int i = 0; i < filter.getFilter().length; i++)
            {
                if (i > 0)
                {
                    queryBuilder.append(" AND");
                }
                
                String[] constraintProperties = filter.getFilter()[i].split(";");

                queryBuilder.append(' ');
                queryBuilder.append(constraintProperties[0]);
                
                queryBuilder.append(ParameterFilter.translateFilterType(constraintProperties[1]));
                
                queryBuilder.append('?');
                
                Integer value = Integer.parseInt(constraintProperties[2]);
                
                if (constraintProperties[0].equals("robustness"))
                {
                    constraintValues.add(new Double(value / 100.0));
                    doubleValuePositions.add(i);
                }
                else
                {
                    constraintValues.add(value);
                }
            }
        }
        
        if (limit)
        {
            queryBuilder.append(" LIMIT 128");
        }
        
        PreparedStatement statement = connection.prepareStatement(queryBuilder.toString());
        
        for (int i = 0; i < constraintValues.size(); i++)
        {
            if (doubleValuePositions.contains(i))
            {
                statement.setDouble((i + 1), (Double)constraintValues.get(i));
            }
            else
            {
                statement.setInt((i + 1), (Integer)constraintValues.get(i));
            }
        }
        
        return statement;
    }
    
    /**
     * Translates the column name from the system one to one readable by human.
     * 
     * @param columnName The column name to be translated.
     * @param connection The connection to the database used for obtaining additional data.
     * @return The translated column name in comprehendible form.
     * @throws SQLException If reading of the database fails.
     */
    private String transformColumnName(String columnName, Connection connection) throws SQLException
    {
        String[] contextData = columnName.split("_");
        if (!contextData[0].equals("K"))
        {
            if (contextData[0].equals("Witness"))
            {
                return "Witness Path";
            }
            
            return columnName;
        }
        
        StringBuilder nameBuilder = new StringBuilder();
        
        nameBuilder.append(contextData[1]);
        nameBuilder.append('{');
        
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try
        {
            statement = connection.prepareStatement("SELECT * FROM regulations WHERE target=?");
            
            statement.setString(1, contextData[1]);
            
            resultSet = statement.executeQuery();
            
            int i = 0;
            while (resultSet.next())
            {
                if (i > 0)
                {
                    nameBuilder.append(',');
                }
                
                nameBuilder.append(resultSet.getString("Regulator"));
                nameBuilder.append(':');
                nameBuilder.append(contextData[2].charAt(i));
                
                i++;
            }
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
            if (statement != null)
            {
                statement.close();
            }
        }
        
        nameBuilder.append('}');
        
        return nameBuilder.toString();
    }
    
}
