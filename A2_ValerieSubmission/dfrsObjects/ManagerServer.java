package dfrsObjects;
import DFRSApp.*;
import dfrsObjects.ManagerServer.Cities;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.time.OffsetDateTime;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import java.util.Properties;

public class ManagerServer extends DFRSPOA{
	
	public enum Cities {MTL, NDL, WST};
	
	private HashMap<Character, List<Passenger>> passengerDatabase;
	private HashMap<Cities, List<Flight>> flightDatabase;
	private String serverCity;
	
	public ORB orb;
	
	public void setORB(ORB orb_val){
		orb = orb_val;
	}
	
	private class UDPThread implements Runnable{
		
		private byte[] m = new byte[1024];
		private InetAddress aHost;
		private int serverPort;
		private String replyMessage;
		
		public UDPThread(byte[] m, InetAddress aHost, int serverPort){
			Arrays.fill(this.m,(byte)0);
			this.m = m;
			this.aHost = aHost;
			this.serverPort = serverPort;
			this.replyMessage = "";
			
		}
		
		public String getReply(){
			return this.replyMessage;
		}

		@Override
		public void run() {
			System.out.println("Started thread");
	        try { 
	                sendPacket(); 
	        } catch(Exception e) {}
	        System.out.println("Finished thread");
			
		}
		
