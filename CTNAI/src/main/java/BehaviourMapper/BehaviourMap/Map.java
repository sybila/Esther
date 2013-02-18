package BehaviourMapper.BehaviourMap;

import BehaviourMapper.Parameter;
import BehaviourMapper.State;
import BehaviourMapper.Transition;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Map
{
    private int lastId = 0;
    private int getId()
    {
        lastId--;
        return lastId;
    }

    private java.util.Map<State, Node> nodes;
    private Set<Edge> edges;

    private int maxInboundTransitionCount;
    private int maxOutboundTransitionCount;
    private int maxTransitionCount;

    private int timeSerieLength;
    
    public Map()
    {
        nodes = new HashMap<>();
        edges = new HashSet<>();
        
        maxInboundTransitionCount = 0;
        maxOutboundTransitionCount = 0;
        maxTransitionCount = 0;
        
        timeSerieLength = 0;
    }
    
    private Node getNode(State state)
    {
        if (nodes.containsKey(state))
        {
            return nodes.get(state);
        }
        
        Node node = new Node(getId(), state);
        nodes.put(state, node);
        return node;
    }
    
    private Edge getEdge(Node from, Node to)
    {
        for (Edge e : edges)
        {
            if (e.getFrom().equals(from) && e.getTo().equals(to))
            {
                return e;
            }
        }
        
        Edge edge = new Edge(from, to);
        edges.add(edge);
        
        return edge;
    }
    
    public void mapParameter(Parameter param)
    {
        for (Transition t : param.getTransitions())
        {
            mapTransition(t, param);
        }
    }
    
    private void mapTransition(Transition transition, Parameter param)
    {
        timeSerieLength = Math.max(timeSerieLength, (transition.getDestinationState().getBuchiAutomatonState() + 1));
        
        Node initialNode = getNode(transition.getInitialState());
        Node destinationNode = getNode(transition.getDestinationState());
        
        if ((initialNode.getMeasurement() == 0) &&
                (transition.getInitialState().getBuchiAutomatonState() !=
                    transition.getDestinationState().getBuchiAutomatonState()))
        {
            initialNode.setMeasurement(transition.getDestinationState().getBuchiAutomatonState());
        }
        
        if (param.isTerminal(transition))
        {
            destinationNode.setMeasurement(transition.getDestinationState().getBuchiAutomatonState() + 1);
        }
        
        Edge edge = getEdge(initialNode, destinationNode);
        
        initialNode.addOutboundTransition();
        destinationNode.addInboundTransition();
        edge.addTransition();
    }
    
    public void finalizeGraph()
    {
        for (Node n : nodes.values())
        {
            maxInboundTransitionCount = Math.max(maxInboundTransitionCount, n.getInboundTransitionCount());
            maxOutboundTransitionCount = Math.max(maxOutboundTransitionCount, n.getOutboundTransitionCount());
        }
        
        for (Edge e : edges)
        {
            maxTransitionCount = Math.max(maxTransitionCount, e.getTransitionCount());
        }
    }
    
    public void export(String fileName) throws ParserConfigurationException, TransformerException
    {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        doc.setXmlStandalone(true);

        Element root = doc.createElement("graph");
        root.setAttribute("label", "Witness Map");
        root.setAttribute("directed", "1");
        root.setAttribute("xmlns:cy", "http://www.cytoscape.org");
        root.setAttribute("xmlns", "http://www.cs.rpi.edu/XGMML");

        doc.appendChild(root);

        Element docVersion = doc.createElement("att");
        docVersion.setAttribute("name", "DocumentVersion");
        docVersion.setAttribute("value", "1.1");
        root.appendChild(docVersion);

        Element backgroundColour = doc.createElement("att");
        backgroundColour.setAttribute("type", "string");
        backgroundColour.setAttribute("name", "backgroundColor");
        backgroundColour.setAttribute("value", "#ffffff");
        root.appendChild(backgroundColour);
        
        for (Node n : nodes.values())
        {
            root.appendChild(n.export(doc, maxInboundTransitionCount, maxOutboundTransitionCount, timeSerieLength));
        }

        for (Edge e : edges)
        {
            root.appendChild(e.export(doc, maxTransitionCount));
        }
            
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result =  new StreamResult(fileName);
        transformer.transform(source, result);
    }
}
