package com.reliableudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;

public class ReliableUDPListener extends Thread {

	
	private	int									m_port;
	private OperationMessageProcessorInterface	m_processor;
	private DatagramSocket						m_socket;
	private boolean 							m_running;
	
	
	private	HashMap<Integer, OperationMessage>	m_packetBuffer;
	
	
	public ReliableUDPListener(OperationMessageProcessorInterface processor, int port) {
		
		m_processor = processor;
		m_port = port;
		
		m_packetBuffer = new HashMap<Integer, OperationMessage>();
		
			
		try { m_socket = new DatagramSocket(m_port); } catch (SocketException e) { e.printStackTrace(); }

		
	}	
	
	public ReliableUDPListener(OperationMessageProcessorInterface processor) {
		
		m_processor = processor;
		
		m_packetBuffer = new HashMap<Integer, OperationMessage>();
		
		// Let DatagramSocket pick an available port for us.
		try { m_socket = new DatagramSocket(); } catch (SocketException e) { e.printStackTrace(); }
		
		m_port = m_socket.getPort();
	}

	
	public int getPort() {
		
		return m_port;
		
	}
	
	
	public void run() {
		
		System.out.println("Server listener succesfully setup");
				
		try {
			while(true) {
				
				// First step: Send packet and wait for acknowledgment.
				
				byte[] buffer = new byte[1000];
				// Get the request.
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				m_socket.receive(request);
				
				// Convert data to OperationMessage instance.
				OperationMessage messageToProcess = new OperationMessage(request.getData());
				
				System.out.println("Server: message has protcol id: " + messageToProcess.getOpid());
				
				// Buffer up the message id.
				int messageId = messageToProcess.getId();
				
				OperationMessage toSend;
				
				// First, we check if the packet is an acknowledgment.
				
				if (messageToProcess.getOpid() == OperationMessage.ACK) {
					// If such is the case, then the ACK refers to a response that the client is attempting to acknowledge.
					System.out.println("Server: ACK received.");
					// Upon reception of an ack, the listener should free the packet that has been acknowledged from the packet buffer.
					if (m_packetBuffer.containsKey(messageId)) {
						
						// Remove packet.
						m_packetBuffer.remove(messageId);
						
					}
					
					// If no packet is found, then the reply acknowledging the client's acknowledgement has been lost.
					// Which is why the packet could not be found in the pack buffer: 
					// The listener has previously received an ack for this packet and has already removed it from the packet buffer.
					
					toSend = new OperationMessage(OperationMessage.ACK);
				}
				
				else {
					// If not, the packet refers to actual operation message.
					// We verify that the packet has not been already processed.
					if (m_packetBuffer.containsKey(messageId)) {
					
						toSend = m_packetBuffer.get(messageId);
					}
					
					// Otherwise, this is a first time we receive an operational message with this messageid.
					else {
				
						// Process request.
						toSend = m_processor.processRequest(messageToProcess);
						
						if (toSend == null) {
							// If there's no response to send, we still need to send an acknowledgment showing that
							// we have indeed received the packet.
							toSend = new OperationMessage(OperationMessage.ACK);
							System.out.println("No response to send.");
							
						}
						else {
						
							// Add it to the buffer.
							m_packetBuffer.put(messageId, toSend);
						}
					
					}
				}
				
				
				toSend.setId(messageId);
				toSend.buildMessage();
				
				// Send the reply.
				byte[] replyMessage = toSend.toBytes();
				
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
		
	}
		
	
	
	public void terminate() {
		
		if (m_running) {
			m_socket.close();
		}
		
	}
	
	
	
	
	
}
