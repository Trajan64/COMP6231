package flightReservationApp;

/** 
 * Helper class for : server
 *  
 * @author OpenORB Compiler
 */ 
public class serverHelper
{
    /**
     * Insert server into an any
     * @param a an any
     * @param t server value
     */
    public static void insert(org.omg.CORBA.Any a, flightReservationApp.server t)
    {
        a.insert_Object(t , type());
    }

    /**
     * Extract server from an any
     *
     * @param a an any
     * @return the extracted server value
     */
    public static flightReservationApp.server extract( org.omg.CORBA.Any a )
    {
        if ( !a.type().equivalent( type() ) )
        {
            throw new org.omg.CORBA.MARSHAL();
        }
        try
        {
            return flightReservationApp.serverHelper.narrow( a.extract_Object() );
        }
        catch ( final org.omg.CORBA.BAD_PARAM e )
        {
            throw new org.omg.CORBA.MARSHAL(e.getMessage());
        }
    }

    //
    // Internal TypeCode value
    //
    private static org.omg.CORBA.TypeCode _tc = null;

    /**
     * Return the server TypeCode
     * @return a TypeCode
     */
    public static org.omg.CORBA.TypeCode type()
    {
        if (_tc == null) {
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();
            _tc = orb.create_interface_tc( id(), "server" );
        }
        return _tc;
    }

    /**
     * Return the server IDL ID
     * @return an ID
     */
    public static String id()
    {
        return _id;
    }

    private final static String _id = "IDL:flightReservationApp/server:1.0";

    /**
     * Read server from a marshalled stream
     * @param istream the input stream
     * @return the readed server value
     */
    public static flightReservationApp.server read(org.omg.CORBA.portable.InputStream istream)
    {
        return(flightReservationApp.server)istream.read_Object(flightReservationApp._serverStub.class);
    }

    /**
     * Write server into a marshalled stream
     * @param ostream the output stream
     * @param value server value
     */
    public static void write(org.omg.CORBA.portable.OutputStream ostream, flightReservationApp.server value)
    {
        ostream.write_Object((org.omg.CORBA.portable.ObjectImpl)value);
    }

    /**
     * Narrow CORBA::Object to server
     * @param obj the CORBA Object
     * @return server Object
     */
    public static server narrow(org.omg.CORBA.Object obj)
    {
        if (obj == null)
            return null;
        if (obj instanceof server)
            return (server)obj;

        if (obj._is_a(id()))
        {
            _serverStub stub = new _serverStub();
            stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
            return stub;
        }

        throw new org.omg.CORBA.BAD_PARAM();
    }

    /**
     * Unchecked Narrow CORBA::Object to server
     * @param obj the CORBA Object
     * @return server Object
     */
    public static server unchecked_narrow(org.omg.CORBA.Object obj)
    {
        if (obj == null)
            return null;
        if (obj instanceof server)
            return (server)obj;

        _serverStub stub = new _serverStub();
        stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
        return stub;

    }

}
