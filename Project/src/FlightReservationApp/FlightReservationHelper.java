package FlightReservationApp;


/**
* FlightReservationApp/FlightReservationHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from FlightReservation.idl
* Saturday, December 3, 2016 4:38:00 AM EST
*/

abstract public class FlightReservationHelper
{
  private static String  _id = "IDL:FlightReservationApp/FlightReservation:1.0";

  public static void insert (org.omg.CORBA.Any a, FlightReservationApp.FlightReservation that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static FlightReservationApp.FlightReservation extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (FlightReservationApp.FlightReservationHelper.id (), "FlightReservation");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static FlightReservationApp.FlightReservation read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_FlightReservationStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, FlightReservationApp.FlightReservation value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static FlightReservationApp.FlightReservation narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof FlightReservationApp.FlightReservation)
      return (FlightReservationApp.FlightReservation)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      FlightReservationApp._FlightReservationStub stub = new FlightReservationApp._FlightReservationStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static FlightReservationApp.FlightReservation unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof FlightReservationApp.FlightReservation)
      return (FlightReservationApp.FlightReservation)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      FlightReservationApp._FlightReservationStub stub = new FlightReservationApp._FlightReservationStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}