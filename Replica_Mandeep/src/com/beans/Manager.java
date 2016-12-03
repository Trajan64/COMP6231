package com.beans;

import java.io.File;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import FlightReservationApp.FlightReservation;
import FlightReservationApp.FlightReservationHelper;

import com.logging.Logger;

public class Manager {
	public static int mgrIdSeq = 10000;
	private String managerId;
	private String location;
	private File file;

	public static final String LOCATION_MONTREAL = "mtl";
	public static final String LOCATION_WASHIGTON = "wst";
	public static final String LOCATION_NEWDELHI = "ndl";

	private Logger logger;

	private FlightReservation server;

	public Manager(String location, String Id,ORB orb) {
		super();
		try {
			this.location = location;
			this.managerId = Id;

			org.omg.CORBA.Object objRef = orb
					.resolve_initial_references("NameService");

			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// resolve the Object Reference in Naming

			if (location.equals("mtl")) {
				server = FlightReservationHelper.narrow(ncRef
						.resolve_str("mtl"));
			} else if (location.equals("wst")) {
				server = FlightReservationHelper.narrow(ncRef
						.resolve_str("wst"));
			} else if (location.equals("ndl")) {
				server = FlightReservationHelper.narrow(ncRef
						.resolve_str("ndl"));
			}

			logger = new Logger("logs/client/Manager_" + managerId + ".log");

		} catch (Exception e) {
			System.out.println("Error in Manager class " + e);
			e.printStackTrace();

		}

	}

	public String getBookedFlightCount(String recordType) {
		logger.info("Start of Manager::getBookedFlightCount method");
		logger.info("Record Type: " + recordType);
		String count = "";
		try {
			count = server.getBookedFlightCount(recordType);

			if (!"fail".equals(count)) {
				logger.info("Records Count for recordType " + recordType + "="
						+ count);
			} else {
				logger.info("Failed to get count of records.");
			}
		} catch (Exception e) {
			logger.info("Manager::getBookedFlightCount method" + e.getMessage());
			e.printStackTrace();
		}
		logger.info("End of Manager::getBookedFlightCount method");
		return count;
	}

	public String editFlightRecord(String recordID, String fieldName,
			String newValue) {

		String result = null;
		try {
			result = server.editFlightRecord(recordID, fieldName, newValue);
			if (!"fail".equals(result)) {
				logger.info("Flight Record created successfully with Record ID : "
						+ result);
			} else {
				logger.info("Failed to editFlightRecord.");
			}
		} catch (Exception e) {
			logger.info("(" + managerId + ") Manager::editFlightRecord method"
					+ e.getMessage());
			e.printStackTrace();
		}
		logger.info("End of Manager::editFlightRecord method");
		return result;

	}

	public String transferRecord(String passengerID,String currentCity, String otherCity) {
		logger.info("Start of Manager::transferRecord method");
		
		String count = "";
		try {
			count = server.transferReservation(passengerID, currentCity, otherCity);

			if (!"fail".equals(count)) {
				logger.info("Passenger Record transferred for passengerId :: "+count);
			} else {
				logger.info("Failed to transferRecord records.");
			}
		} catch (Exception e) {
			logger.info("Manager::transferRecord method" + e.getMessage());
			e.printStackTrace();
		}
		logger.info("End of Manager::transferRecord method");
		return count;
	}
	
	public void log(String logInfo) {
		logInfo = location + " server ::: " + logInfo;
		logger.info(logInfo);
	}
	
	public static void main(String[] args) throws InvalidName {
		ORB orb = ORB.init(args, null);
		
		Manager manager = new Manager("mtl", "1234", orb);
		manager.transferRecord("123", "mtl", "wst");
	}
}
