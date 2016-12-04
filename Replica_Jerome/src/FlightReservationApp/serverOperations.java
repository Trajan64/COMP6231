package FlightReservationApp;

/**
 * Interface definition: server.
 * 
 * @author OpenORB Compiler
 */
public interface serverOperations
{
    /**
     * Operation bookFlight
     */
    public String bookFlight(String firstName, String lastName, String address, String phone, String destination, String date, int classType);

    /**
     * Operation getBookedFlight
     */
    public String getBookedFlight(int recordType);

    /**
     * Operation editFlightRecord
     */
    public String editFlightRecord(int recordID, String fieldName, String newValue);

    /**
     * Operation transferReservation
     */
    public String transferReservation(int passengerID, String currentCity, String otherCity);

    /**
     * Operation stop
     */
    public void stop();

}
