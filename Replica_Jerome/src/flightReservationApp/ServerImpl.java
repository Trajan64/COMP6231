package flightReservationApp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.omg.CORBA.ORB;

public class ServerImpl extends serverPOA {

	private 	String 										m_city;
	public 		HashMap<Character, LinkedList<Passenger>> 	passengerRecords;
	public 		Flight[] 									flightRecords;
	private 	ServerListener 								m_serverListener;
	private 	int 										m_port;
	public 		ServerInformation[] 						m_servers;
	private		MyLogger									m_logger;
	public		int											m_passengerCounter;
	private		ORB											m_orb;
	
	private final static int	MAX_FLIGHTS	= 			1024;
	
	
	public ServerImpl(String city) {
		
		m_city = city;
		
		
	}
	
	@Override
	public String bookFlight(String firstName, String lastName, String address, String phone, String destination,
			String date, int classType)  {
		
		m_logger.log("- bookFlight called with arguments: (" + firstName + ", " + lastName + ", " + address + ", " + phone + ", " + destination + ", " + date + ", " + classType + ")");
		
		String message;
		
		// Convert date string to a Date object.
		Date properDate = fromStringToDate(date);
		
		if (properDate == null) {
			message = "Badly formed date.";
		}
		
		else {
		
			BookingInformation bookingInformation = addPassengerToFlight(firstName, lastName, address, phone, destination, properDate, classType);
			
			
			// Prepare reply message based on status of booking operation.
			switch(bookingInformation.m_status) {
			
				case BookingInformation.NO_FLIGHT_FOUND:
					message = "fail";
					break;
					
				case BookingInformation.FLIGHT_FOUND_BUT_NO_SEATS_AVAILABLE:
					message = "fail";
					break;
					
				case BookingInformation.SUCCESSFULL_BOOKING:
					message = "success";
					break;
				
				default:
					// We should not go through here.
					message = "";
					break;
			}
		
		}
		
		m_logger.log(message);
		return message;
		
	}
	
	
	
	
	class BookingInformation {
		
		public static final int NO_FLIGHT_FOUND = 0;
		public static final int FLIGHT_FOUND_BUT_NO_SEATS_AVAILABLE = 1;
		public static final int SUCCESSFULL_BOOKING = 2;
		
		public int 			m_status;
		public Passenger 	m_passengerRecord;
		public Flight		m_associatedFlight;
		
		BookingInformation(int status, Passenger passengerRecord, Flight associatedFlight) {
			
			m_status = status;
			m_passengerRecord = passengerRecord;
			m_associatedFlight = associatedFlight;
			
		}
		
	}
	
	
	private synchronized int generatePassengerId() {
		
		int counter = m_passengerCounter;
		m_passengerCounter++;
		return counter;
		
	}
	
	
	
	@SuppressWarnings("unused")
	private BookingInformation addPassengerToFlight(String firstName, String lastName, String address, String phone, String destination,
			Date date, int classType) {
		
		boolean flightFoundNoSeatsAvaialble = false;		
		
		// Scan through the flights.
		int i = 0;
		while (i < MAX_FLIGHTS) {
			
			Flight flightToScan = flightRecords[i];
			if (flightToScan == null) {
				
				// No flight found at the index.
				// Continue the iteration.
				i++;
				continue;
			}
			
			if ((flightToScan.getDestination().equals(destination)) && (flightToScan.getTimeOfDeparture().equals(date))) {
				
				synchronized (flightToScan) {
				
					if ((flightToScan == null) || (!((flightToScan.getDestination().equals(destination)) && (flightToScan.getTimeOfDeparture().equals(date))))) {
						
						// The flight object may have been removed or modified during the time spent waiting for the lock.
						// We need to enforce this second check to make sure that the flight still exist/is still valid.
						i++;
						continue;
					
					}
					
					// Check if the flight has an available seat for the specified class type.
					if (flightToScan.getAvailableSeatsForClassType(classType) > 0) {
						
						// We found a flight.
						
						// We first create a new passenger entry and append it to the passenger record.
						Passenger newPassenger = new Passenger(generatePassengerId(), firstName, lastName, address, phone, destination, date, classType);
						// Retrieve LinkedList from hashMap based on first letter of first name.
						char key = lastName.charAt(0);
						LinkedList<Passenger> list = (LinkedList<Passenger>) passengerRecords.get(key);
						
						if (list == null) {
							
							synchronized (passengerRecords) {
							
								// Check if the list has not been set while we waited.
								if (list == null) {
								
									// No passengers (list) records found at specified key.
									list = new LinkedList<Passenger>();
									list.add(newPassenger);
									passengerRecords.put(key, list);
								}
								
								else {
									// Otherwise, just add to the existing list.
									synchronized (list) { list.add(newPassenger); }
								}
							
							}
							
						}
						else {
							// List exists
							synchronized (list) { list.add(newPassenger); }
						}
						
						// We then add the passenger to the list of passengers assigned to the selected flight.
						flightToScan.addPassenger(newPassenger);
						
						// We update count of registered seats.
						flightToScan.setRegisteredSeatForClassType(classType, flightToScan.getRegisteredSeatsForClassType(classType) + 1);
						
						// Work is done. return with a successful message.
						return new BookingInformation(BookingInformation.SUCCESSFULL_BOOKING, newPassenger, flightToScan);
											
					
					}
					
					else {
						// Flight doesn't have any seats available for specified class type.
						// In case it doesn't we don't find another flight, we would like to return a message indicating that flight(s) were/was found but had no available seats.
						flightFoundNoSeatsAvaialble = true;
	
					}
					
					
				}
			
			}
			i++;
			
		}
			
		// If we through here, then the booking was unsuccessful.
		
		// We verify that if we went through a matching flight but that had no seats available for the given class. 
		if (flightFoundNoSeatsAvaialble) {
			
			return new BookingInformation(BookingInformation.FLIGHT_FOUND_BUT_NO_SEATS_AVAILABLE, null, null);
			
		}
		
		return new BookingInformation(BookingInformation.NO_FLIGHT_FOUND, null, null);
				
		
		
	}
	
	
		
