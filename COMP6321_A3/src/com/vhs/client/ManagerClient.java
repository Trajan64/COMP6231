package com.vhs.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.time.OffsetDateTime;
import java.util.Scanner;


import com.vhs.server.*;



public class ManagerClient extends Thread {
	private String threadName;
	private String serverCity;
	private String managerId;
	private ManagerServer server;

	public ManagerClient(String serverCity, String managerId, String threadname){
		if(!managerId.equals("")){
			this.managerId = managerId;
			
			try {
				if(Files.notExists(Paths.get("./"+ this.managerId + "Log.txt"))){
					Files.write(Paths.get("./"+ this.managerId + "Log.txt"), 
							(OffsetDateTime.now().toString() + "\n").getBytes());
				}
				else{
					Files.write(Paths.get("./"+ this.managerId + "Log.txt"), 
							(OffsetDateTime.now().toString() + "\n").getBytes(), StandardOpenOption.APPEND);
				}
			    
			    System.out.println("created log " +this.managerId);
			}catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
			
		}
		this.threadName = threadname;
		this.serverCity = serverCity;
		try {
			switch(serverCity){
				case "MTL":
					ManagerServerImplServiceMTL mtl = new ManagerServerImplServiceMTL();
					ManagerServer dfrsObj = mtl.getManagerServerImplPort();
					server= dfrsObj;
					break;
				case "NDL":
					ManagerServerImplServiceNDL ndl = new ManagerServerImplServiceNDL();
					dfrsObj = ndl.getManagerServerImplPort();
					server= dfrsObj;
					break;
				case "WST":
					ManagerServerImplServiceWST wst = new ManagerServerImplServiceWST();
					dfrsObj = wst.getManagerServerImplPort();
					server= dfrsObj;
					break;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void run() {
		System.out.println("Started thread " + threadName);
        try { 
                testCases(); 
        } catch(Exception e) {}
        System.out.println("Finished thread " + threadName);
        
   }
	
	public static void passengerUser(){
		
		int userChoice = 0;
		
		System.out.println("Would you like to book a flight?");
		System.out.println("1.Yes\n2.Return to main menu");
		
		userChoice = HelperFunctions.verifyValidInt();
		
		while(userChoice < 1 || userChoice > 2){
			System.out.println("Invalid Input, please try again.");
			userChoice = HelperFunctions.verifyValidInt();
		}
		
		switch(userChoice){
			case 1:
				PassengerOperations.flightBooking();
				break;
			case 2:
				break;
			default:
				System.out.println("Invalid Input.");
		}
	}
	

	public static void managerUser() throws RemoteException{
		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		String userId = "";
		
		System.out.println("Please enter your id");
		userId = keyboard.next();
		
		String userIdDigits = "";
		
		while(userId.length() != 7 ||!(userId.substring(0, 3).equals("MTL") ||
				userId.substring(0, 3).equals("NDL") || userId.substring(0, 3).equals("WST"))){
			System.out.println("Invalid ID. Please try again");
			userId = keyboard.next();
		}
		
		while(userId.length() == 7){
			userIdDigits =  userId.substring(4);
			try{
				Integer.parseInt(userIdDigits);
				break;
			}
			catch(Exception e){
				System.out.println("Invalid ID. Please try again");
				userId = keyboard.next();
			}
		}
		
	
		String[] destinations = new String[2];
		

		ManagerServer server = null;
		switch(userId.substring(0, 3)){
			case "MTL":
				destinations[0] = "New Delhi";
				destinations[1] = "Washington";
				ManagerServerImplServiceMTL mtl = new ManagerServerImplServiceMTL();
				server = mtl.getManagerServerImplPort();
				break;
			case "NDL":
				destinations[0] = "Montreal";
				destinations[1] = "Washington";
				ManagerServerImplServiceNDL ndl = new ManagerServerImplServiceNDL();
				server = ndl.getManagerServerImplPort();
				break;
			case "WST":
				destinations[0] = "Montreal";
				destinations[1] = "New Delhi";
				ManagerServerImplServiceWST wst = new ManagerServerImplServiceWST();
				server = wst.getManagerServerImplPort();
				break;
		}
		
		System.out.println("Select an operation to perform");
		int userChoice = 0;
		System.out.println("1.Get booked flight count\n2.Modify a flight\n3.Transfer a flight");
		userChoice = HelperFunctions.verifyValidInt();
		
		while(userChoice < 1 || userChoice > 3){
			System.out.println("Invalid Input, please try again.");
			userChoice = HelperFunctions.verifyValidInt();
		}
		
		switch(userChoice){
			case 1:
				System.out.println("Please select the desired class type");
				System.out.println("1.Economy\n2.Business Class\n3.First Class\n4.All Classes");
				int classNum = HelperFunctions.verifyValidInt();
				
				while(classNum < 1 || classNum > 4 ){
					System.out.println("Invalid selection. Please try again");
					classNum = HelperFunctions.verifyValidInt();
				}
				String recordType = "";
				switch(classNum){
					case 1:
						recordType = "Economy";
						break;
					case 2:
						recordType = "Business";
						break;
					case 3:
						recordType = "First Class";
						break;
					case 4:
						recordType = "";
						break;
				}
				
				
				
				
				ManagerOperations.getFlightsBooked(server, userId, recordType);
				break;
			case 2:
				ManagerOperations.editFlight(server, userId);
				break;
			case 3:
				ManagerOperations.transferFlight(server, userId);
			
		}

	}
	
	public static void mainMenu() throws RemoteException{
		
		int userChoice=0;
		Scanner keyboard = new Scanner(System.in);
		
		
		
		while(true)
		{
			System.out.println("Are you a passenger or a manager?");
			System.out.println("1.Passenger\n2.Manager\n3.Exit");
			
			userChoice = HelperFunctions.verifyValidInt();
			
			
			// Manage user selection.
			switch(userChoice)
			{
			case 1: 
				passengerUser();
				break;
			case 2:
				managerUser();
				break;
			case 3:
				System.out.println("Have a nice day!");
				keyboard.close();
				System.exit(0);
			default:
				System.out.println("Invalid Input, please try again.");
			}
		}
		
	}
	public static void main(String[] args) {
		int userChoice = 0;
		
		System.out.println("Select an option: ");
		System.out.println("1.Run the Console Application\n2.Run Test Cases\n3.Multithreaded Tests");
		
		userChoice = HelperFunctions.verifyValidInt();
		
		while(userChoice < 1 || userChoice > 3){
			System.out.println("Invalid Input, please try again.");
			userChoice = HelperFunctions.verifyValidInt();
		}
		
		switch(userChoice){
		case 1:
			try {
				mainMenu();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case 2:
			basicTestCases();
			break;
		case 3:
			ManagerClient mtlClient1 = new ManagerClient("MTL", "", "mtlClient1");
			ManagerClient mtlClient2 = new ManagerClient("MTL", "", "mtlClient2");
			ManagerClient mtlClient3 = new ManagerClient("MTL", "", "mtlClient3");
			
			
			
			ManagerClient ndlClient1 = new ManagerClient("NDL", "", "ndlClient1");
			ManagerClient ndlClient2 = new ManagerClient("NDL", "", "ndlClient2");
			ManagerClient ndlClient3 = new ManagerClient("NDL", "", "ndlClient3");
			ManagerClient ndlClient4 = new ManagerClient("NDL", "", "ndlClient4");
			
			
			
			ManagerClient wstClient1 = new ManagerClient("WST", "", "wstClient1");
			ManagerClient wstClient2 = new ManagerClient("WST", "", "wstClient2");
			ManagerClient wstClient3 = new ManagerClient("WST", "", "wstClient3");
			
			//create several managers for each server
			
			ManagerClient mtlMgr1 = new ManagerClient("MTL", "MTL1111", "mtlMgr1");
			ManagerClient mtlMgr2 = new ManagerClient("MTL", "MTL1112", "mtlMgr2");	
			
			ManagerClient ndlMgr1 = new ManagerClient("NDL", "NDL1111", "ndlMgr1");
			ManagerClient ndlMgr2 = new ManagerClient("NDL", "NDL1112", "ndlMgr2");
			
			ManagerClient wstMgr1 = new ManagerClient("WST", "WST1111", "wstMgr1");
			ManagerClient wstMgr2 = new ManagerClient("WST", "WST1112", "wstMgr2");
			
			mtlClient1.start();
			mtlClient2.start();
			mtlClient3.start();
			
			wstClient1.start();
			wstClient2.start();
			wstClient3.start();
			
			ndlClient1.start();
			ndlClient2.start();
			ndlClient3.start();
			ndlClient4.start();
			
				
			
			mtlMgr1.start();
			mtlMgr2.start();
			
			
			
			ndlMgr1.start();
			ndlMgr2.start();
			
			
			
			wstMgr1.start();
			wstMgr2.start();
			break;
		default:
			System.out.println("Invalid Input.");
	}
	
	
	//mainMenu(ncRef);

	
	//create clients to perform testing
	//create a passenger for each server
	
}


public static void basicTestCases(){
try {
	
	//Book a flight that does not exist
	ManagerServerImplServiceMTL mtl = new ManagerServerImplServiceMTL();
	ManagerServerImplServiceNDL ndl = new ManagerServerImplServiceNDL();
	ManagerServerImplServiceWST wst = new ManagerServerImplServiceWST();
	
	ManagerServer dfrsObjMTL = mtl.getManagerServerImplPort();

	dfrsObjMTL.bookFlight("abe", "lincoln", "1324 capitol hill", "342-234-2342",
			"New Delhi", "07/11/2016", "Economy");
	
	//Delete a flight that does not exist

	dfrsObjMTL.editFlightRecord("1", "Delete", "");
	
	//Modify the seats of a flight that doesn't exist

	dfrsObjMTL.editFlightRecord("1", "Seats", "1 2 2");
	
	dfrsObjMTL.editFlightRecord("1", "Economy", "1");
	dfrsObjMTL.editFlightRecord("1", "Business", "1");
	dfrsObjMTL.editFlightRecord("1", "First Class", "1");
	
	//Modify the date of a flight that does not exist
	dfrsObjMTL.editFlightRecord("1", "Date", "06/12/2016");
	//Modify the time of a flight that does not exist
	dfrsObjMTL.editFlightRecord("1", "Date", "04:22");
	//Get booked flight count when there are no seats booked
	dfrsObjMTL.getBookedFlightCount("Economy");
	dfrsObjMTL.getBookedFlightCount("Business");
	dfrsObjMTL.getBookedFlightCount("First Class");
	
	//Create a flight in MTL with one seat in economy, business and first class
	String parameters = "MTL" + " " + "WST" + " " + "07/10/2016" + " " + "13:00" + " "
			+ 1 + " " + 1 + " " + 1;
	dfrsObjMTL.editFlightRecord("0", "Create", parameters );
	
	//Book above economy flight seat
	dfrsObjMTL.bookFlight("Mark", "Twain", "456 Tom St", "123-456-7891",
			"WST", "07/10/2016", "Economy");
	
	//Get booked flight count for all seat classes
	dfrsObjMTL.getBookedFlightCount("Economy");
	dfrsObjMTL.getBookedFlightCount("Business");
	dfrsObjMTL.getBookedFlightCount("First Class");
	
	//Try to book same flight with economy seating
	
	dfrsObjMTL.bookFlight("Jane", "Eyre", "456 Austin St", "123-456-7891",
			"WST", "07/10/2016", "Economy");
	
	//Modify seats in economy of above flight to 0
	
	dfrsObjMTL.editFlightRecord("1", "Economy", "0");
	
	//Get booked flight count in economy
	dfrsObjMTL.getBookedFlightCount("Economy");
	
	//Delete flight
	dfrsObjMTL.editFlightRecord("1", "Delete", "");
	
	//Get Booked flight count
	dfrsObjMTL.getBookedFlightCount("Economy");
	dfrsObjMTL.getBookedFlightCount("Business");
	dfrsObjMTL.getBookedFlightCount("First Class");
	
	//Transferring reservation

	parameters = "MTL" + " " + "NDL" + " " + "07/11/2016" + " " + "13:00" + " "
			+ 2 + " " + 2 + " " + 2;
	dfrsObjMTL.editFlightRecord("0", "Create", parameters );
	
	ManagerServer dfrsObjWST = wst.getManagerServerImplPort();
	
	parameters = "WST" + " " + "NDL" + " " + "07/11/2016" + " " + "13:00" + " "
			+ 2 + " " + 2 + " " + 2;
	dfrsObjWST.editFlightRecord("0", "Create", parameters );
	

	dfrsObjMTL.bookFlight("abe", "lincoln", "1324 capitol hill", "342-234-2342",
			"New Delhi", "07/11/2016", "Economy");
	
	dfrsObjMTL.transferReservation("4", "MTL", "WST");
	
	mainMenu();
	
	
} catch (RemoteException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
}

public void testCases(){

	


	//book a flight at each different server
	if(threadName.equals("mtlClient1")){
		System.out.println("mtlClient1 booking flight id1 " + server.bookFlight("Bob", "McGee", "123 some st", "555-555-5551", "New Delhi", "01/11/2016", "Economy"));
		//client tries to book recently added flight
		System.out.println("mtlClient1 trying to book recently added flight #4" + server.bookFlight("Bob", "McGee", "123 some st", "555-555-5551", "New Delhi", "02/11/2016", "Economy"));
	}
	else if(threadName.equals("ndlClient1")){
		System.out.println("ndlClient1 booking flight id1 " + 
				server.bookFlight("Anne", "Shirley", "456 Green Gables", "555-555-5552",
						"Montreal", "01/11/2016", "Economy"));
	}
	else if(threadName.equals("wstClient1")){
	
		System.out.println("wstClient1 booking flight id1 " +
				server.bookFlight("Bruce", "Wayne", "304 Gotham", "555-555-5553", "Montreal", 
						"01/11/2016", "Economy"));

	}
	else if(threadName.equals("mtlClient2")){
		//book a flight that doesn't exist
		System.out.println("mtlClient2 booking flight that does not exist " +
				server.bookFlight("Bob", "McGee", "123 some st", "555-555-5551", "New Delhi", "01/11/2016", "Economy"));
		//book a flight that is full
		System.out.println("mtlClient2 booking flight that is full " + server.bookFlight("Bob",
				"McGee", "123 some st", "555-555-5551", "New Delhi", "01/11/2016", "Economy"));
	}
	else if(threadName.equals("mtlMgr1")){
		//get number of flights booked
		System.out.println("mtlMgr1 getting flights booked #1 " + 		
				server.getBookedFlightCount(""));
		
		
		String parameters = "MTL" + " " + "NDL" + " " + "01/11/2016" + " " + "13:00" + " "
				+ 1 + " " + 1 + " " + 1;
		server.editFlightRecord("0", "Create", parameters );
		parameters = "MTL" + " " + "NDL" + " " + "01/11/2016" + " " + "14:00" + " "
				+ 1 + " " + 1 + " " + 1;
		server.editFlightRecord("0", "Create", parameters );
		
	}
	else if(threadName.equals("mtlMgr2")){
		String parameters = "MTL" + " " + "NDL" + " " + "01/12/2016" + " " + "13:00" + " "
				+ 1 + " " + 1 + " " + 1;
		server.editFlightRecord("0", "Create", parameters );
		//delete flight
		System.out.println("mtlMgr2 deleting flight 1 " + server.editFlightRecord("1", "Delete",
				""));
		//create flight
		System.out.println("mtlMgr2 creating flight 4 " + 
				server.editFlightRecord("1", "Create", "MTL NDL 02/11/2016 13h00 2 2 1" ));

	}
	else if(threadName.equals("mtlClient3")){
		//client tries to book deleted flight
		
		System.out.println("mtlClient3 trying to book deleted flight #1 " +
				server.bookFlight("Sha la", "La la", "333 polka drive", "555-555-5554", "New Delhi",
						"01/11/2016", "Economy"));
		
	}
	else if(threadName.equals("wstMgr2")){
		
		//manager adding seats to server washington to mtl #1 flight (should be booked)
		System.out.println("wstMgr2 adding seat to economy flight 1 " + 
				server.editFlightRecord("1", "Seats", "2 1 1" ));
		//manager changing date of flight
		System.out.println("wstMgr2 changing date of flight 1 " + 
				server.editFlightRecord("1", "Date", "06/12/2016 16h00" ));

	}
	else if(threadName.equals("wstClient2")){
		
		//client tries to book recently added seat
		System.out.println("wstClient2 trying to book recently economy seat flight #1" + 
				server.bookFlight("Peter", "Pan", "222 Neverland", "555-555-5557",
						"Montreal", "01/11/2016", "Economy"));

	}
	else if(threadName.equals("wstClient3")){
		
		//client tries to book flight whose date was recently modified using old dates
		System.out.println("wstClient3 trying to book flight whose date has been modified" + 
				server.bookFlight("Micheline", "Man", "222 Car Drive", "555-555-5559",
						"Montreal", "01/11/2016", "Economy"));

	}


}
}


