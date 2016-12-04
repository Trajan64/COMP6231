package com.replicamanager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

import com.reliableudp.OperationMessage;
import com.reliableudp.OperationMessageProcessorInterface;
import com.reliableudp.ReliableUDPListener;
import com.reliableudp.ReliableUDPSender;
import com.replica.ReplicaClient;
import com.systeminitializer.SystemInitializer;
import com.utils.ContactInformation;
import com.utils.SynchronizedLogger;

public class ReplicaManager extends Thread implements OperationMessageProcessorInterface {

	
	private static final int					MAXERROR = 3;
	
	private int 								m_mode;
	private	int									m_id;
	private boolean[] 							m_faultyImplementations = {false, false, false};
	private	ReplicaManagerInformation[] 		m_replicaManagerInformations;
	private	int 								m_currentImplementationId;
	private	int									m_errorCount;
	private	ReliableUDPListener					m_listener;
	private	SynchronizedLogger					m_logger;
	private	boolean								m_available;
	private int									m_replicaClientPort;
	private	LinkedList<OperationMessage>		m_savedRequests;
	private	int									m_previousValidatedRequestId;
	private	HashMap<Integer, OperationMessage>	m_requestBuffer;
	private	ContactInformation					m_replicaClientInformation;
	private	ContactInformation					m_sequencerInformation;
	private	LinkedList<OperationMessage>		m_requestQueue;
	private	boolean								m_listenerIsProcessingRequest;
	private	ReplicaManagerHeartbeat[]			m_heartbeat;
	private	ReplicaClient						m_replicaClient;
	
	public ReplicaManager(int mode, int id, int outPort, int startingImplementationId, ContactInformation replicaInformation, ReplicaManagerInformation[] replicaManagerInformations, ContactInformation sequencerInformation) {
	
		
		m_mode = mode;
		m_id = id;
		m_currentImplementationId = startingImplementationId;
		
		m_replicaClientInformation = replicaInformation;
		
		m_replicaManagerInformations = replicaManagerInformations;
		
		m_previousValidatedRequestId = 0;
		m_errorCount = 0;
		m_available = true;
		
		m_sequencerInformation = sequencerInformation;
		
		m_listenerIsProcessingRequest = false;
				
		m_listener = new ReliableUDPListener(this, outPort);
		m_listener.start();
		
		m_replicaClientPort = replicaInformation.getPort();
		
		m_savedRequests = new LinkedList<OperationMessage>();
		m_requestBuffer = new HashMap<Integer, OperationMessage>();
		m_requestQueue = new LinkedList<OperationMessage>();
		
		m_heartbeat = new ReplicaManagerHeartbeat[2];
		
		//TODO: Setup logger?
		m_logger = new SynchronizedLogger("ReplicaManager_" + id);
		m_logger.addStartLine();
		
	}
	
	//TODO: Set off the heartbeat.
	
	
	public void initializeHeartbeats() {
		
		
		
		 // Find the two replica managers.
		Integer[] replicaManagerIds = new Integer[2];
		
		int j = 0;
		for (int i = 0; i < 3; i++) {
			
			if (m_replicaManagerInformations[i].getId() == m_id) { continue; }
			replicaManagerIds[j] =  i;
			j++;
		}
		
		j = 1;
		for (int i = 0; i < 2; i++) {
			
			m_logger.log("Initializing new heartbeat:");
			
			int toMonitor = replicaManagerIds[i];
			int toCollaborate = replicaManagerIds[j];
			
			m_logger.log("\tMonitored replica has id: " + toMonitor);
			m_logger.log("\tCollaborating replica manager has id: " + toCollaborate);
			
			m_heartbeat[i] = new ReplicaManagerHeartbeat(m_replicaManagerInformations, toMonitor, toCollaborate, m_id);
			m_replicaManagerInformations[toMonitor].setHeartbeat(m_heartbeat[i]);
			
			j--;
		}
	
		
	}
	
	
	public void startReplica() {
		
		m_replicaClient = new ReplicaClient(m_currentImplementationId, m_replicaClientPort, m_replicaManagerInformations[m_id], m_mode);
		m_replicaClient.start();	
		
		m_logger.log("Replica started.");
	}
	
	
	
