package mu.fi.sybila.esther.behaviourmapwidget.behaviourmapper;

public class Transition
{
    
    private State initialState;
    private State destinationState;
    
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
