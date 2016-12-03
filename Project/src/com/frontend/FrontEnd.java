package com.frontend;

import java.net.InetAddress;
import java.util.LinkedList;

import org.omg.CORBA.ORB;

import com.replicamanager.ReplicaManagerInformation;
import com.utils.ContactInformation;

import FlightReservationApp.FlightReservationPOA;

public class FrontEnd extends FlightReservationPOA {

	private String 							m_city;
	private	int								m_mode;
	private	ORB								m_orb;
	private	ReplicaManagerInformation[] 	m_replicaManagers;
	private ContactInformation				m_sequencerInformation;
	private	InetAddress						m_address;
	
	
	public FrontEnd(int mode, String city, ReplicaManagerInformation[] replicaManagers, ContactInformation sequencerInformation, InetAddress address) {
		
		m_mode = mode;
		m_city = city;
		m_replicaManagers = replicaManagers;
		m_sequencerInformation = sequencerInformation;
		m_address = address;
		
	}
	
	private class Request {
		
		LinkedList<String> m_components;
		
		public Request(String methodName) {
			m_components = new LinkedList<String>();
		}
		
		public void addArgument(String argument) {
			m_components.add(argument);
		}
		
		public void addArgument(int argument) {
			m_components.add(Integer.toString(argument));
		}
		
		public LinkedList<String> getComponents() {
			return m_components;
		}
		
	}
	
	
	public String bookFlight(String firstName, String lastName, String address, String phone, String destination, String date, String classType) {
		
		Request request = new Request("bookFlight");
		request.addArgument(firstName);
		request.addArgument(lastName);
		request.addArgument(address);
		request.addArgument(phone);
		request.addArgument(destination);
		request.addArgument(date);
		request.addArgument(classType);
		
		FrontEndRequestSender requester = new FrontEndRequestSender(m_mode, m_city, m_replicaManagers, m_sequencerInformation, request.getComponents(), m_address);
		return requester.getReply();
	}
	
	
	public String getBookedFlightCount(String recordType) {
		
		Request request = new Request("getBookedFlightCount");
		request.addArgument(recordType);

		FrontEndRequestSender requester = new FrontEndRequestSender(m_mode, m_city, m_replicaManagers, m_sequencerInformation, request.getComponents(), m_address);
		return requester.getReply();

		
	}
	
	
	public String editFlightRecord(String recordId, String fieldName, String newValue) {
		
		Request request = new Request("editFlightRecord");
		request.addArgument(recordId);
		request.addArgument(fieldName);
		request.addArgument(newValue);

		FrontEndRequestSender requester = new FrontEndRequestSender(m_mode, m_city, m_replicaManagers, m_sequencerInformation, request.getComponents(), m_address);
		return requester.getReply();
		
		
	}
	
	
	public String transferReservation(String passengerId, String currentCity, String otherCity) {
		
		Request request = new Request("transferReservation");
		request.addArgument(passengerId);
		request.addArgument(currentCity);
		request.addArgument(otherCity);

		FrontEndRequestSender requester = new FrontEndRequestSender(m_mode, m_city, m_replicaManagers, m_sequencerInformation, request.getComponents(), m_address);
		return requester.getReply();
		
		
	}
	
	
	public void start(ORB orb) throws Exception {
		
		m_orb = orb;
		
	}

}
