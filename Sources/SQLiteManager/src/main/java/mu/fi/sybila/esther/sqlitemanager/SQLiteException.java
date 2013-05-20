package mu.fi.sybila.esther.sqlitemanager;

import java.sql.SQLException;

public class SQLiteException extends SQLException
{

    public SQLiteException(String reason, String SQLState, int vendorCode)
    {
        super(reason, SQLState, vendorCode);
    }

    public SQLiteException(String reason, String SQLState)
    {
        super(reason, SQLState);
    }

    public SQLiteException(String reason)
    {
        super(reason);
    }

    public SQLiteException() { }

    public SQLiteException(Throwable cause)
    {
        super(cause);
    }

    public SQLiteException(String reason, Throwable cause)
    {
        super(reason, cause);
    }

    public SQLiteException(String reason, String sqlState, Throwable cause)
    {
        super(reason, sqlState, cause);
    }

    public SQLiteException(String reason, String sqlState, int vendorCode, Throwable cause)
    {
        super(reason, sqlState, vendorCode, cause);
    }
    
}
