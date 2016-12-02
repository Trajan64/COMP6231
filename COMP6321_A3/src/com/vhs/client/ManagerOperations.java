package com.vhs.client;
import com.vhs.server.*;

import java.rmi.RemoteException;
import java.util.Scanner;

public class ManagerOperations {

	protected static void editFlight(ManagerServer server, String userId) throws RemoteException {

		int userInput = 0;
		System.out.println("Please enter the desired function you'd like to perform");
		System.out.println("1.Create a flight\n2.Delete a flight \n3.Modify the departure date"
				+ "\n4.Modify the departure time\n5.Change the number of seats");
		userInput = HelperFunctions.verifyValidInt();
		
		while(userInput < 1 || userInput > 5){
			System.out.println("Invalid Input, please try again.");
			userInput = HelperFunctions.verifyValidInt();
		}
		switch(userInput){
			case 1:
			createFlight(server, userId);
				break;
			case 2:
			deleteFlight(server, userId);
				break;
			case 3:
			modifyFlightDate(server, userId);
				break;
			case 4:
			modifyFlightTime(server, userId);
				break;
			case 5:
			modifyNumSeats(server, userId);
			
		}
		
		
	}
	
	
	
	private static void createFlight(ManagerServer server,String userId){
		
		@SuppressWarnings("resource")
		Scanner kb = new Scanner(System.in);
		
		System.out.println("Please enter destination of flight (i.e. MTL/NDL/WST)");
		String destination = kb.next();
		
		while(!HelperFunctions.isValidDestination(userId.substring(0, 3), destination)){
			System.out.println("Invalid destination. Please make sure that destination and departure "
					+ "are different locations and try again.");
			destination = kb.next();
		}
		
		System.out.println("Please enter a departure date (dd/MM/yyyy)");
		String date = kb.next();
		
		while(!HelperFunctions.isValidDate(date)){
			System.out.println("Invalid date. Please try again");
			date = kb.next();
		}
		
		System.out.println("Please enter a departure time");
		String time = kb.next();
		
		while(!HelperFunctions.isValidTime(time)){
			System.out.println("Invalid time. Please try again");
			time = kb.next();
		}
		
		int Economy = 0;
		int Business = 0;
		int First = 0;
		
		System.out.println("Please enter the number of seats in Economy Class");
		Economy = HelperFunctions.verifyValidInt();
		
		System.out.println("Please enter the number of seats in Business Class");
		Business = HelperFunctions.verifyValidInt();
		
		System.out.println("Please enter the number of seats in First Class");
		First = HelperFunctions.verifyValidInt();
		
		String parameters = userId.substring(0,3) + " " + destination + " " + date + " " + time + " "
				+ Economy + " " + Business + " " + First;
		
		String result = server.editFlightRecord("0", "Create", parameters );
		if(result.charAt(0) == 'E'){
			System.out.println(result);
			HelperFunctions.writeToLog(userId, " FAILED: Create flight " + parameters + "\n");
		}
		else{
			System.out.println(result);
			HelperFunctions.writeToLog(userId, " SUCCESS: Create flight " + parameters + "\n");
		}
		
		
	}
	
	private static void deleteFlight(ManagerServer server, String userId){
		@SuppressWarnings("resource")
		Scanner kb = new Scanner(System.in);
		String flightId = "";
		System.out.println("Please enter the flight id of the flight you wish to delete");
		flightId = kb.next();
		
		String result = server.editFlightRecord(flightId + "", "Delete", "");
		if(result.charAt(0) == 'E'){
			
			HelperFunctions.writeToLog(userId, " FAILED: " + userId + " " + flightId + " Delete \n");
			System.out.println(result);
		}
		else{
			HelperFunctions.writeToLog(userId, " SUCCESS " + flightId + " Delete\n");
			System.out.println(result);
		}
		
	
	}
	
	private static void modifyFlightDate(ManagerServer server, String userId){
		@SuppressWarnings("resource")
		Scanner kb = new Scanner(System.in);
		
		String flightRecordId = "";
		System.out.println("Please enter the flightId of the flight you wish to modify");
		flightRecordId = kb.next();
		
		String date = "";
		System.out.println("Please enter the new date of the flight.");
		date = kb.next();
		
		while(!HelperFunctions.isValidDate(date)){
			System.out.println("Invalid date. Please try again (format dd/MM/yyyy)");
			date = kb.next();
		}
		
		String result = server.editFlightRecord(flightRecordId + "", "Date", date);
		if(result.charAt(0) == 'E'){
			
			HelperFunctions.writeToLog(userId, " FAILED: " + userId + " " + flightRecordId + " "
					+ "new date: " + date +" \n");
			System.out.println(result);
		}
		else{
			HelperFunctions.writeToLog(userId, " SUCCESS " + flightRecordId + " "
					+ "new date " + date + " \n");
			System.out.println(result);
		}
		
		
		
	}