	private void restartReplicaClient(int newImplementationId) {
		
		m_logger.log("MAIN THREAD> Proceeding to restart replica..");

		// Set status as unavailable.
		// Requests to pass to the ReplicaClient instance will only be buffered.
		m_available = false;
		
		if (m_mode == SystemInitializer.MODE_ERROR_RECOVERY) {
						
			// We need to empty the request queue from all leftover requests. 
			// Those requests will not receive a message, and in the case of error recovery, the respective FEs should be informed.
			
			// First we ensure that the listener is done adding new requests to the requestBuffer.
			while (m_listenerIsProcessingRequest) {
				continue;
			}
			m_logger.log("MAIN THREAD> Passing of requests to queue buffer has been switched off..");
			
			m_logger.log("MAIN THREAD> Proceeding to clean queue from left-over requests..");
			
			// Secondly, we clean the queue from any leftover requests and inform the respective FEs.
			for (int i = 0; i < m_requestQueue.size(); i++) {
				
				OperationMessage message = m_requestQueue.get(i);
				
				if (message.getOpid() == OperationMessage.REQUEST) {
					
					m_logger.log("\tRequest with requestId " + message.getContentComponents().get(0) + " removed from request queue.");
					
					// Inform the FE that it will not receive a response from this request.
					notifyUnavavailabilityToFrontEnd(message);
					
					m_logger.log("MAIN THREAD> Message removed from queue: " + message.getMessage());
					
					// Remove request from queue.
					m_requestQueue.remove(i);
					i--;
				}
				
			}
		
			// Restart error count.
			m_errorCount = 0;
			
		}
		
		// Process to restart.
		m_replicaClient = null;
		m_replicaClient = new ReplicaClient(newImplementationId, m_replicaClientPort, m_replicaManagerInformations[m_id], m_mode);
		m_replicaClient.start();
		
		m_logger.log("MAIN THREAD> Replica restarted. Replica uses implementation id " + newImplementationId);
		
		// Feed past requests to the replica client so that it will have the same state as the other replica clients.
		ReliableUDPSender sender = new ReliableUDPSender(m_replicaClientInformation.getAddress(), m_replicaClientInformation.getPort());
		
		m_logger.log("MAIN THREAD> Proceeding to update replica's state by feeding it past requests..");
		
		for (int i = 0; i < m_savedRequests.size(); i++) {
			
			OperationMessage request = m_savedRequests.get(i);
			request.setOpid(OperationMessage.STATEUPDATEREQUEST);
			
			m_logger.log("MAIN THREAD> request passed as STATEUPDATEREQUEST to replica client.. request is: " + request);
			
			try { sender.send(request); } catch (TimeoutException e) { e.printStackTrace(); }
			
		}
		
		// Send restarted message to all the other replicamanagers.
		if (m_mode == SystemInitializer.MODE_HIGH_AVAILABILITY) {
			
			m_logger.log("MAIN THREAD> Sending RESTARTED signal to all the other replica managers..");
			
			OperationMessage restartedMessage = new OperationMessage(OperationMessage.RESTARTED);
			
			// Send to the two other replicas informing them that the crash has been solved.
			for (int i = 0; i < m_replicaManagerInformations.length; i++) {
				
				if (m_replicaManagerInformations[i].getId() == m_id) { continue; }
				
				InetAddress address = m_replicaManagerInformations[i].getAddress();
				int port = m_replicaManagerInformations[i].getPort();
				
				m_logger.log("MAIN THREAD> Sending RESTARTED signal to replica manager with id " + i);
				
				ReliableUDPSender senderToRM = new ReliableUDPSender(address, port);
				try { senderToRM.send(restartedMessage); } catch (TimeoutException e) { e.printStackTrace(); }
				
				
			}
			
		}
		
		// Switch availability to true.
		m_available = true;
		
		m_logger.log("MAIN THREAD> Replica client has finished restarting and is ready to take in new requests.");

	}
	
	
	private void sendRequest(OperationMessage request) {
		
		ReliableUDPSender sender = new ReliableUDPSender(m_replicaClientInformation.getAddress(), m_replicaClientInformation.getPort());
		sender.setMaxTimeouts(5);
		
		// Setup try catch here.
		try { sender.send(request); }
		catch(TimeoutException e) {
			// Request timeouted. The replica has probably crashed.
		}

		
	}
	
	
	
