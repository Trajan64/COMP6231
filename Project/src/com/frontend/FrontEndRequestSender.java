package com.frontend;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

import com.reliableudp.OperationMessage;
import com.reliableudp.OperationMessageProcessorInterface;
import com.reliableudp.ReliableUDPListener;
import com.reliableudp.ReliableUDPSender;
import com.replicamanager.ReplicaManagerInformation;
import com.systeminitializer.SystemInitializer;
import com.utils.ContactInformation;

public class FrontEndRequestSender implements OperationMessageProcessorInterface {

	private	int								m_mode;
	private String							m_city;
	private	ReplicaManagerInformation[] 	m_replicaManagers;
	private ContactInformation				m_sequencerInformation;
	
	private	int								m_listeningPort;
	private	LinkedList<OperationMessage>	m_responseBuffer;
	private	ReliableUDPListener				m_listener;
	private	int								m_numOfRMs;
	private int								m_numOfResponses;
	
	private String							m_methodName;

	
	FrontEndRequestSender(int mode, String city, ReplicaManagerInformation[] replicaManagers, ContactInformation sequencerInformation, LinkedList<String> requestComponents, InetAddress thisAddress) {
		
		m_mode = mode;
		m_replicaManagers = replicaManagers;
		m_sequencerInformation = sequencerInformation;
		
		m_numOfRMs = replicaManagers.length;
		
		// Setup listener.
		m_listener = new ReliableUDPListener(this);
		m_listeningPort = m_listener.getPort();		
		m_listener.start();
		
		m_methodName = requestComponents.get(0);
		
		m_city = city;
		
		// Create the request packet.
		OperationMessage request = new OperationMessage(OperationMessage.REQUEST);
		
		// Add information about the address and port in the packet.
		request.addMessageComponent(thisAddress.toString());
		request.addMessageComponent(Integer.toString(m_listeningPort));
		
		// Add city to request.
		request.addMessageComponent(m_city);
		
		// Add request components.
		for (int i = 0; i < requestComponents.size(); i++) {
			request.addMessageComponent(requestComponents.get(i));
		}
		
		// Send request to sequencer.
		ReliableUDPSender sender = new ReliableUDPSender(m_sequencerInformation.getAddress(), m_sequencerInformation.getPort());
		try { sender.send(request); } catch (TimeoutException e) { e.printStackTrace(); }

	}
		
	private class Response {
		
		String 				m_response;
		
		ContactInformation 	m_replicaManagerInformation;
		LinkedList<String> 	m_responseComponents;
		
		public Response(OperationMessage response, String methodName) {
			
			// Extract proper response and set contact information.
			
			LinkedList<String> components = response.getContentComponents();
			
			InetAddress address	= null;
			int			port	= Integer.parseInt(components.get(1));
			try { address = InetAddress.getByName(components.get(0)); } catch (UnknownHostException e) { e.printStackTrace(); }
			
			m_replicaManagerInformation = new ContactInformation(address, port);
			
			// Convert response to string.
			m_response = format(methodName);
			
		}
		
		private String format(String methodName) {
			
			String response;
			switch(m_methodName) {
			
				case "getBookedFlight":
					
					// This is a special case.
					
					String subString1 = null; String subString2 = null; String subString3 = null;
					String city = null;
					String value = null;
					String splitted[] = new String[2];
					for (int i = 2; i < 5; i++) {
						splitted = m_responseComponents.get(i).split(" ");
						city = splitted[0];
						value = splitted[1];
						
						switch(city) {
							case "MTL":
								subString1 = "MTL " + value;
								break;
							case "WST":
								subString2 = ", WST " + value; 
								break;
							case "NDL":
								subString3 = ", NDL " + value; 
								break;
						}
						
					}
					
					response = subString1 + subString2 + subString3;
					break;
					
				default:
					response = m_responseComponents.get(2);
				
					
			}
			
			return response;
			
		}
		
