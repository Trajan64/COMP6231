package FlightReservationApp;

/**
 * Holder class for : server
 * 
 * @author OpenORB Compiler
 */
final public class serverHolder
        implements org.omg.CORBA.portable.Streamable
{
    /**
     * Internal server value
     */
    public FlightReservationApp.server value;

    /**
     * Default constructor
     */
    public serverHolder()
    { }

    /**
     * Constructor with value initialisation
     * @param initial the initial value
     */
    public serverHolder(FlightReservationApp.server initial)
    {
        value = initial;
    }

    /**
     * Read server from a marshalled stream
     * @param istream the input stream
     */
    public void _read(org.omg.CORBA.portable.InputStream istream)
    {
        value = serverHelper.read(istream);
    }

    /**
     * Write server into a marshalled stream
     * @param ostream the output stream
     */
    public void _write(org.omg.CORBA.portable.OutputStream ostream)
    {
        serverHelper.write(ostream,value);
    }

    /**
     * Return the server TypeCode
     * @return a TypeCode
     */
    public org.omg.CORBA.TypeCode _type()
    {
        return serverHelper.type();
    }

}
