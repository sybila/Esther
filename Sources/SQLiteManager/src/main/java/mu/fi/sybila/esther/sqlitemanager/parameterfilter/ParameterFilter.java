package mu.fi.sybila.esther.sqlitemanager.parameterfilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class representing a filter of the parameters.
 * 
 * @author George Kolcak
 */
public class ParameterFilter
{

    private List<String> filter;
    
    /**
     * Constructor of parameter filter from a string containing a list of parameter constraints.
     * Every constraint has to be on a separate line, i.e. separated by '\n'.
     * 
     * @param filter The string with the constraint definitions.
     */
    public ParameterFilter(String filter)
    {
        this.filter = new ArrayList<>();
        
        this.filter.addAll(Arrays.asList(filter.split("\n")));
    }
    
    /**
     * Constructor of parameter filter from a file containing a list of parameter constraints.
     * Every constraint has to be on a separate line.
     * 
     * @param file The filter file with the constraint definitions.
     * @throws ParameterFilterException If reading of the file fails.
     */
    public ParameterFilter(File file) throws ParameterFilterException
    {
        filter = new ArrayList<>();
        
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
            throw new ParameterFilterException(e);
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
                    throw new ParameterFilterException(e);
                }
            }
        }
    }
    
    /**
     * Returns the filter constraints as individual strings.
     * 
     * @return The array of parameter constraints specified in the filter.
     */
    public String[] getFilter()
    {
        return filter.toArray(new String[] { });
    }
    
    /**
     * Translates the operation encoded into the constraint definition to a symbol used in SQL queries.
     * 
     * @param type The encoded operation.
     * @return The character representing the operation.
     */
    public static Character translateFilterType(String type)
    {
        switch (type)
        {
            case "eq": return '=';
            case "gt": return '>';
            case "lt": return '<';
            default: return null;
        }
    }
    
}
