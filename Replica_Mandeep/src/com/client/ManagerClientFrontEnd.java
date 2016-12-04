package com.client;


import java.rmi.Naming;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import FlightReservationApp.FlightReservation;
import FlightReservationApp.FlightReservationHelper;

import com.beans.FlightRecord;
import com.beans.Manager;
import com.config.Constants;
import com.server.FlightReservationConstants;
import com.server.FlightReservationImplMtl;
import com.server.FlightReservationImplNdl;
import com.server.FlightReservationImplWst;
import com.server.FlightReservationUtil;

/**
 * @author Mandeep
 * @StudentId 27849559
 */
public class ManagerClientFrontEnd {
	private static Logger log = Logger.getLogger(ManagerClientFrontEnd.class
			.getName());
	private static FileHandler fh;

	// Return basic menu.
	public static void showMenu() {
		System.out.println("\n********\n");
		System.out.println("Please select an option (1-5)");
		System.out.println("1.Create Flight Record");
		System.out.println("2.Get Flight Record ");
		System.out.println("3.Edit Flight Records");
		System.out.println("4.Delete Flight Record ");
		System.out.println("5.Transfer Passenger Record ");
		System.out.println("6.Logout");
	}
	
	public static void showPassengerMenu() {
		System.out.println("\n********\n");
		System.out.println("Please select an option (1-2)");
		System.out.println("1.Book a Flight Record");
		System.out.println("2.Logout");
	}
	
	// Return server choose menu.
	public static void serverMenu() {
		System.out.println("\n****Welcome to Flight Reservation System****\n");
		System.out.println("Please select an option (1-3)");
		System.out.println("1.Login as Manager ");
		System.out.println("2.Login as Passenger ");
		System.out.println("3.Exit ");
	}
	