	public void run() {
		
		while (true) {
			
			// Serve all the requests in the queue until queue is empty.
			while (!m_requestQueue.isEmpty()) {
				
				OperationMessage message = m_requestQueue.poll();

				m_logger.log("MAIN THREAD> New message in queue to be processed by main thread: " + message.getMessage());
				
				switch(message.getOpid()) {
				
					case OperationMessage.STATEUPDATEREQUEST:
						
						m_logger.log("MAIN THREAD> Passing STATEUPDATEREQUEST to replica client.");
						
						sendRequest(message);
						
						break;

					case OperationMessage.RESTART:
						
						// This message has been sent by one of the other replica managers. It indicates that we should restart the replica with the same implementation.
						// We assume here that the mode has been set to crash recovery without verification as the other replica managers would not have sent this message otherwise.
						
						restartReplicaClient(m_currentImplementationId);
							
						break;
						
					
					case OperationMessage.SOFTWAREFAILURE:
						
						m_logger.log("MAIN THREAD> System has encountered maximum number of software failure. Faulty implementation has id: " + m_currentImplementationId);
						
						// Maximum number of error reached: 
						// We need to restart the replica with another implementation.
						// We also need to mark the currently loaded implementation as being faulty so that it won't be loaded again.
						m_faultyImplementations[m_currentImplementationId] = true;
						
						int implementationToLoad = 0;
						
						// Find a working implementation to load.
						// We assume that there will always be a working implementation.
						for (int i = 0; i < m_faultyImplementations.length; i++) {
							
							if (m_faultyImplementations[i] == false) {
								// Clean implementation found.
								implementationToLoad = i;
								break;
							}
							
						}
						
						m_logger.log("MAIN THREAD> New clean implementation has ID: " + m_currentImplementationId);

						
						restartReplicaClient(implementationToLoad);
						break;
		
					case OperationMessage.REQUEST:
						
						m_logger.log("MAIN THREAD> Passing request to replica client.");
						
						sendRequest(message);
						
						break;
						
						
				}
				
			}
			
			
		}
		
	}
	
	
	// Scenario 1: System is up. Request is passed from sequencer to replicamanager.
	//	Request has higher ID than lastProcessedId+1
	//  Request buffered.
	// Replica Manager has received softwarefailure signal and set itself to unavaiable.
	
		
	// Check availability.
	
	
	
	// If availbility is set to false:
		
		// Change OperatioMessage opid as STATEUPDATEREQUEST.
		// Check if this new request can free up the buffer.
			// If so, every requests within the buffer should be marked as STATEUPDATEREQUEST and an appropriate message should be forwarded to the FEs.
		// Save all those requests in savedRequests
		// Update lastProcessedReqId as highest among those in buffer.
		// DONT ADD IT TO REQUEST QUEUE!
	
	// If availability is set to true:
	
