package BehaviourMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Parameter
{
    private int cost;
    private double robustness;
    
    private Set<Transition> transitions;
    
    public Parameter(ResultSet resultSet) throws SQLException
    {
        cost = resultSet.getInt("cost");
        robustness = resultSet.getDouble("robustness");
        
        String witnessPath = resultSet.getString("witness_path");
        
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
