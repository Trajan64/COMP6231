package flightReservationApp;

import java.util.Date;
import java.util.Random;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;


public class ClientManager {
	
	private String m_managerId;
	@SuppressWarnings("unused")
	private String m_managerCity;
	private String m_location;
	private server m_server;
	private ServerInformation[] m_serverInformations;
	
	private MyLogger m_logger; 
	
	private boolean loggedIn = false;
	
	private	ORB	m_orb;

	
	ClientManager(ORB orb) {
		
		// Get all the informations about the servers.
		ServerInformationExtractor servInfExtr = new ServerInformationExtractor();
		m_serverInformations = servInfExtr.getServerInformations();
		
		m_orb = orb;
				
	}
	
	
	
	private void showManagerConsoleHelpManager() {
		
		System.out.println("Possible operations are:");
		System.out.println("	getbookedflightcount 	<class type>");
		System.out.println("	editflightrecord 		<record id> <field name> <new value>");
		System.out.println("	deleteflight	 		<record id>");
		System.out.println("	createFlight	 		<record id> <destination> <number of first seat class> <number of buisness seat class> <number of economy seat class>  <time of departure>");
		System.out.println("	transferreservation 	<passenger id> <city source> <city destination>");
		System.out.println("	quit");
		
	}
	
	private void showManagerConsoleHelpPassenger() {
		System.out.println("Possible operations are:");
		System.out.println("	bookaflight 	 		<first name> <last name> <address> <phone> <destination> <date> <class>");
		System.out.println("	quit");
	}
	
	
	public boolean checkNumberofParamaters(String[] array, int expectedNumOfParameters) {
		
		if (array.length == expectedNumOfParameters) {
			return true;
		}
		
		if (array.length > expectedNumOfParameters) {
			System.out.println("too many paramaters supplied to command");
		}
		
		if (array.length < expectedNumOfParameters) {
			System.out.println("not enough paramaters supplied to command");
		}

		return false;
		
		
	}
	
