package BehaviourMapper.BehaviourMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Edge
{
    private Node from;
    private Node to;
    
    private int transitionCount;

    public Edge(Node from, Node to)
    {
        this.from = from;
        this.to = to;
        
        transitionCount = 0;
    }
    
    public Node getFrom()
    {
        return from;
    }
    
    public Node getTo()
    {
        return to;
    }
    
    public void addTransition()
    {
        transitionCount++;
    }

    public int getTransitionCount()
    {
        return transitionCount;
    }

    private double getThickness(int maxTransitionCount)
    {
        return ((10.0 * transitionCount) / maxTransitionCount);
    }
    
    public Element export(Document doc, int maxTransitionCount)
    {
        Element edge = doc.createElement("edge");

        edge.setAttribute("label", from.getId() + " to " + to.getId());
        edge.setAttribute("source", Integer.toString(from.getId()));
        edge.setAttribute("target", Integer.toString(to.getId()));
        
        Element SourceStateAtt = doc.createElement("att");
        SourceStateAtt.setAttribute("type", "string");
        SourceStateAtt.setAttribute("name", "Source");
        SourceStateAtt.setAttribute("value", from.toString());
        edge.appendChild(SourceStateAtt);
        
        Element TargetStateAtt = doc.createElement("att");
        TargetStateAtt.setAttribute("type", "string");
        TargetStateAtt.setAttribute("name", "Target");
        TargetStateAtt.setAttribute("value", to.toString());
        edge.appendChild(TargetStateAtt);

        Element transitionCountAtt = doc.createElement("att");
        transitionCountAtt.setAttribute("type", "integer");
        transitionCountAtt.setAttribute("name", "Transitions");
        transitionCountAtt.setAttribute("value", Integer.toString(transitionCount));
        edge.appendChild(transitionCountAtt);

        Element graphics = doc.createElement("graphics");
        graphics.setAttribute("width", Double.toString(getThickness(maxTransitionCount)));
        graphics.setAttribute("fill", "#000000");
        graphics.setAttribute("cy:targetArrow", "3");
        graphics.setAttribute("cy:targetArrowColor", "#000000");
        edge.appendChild(graphics);

        return edge;
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((obj == null) || (getClass() != obj.getClass()))
        {
            return false;
        }

        final Edge other = (Edge) obj;
        return (from.equals(other.from) && to.equals(other.to));
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = ((53 * hash) + from.hashCode());
        hash = ((53 * hash) + to.hashCode());
        return hash;
    }
}
