package mu.fi.sybila.esther.behaviourmapwidget.behaviourmapper.behaviourmap;

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
import mu.fi.sybila.esther.behaviourmapwidget.behaviourmapper.Parameter;
import mu.fi.sybila.esther.behaviourmapwidget.behaviourmapper.State;
import mu.fi.sybila.esther.behaviourmapwidget.behaviourmapper.Transition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The class representing a behaviour map.
 * 
 * @author George Kolcak
 */
public class Map
{
    
    private int lastId = 0;
    
    /**
     * Returns a new unique identifier for nodes.
     * 
     * @return A unique ID.
     */
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
    
    /**
     * Behaviour map constructor initialising the attributes to default values.
     */
    public Map()
    {
        nodes = new HashMap<>();
        edges = new HashSet<>();
        
        maxInboundTransitionCount = 0;
        maxOutboundTransitionCount = 0;
        maxTransitionCount = 0;
        
        timeSerieLength = 0;
    }
    
    /**
     * Returns the node representing the given state.
     * 
     * @param state The state represented by the coveted node.
     * @return The node representing the specified state.
     *         If no such node is found a new one is created.
     */
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
    
    /**
     * Returns edge between the given Nodes.
     * 
     * @param from The starting node of the edge.
     * @param to The ending node of the edge.
     * @return The edge connecting the specified nodes in correct direction.
     *         If no such edge is found a new one is created.
     */
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
    
    /**
     * Adds a new parameter to the behaviour map.
     * 
     * @param param The parameter to be added.
     */
    public void mapParameter(Parameter param)
    {
        for (Transition t : param.getTransitions())
        {
            mapTransition(t, param);
        }
    }
    
    /**
     * Adds a new transition to the behaviour map.
     * 
     * @param transition The transition to be added.
     * @param param The parameter the transition belongs to.
     */
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
    
    /**
     * Calculates maximal transition counts for nodes and edges in the graph.
     * It is imperative for the method to be called after all of the desired parameters have been added.
     */
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
    
    /**
     * Exports the behaviour map into an xgmml file.
     * This method should only be called after the {@link #finalizeGraph() finalizeGraph} method has been called.
     * 
     * @param fileName Name of the file to save to.
     * @throws ParserConfigurationException if writing to the XML file fails.
     * @throws TransformerException if writing to the XML file fails.
     */
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
