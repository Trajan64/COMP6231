package com.replica;

import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

import org.omg.CORBA.ORB;

import com.reliableudp.OperationMessage;
import com.reliableudp.OperationMessageProcessorInterface;
import com.reliableudp.ReliableUDPListener;
import com.reliableudp.ReliableUDPSender;
import com.replicamanager.ReplicaManagerInformation;
import com.server.FlightReservationImplMtl;
import com.server.FlightReservationImplNdl;
import com.server.FlightReservationImplWst;
import com.systeminitializer.SystemInitializer;
import com.utils.ContactInformation;
import com.utils.SynchronizedLogger;

public class ReplicaClient extends Thread implements OperationMessageProcessorInterface {

	private int							m_currentImplementationId;
//	private	DRFSInterface				m_implementation;
	private	int							m_port;
	private ReliableUDPListener			m_listener;
	private ReplicaManagerInformation	m_parentReplicaManagerInformation;
	private	int							m_mode;
	
	public static final int 					IMPLEMENTATION_JEROME = 0;
	public static final int 					IMPLEMENTATION_VALERIE = 1;
	public static final int 					IMPLEMENTATION_MANDEEP = 2;
	
	private String[]			m_cities = 		{"MTL", "WST", "NDL"};
	
	private	ORB					m_orb;
	
	private	SynchronizedLogger	m_logger;
	
	
	public ReplicaClient(int implementationId, int outPort, ReplicaManagerInformation parentReplicaManagerInformation, int mode) {
		
		loadImplementation(implementationId);
		m_port = outPort;
		
		m_listener = new ReliableUDPListener(this, m_port);
		m_listener.start();
		
		m_mode = mode;
		
		m_logger = new SynchronizedLogger("ReplicaClient_" + parentReplicaManagerInformation.getId());
		
		m_logger.addStartLine();
		m_logger.log("Replica is live.");
		m_logger.log("Replica is listening on port:" + m_port);
		
		m_parentReplicaManagerInformation = parentReplicaManagerInformation;
		
	}
	
	private	void loadImplementation(int implementationId) {
		
		
		String[] CORBAoptions = {"-ORBInitialPort", "1050", "-ORBInitialHost", "localhost"};
		
		// Setup the orb.
		m_orb = org.omg.CORBA.ORB.init(CORBAoptions, null);
		
		// Setup server.
		for (int i = 0; i < m_cities.length; i++) {
			
			initializeDRFSAtCity(implementationId, m_cities[i]);
			
		}
	}
	
	
	private void initializeDRFSAtCity(int implementationId, String city) {
		
		switch(implementationId){
		
		case IMPLEMENTATION_MANDEEP:

			switch(city){
		case "MTL":
			FlightReservationImplMtl flightReservationImplMtl = new FlightReservationImplMtl();
			Thread t = new Thread(flightReservationImplMtl);
			t.start();
			break;
			
		case "WST":
			
			FlightReservationImplWst FlightReservationImplWst = new FlightReservationImplWst();
			Thread t1 = new Thread(FlightReservationImplWst);
			t1.start();
			
			break;
		case "NDL":
			FlightReservationImplNdl FlightReservationImplNdl = new FlightReservationImplNdl();
			Thread t2 = new Thread(FlightReservationImplNdl);
			t2.start();
			break;	
			default: 
			break;	
			
			}
		
		}
		
		
//		POA rootPOA = POAHelper.narrow(m_orb.resolve_initial_references("RootPOA"));
//		rootPOA.the_POAManager().activate();
//		
//		Server server;
//		
//		switch (implementationId) {
//		
//			case IMPLEMENTATION_JEROME:
//				server = new ServerImpl(city);
//				break;
//			case IMPLEMENTATION_VALERIE:
//				break;
//			case IMPLEMENTATION_MANDEEP:
//				int MTL_UDP_SERVER_PORT = 6790;
//				int WST_UDP_SERVER_PORT = 6791;
//				int NDL_UDP_SERVER_PORT = 6792;
//				
//				case "mtl":
		
		// server = new FlightReservationImpl("Mtl", MTL_UDP_SERVER_PORT, WST_UDP_SERVER_PORT, NDL_UDP_SERVER_PORT);
		// server.start();
		/*switch (city){
		case "MTL":
		FlightReservationImplMtl flightReservationImplMtl = new FlightReservationImplMtl();
		Thread t = new Thread(flightReservationImplMtl);
		t.start();
		
		break;
		
		case "wst":
		
		FlightReservationImplWst FlightReservationImplWst = new FlightReservationImplWst();
		Thread t1 = new Thread(FlightReservationImplWst);
		t1.start();
		
		break;
		
		case "ndl":
		FlightReservationImplNdl FlightReservationImplNdl = new FlightReservationImplNdl();
		Thread t2 = new Thread(FlightReservationImplNdl);
		t2.start();
		break;	
		default: 
		break;
		}*/
//		
//		}
//		
//		org.omg.CORBA.Object ref = rootPOA.servant_to_reference(server);
//		server href = serverHelper.narrow(ref);
//		
//		org.omg.CORBA.Object objRef = m_orb.resolve_initial_references("NameService");
//		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
//		
//		NameComponent path[] = ncRef.to_name(city);
//		ncRef.rebind(path, href);
//		
//		server.start(m_orb);
//					
//		m_orb.run();

	}
	
	
	private	String callMethod(String toCity, String methodName, String[] args) {
		
		// Get the reference to the appropriate city DRFS.
//		org.omg.CORBA.Object objRef = m_orb.resolve_initial_references("NameService");
//		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
		
//		Server server = serverHelper.narrow(ncRef.resolve_str(name));
				
		String response = null;
		
		// Call the appropriate method.
//		if (methodName.equals("bookFlight")) {
//			
//			String 	firstName = args[0];
//			String 	lastName = args[1];
//			String 	address = args[2];
//			String 	phone = args[3];
//			String 	destination = args[4];
//			String 	date = args[5];
//			int		classType = Integer.parseInt(args[6]);
//			
//			response = server.bookFlight(firstName, lastName, address, phone, destination, date, classType);
//			
//		}
//		
//		if (methodName.equals("getBookedFlight")) {
//			
//			int recordType = Integer.parseInt(args[0]);
//			
//			response = server.getBookedFlight(recordType);
//			
//		}
//		
//		if (methodName.equals("editFlightRecord")) {
//			
//			int recordId = Integer.parseInt(args[0]);
//			String fieldName = args[1];
//			String newValue = args[2];
//			
//			response = server.editFlightRecord(recordId, fieldName, newValue);
//			
//		}
//		
//		if (methodName.equals("transferReservation")) {
//			
//			int passengerId = Integer.parseInt(args[0]);
//			String currentCity = args[1];
//			String otherCity = args[2];
//			
//			response = server.transferReservation(passengerId, currentCity, otherCity);
//			
//			
//		}
		
		return "response";
		//return response;
		
	}
	
	
	private String unmarshallAndProcessRequest(OperationMessage request) {
		
		LinkedList<String> content = request.getContentComponents();
				
		String toCity = content.get(3);
		String methodName = content.get(4);
		
		int offset = 5;
		
		int numOfArgs = content.size() - offset;
		String[] args = new String[numOfArgs];
		
		for (int i = 0; i < numOfArgs; i++) {
			args[i] = content.get(i + offset);
		}
		
		// Call method at the specified city server.
		String response = callMethod(toCity, methodName, args);

		return response;
	}
	
	
	
