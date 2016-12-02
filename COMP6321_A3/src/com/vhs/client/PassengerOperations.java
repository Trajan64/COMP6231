package com.vhs.client;

import java.util.Scanner;

import com.vhs.server.ManagerServer;
import com.vhs.server.ManagerServerImplServiceMTL;
import com.vhs.server.ManagerServerImplServiceNDL;
import com.vhs.server.ManagerServerImplServiceWST;


public class PassengerOperations {
	
	protected static void flightBooking(){
		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		
		System.out.println("Please enter your first name");
		String firstName = keyboard.nextLine();
		
		System.out.println("Please enter your last name");
		String lastName = keyboard.nextLine();
		
		System.out.println("Please enter your address");
		String address = keyboard.nextLine();
		
		System.out.println("Please enter your phone number");
		String phone = keyboard.nextLine();
		
		int departureCity = 0;
		System.out.println("Please select your city of departure");
		System.out.println("1.Montreal\n2.New Delhi\n3.Washington");
		departureCity = HelperFunctions.verifyValidInt();
		
		while(departureCity < 1 || departureCity > 3){
			System.out.println("Invalid input.Please select a number between 1 and 3.");
			departureCity = HelperFunctions.verifyValidInt();
		}
		
		ManagerServer server = null;
		String[] destinations = new String[2];
		
		switch(departureCity){
			case 1:
				destinations[0] = "NDL";
				destinations[1] = "WST";
				ManagerServerImplServiceMTL mtl = new ManagerServerImplServiceMTL();
				server = mtl.getManagerServerImplPort();
				break;
			case 2:
				destinations[0] = "MTL";
				destinations[1] = "WST";
				ManagerServerImplServiceNDL ndl = new ManagerServerImplServiceNDL();
				server = ndl.getManagerServerImplPort();
				break;
			case 3:
				destinations[0] = "MTL";
				destinations[1] = "NDL";
				ManagerServerImplServiceWST wst = new ManagerServerImplServiceWST();
				server = wst.getManagerServerImplPort();
				break;
		}
		
		int destinationCity = 0;
		System.out.println("Please select your destination city");
		System.out.println("1."+destinations[0] + "\n2." + destinations[1]);
		
		destinationCity = HelperFunctions.verifyValidInt();
		String destination = "";
		
		while(destinationCity < 1 || destinationCity > 2){
			System.out.println("Invalid input.Please select a number between 1 and 2.");
			destinationCity = HelperFunctions.verifyValidInt();
		}
		
		switch(destinationCity){
			case 1:
				destination = destinations[0];
				break;
			case 2:
				destination = destinations[1];
				break;
		}
		
		System.out.println("Please enter the date of departure in the format dd/mm/yyyy");
		String departureDate = keyboard.nextLine();
		System.out.println("Please select class type");
		
		int user_class = 0;
		
		System.out.println("1.Economy\n2.Business\n3.First");
		user_class = HelperFunctions.verifyValidInt();
		
		String class_type = "";
		
		while(user_class < 1 || user_class > 3){
			System.out.println("Invalid input.Please select a number between 1 and 3.");
			user_class = HelperFunctions.verifyValidInt();
		}
		switch(user_class){
			case 1:
				class_type = "Economy";
				break;
			case 2:
				class_type = "Business";
				break;
			case 3:
				class_type = "First Class";
				break;
		}
		
		
		if(server!=null)
			System.out.println(server.bookFlight(firstName, lastName, address, phone, destination, departureDate, class_type));
		else System.out.println("Unable to book flight");
	}

}