	private static void modifyFlightTime(ManagerServer server, String userId){
		@SuppressWarnings("resource")
		Scanner kb = new Scanner(System.in);
		
		String flightRecordId = "";
		System.out.println("Please enter the flightId of the flight you wish to modify");
		flightRecordId = kb.next();
		
		String time = "";
		System.out.println("Please enter the new time of departure of the flight.");
		time = kb.next();
		
		while(!HelperFunctions.isValidTime(time)){
			System.out.println("Invalid time. Please try again (format HH:mm)");
			time = kb.next();
		}
		
		String result = server.editFlightRecord(flightRecordId + "" , "Time", time);
		if(result.charAt(0) == 'E'){
			
			HelperFunctions.writeToLog(userId, " FAILED: " + userId + ""
					+ " " + flightRecordId + " new time: " + time +" \n");
			System.out.println(result);
		}
		else{
			HelperFunctions.writeToLog(userId, " SUCCESS " + flightRecordId + " new time"
					+ " " + time + " \n");
			System.out.println(result);
		}
	}
	
	private static void modifyNumSeats(ManagerServer server, String userId){
		
		Scanner kb = new Scanner(System.in);
		String flightRecordId = "";
		System.out.println("Please enter the flightId of the flight you wish to modify");
		flightRecordId = kb.next();
		
		int userInput = 0;
		System.out.println("Please select the class of seats you would like to modify");
		System.out.println("1.Economy\n2.Business\n3.First Class\n4.All Classes");
		userInput = HelperFunctions.verifyValidInt();
		
		while(userInput < 1 || userInput > 4){
			System.out.println("Invalid selection. Please try again");
			userInput = HelperFunctions.verifyValidInt();
		}
		
		switch(userInput){
			case 1:
				modifyClassSeats(flightRecordId, server, userId, "Economy");
				break;
			case 2:
				modifyClassSeats(flightRecordId, server, userId, "Business");
				break;
			case 3:
				modifyClassSeats(flightRecordId, server, userId, "First Class");
				break;
			case 4:
				modifyAllSeats(flightRecordId, server, userId, "Seats");
				break;
		}
		
	}
	
	private static void modifyClassSeats(String flightRecordId, ManagerServer server, String userId, String seatClass){
		int seats = 0;
		System.out.println("Please enter the number of seats desired in " + seatClass + " class");
		seats = HelperFunctions.verifyValidInt();
		
		String result = server.editFlightRecord(flightRecordId + "", seatClass , seats + "");
		if(result.charAt(0) == 'E'){
			
			HelperFunctions.writeToLog(userId, " FAILED: " + userId + ""
					+ " " + flightRecordId + " seats in " + seatClass + ": " + seats +" \n");
			System.out.println(result);
		}
		else{
			HelperFunctions.writeToLog(userId, " SUCCESS " + flightRecordId + " seats in "
					+ seatClass +": " + seats + " \n");
			System.out.println(result);
		}
	}
	
	private static void modifyAllSeats(String flightRecordId, ManagerServer server, String userId, String seatClass){
		int economy = 0;
		int business = 0;
		int first = 0;
		System.out.println("Please enter the number of seats desired in economy class");
		economy = HelperFunctions.verifyValidInt();
		
		System.out.println("Please enter the number of seats desired in business class");
		business = HelperFunctions.verifyValidInt();
		
		System.out.println("Please enter the number of seats desired in first class");
		first = HelperFunctions.verifyValidInt();
		
		String result = server.editFlightRecord(flightRecordId + "", seatClass , economy + " " + business + " " + first);
		if(result.charAt(0) == 'E'){
			
			HelperFunctions.writeToLog(userId, " FAILED: " + userId + ""
					+ " " + flightRecordId + " Seats: " + economy + " " + business + " " + first +" \n");
			System.out.println(result);
		}
		else{
			HelperFunctions.writeToLog(userId, " SUCCESS " + flightRecordId + " seats in: " 
					+ economy + " " + business + " " + first + " \n");
			System.out.println(result);
		}
	}

	
	protected static void getFlightsBooked(ManagerServer server, String userId, String recordType){
		
		
		String bookedFlights = server.getBookedFlightCount(recordType);
		HelperFunctions.writeToLog(userId, "Got booked flight count " + bookedFlights + "\n");
		System.out.println(bookedFlights);
		
	}



	public static void transferFlight(ManagerServer dfrsObj, String userId) {
		@SuppressWarnings("resource")
		Scanner kb = new Scanner(System.in);
		
		System.out.println("Please enter the passengerId of the passenger you wish to transfer");
		String passengerId = kb.next();
		
		System.out.println("Please enter the city you wish to transfer the flight to (i.e. MTL/NDL/WST)");
		
		String destination = kb.next();
		
		while(!HelperFunctions.isValidDestination(userId.substring(0, 3), destination)){
			System.out.println("Invalid destination. Please make sure that destination and departure "
					+ "are different locations and try again.");
			destination = kb.next();
		}
		
		String result = dfrsObj.transferReservation(passengerId + "", userId.substring(0, 3), destination);
		if(result.charAt(0) == 'E'){
			
			HelperFunctions.writeToLog(userId, " FAILED Transferring reservation for passenger "
					+ passengerId + " from " + userId.substring(0, 3) + " to " + destination + " \n");
			System.out.println(result);
		}
		else{
			HelperFunctions.writeToLog(userId, " SUCCESS Transferring reservation for passenger "
					+ passengerId + " from " + userId.substring(0, 3) + " to " + destination + " \n");
			System.out.println(result);
		}
	}
}