	public int countBookedFlight(int recordType) {
		
		int count = 0;
		
		synchronized (flightRecords) {
			
			int i = 0;
			while (i < MAX_FLIGHTS) {
				
				if (!(flightRecords[i] == null)) {
					
					// If there is a flight at the given index.
					// Accumulate the number of the registered passengers for seats of the given class type.
					count = count + flightRecords[i].getRegisteredSeatsForClassType(recordType);
					
				}
				
				i++;
				
			}
	
		}
		
		m_logger.log("Counting booked records for " + Flight.seatConstantToString(recordType) + " (" + recordType + ") " + "type: " + count);
		return count;
		
	}
	
	

	@Override
	public String getBookedFlight(int recordType) {
		
		m_logger.log("- getBookedFlight called with arguments: (" + recordType + ")");
		
		String bookedFlights;
				
		// Setup threads to send a message to all the other servers.
		int i;
		int port; String city;
		ServerUDPSender[] threads = new ServerUDPSender[2];
		for (i = 0; i < m_servers.length; i++) {
			
			port = m_servers[i].getPort();
			city = m_servers[i].getCity();
			
			threads[i] = new ServerUDPSender(port, city, "REQ getBookedFlights " + recordType);
			threads[i].start();
		}
		
		
		// Retrieve data stored on this server.
		int localBookedFlights = countBookedFlight(recordType);
		
		bookedFlights = m_city + " " + localBookedFlights;
		
		
		// Retrieve the responses gathered by the threads.
		for (i = 0; i < m_servers.length; i++) {
			
			try {
				// Wait for the completion of the thread.
				threads[i].join();
				
				bookedFlights = bookedFlights + ", " + threads[i].getThreadName() + " " + threads[i].getReply();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		return bookedFlights;
	}
	
	
	
	private String reportFlightNotFound(int recordId) {
		
		String message = "Could not find flight with ID: " + recordId;
		m_logger.log(message);
		return "ERROR: " + message;
		
	}
	
	
	
	@Override
	public String editFlightRecord(int recordId, String fieldName, String newValue) {
		
		m_logger.log("- editFlightRecord called with arguments: (" + recordId + ", " + fieldName + ", " + newValue.toString() + ")");
		
		String message;
		Flight flight;
		
		if (fieldName.equals("create")) {
			
			synchronized (flightRecords) {
			
				// The third argument is supposed to hold all the attributes of the flight that we need to create.
				// We must then extract those attributes.
				Flight newFlight = null;
				
				try {
					
					String[] flightInfo = newValue.split(" ");
					
					int flightRecordId = Integer.parseInt(flightInfo[0]);
					String flightLocation = flightInfo[1];
					String flightDestination = flightInfo[2];
					int flightSeatsFirstClass = Integer.parseInt(flightInfo[3]);
					int flightSeatsBuisnessClass = Integer.parseInt(flightInfo[4]);
					int flightSeatsEconomyClass = Integer.parseInt(flightInfo[5]);
					
					Date flightTimeOfDeparture = fromStringToDate(flightInfo[6]);
					if (flightTimeOfDeparture == null) {
						message = "Badly formed date.";
						m_logger.log(message);
						return "ERROR: " + message;
	
					}
					
					flight = new Flight(flightRecordId, flightLocation, flightDestination, flightSeatsFirstClass, flightSeatsBuisnessClass, flightSeatsEconomyClass, flightTimeOfDeparture);
					
					
				}
				catch (Exception e) {
					
					message = "Cannot create flight: invalid information passed.";
					m_logger.log(message);
					return "ERROR: " + message;
					
				}
										
				
				// Check that there is no flight with provided id.
				if (getFlightFromID(flight.getId()) != null) {
					message =  "Could not create flight (provided identifier for flight is already in-use)";
					m_logger.log(message);
					return "ERROR: " + message;
				}
					
						
				createFlight(flight);
				message = "Flight created with ID: " + recordId;
				return message;
				
			}
				
				
		}

		flight = getFlightFromID(recordId);
		
		
		if (flight == null) {
			
			return reportFlightNotFound(recordId);
			
		}
		
		
			
		if (fieldName.equals("delete")) {
			
			synchronized (flight) {
				
				if (!deleteFlight(recordId)) {
					
					// Flight was deleted in between.
					return reportFlightNotFound(recordId);
				}
				
				message = "Flight with ID: " + recordId + " has been successfully deleted";
				return message;
				
			}
		
		}
		
		synchronized (flight) {
			
			if ((fieldName.equals("seatsFirstClass")) || (fieldName.equals("seatsBuisnessClass")) || (fieldName.equals("seatsEconomyClass"))) {
			
				int seatsValue;
				try { seatsValue = Integer.parseInt(newValue); }
				catch (Exception e) { 
					message = "Expected Intenger as third argument";
					m_logger.log(message);
					return "ERROR: " + message;
				}
				
				// Convert attribute to a constant int.
				int classType = 0;
				if (fieldName.equals("seatsFirstClass")) { classType = Flight.FIRSTCLASS; }
				if (fieldName.equals("seatsBuisnessClass")) { classType = Flight.BUISNESSCLASS; }
				if (fieldName.equals("seatsEconomyClass")) { classType = Flight.ECONOMYCLASS; }
				
				// Update the number of seats and log the information.
				updateSeats(flight, classType, seatsValue);
				return fieldName + " attribute for flight ID " + recordId + " has now the value :" + newValue;
				
			}
			
			if (fieldName.equals("timeOfDeparture")) {
	
				
				Date date = fromStringToDate(newValue);
				
				if (date == null) {
					message = "Badly formed date.";
					m_logger.log(message);
					return "ERROR: " + message;
				}
				
				// TODO:Synch here.
				
				//TODO: update passengers after changing time of departure.
				flight.setTimeOfDeparture(date);
				
				// As we changed the date of the flight, all the passengers previously booked for that flight must be removed.
				deletePassengersFromFlight(flight, Flight.ALLCLASS, flight.getPassengers().size());
				message = "timeOfDeparture attribute for flight ID " + recordId + " has now the value :" + newValue;
				return message;
	
			}
			
			message = "Command/field unrecognized/not allowed to modify";
			m_logger.log(message);		
			return "ERROR: " +message;
		
		}
	}
	
	
	
	public void createFlight(Flight newFlight) {
		
		int flightId = newFlight.getId();
		
		// Update overall flight record.
		flightRecords[flightId] = newFlight;		
		
		m_logger.log("Flight ID" + flightId + " created; " + newFlight.toString());
		
	}
	
	
	private Flight getFlightFromID(int recordId) {
		
		return flightRecords[recordId];
		
	}

	
	private void updateSeats(Flight flight, int classType, int newNumOfSeats) {
		
		int currentNumOfRegisteredSeats = flight.getRegisteredSeatsForClassType(classType);
		int currentNumOfSeats = flight.getSeatsForClassType(classType);
		int numOfPassengersToRemove = 0;
		
		if (currentNumOfRegisteredSeats > newNumOfSeats) {
			
			// The new number of seats is less than the number of passengers registered. 
			// We must remove passengers from the flight.
			numOfPassengersToRemove = currentNumOfRegisteredSeats - newNumOfSeats;
			deletePassengersFromFlight(flight, numOfPassengersToRemove, classType);
			
			flight.setRegisteredSeatForClassType(classType, currentNumOfRegisteredSeats - numOfPassengersToRemove);
			
		}
			
		flight.setSeatsForClassType(classType, newNumOfSeats);
		
		m_logger.log("Flight ID" + flight.getId() + " has now " + flight.getSeatsForClassType(classType) + " seats for " + flight.seatConstantToString(classType) + " (old value was " + currentNumOfSeats + "). Number of passengers removed from flight: " + numOfPassengersToRemove);
		
	}
	
	
	
	private void deletePassengersFromFlight(Flight flight, int numOfPassengers, int classType) {
		
		int k = 0;
		LinkedList<Passenger> flightPassengers = flight.getPassengers();
		Passenger currentPassenger; char L;
		for (int i = 0; i < numOfPassengers; i++) {
						
			currentPassenger = flightPassengers.get(k);
		
			if ( (classType != Flight.ALLCLASS) && (currentPassenger.getClassType() != classType) ) {
				// Skip this passenger if its class type does not matches the one we specified.
				// Specifying the constant ALLCLASS will remove this constraint.
				k++;
				continue;
			}
			
			// Get the first letter of the passenger's name.
			L = currentPassenger.getLastName().charAt(0);
			
			// Find the passenger record in the hashmap.
			LinkedList<Passenger> mapPassengers =  passengerRecords.get(L);
			
			synchronized (mapPassengers) {
			
				for (int j = 0; j < mapPassengers.size(); j++) {
					if (mapPassengers.get(j) == currentPassenger) {
						
						m_logger.log("Passenger with ID " + currentPassenger.getId() + " removed from flight ID " + flight.getId());
						
						// Remove the passenger from the list.
						mapPassengers.remove(j);
					}
				}
				
			}
			
			// Remove the passenger from the flight.
			flightPassengers.remove(k);
			
		}
		
		m_logger.log("Flight ID" + flight.getId() + " has had " + numOfPassengers + " passengers removed from the flight/passenger record.");
		
		
	}
	
	
	// Return true if deletion was successful, false otherwise.
	// Assume that the flight's ID is valid.
	private boolean deleteFlight(int recordId) {
				
		Flight flightToDelete = flightRecords[recordId];
		
		if (flightToDelete == null) {
			return false;
		}
		
		synchronized (flightToDelete) {
			
			// Remove all the passengers booked for that flight.
			deletePassengersFromFlight(flightToDelete, flightToDelete.getPassengers().size(), Flight.ALLCLASS);
			
			// Remove from global flight list.
			flightRecords[recordId] = null;
			
			m_logger.log("Flight ID" + flightToDelete.getId() + " has been deleted.");
		
		}
		
		return true;
				
	}

	
	public void start(ORB orb) throws Exception {
		
		m_orb = orb;
		
		passengerRecords = new HashMap<Character, LinkedList<Passenger>>();
		flightRecords = new Flight[MAX_FLIGHTS];
		
		// Extract informations about the servers.
		ServerInformationExtractor servInfExtract = new ServerInformationExtractor();
		ServerInformation[] serverInfos = servInfExtract.getServerInformations();
		
		// Build a table containing information about the other servers and obtain the port number of this server.
		
		m_servers = new ServerInformation[servInfExtract.getCount() - 1];
		
		int j = 0;
		for (int i = 0; serverInfos[i] != null; i++) {
			
			ServerInformation currentServer = serverInfos[i];
			
			if (currentServer.getCity().equals(m_city)) {
				// This ServerInformation instance represents the server we are attempting to launch.
				// Get and set the port number.
				m_port = currentServer.getPort(); 
				
			}
			else {
				// This server represents another distant server to which this server instance may communicate with.
				// Add it to the server information list so as to be able to extract the port number when needed.
				m_servers[j] = currentServer;
				j++;
				
			}
			
		}
		
		// Setup logger.
		m_logger = new MyLogger(m_city + "_server");
		
		m_logger.log("-------------------");
		m_logger.log("Server is now live.");
		
		m_serverListener = new ServerListener(this, m_city + "_SERV_LISTENER", m_port, m_logger);
		m_serverListener.start();

		
		// Set the server live.
				
		System.out.println("Server at " + m_city + " is up and running.");
		
	}
	
	private ServerInformation locateServer(String serverCity) {
		
		// Check if the given name happens to be the name of this server.
		if (serverCity.equals(m_city)) {
			
			return new ServerInformation(serverCity, m_port);
			
		}
		
		// Scan all the other servers.
		int i;
		for (i = 0; i < m_servers.length; i++) {
			
			if (serverCity.equals(m_servers[i].getCity())) {
				return m_servers[i];
			}
			
		}
		// Server could not be found.
		return null;
	}
	
	
	public String transferReservation(int passengerID, String currentCity, String otherCity) {
		
		m_logger.log("- transferReservation called with arguments: (" + passengerID + ", " + currentCity + ", " + otherCity + ")");
		
		// Locate passenger's current city.
		ServerInformation currentCityServerInformation = locateServer(currentCity);
		
		if (currentCity.equals(m_city)) {
			// No need to send a message through the network as it is this server that should request the transfer.
			m_logger.log("Source city is the same as this server for passenger with ID: " + passengerID);
			return requestTransferReservationFromCurrentCity(passengerID, otherCity);
			
		}
		
		
		if (currentCityServerInformation == null) {
			// Specified current city server could not be found.
			m_logger.log("Could not find source city server: " + currentCity + " for passenger with ID: " + passengerID);
			return "ERROR: Passenger's current city server could not be found.";
			
		}		
		
		
		m_logger.log("Distant server at " + currentCity + " for passenger with ID: " + passengerID + " found; proceeding to contact.");
		
		String requestMessage = "REQ proceedTransferReservation " + passengerID + " " + otherCity;
		
		// Send the message to the current city server so that it can proceed to the transfer.
		ServerUDPSender sender = new ServerUDPSender(currentCityServerInformation.getPort(), currentCityServerInformation.getCity(), requestMessage);
		sender.run();
		
		// Return what the current city server replied.
		String message = sender.getReply();
		m_logger.log("Reservation status: " + message);
		return message;
		
	}
	
	
	public String serveTransfer(Passenger newPassenger) {
	
		m_logger.log("Transfer request received with this server as destination. Passenger has ID " + newPassenger.getId());
		
		// Book the flight for the passenger.
		BookingInformation transferInformation = addPassengerToFlight(newPassenger.getFirstName(), newPassenger.getLastName(),
				newPassenger.getAddress(), newPassenger.getPhone(), newPassenger.getDestination(), newPassenger.getDate(),
				newPassenger.getClassType());
		
		String message;
	
		// Prepare reply message based on status of transfer booking operation.
		switch(transferInformation.m_status) {
		
			case BookingInformation.NO_FLIGHT_FOUND:
				message = "Transfer unsucessfull for passenger with ID " + newPassenger.getId() + ": no flights found.";
				break;
				
			case BookingInformation.FLIGHT_FOUND_BUT_NO_SEATS_AVAILABLE:
				message = "Transfer unsucessfull for passenger with ID " + newPassenger.getId() + ": flight found but no seats were available.";
				break;
				
			case BookingInformation.SUCCESSFULL_BOOKING:
				message = "Transfer sucessfull for passenger with ID " + newPassenger.getId() + ": Passenger has now ID " + transferInformation.m_passengerRecord.getId() + "; booked flight has ID: " + transferInformation.m_associatedFlight.getId();
				break;
			
			default:
				// We should not go through here.
				message = "";
				break;
		}
		
		
		m_logger.log(message);
		
		// Return and add status operation in the packet.
		// The receiver of the message will be able to directly identify if the operation was a success or not.
		return "REPLY " + transferInformation.m_status + " " + message;
		
		
	}
	
	
	
	
	private Passenger getPassengerRecordFromID(int passengerID) {
		
		int i;
		Passenger currentPassenger;
		for (LinkedList<Passenger> list : passengerRecords.values()) {
			
			synchronized (list) {
				
				for (i = 0; i < list.size(); i++) {
					
					currentPassenger = list.get(i);
					if (list.get(i).getId() == passengerID) {
						return currentPassenger;
					}
					
				}
				
			}
			
		}
		
		return null;
		
	}
	
	
	private void removePassengerFromFlight(int passengerId) {
		
		Passenger passenger = null;
		
		// Remove passenger record from flight and update flight.
		// TODO
		synchronized (flightRecords) {
			
			int i, j;
			outerloop:
			for (i = 0; i < MAX_FLIGHTS; i++) {
				
				Flight currentFlight = flightRecords[i];
				
				if (currentFlight == null) {
					// No flight at this index.
					continue;
				}
				
				LinkedList<Passenger> passengers = currentFlight.getPassengers();
				
				//if (passengers == null) { continue; }
				
				for (j = 0; j < passengers.size(); j++) {
					
					passenger = passengers.get(j);
					if (passenger.getId() == passengerId) {
						
						// Remove passenger from list.
						passengers.remove(passenger);
						m_logger.log("Passenger with ID " + passenger.getId() + " removed from flight with ID " + currentFlight.getId());
						
						// Update seats on the flight.
						int classType = passenger.getClassType();
						
						int numOfRegisteredSeats = currentFlight.getRegisteredSeatsForClassType(classType);
						currentFlight.setRegisteredSeatForClassType(classType, numOfRegisteredSeats - 1);
						
						break outerloop;
						
					}
					
				}
				
			}
		
		}
		
		// Remove passenger from hashmap
		
		char key = passenger.getLastName().charAt(0);
		LinkedList<Passenger> list = passengerRecords.get(key);
		
		synchronized (list) {
			
			list.remove(passenger);
			
		}
		
	}	
	
	
	public String requestTransferReservationFromCurrentCity(int passengerID, String otherCity) {
		
		m_logger.log("Received transfer reservation request to " + otherCity + " for passenger with ID " + passengerID);
		
		String message = "";
		
		synchronized (flightRecords) {
			
			// TODO: Synchronize passenger record.
			
			Passenger passengerRecord = getPassengerRecordFromID(passengerID);
			ServerInformation otherCityServerInformation = locateServer(otherCity);
			
			if (passengerRecord == null) {
				// Passenger with specified id not found within the server.
				message = "Passenger with ID " + passengerID + " not found at " + m_city + " server.";
				m_logger.log(message);
			}
			
			else if (otherCity.equals(m_city)) {
				// City destination is the same as the current one.
				message = "City destination is the same one as the current one (" + m_city + ") for passenger with ID " + passengerID;
				m_logger.log(message);
			}
			
			
			else if (otherCityServerInformation == null) {
				// No other server found for the specified city.
				message = "City destination not found (" + otherCity + ") for passenger with ID " + passengerID;
				m_logger.log(message);
			}
			
			else {
				
				m_logger.log("Passenger with ID " + passengerID + " found.");
				
				try {
					// Proceed to the transfer.
					
					String requestMessage = "REQ transferReservation ";
					
					// Serialize the passenger record and get the bytes.
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					outputStream.write(requestMessage.getBytes());
					
					ObjectOutputStream os = new ObjectOutputStream(outputStream);
					os.writeObject(passengerRecord);
					byte[] data = outputStream.toByteArray();
					
					
					// Prepare packet departure and reception.
					DatagramSocket socket = new DatagramSocket();
					byte[] incomingData = new byte[1024];
					InetAddress address = InetAddress.getByName("localhost");
					
					// Send packet to the other city server.
					DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, otherCityServerInformation.getPort());
					socket.send(sendPacket);
					m_logger.log("Transfer request sent through UDP to " + otherCity + " for passenger with ID " + passengerID);

					// Get server's reply.
					DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
					socket.receive(incomingPacket);
					
					socket.close();
					
					// Return its response.
					String reply = new String(incomingPacket.getData()).trim();
					m_logger.log("Reply received for transfert request from " + otherCity + " for passenger with ID " + passengerID + ". Reply is: " + reply);
				
					// Split response.
					String replyArray[] = reply.split(" ");
					
					// Convert second word to integer as it represents the op status code.
					int opStatus = Integer.parseInt(replyArray[1]);
					if (opStatus == BookingInformation.SUCCESSFULL_BOOKING) {
						// Transfer was a success: a flight was found in the other city.
						m_logger.log("Transfer operation on the other city was sucessful.");
						// Remove record from this server.
						removePassengerFromFlight(passengerID);
						
					}
					
					else {
						// Transfer was unsuccessful.
						m_logger.log("Transfer operation on the other city was unsucessful.");
					}
					
					// Extract detailing message.
					int startIndexMessage = replyArray[0].length() + 1 + replyArray[1].length() + 1;
					message = reply.substring(startIndexMessage);

				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			return message;
			
			
		}
		
		
	}
	
	
	
	private Date fromStringToDate(String dateString) {
		
		Date date;
		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		try {
			date = (Date) formatter.parse(dateString);
		} catch (ParseException e) {
			String message = "Badly formed date.";
			System.out.println(message);
			m_logger.log(message);

			return null;
		}
		
		return date;

		
	}

	
	
	
	public void stop()  {
				
		System.out.println("Server " + m_city + " stopping..");
		
		
		m_logger.log("Server stopped.");
		
		m_serverListener.terminate();
		
		m_orb.shutdown(true);

	}
	
	
}
