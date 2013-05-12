package mu.fi.sybila.esther.behaviourmapwidget.behaviourmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import mu.fi.sybila.esther.behaviourmapwidget.behaviourmapper.behaviourmap.Map;

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