	public OperationMessage processRequest(OperationMessage request) {
	
		// Possible operations:
		
		// The replica client can receive CORBA-like requests.
	
		// The replica client has the responsibility of answering to alive messages sent by other replicaManagers.
		
		m_logger.log("Message received: " + request.getMessage());

		
		switch(request.getOpid()) {
			
		case OperationMessage.REQUEST:
			
			m_logger.log("Request received.");
			
			String response = unmarshallAndProcessRequest(request);
			
			m_logger.log("Response from [unmarshallAndProcessRequest] method is: " +response);
			
			// Fetch contact information about the front end who initiated the request.
			LinkedList<String> content = request.getContentComponents();
			
			ContactInformation frontEndCallee = new ContactInformation(content.get(1), content.get(2));
			
			m_logger.log("Extracted front end address and port is: " + frontEndCallee.getAddressString() + ":" + frontEndCallee.getPortString());
			
			// Reply to the front end.
			OperationMessage replyToCallee = new OperationMessage(OperationMessage.RESPONSE);
			
			replyToCallee.addMessageComponent(frontEndCallee.getAddressString());
			replyToCallee.addMessageComponent(frontEndCallee.getPortString());			
			
			replyToCallee.addMessageComponent(response);
			
			ReliableUDPSender sender = new ReliableUDPSender(frontEndCallee.getAddress(), frontEndCallee.getPort());
			
			if (m_mode == SystemInitializer.MODE_HIGH_AVAILABILITY) {
				
				// In high availability, there is no guarantee that all the responses will be received.
				// The FE will only wait for one response.
				// Hence, we have to setup a maximum number of timeout, otherwise replicas will block forever waiting for an acknowledgment that will never come.
				sender.setMaxTimeouts(5);
				
			}
			
			try { sender.send(replyToCallee); } catch (TimeoutException e) { e.printStackTrace(); }
			
			// Reply to the parent replica manager that passed the request.
			return new OperationMessage(OperationMessage.ACK);
			
		
		case OperationMessage.STATEUPDATEREQUEST:
			
			// This is a request but it has been sent for the sole purpose of updating the state of the replica.
			// Hence, we do not have to send a reply to the request's corresponding front end.
			
			unmarshallAndProcessRequest(request);
			
			return new OperationMessage(OperationMessage.ACK);
			
			
		case OperationMessage.ALIVE:
			
			// Alive message verifying that the ReplicaClient instance is still up and running.
			// We only have to reply with an acknowledgment.
			return new OperationMessage(OperationMessage.ACK);
			
			
		default:
			
			// We should not go through here.
			return new OperationMessage(OperationMessage.ACK);
			
		}
		
		
	}

}
