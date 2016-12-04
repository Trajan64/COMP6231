package com.replica.vhs;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.omg.CORBA.ORB;
import FlightReservationApp.*;


public class ServerMTL extends FlightReservationPOA {
	
	public enum Cities {MTL, NDL, WST};
	
	private HashMap<Character, List<Passenger>> passengerDatabase;
	private HashMap<Cities, List<Flight>> flightDatabase;
	
	public final String CITY = "MTL";
	public final int PORT = 6793;
	
	public ORB orb;
	

	
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
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			
		}
		
	}
	
	public ServerMTL(){
		this.passengerDatabase = new HashMap<Character, List<Passenger>>();
		this.flightDatabase = new HashMap<Cities, List<Flight>>();
		
		HelperFunctions.writeToLog(this.CITY, "------------SERVER " + this.CITY +
				" CREATED ------------\n");
		
		this.startUDPServer(this.PORT);
	}
	
	@Override
	public String bookFlight(String firstName, String lastName, String address, String phone, String destination,
			String date,String classType) {
		
		//create passenger
		Passenger curr = new Passenger(firstName, lastName, address, phone, destination, date, classType, "");
		HelperFunctions.writeToLog(this.CITY, "bookFlight(" + firstName + ", " +lastName + ", " + address + ", " +
				phone + ", " + destination + ", " + date + ", " + classType + ")\n");
		
		
		switch(destination){
			case "NDL":
				return reserveFlightForBooking(flightDatabase.get(Cities.NDL), curr, false) ?  "Flight booking successful" : "false";
			case "WST":
				return reserveFlightForBooking(flightDatabase.get(Cities.WST), curr, false) ? "Flight booking successful" : "false";
			default:
				return "false";
		}
	
		
		
		
	}

	@Override
	public String getBookedFlightCount(String recordType) {
		
		HelperFunctions.writeToLog(this.CITY, "getBookedFlightCount(" + recordType + ")\n");
		
		byte[] m = recordType.getBytes();
		InetAddress aHost;
		try {
			aHost = InetAddress.getByName("localhost");
		
			int serverPort1 = 6794;
			int serverPort2 = 6795;
			
			int currServBooked = getBookedFlightByClassType(recordType);
			
			UDPThread requestThread1 = new UDPThread(m, aHost, serverPort1);
			UDPThread requestThread2 = new UDPThread(m, aHost, serverPort2);
			
			requestThread1.run();
			requestThread2.run();
			
			
			
			return this.CITY + ": " + currServBooked +
					" " + requestThread1.getReply() + " " + requestThread2.getReply();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return "";
		
		
	}

	@Override
	public String editFlightRecord(String recordID, String fieldName, String newValue) {
		
		
		HelperFunctions.writeToLog(this.CITY, "editFlightRecord(" + recordID + ", " + fieldName + ", " + 
				newValue + ")\n");
		
		//---------------------------------CREATE FLIGHT----------------------------------
		if(fieldName.equals("Create")){
			String flightDetails[] = newValue.split("\\s+");
			if(flightDetails.length < 7){
				return "false";
			}
			Flight newFlight = new Flight(flightDetails[0],flightDetails[1],flightDetails[2],flightDetails[3],
					Integer.parseInt(flightDetails[4]),Integer.parseInt(flightDetails[5]),Integer.parseInt(flightDetails[6]));
			
			if(newFlight.getDestinationCity().equals(this.CITY)){
				return "false";
			}
			
			switch(newFlight.getDestinationCity()){
				case "NDL":
					return createFlight(Cities.NDL, newFlight) ? "Succesfully created flight." : "false" ;
					
				case "WST":
					return createFlight(Cities.WST, newFlight) ? "Succesfully created flight." : "false" ;
				default:
					return "false";
				
			
			}
			
		}
		
		//------------------------------------DELETE FLIGHT---------------------------------------
		else if(fieldName.equals("Delete")){
			return deleteFlight(recordID)  ? "Succesfully deleted flight." : "false" ;
			
		}
		
		//------------------------------------MODIFY NUMBER OF SEATS------------------------------
		
		//Modify seats by class
		else if(fieldName.equals("Economy") || fieldName.equals("Business") || fieldName.equals("First Class")){
			int seats = Integer.parseInt(newValue);
			if(seats < 0 ) return "false";
			
			return modifySeatsByClass(recordID, fieldName, seats) ? "Succesfully modified the number of seats." : "false" ;
			
		}
		
		//------------------------------------MODIFY DATE OF DEPARTURE------------------------------
		else if(fieldName.equals("Date")){
			boolean flightFound = false;
			Iterator<Map.Entry<Cities, List<Flight>>> iteratorFlight = flightDatabase.entrySet().iterator();
			while(iteratorFlight.hasNext() && !flightFound){
				Map.Entry<Cities, List<Flight>> entryFlight = iteratorFlight.next();
				for(int i = 0; i < entryFlight.getValue().size(); i++){
					if(entryFlight.getValue().get(i).getRecordId().equals(recordID)){
						flightFound = true;
						synchronized(entryFlight.getValue().get(i)){
							entryFlight.getValue().get(i).setDepartureDate(newValue);
						}
						break;
					}
				}
			}
			if(!flightFound) return "false";
			
			Iterator<Map.Entry<Character, List<Passenger>>> iteratorPassenger = passengerDatabase.entrySet().iterator();
	        while(iteratorPassenger.hasNext()){
	            Map.Entry<Character, List<Passenger>> entry = iteratorPassenger.next();
	            for(int j = 0; j < entry.getValue().size(); j++){
	            	if(entry.getValue().get(j).getFlightNo().equals(recordID)){
	            		synchronized(entry.getValue().get(j)){
	            			entry.getValue().get(j).setDate(newValue);
	            			return "Successfully modified date of departure";
	            		}
	            		
	            	}
	            } 
	        }
	        return "false";
		}
		
		//------------------------------------MODIFY TIME OF DEPARTURE------------------------------
		else if(fieldName.equals("Time")){
			boolean flightFound = false;
			Iterator<Map.Entry<Cities, List<Flight>>> iteratorFlight = flightDatabase.entrySet().iterator();
			while(iteratorFlight.hasNext() && !flightFound){
				Map.Entry<Cities, List<Flight>> entryFlight = iteratorFlight.next();
				for(int i = 0; i < entryFlight.getValue().size(); i++){
					if(entryFlight.getValue().get(i).getRecordId().equals(recordID)){
						flightFound = true;
						synchronized(entryFlight.getValue().get(i)){
							entryFlight.getValue().get(i).setDepartureTime(newValue);
							return "Successfully modified time of departure";
						}
					}
				}
			}
			return "false";
		}
		return "false";
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
					
					replyMessage = "MTL;" + (getBookedFlightByClassType(new String(request.getData(), request.getOffset(), request.getLength())));
					buffer = replyMessage.getBytes();
					
			
					
				}
				else if(request_message.length == 10){
					Cities city = null;
					boolean successful = false;
					switch(request_message[6].trim()){
						case "MTL":
							System.out.println("Destination city: MTL");
							city = Cities.MTL;
							break;
						case "NDL":
							System.out.println("Destination city: NDL");
							city = Cities.NDL;
							break;
						case "WST":
							System.out.println("Destination city: WST");
							city = Cities.WST;
					}
					if(city != null){
						System.out.println("Creating passenger in server");
						
						Passenger person = new Passenger(request_message[2], request_message[3],
									request_message[4], request_message[5], request_message[6], 
									request_message[7], request_message[8], request_message[9]);

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
		
		
		HelperFunctions.writeToLog(this.CITY, "transferReservation(" + passengerID + ", " + currentCity + ", " + 
				otherCity + ")\n");
		
		System.out.println("Attempting to transfer reservation...");

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
            						serverPort = 6793;
            						break;
            					case "NDL":
            						serverPort = 6794;
            						break;
            					case "WST":
            						serverPort = 6795;
            						break;
            				}
            				
            				System.out.println("Sending request to port " + serverPort);
            				UDPThread requestThread = new UDPThread(m, aHost, serverPort);
            				requestThread.run();
            				
            				
            				if(requestThread.getReply().trim().equals("true")){
            					System.out.println("Passenger successfully transferred.\nRemoving passenger"
            							+ " in current server");
            					
            					boolean flightFound =false;
            					
            					synchronized(flightDatabase){
            						String recordID = entry.getValue().get(j).getFlightNo();
            						String classType = entry.getValue().get(j).getClassType();
            					
            						Iterator<Map.Entry<Cities, List<Flight>>> iteratorFlight = flightDatabase.entrySet().iterator();
                					while(iteratorFlight.hasNext() && !flightFound){
                						Map.Entry<Cities, List<Flight>> entryFlight = iteratorFlight.next();
                						for(int i = 0; i < entryFlight.getValue().size(); i++){
                							if((entryFlight.getValue().get(i).getRecordId()).equals(recordID)){
                								entry.getValue().remove(j);
                								
                								entryFlight.getValue().get(i).removePassenger(classTypeInt(classType), recordID);
                							}
                						}
                					}
            					
            					}
            					return "Successfully transferred passenger";
            				}
            				else{
            					System.out.println("Error unable to transfer passenger ");
            					return "false";
            				}
            			}catch (UnknownHostException e) {
            				e.printStackTrace();
            			}
            		}
            	} 
            }
		}
        System.out.println("Error: Did not find passengerId in database");
		return "false"; 
	}
	
	
	protected boolean createFlight(Cities serverCity, Flight newFlight){
		System.out.println("Creating Flight...");
		synchronized(this.flightDatabase){
			if(this.flightDatabase.get(serverCity) == null){
			
				List<Flight> tmp = new ArrayList<Flight>();
				tmp.add(newFlight);
				flightDatabase.put(serverCity, tmp);
				
				HelperFunctions.writeToLog(this.CITY, " Flight added: " + newFlight.toString() + "\n");
					
				return true;
			}
		}
		synchronized(flightDatabase.get(serverCity)){
			if(this.flightDatabase.get(serverCity) != null){
			
				flightDatabase.get(serverCity).add(newFlight);
		
				HelperFunctions.writeToLog(this.CITY, " Flight added: " + newFlight.toString() + "\n");

				
				return true;
			}
		}
		return false;
	}
	
	protected boolean deleteFlight(String recordID){
		boolean flightRemoved= false;
		System.out.println("Begin flight deletion");
		synchronized(flightDatabase){
			synchronized(passengerDatabase){
				Iterator<Map.Entry<Character, List<Passenger>>> iteratorPassenger = passengerDatabase.entrySet().iterator();
		        while(iteratorPassenger.hasNext()){
		            Map.Entry<Character, List<Passenger>> entry = iteratorPassenger.next();
		            for(int j = 0; j < entry.getValue().size(); j++){
		            	if(entry.getValue().get(j).getFlightNo().equals(recordID)){
		            		entry.getValue().remove(j);
		            		System.out.println("Passenger removed.");
		            	}
		            } 
		        }
			}
			
			Iterator<Map.Entry<Cities, List<Flight>>> iteratorFlight = flightDatabase.entrySet().iterator();
			while(iteratorFlight.hasNext() && !flightRemoved){
				Map.Entry<Cities, List<Flight>> entryFlight = iteratorFlight.next();
				for(int i = 0; i < entryFlight.getValue().size(); i++){
					if(entryFlight.getValue().get(i).getRecordId().equals(recordID)){
						entryFlight.getValue().remove(i);
						flightRemoved = true;
						System.out.println("Removing flight...");
						return true;
					}
				}
			}
			if(!flightRemoved) return false;
		}
		
		return false;
	}
	
	protected boolean modifySeatsByClass(String recordID, String classType, int seats){
		
		int classDiff = 0;
		System.out.println("Modifying seat by class");
		
		boolean flightFound= false;

			
		Iterator<Map.Entry<Cities, List<Flight>>> iteratorFlight = flightDatabase.entrySet().iterator();
		while(iteratorFlight.hasNext() && !flightFound){
			Map.Entry<Cities, List<Flight>> entryFlight = iteratorFlight.next();
			for(int i = 0; i < entryFlight.getValue().size(); i++){
				System.out.println("Trying to find a matching flight...");
				if(entryFlight.getValue().get(i).getRecordId().equals(recordID)){
					
					synchronized(entryFlight.getValue()){
						
						System.out.println("Num seats booked " + entryFlight.getValue().get(i).getNumSeatsBooked(classTypeInt(classType)));
						classDiff = entryFlight.getValue().get(i).getNumSeatsBooked(classTypeInt(classType)) + 
								entryFlight.getValue().get(i).getNumberSeatsAvailable(classType) - seats;
						
						int numPassengersToDelete = 0;
						if(classDiff >= 0)
							numPassengersToDelete = classDiff - entryFlight.getValue().get(i).getNumberSeatsAvailable(classType);
						else if (entryFlight.getValue().get(i).getNumberSeatsAvailable(classType) - classDiff > 0){
							numPassengersToDelete = classDiff - entryFlight.getValue().get(i).getNumberSeatsAvailable(classType);
							System.out.println("num seats available " + numPassengersToDelete);
						}
						else{
							numPassengersToDelete = classDiff;
						}
						System.out.println("Difference in seats is " + classDiff);
						
						if(seats == 0) numPassengersToDelete = classDiff;

						entryFlight.getValue().get(i).setNumberSeats(classType, numPassengersToDelete );
						

						System.out.println("Number of seats modified.");
						
						flightFound = true;
						
						if(entryFlight.getValue().get(i).getNumSeatsBooked(classTypeInt(classType)) == 0) return true;
						
						
						synchronized(passengerDatabase){
							Iterator<Map.Entry<Character, List<Passenger>>> iterator = passengerDatabase.entrySet().iterator();
							
							while(iterator.hasNext() && (numPassengersToDelete > 0)){
					            Map.Entry<Character, List<Passenger>> entry = iterator.next();
					            for(int j = 0; j < entry.getValue().size(); j++){
					            	if(numPassengersToDelete <= 0) return true; 
					            	System.out.println("Passenger exists in flight db " + entryFlight.getValue().get(i).passengerExists(entry.getValue().get(j).getRecordId(), classTypeInt(classType)));
					            	if(numPassengersToDelete > 0 && entry.getValue().get(j).getFlightNo().equals(recordID) && entry.getValue().get(j).getClassType().equals(classType)
					            			&& !entryFlight.getValue().get(i).passengerExists(entry.getValue().get(j).getRecordId(), classTypeInt(classType))){
					            		System.out.println("Removing Passengers from passenger database");
					            		entry.getValue().remove(i);
					            		numPassengersToDelete--;
					            		
					            		
					            		
					            	}
					            }
					            
					            if(numPassengersToDelete <= 0) return true;
					          
					        }
							
						}
						if(classDiff < 0) return true;
						break;
					}
					
				}
			}
		}

		
		return false;
	}
	
	protected int getBookedFlightByClassType(String classType){

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
		            	
		            	seatCount++;

		            } 
		        }
			}
	        return seatCount;
		}
	}
	
	protected boolean reserveFlightForBooking(List<Flight> cityFlights,
			Passenger person, boolean isFlightTransfer){
			System.out.println("Reserving flight for passenger");
			if(cityFlights == null){
				System.out.println("No flights offered on this day");
				return false;
			}
			boolean seatsAvailable = false;
			synchronized(cityFlights){
				System.out.println("Number of flights on this day " + cityFlights.size());
				for(int i = 0; i < cityFlights.size(); i++){
					
					System.out.println(cityFlights.get(i));
					
					if((isFlightTransfer ||  person.getFlightNo().equals("")) && 
							cityFlights.get(i).getDepartureDate().equals(person.getDate())){
						
						if(cityFlights.get(i).getNumberSeatsAvailable(person.getClassType()) > 0 ){
							seatsAvailable = true;
						
					
							cityFlights.get(i).setNumberSeats(person.getClassType(),
									-1 * (cityFlights.get(i).getNumberSeatsAvailable(person.getClassType()) -1));
							
							person.setFlightNo(cityFlights.get(i).getRecordId());
							cityFlights.get(i).addPassengers(classTypeInt(person.getClassType()), person);
							
	
					
							
							System.out.println(person);
							//if there are no passengers in database whose last names start with that specific character
							//create a list, insert the passenger and add it to hashmap
							synchronized(passengerDatabase){
								if(passengerDatabase.get(person.getLastName().charAt(0)) == null){
									System.out.println("List empty at passenger's last name");
									
									List<Passenger> temp = new ArrayList<Passenger>();
									temp.add(person);
									passengerDatabase.put(person.getLastName().charAt(0), temp);
								
									HelperFunctions.writeToLog(cityFlights.get(i).getDepartureCity(), " Passenger added: " + person.toString() + "\n");
									
									System.out.println("it worked!");
									return true;
									
								}
								
							
							}
							synchronized(passengerDatabase.get(person.getLastName().charAt(0))){
								
								if(passengerDatabase.get(person.getLastName().charAt(0)) != null){
									System.out.println("List not empty at passenger's last name");
									passengerDatabase.get(person.getLastName().charAt(0)).add(person);
									
									HelperFunctions.writeToLog(cityFlights.get(i).getDepartureCity(), " Passenger added: " + person.toString() + "\n");
									return true;
								}
	
								
							}
						}
						
					}
					
				}
				if(!seatsAvailable){
					System.out.println("No seats available");
					return false;
				}
			}
			System.out.println("Unable to book");
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