		// 
	
	
	LinkedList<OperationMessage> dequeue(OperationMessage request) {
		
		int requestId = Integer.parseInt(request.getContentComponents().get(0));
		LinkedList<OperationMessage> requests = new LinkedList<OperationMessage>();
		
		m_logger.log("LISTENER> Request has requestId " + requestId + ". Last validated id is: " + m_previousValidatedRequestId);
		
		//TODO: requestIdWe need to keep the first element as being representative of the request id. Change ReplicaClient.
		if (requestId-1 == m_previousValidatedRequestId) {
			
			m_logger.log("LISTENER> Request is the immediate successor of previous request id.");
			m_logger.log("LISTENER> Proceeding to see if other buffered requests can be freed up..");
			
			// Add it to the sequentially ordered list of requests.
			m_savedRequests.add(request);
			requests.add(request);
			// Check if we can validate some requests in the buffer.
			int i;
			for (i = 0; i < m_requestBuffer.size(); i++) {
				
				if (m_requestBuffer.containsKey(requestId + 1 + i)) {
					
					int id = requestId + 1 + i;
					
					// Remove it from buffer and add it to the list.
					OperationMessage validated = m_requestBuffer.get(id);
					m_requestBuffer.remove(id);
					
					m_logger.log("LISTENER> Request in buffer with id: " + id + "freed from buffer. Message is:" + validated.getMessage());
					
					m_savedRequests.add(validated);
					requests.add(request);
				}
				
			}
			
			// Update last processed request id;
			m_previousValidatedRequestId = requestId + i;
		
		}
		
		else {
			
			m_logger.log("LISTENER> RequestId is not the immediate successor of previous request id. Request was put into the request buffer.");
			
			// Only put request in buffer.
			m_requestBuffer.put(requestId, request);
			
		}
		
		//TODO: Callee should use requests.isEmpty() instead of == null
		return requests;
		
	}
	

	// Send an unavailable message to a specific front end.
	private void notifyUnavavailabilityToFrontEnd(OperationMessage message) {
		
		m_logger.log("LISTENER> FE will be notified of unavaiability of the request manager. Request is: " + message.getMessage());
		
		LinkedList<String> content = message.getContentComponents();
		
		InetAddress frontEndAddress = null;
		int frontEndPort = Integer.parseInt(content.get(2));
		
		try { frontEndAddress = InetAddress.getByName(content.get(1)); } catch (UnknownHostException e) { e.printStackTrace(); }

		
		OperationMessage unavailableMessage = new OperationMessage(OperationMessage.RMUNAVAILABLE);
		
		m_logger.log("LISTENER> Sending message to " + frontEndAddress.toString() + ", " + Integer.toString(frontEndPort));
		
		ReliableUDPSender sender = new ReliableUDPSender(frontEndAddress, frontEndPort);
		try { sender.send(unavailableMessage); } catch (TimeoutException e) { e.printStackTrace(); }
		
	}
	
	
	//TODO: Requests that remain the requestQueue should not be replied to if the system hsa been to unaivalable.
	//TODO: Just use a if (m_available), otherwise the Replica Manager only have to ignore those requests.
	//TODO: That wont work. Those requests expect replies. Best way would be to process every REQUEST packets in the queue and then switch to RESTART mode.
	
