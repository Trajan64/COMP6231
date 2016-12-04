/*package com.replicamanager;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import com.config.Constants;
import com.server.FlightReservationImpl;
import com.server.FlightReservationImplMtl;
import com.server.FlightReservationImplNdl;
import com.server.FlightReservationImplWst;

import FlightReservationApp.FlightReservation;
import FlightReservationApp.FlightReservationHelper;

public class ReplicaClient  {

	private int					m_currentImplementationId;
	//private	DRFSInterface		m_implementation;
	private	int					m_port;
	//private ReliableUDPListener	m_listener;
	
	public static final int 					IMPLEMENTATION_JEROME = 0;
	public static final int 					IMPLEMENTATION_VALERIE = 1;
	public static final int 					IMPLEMENTATION_MANDEEP = 2;
	
	private String[]			m_cities = 		{"mtl", "wst", "ndl"};
	
	private	ORB					m_orb;
	
	
	public ReplicaClient(int implementationId, int outPort) {
		
		loadImplementation(implementationId);
		m_port = outPort;
		//m_listener = new ReliableUDPListener(this, m_port);
		
		
	}
	
	private	void loadImplementation(int implementationId) {
		
		
		//String[] CORBAoptions = {"-ORBInitialPort", "1050", "-ORBInitialHost", "localhost"};
		
		// Setup the orb.
		//m_orb = org.omg.CORBA.ORB.init(CORBAoptions, null);
		
		// Setup server.
		for (int i = 0; i < m_cities.length; i++) {
			
			initializeDRFSAtCity(implementationId, m_cities[i]);
			
		}
	}
	
	
	private void initializeDRFSAtCity(int implementationId, String city) {
		
		
		POA rootPOA = POAHelper.narrow(m_orb.resolve_initial_references("RootPOA"));
		rootPOA.the_POAManager().activate();
		
		FlightReservationImpl server;
		
		switch (implementationId) {
		
			case IMPLEMENTATION_JEROME:
				//server = new ServerImpl(city);
				break;
			case IMPLEMENTATION_VALERIE:
				break;
			case IMPLEMENTATION_MANDEEP:
				int MTL_UDP_SERVER_PORT = 6790;
				int WST_UDP_SERVER_PORT = 6791;
				int NDL_UDP_SERVER_PORT = 6792;
				String[] args = {"-ORBInitialPort", "1050", "-ORBInitialHost", "localhost"};
				switch (city){
				
				case "mtl":
					
					// server = new FlightReservationImpl("Mtl", MTL_UDP_SERVER_PORT, WST_UDP_SERVER_PORT, NDL_UDP_SERVER_PORT);
					// server.start();
					
					FlightReservationImplMtl flightReservationImplMtl = new FlightReservationImplMtl();
					Thread t = new Thread(flightReservationImplMtl);
					t.start();
					server = new FlightReservationImpl("Mtl", MTL_UDP_SERVER_PORT, 
							WST_UDP_SERVER_PORT, NDL_UDP_SERVER_PORT);
					startOrb(server, "mtl");
					break;
					
				case "wst":
					
					FlightReservationImplWst FlightReservationImplWst = new FlightReservationImplWst();
					Thread t1 = new Thread(FlightReservationImplWst);
					t1.start();
					server = new FlightReservationImpl("Wst", WST_UDP_SERVER_PORT, 
							NDL_UDP_SERVER_PORT, MTL_UDP_SERVER_PORT);
					startOrb(server, "wst");
					break;
					
				case "ndl":
					FlightReservationImplNdl FlightReservationImplNdl = new FlightReservationImplNdl();
					Thread t2 = new Thread(FlightReservationImplNdl);
					t2.start();
					break;	
				default: 
                break;
				}
				
		
		}
		
		org.omg.CORBA.Object ref = rootPOA.servant_to_reference(server);
		server href = serverHelper.narrow(ref);
		
		org.omg.CORBA.Object objRef = m_orb.resolve_initial_references("NameService");
		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
		
		NameComponent path[] = ncRef.to_name(city);
		ncRef.rebind(path, href);
		
		server.start(m_orb);
					
		m_orb.run();

	}
	
	public void startOrb(FlightReservationImpl server , String name){
		String[] args = {"-ORBInitialPort", "1050", "-ORBInitialHost", "localhost"};
		try {
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);

			// get reference to rootpoa & activate the POAManager
			POA rootpoa = POAHelper.narrow(orb
					.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB
			FlightReservationImpl server = new FlightReservationImpl("Mtl", Constants.MTL_UDP_SERVER_PORT, 
					Constants.WST_UDP_SERVER_PORT, Constants.NDL_UDP_SERVER_PORT);
			
			server.setORB(orb);
			server.start();

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(server);
			FlightReservation sref = FlightReservationHelper.narrow(ref);

			// get the root naming context
			// NameService invokes the name service
			org.omg.CORBA.Object objRef = orb
					.resolve_initial_references("NameService");
			// Use NamingContextExt which is part of the Interoperable
			// Naming Service (INS) specification.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// bind the Object Reference in Naming
			//String name = "mtl";
			NameComponent path[] = ncRef.to_name(name);
			ncRef.rebind(path, sref);

			System.out.println("Mtl Server ready and waiting ...");

			// wait for invocations from clients
			orb.run();
			System.out.println("MANDEEP--------");
		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

		System.out.println(name+"Mtl Server Exiting ...");
	}
	
	private	String callMethod(String toCity, String methodName, String[] args) {
		
		// Get the reference to the appropriate city DRFS.
		org.omg.CORBA.Object objRef = m_orb.resolve_initial_references("NameService");
		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
		
		Server server = serverHelper.narrow(ncRef.resolve_str(name));
				
		String response;
		
		// Call the appropriate method.
		if (methodName.equals("bookFlight")) {
			
			String 	firstName = args[0];
			String 	lastName = args[1];
			String 	address = args[2];
			String 	phone = args[3];
			String 	destination = args[4];
			String 	date = args[5];
			int		classType = Integer.parseInt(args[6]);
			
			response = server.bookFlight(firstName, lastName, address, phone, destination, date, classType);
			
		}
		
		if (methodName.equals("getBookedFlight")) {
			
			int recordType = Integer.parseInt(args[0]);
			
			response = server.getBookedFlight(recordType);
			
		}
		
		if (methodName.equals("editFlightRecord")) {
			
			int recordId = Integer.parseInt(args[0]);
			String fieldName = args[1];
			String newValue = args[2];
			
			response = server.editFlightRecord(recordId, fieldName, newValue);
			
		}
		
		if (methodName.equals("transferReservation")) {
			
			int passengerId = Integer.parseInt(args[0]);
			String currentCity = args[1];
			String otherCity = args[2];
			
			response = server.transferReservation(passengerId, currentCity, otherCity);
			
			
		}
		
		
		return response;
		
	}
	
	
	
	public OperationMessage processRequest(OperationMessage request) {
	
		// Possible operations:
		
		// The replica client can receive CORBA-like requests.
	
		// The replica client has the responsibility of answering to alive messages sent by other replicaManagers.
		
		
		switch(request.getOpid()) {
			
		case OperationMessage.REQUEST:
						
			// Unmarshall request.
			LinkedList<String> content = request.getContentComponents();
			
			// Contact information about the front end who initiated the request.
			InetAddress calleeAddress = InetAddress.getByName(content.get(0));
			int			calleePort = Integer.parseInt(content.get(1));
			
			String toCity = content.get(2);
			String methodName = content.get(3);
			
			int offset = 4;
			
			int numOfArgs = content.size() - offset;
			String[] args = new String[numOfArgs];
			
			for (int i = 0; i < numOfArgs; i++) {
				args[i] = content.get(i + offset);
			}
			
			// Call method at the specified city server.
			String response = callMethod(toCity, methodName, args);
			
			// Reply to the front end.
			OperationMessage replyToCallee = new OperationMessage(OperationMessage.RESPONSE);
			
			//TODO: Add replicamanger id to replyToCallee.
			
			replyToCallee.addMessageComponent(response);
			
			ReliableUDPSender sender = new ReliableUDPSender(calleeAddress, calleePort);
			sender.send(replyToCallee);
			
			// Reply to the component that passed the request.
			return new OperationMessage(OperationMessage.ACK);
			
			
		case OperationMessage.ALIVE:
			
			// Alive message verifying that the ReplicaClient instance is still up and running.
			// We only have to reply with an acknowledgment.
			return new OperationMessage(OperationMessage.ACK);
		}
		
	}
	
	public static void main(String[] args) {
		ReplicaClient replicaClient = new ReplicaClient(2, 12);
		
		//replicaClient.loadImplementation(2);
		
	}

}
*/