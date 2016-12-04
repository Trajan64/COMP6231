package flightReservationApp;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class ServerListener extends Thread {

	private Thread 			m_thread;
	private String 			m_threadName;
	private ServerImpl 		m_server;
	private DatagramSocket 	m_socket;
	private boolean 		m_running;
	private int				m_port;
	private MyLogger		m_logger;
	
	
	ServerListener(ServerImpl server, String threadName, int port, MyLogger logger) {
		
		m_server = server;
		m_threadName = threadName;
		m_port = port;
		m_logger = logger;
		
	}
	
	public void start() {
		
		m_running = true;
		int port = m_port;
		try {
			m_socket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.out.println("Socket error: " + e.getMessage());
			m_running = false;
			return;
		}
		
		if (m_thread != null) { return; }
		System.out.println("Starting thread: " + m_threadName);
		m_thread = new Thread(this, m_threadName);
		m_thread.start();
		
	}
	
	
	
	private String processRequest(String request, byte[] rawRequest) throws IOException {
		
		m_logger.log("UDP packet received: " + request);
				
		String stringRequest[] = request.toString().split(" ");
		System.out.println(stringRequest[0]);
		
		// Split the string
		
		// Check if the message includes an appropriate request header.
		if (!stringRequest[0].equals("REQ")) {
			return null;
		}
		
		if (stringRequest.length < 2) {
			// No operation provided in the request message
			return null;
		}
		
		// Extract the operation.
		String operation = stringRequest[1];
		
		if (operation.equals("getBookedFlights")) {
						
			// This operation requires an argument.
			// Check if it's here.
			if (stringRequest.length < 3) {
				return null;
			}
			
			// Extract the argument (class type).
			int classType;
			
			try {
				classType = Integer.parseInt(stringRequest[2]);

			} catch (Exception e) {
				// Argument is not an integer.
				return null;
			}
			
			// Get the count of bookedFlight.
			int count = m_server.countBookedFlight(classType);
			return String.valueOf(count);
		}
		
		if (operation.equals("proceedTransferReservation")) {
			
			int passengerId;
			
			try {
				passengerId = Integer.parseInt(stringRequest[2]);

			} catch (Exception e) {
				// Argument is not an integer.
				return null;
			}
			
			String otherCity = stringRequest[3];
			
			return m_server.requestTransferReservationFromCurrentCity(passengerId, otherCity);
			
		}
		
		if (operation.equals("transferReservation")) {
			
			// Get the serialized object.
			int byteSerializedObjectIndex = "REQ transferReservation ".length();
			
			byte[] serializedObjectData = Arrays.copyOfRange(rawRequest, byteSerializedObjectIndex, rawRequest.length);
			ByteArrayInputStream in = new ByteArrayInputStream(serializedObjectData);
			ObjectInputStream is = new ObjectInputStream(in);
			
			Passenger newPassenger;
			
			try {
				newPassenger = (Passenger) is.readObject();
				return m_server.serveTransfer(newPassenger);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			
			
			
			
		}
		
		// Unrecognized operation
		return null;
		
	}

	
	public void run() {
		
		System.out.println("Server listener succesfully setup");
		
		String toSend;
		
		try {
			byte[] buffer = new byte[1000];
			while(true) {
				// Get the request.
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				m_socket.receive(request);
				byte[] message = new byte[1000];
				message = request.getData();
				
				// Process the data and get the reply to send
				toSend = processRequest(new String(message).trim(), message);
				
				if (toSend == null) {
					// No response to send.
					System.out.println("No response to send.");
					continue;
				}
				
				byte[] replyMessage = new byte[1000];
				replyMessage = toSend.getBytes();
				
				// Send the reply.
				DatagramPacket reply = new DatagramPacket(replyMessage, replyMessage.length, request.getAddress(), request.getPort());
				m_socket.send(reply);
				
			}
		} catch(SocketException e) { 
			
			if (m_running == true) {
				// Closing off the socket to terminate the thread will cause a socket exception to be triggered.
				// Hence, if the thread has been set to terminate, we can safely ignore the exception.
				
				System.out.println("Socket error: " + e.getMessage()); 
			}
			
		} catch(IOException e) { System.out.println("IO error: " + e.getMessage());
		} finally {
			if (m_socket != null) { m_socket.close(); }
		}
		
		System.out.println("Thread " + m_threadName + " terminated.");
		
	}
	
	public void terminate() {
		
		if (m_running) {
			m_socket.close();
		}
		
	}
	
	
}
