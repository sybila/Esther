package mu.fi.sybila.behaviourmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Class representing a parametrization of the model.
 * 
 * @author George Kolcak
 */
public class Parameter
{
    
    private int cost;
    private double robustness;
    
    private Set<Transition> transitions;
    
    /**
     * Constructor that parses the parametrization from the SQL result set.
     * 
     * @param resultSet The result set containing the parametrization definition.
     * @throws SQLException if reading of the result set fails.
     */
    public Parameter(ResultSet resultSet, String parentId) throws SQLException
    {
        cost = resultSet.getInt("Cost");
        robustness = resultSet.getDouble("Robust_" + parentId);
        
        String witnessPath = resultSet.getString("Witness_" + parentId);
        
        transitions = new HashSet<>();
        for (String t : witnessPath.subSequence(2, (witnessPath.length() - 2)).toString().split("[)],[(]"))
        {
            transitions.add(new Transition(t));
        }
    }

    public int getCost()
    {
        return cost;
    }

    public double getRobustness()
    {
        return robustness;
    }    
    
    public Set<Transition> getTransitions()
    {
        return Collections.unmodifiableSet(transitions);
    }
    
    /**
     * Determines whether the given transition is the last transition in the model behaviour in case this parametrization is active.
     * 
     * @param transition The transition that is to be checked for being terminal.
     * @return True if the transition is the last transition needed for the parametrised model to exhibit the desired behaviour. False otherwise.
     */
    public boolean isTerminal(Transition transition)
    {
        for (Transition t : getTransitions())
        {
            if (!t.equals(transition) && t.getInitialState().equals(transition.getDestinationState()))
            {
                return false;
            }
        }
        
        return true;
    }
    
}