		public ContactInformation getContactInformation() {
			return m_replicaManagerInformation;
		}
		
		public String getResponse() {
			return m_response;
		}
		
		public boolean equals(Response response) {
			return m_response.equals(response.getResponse());
		}
		
	}
	
	//TODO: In case of mode set to high availability, the replicaManager should timeout after a certain amount of time if it cannot reach the FE. 
	
	public String getReply() {
		
		while (m_numOfResponses < m_numOfRMs) { continue; }
		
		Response response = null;
		
		// The method response of getBookedFlight require re-formatting to ensure that they have all the same formats.
		Response[] responses = new Response[3];
		for (int i = 0; i < m_numOfResponses; i++) {
			responses[i] = new Response(m_responseBuffer.get(i), m_methodName);
		}
		
		
		// We have gathered the responses from all (available) the replica managers.
		
		if (m_mode == SystemInitializer.MODE_ERROR_RECOVERY) {
			
			// Check the number of available replicas.
			if (m_numOfRMs == 2) {
				// If only two replicas are available, then we only send the result from either of the two.
				return responses[0].getResponse();
				
			}
			
			if (m_numOfRMs == 3) {
				// If all the replicas are available, then we compare the three responses.
				// If one mismatch happens, then the system should send a message to the replica that failed indicating it of the software failure.
								
				Response x = responses[0];
				Response y = responses[1];
				Response z = responses[2];
				
				Response erroneousResponse = null;
				
				if (x.equals(y)) {
					
					// If z is not equal then z is erroneous.
					if (!y.equals(z)) {
						erroneousResponse = z;
					}
					
					response = y;
				}
				else {
					// Mismatch.
					// Find if the error comes from x or y.
					if (!y.equals(z)) {
						// The error comes from y.
						erroneousResponse = y;
					}
					else {
						// The error has to come from x otherwise.
						erroneousResponse = x;
					}
					
					response = z;
				}
				
				if (erroneousResponse != null) {
					// We encountered an erroneous response.
					// We have to inform the respective replica manager of the failure.
					
					OperationMessage softwareFailureMessage = new OperationMessage(OperationMessage.SOFTWAREFAILURE);
					
					ContactInformation errReplicaManagerInformation = erroneousResponse.getContactInformation();
					ReliableUDPSender sender = new ReliableUDPSender(errReplicaManagerInformation.getAddress(), errReplicaManagerInformation.getPort());
					
					try { sender.send(softwareFailureMessage); } catch (TimeoutException e) { e.printStackTrace(); }
				
				}
								
			}
			
			
		}
		
		if (m_mode == SystemInitializer.MODE_HIGH_AVAILABILITY) {
			
			// We fetch the fastest reply.
			while (m_numOfResponses < 1) { continue; }
			
			// We have at least one response inside the buffer.
			response = new Response(m_responseBuffer.get(0), m_methodName);
			
		}

		return response.getResponse();
		
		
	}
	
	
	
	//TODO: When a software failure happens and the system has recovered, the associated replica manager should send a message to the front end indicating it the system now runs with all the replicas.
	
	public OperationMessage processRequest(OperationMessage request) {
		
		switch (request.getOpid()) {
		
			case OperationMessage.RMUNAVAILABLE:
				
				// An RM is currently undergonig a restart.
				// We cannot expect a reply from it.
				m_numOfRMs--;
				
				return new OperationMessage(OperationMessage.ACK);
			
			case OperationMessage.RESPONSE:
				
				// We have received a response from one of the replica managers.
				
				if (m_numOfResponses < m_numOfRMs) {
					
					// Not all responses gathered. We need to wait.
					m_responseBuffer.add(request);
					m_numOfResponses++;
					
					return new OperationMessage(OperationMessage.ACK);
				
				}
				
			default:
				
				// We should not go through here.
				return new OperationMessage(OperationMessage.ACK);
			
		}
		
		
	}	
	
	
	
}