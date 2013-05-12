package mu.fi.sybila.esther.behaviourmapwidget.behaviourmapper.behaviourmap;

import mu.fi.sybila.esther.behaviourmapwidget.behaviourmapper.State;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Node
{
    
    private int id;
        
    private State state;
    
    private int measurement;
    
    private int inboundTransitionCount;
    private int outboundTransitionCount;
    
    public Node(int id, State state)
    {
        this.id = id;

        this.state = state;
        
        measurement = 0;
        
        inboundTransitionCount = 0;
        outboundTransitionCount = 0;
    }
    
    public int getId()
    {
        return id;
    }
    
    public int getMeasurement()
    {
        return measurement;
    }
    
    public void setMeasurement(int measurement)
    {
        this.measurement = measurement;
    }
    
    public void addInboundTransition()
    {
        inboundTransitionCount++;
    }
    
    public void addOutboundTransition()
    {
        outboundTransitionCount++;
    }

    public int getInboundTransitionCount()
    {
        return inboundTransitionCount;
    }

    public int getOutboundTransitionCount()
    {
        return outboundTransitionCount;
    }
    
    private String computeColour(int timeSerieLength)
    {
        if (measurement == 0)
        {
            return "#cccccc";
        }
        else
        {
            int red = (int)(16 + (176 * ((measurement - 1.0) / (timeSerieLength - 1))));
            int gb = (int)(128 + (112 * ((measurement - 1.0) / (timeSerieLength - 1))));

            return String.format("#%02x%02x%02x", red, gb, gb);
        }
    }
    
    public Element export(Document doc, int maxInboundTransitionCount, int maxOutboundTransitionCount, int timeSerieLength)
    {
        Element node = doc.createElement("node");
        node.setAttribute("label", state.toString());
        node.setAttribute("id", Integer.toString(id));

        Element stateAtt = doc.createElement("att");
        stateAtt.setAttribute("type", "string");
        stateAtt.setAttribute("name", "State");
        stateAtt.setAttribute("value", state.toString());
        node.appendChild(stateAtt);

        Element inboundTransitionCountAtt = doc.createElement("att");
        inboundTransitionCountAtt.setAttribute("type", "integer");
        inboundTransitionCountAtt.setAttribute("name", "Inbound Transitions");
        inboundTransitionCountAtt.setAttribute("value", Integer.toString(inboundTransitionCount));
        node.appendChild(inboundTransitionCountAtt);
        
        Element outboundTransitionCountAtt = doc.createElement("att");
        outboundTransitionCountAtt.setAttribute("type", "integer");
        outboundTransitionCountAtt.setAttribute("name", "Outbound Transitions");
        outboundTransitionCountAtt.setAttribute("value", Integer.toString(outboundTransitionCount));
        node.appendChild(outboundTransitionCountAtt);

        Element measurementAtt = doc.createElement("att");
        measurementAtt.setAttribute("type", "integer");
        measurementAtt.setAttribute("name", "Measurement");
        measurementAtt.setAttribute("value", Integer.toString(measurement));
        node.appendChild(measurementAtt);

        Element graphics = doc.createElement("graphics");
        graphics.setAttribute("type", "ELLIPSE");
        graphics.setAttribute("h", "64.0");
        graphics.setAttribute("w", "64.0");
        graphics.setAttribute("x", Integer.toString((state.getBuchiAutomatonState() * 100)));
        graphics.setAttribute("y", Integer.toString(0));
        graphics.setAttribute("fill", computeColour(timeSerieLength));
        graphics.setAttribute("width", "1");
        graphics.setAttribute("outline", "#000000");
        graphics.setAttribute("cy:nodeLabel", state.toString());
        node.appendChild(graphics);

        return node;
    }

    @Override
    public String toString()
    {
        return state.toString();
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if ((obj == null) || (getClass() != obj.getClass()))
        {
            return false;
        }

        final Node other = (Node) obj;
        return (this.id == other.id);
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = ((97 * hash) + this.id);
        return hash;
    }
    
}
