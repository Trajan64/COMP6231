package FlightReservationApp;


/**
* FlightReservationApp/FlightReservationOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from FlightReservation.idl
* Sunday, December 4, 2016 1:33:22 AM EST
*/

public interface FlightReservationOperations 
{
  String bookFlight (String firstName, String lastName, String address, String phone, String destination, String date, String flightClass);
  String getBookedFlightCount (String recordType);
  String editFlightRecord (String recordID, String fieldName, String newValue);
  String transferReservation (String passengerID, String currentCity, String otherCity);
} // interface FlightReservationOperations
