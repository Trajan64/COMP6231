package com.reliableudp;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

public class ReliableUDPSender {

	
	public static final int DELIVERY_SUCCESS = 0;
	public static final int DELIVERY_FAILURE_NOT_AN_ACK = 1;
	
	private static final int TIME_TO_TIMEOUT = 100;
	
	InetAddress				m_destAddress;
	int						m_destPort;
	static int				m_messageId;
	private int 			m_maxTimeouts;
	private int 			m_timeouts;
	private DatagramSocket 	m_socket;
	
	
	
	public ReliableUDPSender(InetAddress destAddress, int destPort) {
		
		m_destAddress = destAddress;
		m_destPort = destPort;
		
		try {
			m_socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}

	}

	
	public void setMaxTimeouts(int maxTimeouts) {
		
		m_maxTimeouts = maxTimeouts;
		
	}
	
	
	private synchronized int generateMessageId() {
		
		// Generate a unique ID for the packet that is supposed to be about to be transmitted.
		int id = m_messageId;
		m_messageId++;
		
		return id;
		
	}
	
	
	private boolean timeoutContinue() {
		
		if (m_maxTimeouts > 0) {
			// If maxTimeouts is not set to zero, then the instance has been set a certain amount of maximum timeouts.
			m_timeouts++;
			if (m_timeouts == m_maxTimeouts) {
				// We have reached the maximum number of timeouts.
				
				return false;
			}
			
			return true;
			
		}
		// Otherwise, this particular feature is switched off.
		return true;
		
	}
	
	
	private OperationMessage exchange(OperationMessage message) throws TimeoutException {
		
		try {
		
			int messageId = message.getId();
			byte[] byteMessage = message.toBytes();
			DatagramPacket request = new DatagramPacket(byteMessage, byteMessage.length, m_destAddress, m_destPort);
					
			while (true) {
				
				try {
					
					byte[] buffer = new byte[1000];
					DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
					
					// Send packet.
					m_socket.send(request);
					// Setup timeout timer.
					m_socket.setSoTimeout(TIME_TO_TIMEOUT);			
					// Wait for acknowledgment.
					m_socket.receive(reply);
							
					OperationMessage replyMessage = new OperationMessage(reply.getData());
					
					// Verify that the reply has the right message id.
					if (replyMessage.getId() == messageId) {
						
						if (replyMessage.getOpid() == OperationMessage.ACK) {
							// We've received an acknowledgment.
		
							//TODO: this is a test. Replace this with break;
							return new OperationMessage(buffer);
						}
						
						else {
							// Its a reply with content.
							return new OperationMessage(buffer);
							
						}
						
							
					}
					
					else {
						
						// Otherwise, the receiver sent packet with a wrong message id.
						// We start over again.
						continue;
						
					}
				
				} catch (SocketTimeoutException e) {
					// Timeout
					if (!timeoutContinue()) { throw new TimeoutException(); }
					System.out.println("Client: timeout. sending again..");
				}
				
			}
		
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// We should not go through here.
		return null;
	}
	
	
	public OperationMessage send(OperationMessage message) throws TimeoutException {
		
		
		// Attribute a unique message ID.
		int messageId = generateMessageId();
		
		// Add Id to OperationMessage instance.
		message.setId(messageId);
		message.buildMessage();
		
		System.out.println("Client: message is : " + new String(message.toBytes()).trim());
		
		OperationMessage reply;
		
		try {
			reply = exchange(message);
			acknowledge(messageId);
			
		} catch (TimeoutException e) {
			throw e;
				
		}
		
		return reply;
		
	}
	
	
	
	
	private void acknowledge(int messageId) throws TimeoutException {
	
		OperationMessage message = new OperationMessage(OperationMessage.ACK);
		message.setId(messageId);
		message.buildMessage();
		
		try {
			
			byte[] byteMessage = message.toBytes();
			DatagramPacket request = new DatagramPacket(byteMessage, byteMessage.length, m_destAddress, m_destPort);
					
			while (true) {
				
				try {
					
					byte[] buffer = new byte[1000];
					DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
					
					// Send packet.
					m_socket.send(request);
					// Setup timeout timer.
					m_socket.setSoTimeout(TIME_TO_TIMEOUT);			
					// Wait for acknowledgment.
					m_socket.receive(reply);
		
					OperationMessage replyMessage = new OperationMessage(reply.getData());
					
					// Verify that the reply has the right message id.
					if (replyMessage.getId() == messageId) {
						
						if (replyMessage.getOpid() == OperationMessage.ACK) {
							// We've received an acknowledgment.
							// The entire communication was a success.
		
							return;
						}
						
					
						else {
							
							// The operation carried out in the packet was not the one expected.
							// We start over again.
							continue;
							
						}
						
					}
					
					else {
						
						// Otherwise, the receiver sent a reply referring to another packet.
						// We start over again.
						System.out.println("Acknowledgment: running again..");
						continue;
						
					}
				
				} catch (SocketTimeoutException e) {
					// Timeout
					if (!timeoutContinue()) { throw new TimeoutException(); }
					System.out.println("Client: timeout for acknowledgment. sending ack again..");
				}
				
			}
		
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
}
