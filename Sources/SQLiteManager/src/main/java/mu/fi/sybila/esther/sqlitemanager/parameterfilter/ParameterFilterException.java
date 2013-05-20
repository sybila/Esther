package mu.fi.sybila.esther.sqlitemanager.parameterfilter;

public class ParameterFilterException extends Exception
{

    public ParameterFilterException() { }

    public ParameterFilterException(String message)
    {
        super(message);
    }

    public ParameterFilterException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ParameterFilterException(Throwable cause)
    {
        super(cause);
    }
    
}
