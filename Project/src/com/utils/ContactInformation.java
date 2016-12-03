package com.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ContactInformation {

	InetAddress m_address;
	int			m_port;
	
	public ContactInformation(InetAddress address, int port) {
		
		m_address = address;
		m_port = port;
		
	}
	
	public ContactInformation(String address, String port) {
		
		try { m_address = InetAddress.getByName(address); } catch (UnknownHostException e) { e.printStackTrace(); }
		m_port = Integer.parseInt(port);
		
		
	}
	
	
	public InetAddress getAddress() {
		
		return m_address;
		
	}
	
	public int getPort() {
		
		return m_port;
		
	}
	
	public String getAddressString() {
		
		return m_address.toString();
		
	}
	
	public String getPortString() {
		
		return Integer.toString(m_port);
	}
	

}
