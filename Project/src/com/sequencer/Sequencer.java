package com.sequencer;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

import com.reliableudp.OperationMessage;
import com.reliableudp.OperationMessageProcessorInterface;
import com.reliableudp.ReliableUDPListener;
import com.reliableudp.ReliableUDPSender;
import com.replicamanager.ReplicaManagerInformation;

public class Sequencer extends Thread implements OperationMessageProcessorInterface {

	private	ReplicaManagerInformation[]	m_replicaManagers;
	private	int							m_requestId;
	private	ReliableUDPListener			m_listener;
	private	Queue<OperationMessage>		m_requestQueue;
	
	public Sequencer(ReplicaManagerInformation[] replicaManagers, int outPort) {
		
		m_replicaManagers = replicaManagers;
		
		m_listener = new ReliableUDPListener(this, outPort);
		m_listener.start();
		
	}
	
	
	private int generateNewRequestId() {
		
		m_requestId++;
		return m_requestId;
		
	}
	
	
	
	public void run() {
		
		while (true) {
			
			// Serve all the requests in the queue until queue is empty.
			while (!m_requestQueue.isEmpty()) {
				
				OperationMessage message = m_requestQueue.poll();
				
				switch(message.getOpid()) {
				
					case OperationMessage.REQUEST:
						
						// We need to stamp the request with a unique requestId.
						int requestId = generateNewRequestId();
						
						LinkedList<String> content = message.getContentComponents();
						
						OperationMessage newRequest = new OperationMessage(OperationMessage.REQUEST);
						newRequest.addMessageComponent(Integer.toString(requestId));
						
						// Copy content.
						for (int i = 0; i < content.size(); i++) {
							
							newRequest.addMessageComponent(content.get(i));
							
						}
						
						// Send modified request to all the replicas.
						for (int i = 0; i < m_replicaManagers.length; i++) {
							
							ReplicaManagerInformation replicaManager = m_replicaManagers[i];
							
							ReliableUDPSender sender = new ReliableUDPSender(replicaManager.getAddress(), replicaManager.getPort());
							
							try { sender.send(newRequest); } catch (TimeoutException e) { e.printStackTrace(); }
							
						}
						
						// The new request has successfully sent to the replica managers.
				
				}
				
			}
			
		}
		
		
	}
	
	
	
	
	
	public OperationMessage processRequest(OperationMessage request) {
		
		// Add request to queue so that it will be processed by the main thread.
		m_requestQueue.add(request);
		
		// Just indicate that the request has been received.
		return new OperationMessage(OperationMessage.ACK);
		
	}
	
	

}
