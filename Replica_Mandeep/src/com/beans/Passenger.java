/*package com.beans;

import java.io.File;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Calendar;

import com.logging.Logger;
import com.server.FlightReservationImplNdl;
import com.server.FlightReservationImplWst;
import com.server.FlightReservationImplMtl;
import com.server.FlightReservation;

*//** 
 * @author Mandeep 
 * @StudentId 27849559
 *//*
public class Passenger {
	public static int mgrIdSeq = 10000;
	private String managerId;
	private String location;
	private File file;
	 
	public static final String LOCATION_MONTREAL = "mtl";
	public static final String LOCATION_WASHINGTON = "wst";
	public static final String LOCATION_NEWDELHI = "ndl";
	
	private Logger log;
	
	private FlightReservation server;

	
	 * gets the respective server stub according to location
	 
	public Passenger(String location) {
		super();
		try {
			this.location = location;
			this.managerId = location + mgrIdSeq ++ ;
			
			if(location.equals("mtl")) 
			{
				server = (FlightReservation) Naming.lookup("rmi://localhost:" + FlightReservationImplMtl.RMI_SERVER_PORT + "/" + FlightReservationImplMtl.RMI_SERVER_NAME);
			} else if (location.equals("wst")) 
			{
				server = (FlightReservation) Naming.lookup("rmi://localhost:" + FlightReservationImplWst.RMI_SERVER_PORT + "/" + FlightReservationImplWst.RMI_SERVER_NAME);
			} else if (location.equals("ndl")) 
			{
				server = (FlightReservation) Naming.lookup("rmi://localhost:" + FlightReservationImplNdl.RMI_SERVER_PORT + "/" + FlightReservationImplNdl.RMI_SERVER_NAME);
			}
			
			log = new Logger("logs/client/PassengerClient.log");
			  
		}catch(Exception e) {
			System.out.println("Error in Passenger class " + e);  
			e.printStackTrace();
			
		}
		
	}
	
	
	 * calls the server bookFlight method
	 
	public void bookFlight(String firstName, String lastName, String address, String phone, String destination, Calendar date, String flightClass) {
		log.info("Start of Passenger::bookFlight method");
		String result;
		
		try {
			result = server.bookFlight(firstName, lastName, address, phone, destination, date, flightClass);
			if(!"fail".equals(result)) {
				log.info("Passenger Record created successfully with Record ID : " +result);
			}
			else {
				log.info("Failed to create Passenger record.");
			}
		} catch (RemoteException e) {
			log.info("Passenger::bookFlight method" + e.getMessage());
			e.printStackTrace();
		}
		log.info("End of Passenger::bookFlight method");
	}
	
	
}
*/