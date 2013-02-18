package BehaviourMapper;

import BehaviourMapper.BehaviourMap.Map;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class BehaviourMapper
{
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