	public static void main(String[] args) {
		try {
			fh = new FileHandler("logs/client/ManagerClientFrontEnd.log");
			log.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

			log.info("Inside main method of ManagerClientFrontEnd class");
			
			ORB orb = ORB.init(args, null);
			org.omg.CORBA.Object objRef = orb
					.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			
			boolean passengerLogin = false;
			boolean loggedIn = false;
			FlightReservation server = null;
			Manager manager = null;
			String source = null;
			int userChoice1 = 0;
			int userChoice2 = 0;
			int userChoice3= 0;
			Scanner keyboard = new Scanner(System.in);
			//serverMenu();
			String managerId = "";
			while (true) {
				serverMenu();
				Boolean valid1 = false;
				// Enforces a valid integer input.
				while (!valid1) {
					try {
						userChoice1 = keyboard.nextInt();
						valid1 = true;
					} catch (Exception e) {
						System.out.println("Invalid Input, please enter an Integer");
						valid1 = false;
						keyboard.nextLine();
					}
				}
				
				switch(userChoice1) {
				case 1: 
					System.out.println("Enter Manager ID: ");
					managerId = keyboard.next();
					if(managerId.contains(Constants.MTL)) 
					{  	
						source=Constants.MTL;
						loggedIn=true;
						manager = new Manager(Constants.MTL,managerId.substring(3,7),orb);
						
					} else if (managerId.contains(Constants.WST)) 
					{
						source=Constants.WST;
						loggedIn=true;
						manager = new Manager(Constants.WST,managerId.substring(3,7),orb);
						
					} else if (managerId.contains(Constants.NDL)) 
					{	source=Constants.NDL;
						loggedIn=true;
						manager = new Manager(Constants.NDL,managerId.substring(3,7),orb);
						
					}
					else {
						System.out.println("Invalid manager id");
						serverMenu();
						continue;
					}
					break;
				
				case 2:
					System.out.println("Enter the city[ndl,wst,mtl]");
					String city ="";
					city = keyboard.next();
					
					if(city.contains(Constants.MTL)) 
					{  //source="mtl";
						passengerLogin = true;
						server = FlightReservationHelper.narrow(ncRef.resolve_str(Constants.MTL));
					} else if (city.contains(Constants.WST)) 
					{//source="wst";
						passengerLogin = true;
						server = FlightReservationHelper.narrow(ncRef.resolve_str(Constants.WST));
					} else if (city.contains(Constants.NDL)) 
					{//source="ndl";
						passengerLogin = true;
						server = FlightReservationHelper.narrow(ncRef.resolve_str(Constants.NDL));
					}
					else {
						System.out.println("Invalid city");
						serverMenu();
						continue;
					}
					break;
				case 3:
					System.exit(0);
					break;	
				default:
					System.out.println("Invalid Input, please enter a choice");
					serverMenu();
					continue;
				}
				
				while(passengerLogin){
					Boolean valid3 = false;
					showPassengerMenu();
					// Enforces a valid integer input.
					while (!valid3) {
						try {
							userChoice3 = keyboard.nextInt();
							valid3 = true;
						} catch (Exception e) {
							System.out
									.println("Invalid Input, please enter an Integer");
							valid3 = false;
							keyboard.nextLine();
						}
					}
					
					switch(userChoice3){

					case 1: {
						log.info(" Book Record option selected ");
						String firstName = "";
						String lastName = "";
						String address = "";
						String phone = "";
						String destination = "";
						String date = "";
						String flightClass = "";
						System.out.println("Please enter first name");
						firstName = keyboard.next();
					
						System.out.println("Please enter last name");
						lastName = keyboard.next();
						
						System.out.println("Please enter address");
						address = keyboard.next();
					
						System.out.println("Please enter phone");
						phone = keyboard.next();
					
						System.out.println("Please enter destination");
						destination = keyboard.next();
						
						System.out.println("Please enter date(dd/MM/yyyy)");
						date = keyboard.next();
						
						System.out.println("Please enter flightClass[Business,Economy,First]");
						flightClass = keyboard.next();
						
						String result = server.bookFlight(firstName, lastName, address, phone, destination, date, flightClass);
						if (!"fail".equals(result)) {
							log
									.info("Flight Record created successfully with Record ID : "
											+ result);
							System.out
									.println("Flight Record created successfully with Record ID : "
											+ result);
						} else {
							System.out.println("Failed to create Passenger Record.");
							log.info("Failed to create Passenger Record.");
						}
						//showPassengerMenu();
						break;
					}
					
					case 2:
						log.info(" Logout option selected ");
						passengerLogin = false;
						break;
					default:
						log.info("Invalid Input, please try again.");
						System.out.println("Invalid Input, please try again.");
					
					}
					
				}
				
				/*if(!passengerLogin && !loggedIn)
				serverMenu();*/
				
				while (loggedIn) {
					Boolean valid2 = false;
					showMenu();
					// Enforces a valid integer input.
					while (!valid2) {
						try {
							userChoice2 = keyboard.nextInt();
							valid2 = true;
						} catch (Exception e) {
							System.out
									.println("Invalid Input, please enter an Integer");
							valid2 = false;
							keyboard.nextLine();
						}
					}

					// Manage user selection.
					switch (userChoice2) {
					case 1: {
						log.info(" Create Flight Record option selected ");
						String flightId = "";
						String destination = "";
						String date = "";
						String busniessClassSeats = "";
						String economyClassSeats = "";
						String firstClassSeats = "";
						System.out.println("Please enter Flight Id");
						flightId = keyboard.next();
					
						System.out.println("Please enter Destination");
						destination = keyboard.next();
						
						System.out.println("Please enter Date of the Flight");
						date = keyboard.next();
					
						System.out.println("Please enter number of Total number of Business Class Seats");
						busniessClassSeats = keyboard.next();
					
						System.out.println("Please enter number of Total number of Economy Class Seats");
						economyClassSeats = keyboard.next();
						
						System.out.println("Please enter number of Total number of First Class Seats");
						firstClassSeats = keyboard.next();
						
						String flightRecord = flightId+","+source+","+destination+","+date+","+busniessClassSeats+","+economyClassSeats+","+firstClassSeats;
						
						String result = manager.editFlightRecord(flightId,  FlightReservationConstants.CREATE_FLIGHT, flightRecord);
						if (!"fail".equals(result)) {
							log
									.info("Flight Record created successfully with Record ID : "
											+ result);
							System.out
									.println("Flight Record created successfully with Record ID : "
											+ result);
						} else {
							log.info("Failed to Flight record.");
						}
						//showMenu();
						break;
					}
					case 2: {
						log.info(" Get Flight Record option selected ");
						String recordType = "";
						
						System.out.println("Please enter Flight Class[Business,Economy,First,All]");
						recordType = keyboard.next();

						

						String count = manager.getBookedFlightCount(recordType);
						
						if (!"fail".equals(count)) {
							log.info("Records Count for recordType "+recordType+"=" + count);
							System.out.println("Records Count for recordType "+recordType+"=" + count);
						} else {
							log.info("Failed to get record.");
						}
						//showMenu();
						break;
					}
					
					case 3: {
						log.info(" Edit Flight Records option selected ");
						String flightId = "";
						String fieldName = "";
						String newValue = "";
						System.out.println("Please Enter Flight Id.");
						flightId = keyboard.next();
						
						System.out.println("Please Enter Field to be update[Business,Economy,First,Date(dd/MM/yyyy)]");
						fieldName = keyboard.next();
						
						System.out.println("Please Enter the new value. ");
						newValue = keyboard.next();
						
						String result = manager.editFlightRecord(flightId, fieldName, newValue);
						if (!"fail".equals(result)) {
							log.info("Flight Id updated is :: " + result);
							System.out.println("Flight Id updated is :: " + result);
						} else {
							log.info("Failed to update flight Id :: "+result);
						}

						//showMenu();
						break;
					}
					
					case 4: {
						log.info(" Delete Flight Record option selected ");
						String flightId = "";
						System.out.println("Please Enter Flight Id.");
						flightId = keyboard.next();

						String result = manager.editFlightRecord(flightId, FlightReservationConstants.DELETE_FLIGHT, "");
						if (!"fail".equals(result)) {
							log.info("Flight Id deleted successfully is :: " + result);
							System.out.println("Flight Id deleted successfully is :: " + result);
						} else {
							log.info("Failed to delete the flight Id :: "+result);
						}

						//showMenu();
						break;
					}
					
					case 5: {
						log.info("Transfer passenger record");
						String passengerID = "";
						String currentCity = "";
						String otherCity = "";
						System.out.println("Please Enter Passenger Id.");
						passengerID = keyboard.next();
						
						System.out.println("Please enter current city");
						currentCity = keyboard.next();
						
						System.out.println("Please enter other city");
						otherCity = keyboard.next();

						String result = manager.transferRecord(passengerID, currentCity, otherCity);
						if (!"fail".equals(result)) {
							log.info("Transfer passenger record successfully is :: " + result);
							System.out.println("Transfer passenger record successfully is :: " + result);
						} else {
							log.info("Transfer passenger record :: "+result);
						}

						//showMenu();
						break;
					}
					case 6:
						log.info(" Logout option selected ");
						loggedIn = false;
						break;
					default:
						log.info("Invalid Input, please try again.");
						System.out.println("Invalid Input, please try again.");
					}
				}
				//serverMenu();
			}
		} catch (Exception e) {
			log.info("Error in ManagerClientFrontEnd class " + e);
			e.printStackTrace();

		}
	}
}
