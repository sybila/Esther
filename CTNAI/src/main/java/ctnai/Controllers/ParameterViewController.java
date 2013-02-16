package ctnai.Controllers;

import ctnai.Database.FileSystemManager;
import java.io.File;
import java.io.FileOutputStream;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ParameterViewController
{
    private FileSystemManager fileSystemManager = new FileSystemManager();
    public static final Logger logger = Logger.getLogger(FileSystemController.class.getName());
    
    @Resource
    public void setDataSource(DataSource dataSource)
    {
        fileSystemManager.setDataSource(dataSource);
    }
    
    @Value("${data_location}")
    public void setDataLocation(String dataLocation)
    {
        fileSystemManager.setDataLocation(dataLocation);
    }
    
    public void setLogger(FileOutputStream fs)
    {
        logger.addHandler(new StreamHandler(fs, new SimpleFormatter()));
        fileSystemManager.setLogger(fs);
    }
    
    @RequestMapping(value = "/ListParameters", method = RequestMethod.GET)
    public String getParameterView(ModelMap model, @RequestParam("file") Long id)
    {
        if (id == null)
        {
            return null;
        }
        
        File file = fileSystemManager.getSystemFileById(id);
        
        List<Map<String, Object>> rows = new ArrayList<>();
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
    
        try
        {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            statement = connection.prepareStatement("SELECT * FROM parametrizations LIMIT 128");
            resultSet = statement.executeQuery();

            ResultSetMetaData tableData = resultSet.getMetaData();

            while (resultSet.next())
            {
                Map<String, Object> cells = new LinkedHashMap<>();

                for (int i = 1; i <= tableData.getColumnCount(); i++)
                {
                    cells.put(transformColumnName(tableData.getColumnName(i), connection), resultSet.getObject(i));
                }

                rows.add(cells);
            }
        }
        catch (ClassNotFoundException | SQLException e)
        {
            logger.log(Level.SEVERE, ("Error reading parameter database ID: " + id), e);
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
            catch (Exception e)
            {
                logger.log(Level.SEVERE, ("Error closing connection to parameter database ID: " + id), e);
            }
        }

        model.addAttribute("rows", rows);
        
        return "parameterList";
    }
    
    private String transformColumnName(String columnName, Connection connection) throws SQLException
    {
        String[] contextData = columnName.split("_");
        if (!contextData[0].equals("K"))
        {
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
