package mu.fi.sybila.esther.behaviourmapwidget.behaviourmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import mu.fi.sybila.esther.behaviourmapwidget.behaviourmapper.behaviourmap.Map;

/**
 * Class for initiating behaviour map creation.
 * 
 * @author George Kolcak
 */
public class BehaviourMapper
{
    
    /**
     * Static method that creates a behaviour map.
     * 
     * @param input The result set with the data to be mapped.
     * @param outputPath The path to the output xgmml file.
     * @throws SQLException If reading of the result set fails.
     * @throws ParserConfigurationException If writing the output XML fails.
     * @throws TransformerException If writing the output XML fails.
     */
    public static void behaviourMap(ResultSet input, String outputPath) throws SQLException,
            ParserConfigurationException, TransformerException
    {
        Map behaviourMap = new Map();
        
        while (input.next())
        {
            behaviourMap.mapParameter(new Parameter(input));
        }
        
        behaviourMap.finalizeGraph();
        behaviourMap.export(outputPath);
    }
    
}
