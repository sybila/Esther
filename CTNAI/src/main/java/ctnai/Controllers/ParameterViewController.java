package ctnai.Controllers;

import BehaviourMapper.BehaviourMapper;
import ctnai.Database.CTNAIFile;
import ctnai.Database.FileSystemManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    
    @Autowired
    private FileSystemController fileSystemController;
    
    @RequestMapping(value = "/Parameters/List", method = RequestMethod.GET)
    public String getParameterView(ModelMap model, @RequestParam("file") Long id,
        @RequestParam(value = "filter", required = false) Long filterId)
    {
        if (id == null)
        {
            return null;
        }
        
        model.addAttribute("file", id);
        
        Map<String, String> contextMasks = new HashMap<>();
        
        String[] filter = null;
        
        Map<Integer, Object[]> filterProperties = new HashMap<>();
        
        if (filterId != null)
        {
            model.addAttribute("filter", filterId);
            
            filter = readFilter(filterId);
        }
        
        List<Map<String, Object>> rows = generateRows(id, filter, contextMasks);

        if (filterId != null)
        {
            for (int i = 1; i <= filter.length; i++)
            {
                String[] constraintProperties = filter[(i - 1)].split(";");
                Object[] filterProp = new Object[6];
                
                filterProp[0] = constraintProperties[0];
                
                if (constraintProperties[0].startsWith("K_"))
                {
                    filterProp[1] = contextMasks.get(constraintProperties[0]);
                }
                else
                {
                    filterProp[1] = constraintProperties[0].substring(0, 1).toUpperCase().concat(constraintProperties[0].substring(1));
                }
                filterProp[2] = constraintProperties[1];
                filterProp[3] = translateFilterType(constraintProperties[1]);
                filterProp[4] = constraintProperties[2];
                filterProp[5] = ((constraintProperties[0].equals("robustness") ? "%." : "."));
                
                filterProperties.put(i, filterProp);
            }
            
            model.addAttribute("filter_properties", filterProperties);
        }
        
        model.addAttribute("context_masks", contextMasks);
        model.addAttribute("rows", rows);
        
        return "parameterView";
    }
    
    @RequestMapping(value = "/Parameters/Filter", method = RequestMethod.GET)
    public String filterParameterView(ModelMap model, @RequestParam("source") Long id, @RequestParam("filter") String filter)
    {
        if (id == null)
        {
            return null;
        }
        
        List<Map<String, Object>> rows = generateRows(id, (filter.isEmpty() ? null : filter.split("\n")));
        
        model.addAttribute("rows", rows);
        
        return "parameterList";
    }
    
    @RequestMapping(value = "/BehaviourMap", method = RequestMethod.POST)
    @ResponseBody
    public String generateBehaviourMap(@RequestParam("file") Long sourceId,
        @RequestParam(value = "filter", required = false) Long filterId)
    {
        if (sourceId == null)
        {
            return null;
        }
        
        String[] filter = null;
        
        CTNAIFile source = fileSystemManager.getFileById(sourceId);
        File file = fileSystemManager.getSystemFileById(sourceId);
        
        Long targetId;
        
        if (filterId != null)
        {
            CTNAIFile filterFile = fileSystemManager.getFileById(filterId);
            
            filter = readFilter(filterId);
            
            targetId = Long.parseLong(fileSystemController.createFile(filterFile.getName(), "xgmml", new Long[] { filterId }, false));
        }
        else
        {
            targetId = Long.parseLong(fileSystemController.createFile(source.getName(), "xgmml", new Long[] { source.getId() }, false));
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try
        {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            statement = constructSQLQuery(filter, connection, false);
            resultSet = statement.executeQuery();
            
            File newFile = fileSystemManager.getSystemFileById(targetId);
            
            BehaviourMapper.behaviourMap(resultSet, newFile.getAbsolutePath());
            
            CTNAIFile bmFile = fileSystemManager.getFileById(targetId);
            bmFile.setSize(newFile.getTotalSpace());
            fileSystemManager.updateFile(bmFile);
        }
        catch (ClassNotFoundException | SQLException | ParserConfigurationException | TransformerException e)
        {
            logger.log(Level.SEVERE, ("Error building behaviour map for file ID: " + sourceId), e);
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
                logger.log(Level.SEVERE, ("Error closing connection to parameter database ID: " + sourceId), e);
            }
        }
        
        return null;
    }
    
    private String[] readFilter(Long id)
    {
        File file = fileSystemManager.getSystemFileById(id);
        
        List<String> filter = new ArrayList<>();
        
        BufferedReader br = null;
        
        try
        {
            br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null)
            {
                filter.add(line);
            }
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, ("Error reading filter ID: " + id), e);
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    logger.log(Level.SEVERE, ("Error closing filter ID: " + id), e);
                }
            }
        }
        
        return filter.toArray(new String[] { });
    }
    
    private Character translateFilterType(String type)
    {
        switch (type)
        {
            case "eq": return '=';
            case "gt": return '>';
            case "lt": return '<';
            default: return null;
        }
    }
    
    private PreparedStatement constructSQLQuery(String[] filter, Connection connection, boolean limit) throws SQLException
    {
        StringBuilder queryBuilder = new StringBuilder();
        List<Object> constraintValues = new ArrayList<>();
        List<Integer> doubleValuePositions = new ArrayList<>();
        
        queryBuilder.append("SELECT * FROM PARAMETRIZATIONS");
        
        if ((filter != null) && (filter.length > 0))
        {
            queryBuilder.append(" WHERE");
        
            for (int i = 0; i < filter.length; i++)
            {
                if (i > 0)
                {
                    queryBuilder.append(" AND");
                }
                
                String[] constraintProperties = filter[i].split(";");

                queryBuilder.append(' ');
                queryBuilder.append(constraintProperties[0]);
                
                queryBuilder.append(translateFilterType(constraintProperties[1]));
                
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
    
    private List<Map<String, Object>> generateRows(Long id, String[] filter)
    {
        return generateRows(id, filter, null);
    }
    
    private List<Map<String, Object>> generateRows(Long id, String[] filter, Map<String, String> contextMasks)
    {
        File file = fileSystemManager.getSystemFileById(id);
        
        List<Map<String, Object>> rows = new ArrayList<>();
        
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
                }
            }
            
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
            catch (SQLException e)
            {
                logger.log(Level.SEVERE, ("Error closing connection to parameter database ID: " + id), e);
            }
        }
        
        return rows;
    }
    
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
