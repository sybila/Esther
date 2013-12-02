package mu.fi.sybila.behaviourmapper;

/**
 * Class representing a transition in the state transition graph of the model.
 * 
 * @author George Kolcak
 */
public class Transition
{
    
    private State initialState;
    private State destinationState;
    
    /**
     * Constructor decoding the transition from a string.
     * 
     * @param data The string encoding the transition. The input string should be in the following format:<BR/>
     *             *activity levels of state 1*)>(*activity levels of state 2*<BR/>
     *             Activity levels are a comma separated values.
     */
    public Transition(String data)
    {
        String[] states = data.split("[)]>[(]");
        
        initialState = new State(states[0]);
        destinationState = new State(states[1]);
    }
    
    public State getInitialState()
    {
        return initialState;
    }
    
    public State getDestinationState()
    {
        return destinationState;
    }

    @Override
    public String toString()
    {
        return (initialState + ">" + destinationState);
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((obj == null) || (getClass() != obj.getClass()))
        {
            return false;
        }
        
        final Transition other = (Transition) obj;
        
        return (initialState.equals(other.initialState) && destinationState.equals(other.destinationState));
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = ((11 * hash) + initialState.hashCode());
        hash = ((11 * hash) + destinationState.hashCode());
        return hash;
    }
    
}
