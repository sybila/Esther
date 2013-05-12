package mu.fi.sybila.esther.sqlitemanager.parameterfilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParameterFilter
{

    private List<String> filter;
    
    public ParameterFilter(String filter)
    {
        this.filter = new ArrayList<>();
        
        for (String s : filter.split("\n"))
        {
            this.filter.add(s);
        }
    }
    
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
    
    public String[] getFilter()
    {
        return filter.toArray(new String[] { });
    }
    
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
