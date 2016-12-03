package com.client;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.omg.CORBA.ORB;

import com.beans.FlightRecord;
import com.beans.Manager;
import com.server.FlightReservationConstants;


public class ManagerClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		clearLogs();
		ORB orb = ORB.init(args, null);
		Manager m1Wst = new Manager(Manager.LOCATION_WASHIGTON,"1234",orb);
		Manager m2Wst = new Manager(Manager.LOCATION_WASHIGTON,"1235",orb);
		
		Manager m1Mtl = new Manager(Manager.LOCATION_MONTREAL,"2345",orb);
		Manager m2Mtl = new Manager(Manager.LOCATION_MONTREAL,"2346",orb);
		
		Manager m1Ndl = new Manager(Manager.LOCATION_NEWDELHI,"3345",orb);
		Manager m2Ndl = new Manager(Manager.LOCATION_NEWDELHI,"3346",orb);
		
	
		
		
		String flightRecord = "w100"+","+"wst"+","+"ndl"+","+"12/12/2012"+","+"10"+","+"10"+","+"10";
		String flightRecord1 = "w101"+","+"wst"+","+"ndl"+","+"12/12/2012"+","+"10"+","+"10"+","+"10";
		m1Wst.editFlightRecord("w100", FlightReservationConstants.CREATE_FLIGHT, flightRecord);
		m2Wst.editFlightRecord("w101",FlightReservationConstants.CREATE_FLIGHT, flightRecord1);
		
		String flightRecord2 = "m100"+","+"mtl"+","+"ndl"+","+"12/12/2012"+","+"10"+","+"10"+","+"10";
		String flightRecord3 = "m101"+","+"mtl"+","+"ndl"+","+"12/12/2012"+","+"10"+","+"10"+","+"10";
		m1Mtl.editFlightRecord("m100", FlightReservationConstants.CREATE_FLIGHT, flightRecord2);
		m2Mtl.editFlightRecord("m101",FlightReservationConstants.CREATE_FLIGHT, flightRecord3);
		
		String flightRecord4 = "n100"+","+"ndl"+","+"mtl"+","+"12/12/2012"+","+"10"+","+"10"+","+"10";
		String flightRecord5 = "n101"+","+"ndl"+","+"mtl"+","+"12/12/2012"+","+"10"+","+"10"+","+"10";
		m1Ndl.editFlightRecord("n100", FlightReservationConstants.CREATE_FLIGHT, flightRecord4);
		m2Ndl.editFlightRecord("n101",FlightReservationConstants.CREATE_FLIGHT, flightRecord5);
		
	
		
		
		
		
	}
	
	private static void clearLogs() { 
		File dir = new File("logs/client");
		for(File file: dir.listFiles()) file.delete();
	}
}