	public OperationMessage processRequest(OperationMessage message) {
		
		m_logger.log("LISTENER> New message received: " + message.getMessage());
		
		switch (message.getOpid()) {
		
		
			case OperationMessage.REQUEST:
				
				m_listenerIsProcessingRequest = true;
				
				m_logger.log("LISTENER> New message processed as REQUEST.");

				if (m_available) {
					
					m_logger.log("LISTENER> Replica manager is available and ready to process the request.");

					LinkedList<OperationMessage> requests = dequeue(message);
					
					if (requests != null) {
						// The requests, and possibly other buffered up past requests have been extracted.
						// Send all of them into the requestQueue so that it will be processed by the main thread.
						for (int i = 0; i < requests.size(); i++) {
							m_logger.log("LISTENER> Proceeding to put in request to request queue: " + message.getMessage());
							m_requestQueue.offer(requests.get(i));
						}
						
					}
					
					// Otherwise no requests: request was only put into the buffer and there is nothing more to do for now.
				}
				
				else {
					
					m_logger.log("LISTENER> Replica manager is currently unable to process and get replies from new requests.");
					
					// Ensure that the request will not be processed.
					//TODO: Maybe requests that are after this request in the request buffer could be processed.
					message.setOpid(OperationMessage.STATEUPDATEREQUEST);
					
					// The replica manager is currently undergoing a restart and cannot process the request(s).
					LinkedList<OperationMessage> requests = dequeue(message);
					
					if (requests != null) {
					
						// We should message each of the FEs to let them know they cannot expect the replica manager to respond to those requests.
						for (int i = 0; i < requests.size(); i++) {
														
							// Change request to STATEUPDATEREQUEST.
							requests.get(i).setOpid(OperationMessage.STATEUPDATEREQUEST);
							
							notifyUnavavailabilityToFrontEnd(requests.get(i));
						}
					}
					
					// Otherwise no requests: request was only put into the buffer and there is nothing more to do for now.
					
				}
				
				m_listenerIsProcessingRequest = false;
				
				return new OperationMessage(OperationMessage.ACK);
				
				
			//TODO: Add case for RESTARDED
		
			//TODO: If the replica is undergoing a restart, the listener may block on trying to send a request to that particular replica.
			//		A solution to that problem would be to ensure that the replica won't process requests that have a request ID higher that the last request ID it processed.
			//		Anther added safety would be to set a timetotimeout on the sending operation of the request. If the send timeouts, the listener won't update the m_previousValidatedRequestId variable.
			//TODO: We must ensure that m_previousValidatedRequestId is updated accordingly.
		
			
			case OperationMessage.RESTARTED:
				
				// The distant replica has successfully restarted.
				
				// Get the replica manager id.
				int id = Integer.parseInt(message.getContentComponents().get(0));
				
				// We can switch the heartbeat back to on.
				m_replicaManagerInformations[id].getHeartbeat().setOn();
				
				return new OperationMessage(OperationMessage.ACK);
			
			
			case OperationMessage.RESTART:
				
				// Add message to queue so that it will be processed by the main thread.
				m_requestQueue.add(message);
				
				return new OperationMessage(OperationMessage.ACK);
				
			//TODO: A delayed SOFTWAREFAILURE message referring to a request that was processed before the recovery may happen.
			//		Use the requestId to determine if the SOFTWAREFAILURE signal is out of date.
			case OperationMessage.SOFTWAREFAILURE:
				
				// This message has been sent by one of the front end to inform that an error was noticed in the replica manager's replica's response.
				// We assume here that the mode has been set to error detection without verification as the front end would not have been able to send this message otherwise.
				
				// Increase error counter.
				m_errorCount++;
				
				if (m_errorCount == MAXERROR) {
					
					// Maximum number of error reached.
					// Set availability to off so that no new requests will be processed.
					m_available = false;
					
					// Add message to queue so that it will be processed by the main thread.
					m_requestQueue.add(message);

				}
				
				return new OperationMessage(OperationMessage.ACK);
				
			
			case OperationMessage.FAILCONSENSUSREQ:
				
				LinkedList<String> content = message.getContentComponents();
				
				// Replica to investigate.
				int faultyReplicaManagerId = Integer.parseInt(content.get(0));
				int senderReplicaManagerId = Integer.parseInt(content.get(1));
			
				
				// Check if we have a possible crash on this replica
				if (m_replicaManagerInformations[faultyReplicaManagerId].hasCrashed()) {
					
					// We do.
					
					// Send within the reply a pseudo boolean indicating if the resolver should be the replica that initiated the message.
					
					String shouldResolveCrash;
					if (m_id < senderReplicaManagerId) {
						shouldResolveCrash = "1";
					}
					else { shouldResolveCrash = "0"; }
					
					OperationMessage reply = new OperationMessage(OperationMessage.FAILCONSENSUSREPLYPOSITIVE);
					reply.addMessageComponent(shouldResolveCrash);
					
					return reply;
						
						
				}
				
				// Otherwise no crash was noticed on this particular replica.
				return new OperationMessage(OperationMessage.FAILCONSENSUSREPLYNEGATIVE);
				
				
				
			default:
				
				// We should not go through here.
				return new OperationMessage(OperationMessage.ACK);
						
		}
		
		
		
	}

}