	public void showLoginHelp() {
		
		System.out.println("To login as manager, type: login manager <managerID>");
		System.out.println("To login as a passenger, type: login passenger <city>");
		
		
	}
	
	
	
	
	private void setCORBA(ServerInformation server) throws Exception {
		
		try {
		
			//int port = server.getPort();
			String name = server.getCity();
								
			
			org.omg.CORBA.Object objRef = m_orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			
			m_server = serverHelper.narrow(ncRef.resolve_str(name));
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
	}
	
	private ServerInformation getServerInformationFromCity(String cityStr) {
		
		int i;
		for (i = 0; m_serverInformations[i] != null; i++) {
			if (m_serverInformations[i].getCity().equals(cityStr)) {
				return m_serverInformations[i];
			}
			
		}
		
		System.out.println(cityStr + " is not a valid city");
		return null;
		
	}
	
	
	private String generateRandomString(int length) {
		
		String candidateChars = "abcdefghijklopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		
	    StringBuilder sb = new StringBuilder();
	    Random random = new Random();
	    for (int i = 0; i < length; i++) {
	        sb.append(candidateChars.charAt(random.nextInt(candidateChars.length())));
	    }

	    return sb.toString();
	}
	
	// Returns true if the session is over.
	// False otherwise.
	public boolean process(String input) {
		
		String[] command = input.split("\\s+");
		String message;
				
		try {
			
			if (command[0].equals("quit")) {
				
				message = "Exitting..";
				
				if (m_logger != null) {
					m_logger.log(message);
				}
				System.out.println(message);
				return true;
				
			}
			
			
			if (!loggedIn) {
				
				
				if 	(command.length < 3) {
					
					// Not enough parameters.
					showLoginHelp();
					return false;
				}

				if (command[0].equals("login")) {
						
					if (command[1].equals("manager")) {
						
						String managerId = command[2];
						
						// Check if the provided managerID is lexically correct.
						if (managerId.length() != 7) {
							System.out.println("Badly formed manager ID");
							return false;
						}
						String managerCity = managerId.substring(0, 3);
						@SuppressWarnings("unused")
						int managerSerial;
						try { 
							managerSerial = Integer.parseInt(managerId.substring(3, 7));
						} catch (Exception e) {
							System.out.println("Manager ID should contain an id number");
							return false;
						}
						
						// Check if city is valid.
						ServerInformation server = getServerInformationFromCity(managerCity);
						
						if (server == null) {
							return false;
						}
						
						// Store the informations about the manager.
						m_managerId = managerId;
						m_managerCity = managerCity;
						
						// Setup the connection.
						setCORBA(server);
						
						// Setup logger.
						m_logger = new MyLogger("Manager_" + managerId);
						
						m_logger.log("------------");
						message = "Manager " + m_managerId + " logged in.";
						System.out.println(message);
						m_logger.log(message);
						
						showManagerConsoleHelpManager();
						
						loggedIn = true;
						return false;
						
					}
					
					if (command[1].equals("passenger")) {
						
						String cityStr = command[2];
						ServerInformation server = getServerInformationFromCity(cityStr);
						
						if (server == null) {
							System.out.println("Unrecognized city");
							return false;
						}
						
						// City is valid. Setup the CORBA connection with the designated city server.
						setCORBA(server);
						
						// Setup logger.
						m_logger = new MyLogger("Passenger_" + generateRandomString(12));
						
						m_logger.log("------------");
						message = "Passenger from " + cityStr + " logged in.";
						System.out.println(message);
						m_logger.log(message);
						
						showManagerConsoleHelpPassenger();
						
						loggedIn = true;
						return false;
						
					}
					
				}
				 	
			}
			
			
			if (loggedIn) {
				
				m_logger.log("Command executed: " + input);
				
				if (m_managerId != null) {
				
					// Manager commands.
					
					if (command[0].equals("transferreservation")) {
						
						if (!checkNumberofParamaters(command, 4)) {
							return false;
						}
						
						int passengerId;
						
						try {
							passengerId = Integer.parseInt(command[1]);
						} catch (Exception e) {
							message = "Non-integer paramater supplied";
							System.out.println(message);
							m_logger.log(message);
							return false;
						}
						
						String currentCity = command[2];
						String otherCity = command[3];
						
						String serverReply = m_server.transferReservation(passengerId, currentCity, otherCity);
						
						System.out.println(serverReply);
						m_logger.log(serverReply);

						return false;
						
						
					}
					
					if (command[0].equals("getbookedflightcount")) {
						
						if (!checkNumberofParamaters(command, 2)) {
							return false;
						}
						
						int recordType;
						
						try {
							recordType = Integer.parseInt(command[1]);
						} catch (Exception e) {
							message = "Non-integer paramater supplied";
							System.out.println(message);
							m_logger.log(message);
							return false;
						}
						
						String serverReply = m_server.getBookedFlight(recordType);
						
						System.out.println(serverReply);
						m_logger.log(serverReply);

						return false;
						
					}
					
					if (command[0].equals("editbookflight")) {
													
						if 	(!checkNumberofParamaters(command, 4)) {
							return false;	
						}
						
						int recordId;
						
						try {
							recordId = Integer.parseInt(command[1]);
						} catch (Exception e) {
							message = "Non-integer paramater supplied";
							System.out.println(message);
							m_logger.log(message);
							return false;
						}
						
						String fieldName = command[2];
						String newValue = command[3];
												
						String serverReply = m_server.editFlightRecord(recordId, fieldName, newValue);
						
						System.out.println(serverReply);
						m_logger.log(serverReply);
						
						return false;
					}
										
					if (command[0].equals("createflight")) {
						// Create a flight
						
						// Check if manager is identified.
						if (m_managerId != null) {
							
							if 	(checkNumberofParamaters(command, 7)) {
								
								int recordId;
								String destination = command[2];
								int seatsFirstClass;
								int seatsBuisnessClass;
								int seatsEconomyClass;
								Date timeOfDeparture;
								
								try {
								
									recordId = Integer.parseInt(command[1]);
									seatsFirstClass = Integer.parseInt(command[3]);
									seatsBuisnessClass = Integer.parseInt(command[4]);
									seatsEconomyClass = Integer.parseInt(command[5]);
		
								} catch (Exception e) {
									message = "Non-integer paramater supplied";
									System.out.println(message);
									m_logger.log(message);
									return false;
								}
								
								String flightCreationString = command[1] + " " + m_location + " " + command[2] + " " + command[3] + " " + command[4] + " " + command[5] + " " + command[6];
																
								
								//Flight newFlight = new Flight(recordId, m_location, destination, seatsFirstClass, seatsBuisnessClass, seatsEconomyClass, timeOfDeparture);
								
								String serverReply = m_server.editFlightRecord(recordId, "create", flightCreationString);
								
								System.out.println(serverReply);
								m_logger.log(serverReply);
								
								return false;
								
								
							}
							else { return false; }
						}
						else { return false; }
						
						
					}
					
					if (command[0].equals("deleteflight")) {
						
						if 	(!checkNumberofParamaters(command, 2)) {
							return false;	
						}
						
						int recordId;
						
						try {
	
							recordId = Integer.parseInt(command[1]);
							
						} catch (Exception e) {
							System.out.println("Non-integer paramater supplied");
							return false;
						}
						
						String serverReply = m_server.editFlightRecord(recordId, "delete", null);

						System.out.println(serverReply);
						m_logger.log(serverReply);
						
						return false;

						
					}
				
				}
				
				else {
					
					// Passenger commands.

					if (command[0].equals("bookaflight")) {
						// Book a flight
						if 	(checkNumberofParamaters(command, 8)) {
						
						String firstName = command[1];
						String lastName = command[2];
						String address = command[3];
						String phone = command[4];
						String destination = command[5];
						String date = command[6];
						int classType;
												
						try {
							classType = Integer.parseInt(command[7]);
						} catch (Exception e) {
							System.out.println("Non-integer paramater supplied");
							return false;
						}
						
						
						
												
						String serverReply = m_server.bookFlight(firstName, lastName, address, phone, destination, date, classType);

						System.out.println(serverReply);
						m_logger.log(serverReply);
						
						return false;
						
						}
						
						else { return false; }
						
					}

					
					
				}


			}
		
		System.out.println("Unrecognized command");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

		
	}
	
	
}