		private void sendPacket(){
			DatagramSocket aSocket = null;
			try{

				aSocket = new DatagramSocket();
				DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
				
				System.out.println("Sending request " + new String(request.getData(), request.getOffset(), request.getLength()));
				aSocket.send(request);
				
				byte[] buffer = new byte[1024];
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				
				reply.setLength(1024);
				aSocket.receive(reply);
				aSocket.close();
				this.replyMessage = new String(reply.getData(), reply.getOffset(), reply.getLength());
				
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
	}
	
	public ManagerServer(){
		
	}
	
	public ManagerServer(String serverCity){
		this.passengerDatabase = new HashMap<Character, List<Passenger>>();
		this.flightDatabase = new HashMap<Cities, List<Flight>>();
		this.serverCity = serverCity;
		
		HelperFunctions.writeToLog(this.serverCity, "------------SERVER " + this.serverCity +
				" CREATED ------------\n");
		
	}

	@Override
	public String bookFlight(String firstName, String lastName, String address, String phone, String destination,
			String date,String classType) {
		
		//create passenger
		Passenger curr = new Passenger(firstName, lastName, address, phone, destination, date, classType, "");
		
		switch(destination){
			case "Montreal":
				return reserveFlightForBooking(flightDatabase.get(Cities.MTL), curr, false) ? "Flight booking successful" : "Unable to book flight";
			case "New Delhi":
				return reserveFlightForBooking(flightDatabase.get(Cities.NDL), curr, false) ?  "Flight booking successful" : "Unable to book flight";
			case "Washington":
				return reserveFlightForBooking(flightDatabase.get(Cities.WST), curr, false) ? "Flight booking successful" : "Unable to book flight";
			default:
				return "Unable to book flight.";
		}
	
		
		
		
	}

	@Override
	public String getBookedFlightCount(String recordType) {
		
		byte[] m = recordType.getBytes();
		InetAddress aHost;
		try {
			aHost = InetAddress.getByName("localhost");
		
			int serverPort1 = 0;
			int serverPort2 = 0;
			switch(this.serverCity){
				case "MTL":
					serverPort1 = 6790;
					serverPort2 = 6791;
					break;
				case "NDL":
					serverPort1 = 6789;
					serverPort2 = 6791;
					break;
				case "WST":
					serverPort1 = 6789;
					serverPort2 = 6790;
					break;
			}
			
			int currServBooked = getBookedFlightByClassType(recordType);
			
			UDPThread requestThread1 = new UDPThread(m, aHost, serverPort1);
			UDPThread requestThread2 = new UDPThread(m, aHost, serverPort2);
			
			requestThread1.run();
			requestThread2.run();
			
			
			
			return this.serverCity + ": " + currServBooked +
					" " + requestThread1.getReply() + " " + requestThread2.getReply();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
		
		
	}

	@Override
	public String editFlightRecord(String recordID, String fieldName, String newValue) {
		
		//---------------------------------CREATE FLIGHT----------------------------------
		if(fieldName.equals("Create")){
			String flightDetails[] = newValue.split("\\s+");
			if(flightDetails.length < 7){
				return "Error: Unable to create flight.";
			}
			Flight newFlight = new Flight(flightDetails[0],flightDetails[1],flightDetails[2],flightDetails[3],
					Integer.parseInt(flightDetails[4]),Integer.parseInt(flightDetails[5]),Integer.parseInt(flightDetails[6]));
			
			if(newFlight.getDestinationCity().equals(serverCity)){
				return "Error: Unable to create flight.";
			}
			
			switch(newFlight.getDestinationCity()){
				case "MTL":
					return createFlight(Cities.MTL, newFlight) ? "Succesfully created flight." : "Error: Unable to create flight" ;
				case "NDL":
					return createFlight(Cities.NDL, newFlight) ? "Succesfully created flight." : "Error: Unable to create flight" ;
					
				case "WST":
					return createFlight(Cities.WST, newFlight) ? "Succesfully created flight." : "Error: Unable to create flight" ;
				default:
					return "Error: Unable to create flight.";
				
			
			}
			
		}
		
		//------------------------------------DELETE FLIGHT---------------------------------------
		else if(fieldName.equals("Delete")){
			return deleteFlight(recordID)  ? "Succesfully deleted flight." : "Error: Unable to delete flight" ;
			
		}
		
		//------------------------------------MODIFY NUMBER OF SEATS------------------------------
		else if(fieldName.equals("Seats")){
			String seats[] = newValue.split("\\s+");
			if(seats.length < 3) return "Error: Unable to modify the number of seats";
			
			return modifyAllSeats(recordID, seats) ? "Succesfully modified the number of seats." : "Error: Unable to modify the number of seats";
			
		}
		
		//Modify seats by class
		else if(fieldName.equals("Economy") || fieldName.equals("Business") || fieldName.equals("First Class")){
			int seats = Integer.parseInt(newValue);
			if(seats < 0 ) return "Error: Unable to modify the number of seats";
			
			return modifySeatsByClass(recordID, fieldName, seats) ? "Succesfully modified the number of seats." : "Error: Unable to modify the number of seats" ;
			
		}
		
		//------------------------------------MODIFY DATE OF DEPARTURE------------------------------
		else if(fieldName.equals("Date")){
			boolean flightFound = false;
			Iterator<Map.Entry<Cities, List<Flight>>> iteratorFlight = flightDatabase.entrySet().iterator();
			while(iteratorFlight.hasNext() && !flightFound){
				Map.Entry<Cities, List<Flight>> entryFlight = iteratorFlight.next();
				for(int i = 0; i < entryFlight.getValue().size(); i++){
					if(entryFlight.getValue().get(i).getRecordId() == recordID){
						flightFound = true;
						synchronized(entryFlight.getValue().get(i)){
							entryFlight.getValue().get(i).setDepartureDate(newValue);
						}
						break;
					}
				}
			}
			if(!flightFound) return "Error: unable to modify date of departure";
			
			Iterator<Map.Entry<Character, List<Passenger>>> iteratorPassenger = passengerDatabase.entrySet().iterator();
	        while(iteratorPassenger.hasNext()){
	            Map.Entry<Character, List<Passenger>> entry = iteratorPassenger.next();
	            for(int j = 0; j < entry.getValue().size(); j++){
	            	if(entry.getValue().get(j).getRecordId() == recordID){
	            		synchronized(entry.getValue().get(j)){
	            			entry.getValue().get(j).setDate(newValue);
	            			return "Successfully modified date of departure";
	            		}
	            		
	            	}
	            } 
	        }
	        return "Error: unable to modify date of departure";
		}
		
		//------------------------------------MODIFY TIME OF DEPARTURE------------------------------
		else if(fieldName.equals("Time")){
			boolean flightFound = false;
			Iterator<Map.Entry<Cities, List<Flight>>> iteratorFlight = flightDatabase.entrySet().iterator();
			while(iteratorFlight.hasNext() && !flightFound){
				Map.Entry<Cities, List<Flight>> entryFlight = iteratorFlight.next();
				for(int i = 0; i < entryFlight.getValue().size(); i++){
					if(entryFlight.getValue().get(i).getRecordId() == recordID){
						flightFound = true;
						synchronized(entryFlight.getValue().get(i)){
							entryFlight.getValue().get(i).setDepartureTime(newValue);
							return "Successfully modified time of departure";
						}
					}
				}
			}
			return "Error: unable to modify time of departure";
		}
		return "Error: unable to modify time of departure";
	}
	
	public void startUDPServer(int portNum){
		DatagramSocket aSocket = null;
		try {
			System.out.println("Listening on port " + portNum);
			aSocket = new DatagramSocket(portNum);
			
			while(true){
				byte[] buffer = new byte[1024];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				request.setLength(1024);
				aSocket.receive(request);
				
				String request_message[] = new String(request.getData(), request.getOffset(), request.getLength()).split("\\s+");
				String replyMessage = "";
				
				System.out.println("Request: " +  new String(request.getData(), request.getOffset(), request.getLength()) + " length: " + request_message.length);
				
				if(request_message.length == 1){
					switch(serverCity){
					case "MTL":
						replyMessage = "MTL: " + (getBookedFlightByClassType(new String(request.getData(), request.getOffset(), request.getLength())));
						buffer = replyMessage.getBytes();
						break;
					case "NDL":
						replyMessage = "NDL: " + (getBookedFlightByClassType(new String(request.getData(), request.getOffset(), request.getLength())));
						buffer = replyMessage.getBytes();
						break;
					case "WST":
						replyMessage = "WST: " + (getBookedFlightByClassType(new String(request.getData(), request.getOffset(), request.getLength())));
						buffer = replyMessage.getBytes();
						break;
					
					}
					
				}
				else if(request_message.length == 10){
					Cities city = null;
					boolean successful = false;
					switch(request_message[6].trim()){
						case "Montreal":
							System.out.println("Destination city: MTL");
							city = Cities.MTL;
							break;
						case "NewDelhi":
							System.out.println("Destination city: NDL");
							city = Cities.NDL;
							break;
						case "Washington":
							System.out.println("Destination city: WST");
							city = Cities.WST;
					}
					if(city != null){
						System.out.println("Creating passenger in server");
						
						Passenger person;
						
						if(request_message[6].equals("NewDelhi")){
							person = new Passenger(request_message[2], request_message[3],
									request_message[4], request_message[5], "New Delhi", 
									request_message[7], request_message[8], request_message[9]);
						}
						else{
							person = new Passenger(request_message[2], request_message[3],
									request_message[4], request_message[5], request_message[6], 
									request_message[7], request_message[8], request_message[9]);
						}
						if(flightDatabase.get(city) != null){
							successful = reserveFlightForBooking(flightDatabase.get(city), person, true);
						} else System.out.print("Flight does not exist in database ");
						if(successful) replyMessage = "true";
						else replyMessage = "false";
						buffer = replyMessage.getBytes();
						
					}
				}
				
				
				DatagramPacket reply = new DatagramPacket(buffer, replyMessage.length(), request.getAddress(), request.getPort());
				aSocket.send(reply);
				
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		}
		finally{
			if(aSocket != null)
				aSocket.close();
		}
	}


	@Override
	public String transferReservation(String passengerID, String currentCity, String otherCity) {
		
		System.out.println("Attempting to transfer reservation...");
		String message = "";
		Iterator<Map.Entry<Character, List<Passenger>>> iteratorPassenger = passengerDatabase.entrySet().iterator();
        while(iteratorPassenger.hasNext()){
            Map.Entry<Character, List<Passenger>> entry = iteratorPassenger.next();
            for(int j = 0; j < entry.getValue().size(); j++){
            	synchronized(entry.getValue()){
            		if(entry.getValue().get(j).getRecordId().equals(passengerID)){
            			System.out.println("Reservation to transfer " +
            					currentCity + " " + otherCity + " " + 
            					entry.getValue().get(j).recordTransferString());
            			byte[] m = (currentCity + " " + otherCity + " " + 
            					entry.getValue().get(j).recordTransferString()).getBytes();
            			InetAddress aHost;
            			try {
            				aHost = InetAddress.getByName("localhost");
            			
            				int serverPort = 0;
            				switch(otherCity){
            					case "MTL":
            						serverPort = 6789;
            						break;
            					case "NDL":
            						serverPort = 6790;
            						break;
            					case "WST":
            						serverPort = 6791;
            						break;
            				}
            				
            				System.out.println("Sending request to port " + serverPort);
            				UDPThread requestThread = new UDPThread(m, aHost, serverPort);
            				requestThread.run();
            				
            				
            				if(requestThread.getReply().trim().equals("true")){
            					System.out.println("Passenger successfully transferred.\nRemoving passenger"
            							+ " in current server");
            					iteratorPassenger.remove();
            					return "Successfully transferred passenger";
            				}
            				else{
            					System.out.println("Error unable to transfer passenger ");
            					return "Error: unable to transfer passenger";
            				}
            			}catch (UnknownHostException e) {
            				// TODO Auto-generated catch block
            				e.printStackTrace();
            			}
            		}
            	} 
            }
		}
        System.out.println("Error: Did not find passengerId in database");
		return "Error: unable to transfer passenger"; 
	}
	
	public void printFlightDatabase(){
		Iterator<Map.Entry<Cities, List<Flight>>> iteratorFlight = this.flightDatabase.entrySet().iterator();
		while(iteratorFlight.hasNext()){
			Map.Entry<Cities, List<Flight>> entryFlight = iteratorFlight.next();
			for(int i = 0; i < entryFlight.getValue().size(); i++){
				System.out.println(entryFlight.getValue().get(i));
			}
		}
		System.out.println("Done printing flights");
	}
	
	
	protected boolean createFlight(Cities serverCity, Flight newFlight){
		System.out.println("Creating Flight...");
		synchronized(this.flightDatabase){
			if(this.flightDatabase.get(serverCity) == null){
			
				List<Flight> tmp = new ArrayList<Flight>();
				tmp.add(newFlight);
				flightDatabase.put(serverCity, tmp);
			}
		}
		synchronized(flightDatabase.get(serverCity)){
			if(this.flightDatabase.get(serverCity) != null){
			
				flightDatabase.get(serverCity).add(newFlight);
			}
		}
		return true;
	}
	
	protected boolean deleteFlight(String recordID){
		boolean flightRemoved= false;
		synchronized(flightDatabase){
			synchronized(passengerDatabase){
				Iterator<Map.Entry<Character, List<Passenger>>> iteratorPassenger = passengerDatabase.entrySet().iterator();
		        while(iteratorPassenger.hasNext()){
		            Map.Entry<Character, List<Passenger>> entry = iteratorPassenger.next();
		            for(int j = 0; j < entry.getValue().size(); j++){
		            	if(entry.getValue().get(j).getRecordId().equals(recordID)){
		            		iteratorPassenger.remove();
		            		return true;
		            	}
		            } 
		        }
			}
			
			Iterator<Map.Entry<Cities, List<Flight>>> iteratorFlight = flightDatabase.entrySet().iterator();
			while(iteratorFlight.hasNext() && !flightRemoved){
				Map.Entry<Cities, List<Flight>> entryFlight = iteratorFlight.next();
				for(int i = 0; i < entryFlight.getValue().size(); i++){
					if(entryFlight.getValue().get(i).getRecordId() == recordID){
						iteratorFlight.remove();
						flightRemoved = true;
						break;
					}
				}
			}
			if(!flightRemoved) return false;
		}
		
		return false;
	}
	protected boolean modifyAllSeats(String recordID, String[] seats){
		int economyDiff = 0;
		int businessDiff = 0;
		int firstDiff = 0;
		
		boolean flightFound= false;

			
		Iterator<Map.Entry<Cities, List<Flight>>> iteratorFlight = flightDatabase.entrySet().iterator();
		while(iteratorFlight.hasNext() && !flightFound){
			Map.Entry<Cities, List<Flight>> entryFlight = iteratorFlight.next();
			for(int i = 0; i < entryFlight.getValue().size(); i++){
				synchronized(entryFlight.getValue()){
					if(entryFlight.getValue().get(i).getRecordId() == recordID){
					
					
						economyDiff = entryFlight.getValue().get(i).getNumberSeatsAvailable("Economy") - Integer.parseInt(seats[0]);
						businessDiff = entryFlight.getValue().get(i).getNumberSeatsAvailable("Business") - Integer.parseInt(seats[1]);
						firstDiff = entryFlight.getValue().get(i).getNumberSeatsAvailable("First Class") - Integer.parseInt(seats[2]);
						
						int diff = economyDiff + businessDiff + firstDiff;
						
						entryFlight.getValue().get(i).setNumberSeats("Economy", Integer.parseInt(seats[0]));
						entryFlight.getValue().get(i).setNumberSeats("Business", Integer.parseInt(seats[1]));
						entryFlight.getValue().get(i).setNumberSeats("First Class", Integer.parseInt(seats[2]));
						
						flightFound = true;
						
						synchronized(passengerDatabase){
							Iterator<Map.Entry<Character, List<Passenger>>> iterator = passengerDatabase.entrySet().iterator();
							
							while(iterator.hasNext() && (economyDiff > 0 || businessDiff > 0 || firstDiff > 0)){
					            Map.Entry<Character, List<Passenger>> entry = iterator.next();
					            for(int j = 0; j < entry.getValue().size(); j++){
					            	if(entry.getValue().get(j).getFlightNo() == recordID && !entryFlight.getValue().get(i).passengerExists(entry.getValue().get(j).getRecordId(), -1)){
					            		iterator.remove();
					            		diff--;
					            		
					            	}
					            	
					            }
					            
					            if(diff <= 0) return true;
					          
					        }
							
						}
						
						break;
					}
					
				}
			}
		}
		return false;
	}
	protected boolean modifySeatsByClass(String recordID, String classType, int seats){
		
		int classDiff = 0;

		
		boolean flightFound= false;

			
		Iterator<Map.Entry<Cities, List<Flight>>> iteratorFlight = flightDatabase.entrySet().iterator();
		while(iteratorFlight.hasNext() && !flightFound){
			Map.Entry<Cities, List<Flight>> entryFlight = iteratorFlight.next();
			for(int i = 0; i < entryFlight.getValue().size(); i++){
				if(entryFlight.getValue().get(i).getRecordId() == recordID){
					
					synchronized(entryFlight.getValue()){
						classDiff = entryFlight.getValue().get(i).getNumberSeatsAvailable(classType) - seats;
						
						entryFlight.getValue().get(i).setNumberSeats(classType, seats);

						
						flightFound = true;
						
						synchronized(passengerDatabase){
							Iterator<Map.Entry<Character, List<Passenger>>> iterator = passengerDatabase.entrySet().iterator();
							
							while(iterator.hasNext() && classDiff > 0){
					            Map.Entry<Character, List<Passenger>> entry = iterator.next();
					            for(int j = 0; j < entry.getValue().size(); j++){
					            	if(classDiff > 0 && entry.getValue().get(j).getRecordId() == recordID && entry.getValue().get(j).getClassType().equals(classType)
					            			&& !entryFlight.getValue().get(i).passengerExists(entry.getValue().get(j).getRecordId(), classTypeInt(classType))){
					            		iterator.remove();
					            		classDiff--;
					            		
					            	}
					            }
					            
					            if(classDiff <= 0) return true;
					          
					        }
							
						}
						
						break;
					}
					
				}
			}
		}

		
		return false;
	}
	
	protected int getBookedFlightByClassType(String classType){
		// TODO add classtype ALL which fetches for the flight classs
		int seatCount = 0;
		
		synchronized(passengerDatabase){
			Iterator<Map.Entry<Character, List<Passenger>>> iteratorPassenger = passengerDatabase.entrySet().iterator();
			if(classType.equals("Economy") || classType.equals("Business") || classType.equals("First Class")){
		        while(iteratorPassenger.hasNext()){
		            Map.Entry<Character, List<Passenger>> entry = iteratorPassenger.next();
		            for(int j = 0; j < entry.getValue().size(); j++){
		            	if(entry.getValue().get(j).getClassType().equals(classType)){
		            		seatCount++;
		            	}
		            } 
		        }
			}
			else{
				while(iteratorPassenger.hasNext()){
		            Map.Entry<Character, List<Passenger>> entry = iteratorPassenger.next();
		            for(int j = 0; j < entry.getValue().size(); j++){
		            	if(entry.getValue().get(j).getClassType().equals(classType)){
		            		seatCount++;
		            	}
		            } 
		        }
			}
	        return seatCount;
		}
	}
	
	protected boolean reserveFlightForBooking(List<Flight> cityFlights,
			Passenger person, boolean isFlightTransfer){
			System.out.println("Reserving flight for passenger");
			if(cityFlights == null) return false;
			for(int i = 0; i < cityFlights.size(); i++){
				if((isFlightTransfer || cityFlights.get(i).getRecordId() == person.getFlightNo()) && 
						cityFlights.get(i).getDepartureDate().equals(person.getDate())){
					if(cityFlights.get(i).getNumberSeatsAvailable(person.getClassType()) == 0){
						return false;
					}
					synchronized(cityFlights){
						cityFlights.get(i).setNumberSeats(person.getClassType(),
								cityFlights.get(i).getNumberSeatsAvailable(person.getClassType()) -1);
						person.setFlightNo(cityFlights.get(i).getRecordId());

				
						
						System.out.println(person);
						//if there are no passengers in database whose last names start with that specific character
						//create a list, insert the passenger and add it to hashmap
						synchronized(passengerDatabase){
							if(passengerDatabase.get(person.getLastName().charAt(0)) == null){
							
								
								List<Passenger> temp = new ArrayList<Passenger>();
								temp.add(person);
								passengerDatabase.put(person.getLastName().charAt(0), temp);
							
								HelperFunctions.writeToLog(cityFlights.get(i).getDepartureCity(), " Passenger added: " + person.toString() + "\n");
								
								System.out.println("it worked!");
								return true;
								
							}
							
						
						}
						synchronized(passengerDatabase.get(person.getLastName().charAt(0))){
							
							if(passengerDatabase.get(person.getLastName().charAt(0)) == null){
								passengerDatabase.get(person.getLastName().charAt(0)).add(person);
								
								HelperFunctions.writeToLog(cityFlights.get(i).getDepartureCity(), " Passenger added: " + person.toString() + "\n");
								return true;
							}

							
						}
						
					}
					
				}
			}
			return false;
		
	}
	
	public static int classTypeInt(String classType){
		switch(classType){
			case "Economy":
				return 0;
			case "Business":
				return 1;
			case "First Class":
				return 2;
		}
		return -1;
			
		
	}

	public void shutdown() {
		orb.shutdown(false);
		
	}

	

}
