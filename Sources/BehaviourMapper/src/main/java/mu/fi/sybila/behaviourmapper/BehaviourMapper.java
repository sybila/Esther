package mu.fi.sybila.behaviourmapper;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import mu.fi.sybila.behaviourmapper.behaviourmap.Map;
import mu.fi.sybila.esther.sqlitemanager.SQLiteManager;
import mu.fi.sybila.esther.sqlitemanager.parameterfilter.ParameterFilter;
import mu.fi.sybila.esther.sqlitemanager.parameterfilter.ParameterFilterException;

public class BehaviourMapper 
{
    
    private static SQLiteManager sqliteManager = new SQLiteManager();
    
    public static void main( String[] args )
    {
        List<String> inputFiles = new ArrayList<>();
        List<String> filterFiles = new ArrayList<>();
        String output = null;
        
        String property = null;
        
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].contains("-p") || args[i].equals("--property"))
            {
                if (property != null)
                {
                    System.err.println("! Error: Two parent properties specified.");
                    System.exit(1);
                }
                
                i++;
                property = args[i];
            }
            else if (args[i].endsWith(".xgmml"))
            {
                if (output != null)
                {
                    System.err.println("! Error: Two output files specified.");
                    System.exit(1);
                }
                
                output = args[i];
            }
            else if (args[i].endsWith(".filter"))
            {
                filterFiles.add(args[i]);
            }
            else if (args[i].endsWith(".sqlite"))
            {
                inputFiles.add(args[i]);
            }
            else
            {
                System.err.println("! Error: Invalid Argument specified: " + args[i]);
                System.exit(1);
            }
        }
        
        if (inputFiles.size() <= 0)
        {
            System.err.println("! Error: Not enough arguments. No input file specified.");
            System.exit(1);
        }
        
        if (output == null)
        {
            System.err.println("! Error: Not enough arguments. No output file specified.");
            System.exit(1);
        }
        
        if (property == null)
        {
            System.err.println("! Error: Not enough arguments. Parent property missing.");
            System.exit(1);
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try
        {
            List<ParameterFilter> filters = new ArrayList<>();
            for (String filter : filterFiles)
            {
                filters.add(new ParameterFilter(new File(filter), property));
            }
            
            Class.forName("org.sqlite.JDBC");
            
            Map behaviourMap = new Map();
                
            System.out.println("* Reading input databases...");
                
            List<Parameter> parameters = new ArrayList<>();
            
            for (int i = 0; i < inputFiles.size(); i++)
            {
                connection = DriverManager.getConnection("jdbc:sqlite:" + inputFiles.get(i));
                statement = sqliteManager.constructSQLQuery(filters, connection, false);
                resultSet = statement.executeQuery();
                
                while(resultSet.next())
                {
                    parameters.add(new Parameter(resultSet, property));
                }
                
                System.out.println("# Reading parameters from databases: " + (i + 1) + "/" + inputFiles.size() + ".");
                
                resultSet.close();
                statement.close();
                connection.close();
            }
            
            for (int i = 0; i < parameters.size(); i++)
            {
                behaviourMap.mapParameter(parameters.get(i));
                System.out.println("# Mapping input parameters: " + (i + 1) + "/" + parameters.size() + ".");
            }
            
            System.out.println("* Finalizing graph...");
            behaviourMap.finalizeGraph();
            
            System.out.println("* Saving the output file...");
            behaviourMap.export(output);
        }
        catch (ClassNotFoundException | SQLException | ParserConfigurationException | TransformerException | ParameterFilterException e)
        {
            System.err.println("! Error reading the input databases. " + e.getLocalizedMessage());
            System.exit(2);
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
                System.err.println("! Error reading the input databases. " + e.getLocalizedMessage());
                System.exit(2);
            }
        }
    }
    
}
