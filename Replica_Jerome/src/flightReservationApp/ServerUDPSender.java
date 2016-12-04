package flightReservationApp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ServerUDPSender extends Thread {

	private int 	m_port;
	private String	m_messageToSend;
	private String	m_name;
	private String 	m_reply;

	
	
	ServerUDPSender(int port, String name, String messageToSend) {
		
		m_port = port;
		m_name = name;
		m_messageToSend = messageToSend;
		
	}
	
		
	
	public void run() {
		
		DatagramSocket socket = null;
		try {
			
			// Send the message.
			socket = new DatagramSocket();
			String stringMessage = m_messageToSend;
			byte[] message = stringMessage.getBytes();
			InetAddress host = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(message, stringMessage.length(), host, m_port);
			socket.send(request);
			
			// Get the reply.
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			socket.receive(reply);
			
			// Process the reply.
			m_reply = new String(buffer).trim();
			
		} catch(SocketException e) { System.out.println("Socket error: " + e.getMessage()); 
		} catch(IOException e) { System.out.println("IO error: " + e.getMessage());
		} finally {
			if (socket != null) { socket.close(); }
		}

		
	}
	
	public String getReply() {
		
		return m_reply;
		
	}
	
	
	public String getThreadName() {
		
		return m_name;
	}
	
				
};

