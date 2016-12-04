package flightReservationApp;

import java.util.Date;
import java.util.LinkedList;

public class Flight {

	private int m_recordId;
	
	private String m_location;
	private String m_destination;
	private int m_seatsFirstClass;
	private int	m_seatsBuisnessClass;
	private int m_seatsEconomyClass;
	private Date m_timeOfDeparture;
	
	private int m_registeredSeatsFirstClass;
	private int m_registeredSeatsBuisnessClass;
	private int m_registeredSeatsEconomyClass;
	
	public static final int ALLCLASS = 0;
	public static final int FIRSTCLASS = 1;
	public static final int BUISNESSCLASS = 2;
	public static final int ECONOMYCLASS = 3;
	
	private LinkedList<Passenger> m_registeredPassengers;
	
	
	Flight(int recordId, String location, String destination, int seatsFirstClass, int seatsBuisnessClass, int seatsEconomyClass, Date timeOfDeparture) {
		
		m_recordId = recordId;
		
		m_location = location;
		m_destination = destination;
		m_seatsFirstClass = seatsFirstClass;
		m_seatsBuisnessClass = seatsBuisnessClass;
		m_seatsEconomyClass = seatsEconomyClass;
		m_timeOfDeparture = timeOfDeparture;
		
		m_registeredSeatsFirstClass = 0;
		m_registeredSeatsBuisnessClass = 0;
		m_registeredSeatsEconomyClass = 0;
		
		m_registeredPassengers = new LinkedList<Passenger>();
	}
	
	public String getLocation() {
		
		return m_location;
		
	}
	
	public String getDestination() {
		
		return m_destination;
		
	}
	
	public int getAvailableSeatsFirstClass() {
		
		return m_seatsFirstClass - m_registeredSeatsFirstClass;
		
	}
	
	public int getAvailableSeatsBuisnessClass() {
		
		return  m_seatsBuisnessClass - m_registeredSeatsBuisnessClass;
	}
	
	public int getAvailableSeatsEconomyClass() {
		
		return m_seatsEconomyClass - m_registeredSeatsEconomyClass;
		
	}
	
	
	public int getAvailableSeatsForClassType(int classType) {
		
		switch(classType) {
			case ALLCLASS:
				return 0; //TODO
			case FIRSTCLASS:	
				return getAvailableSeatsFirstClass();
			case BUISNESSCLASS:
				return getAvailableSeatsBuisnessClass();
			case ECONOMYCLASS:
				return getAvailableSeatsEconomyClass();
			default:
				return 0;
		}
		
	}
	
	
	public int getTotalRegisteredSeats() {
		
		return m_registeredSeatsFirstClass + m_registeredSeatsBuisnessClass + m_registeredSeatsEconomyClass;
		
	}
	
	public int getRegisteredSeatsFirstClass() {
		
		return m_registeredSeatsFirstClass;
		
	}

	public int getRegisteredSeatsBuisnessClass() {
		
		return m_registeredSeatsBuisnessClass;
		
	}
	
	public int getRegisteredSeatsEconomyClass() {
		
		return m_registeredSeatsEconomyClass;
		
	}
	
	
	public Date getTimeOfDeparture() {
		
		return m_timeOfDeparture;
		
	}
	
	public void setRegisteredSeatForClassType(int classType, int seats) {
		
		switch(classType) {
			case FIRSTCLASS:	
				m_registeredSeatsFirstClass = seats;
				return;
			case BUISNESSCLASS:
				m_registeredSeatsBuisnessClass = seats;
				return;
			case ECONOMYCLASS:
				m_registeredSeatsEconomyClass = seats;
				return;
			default:
				return;
		}
	}
	
	public void setSeatsForClassType(int classType, int seats) {
		
		switch(classType) {
			case FIRSTCLASS:	
				m_seatsFirstClass = seats;
				return;
			case BUISNESSCLASS:
				m_seatsBuisnessClass = seats;
				return;
			case ECONOMYCLASS:
				m_seatsEconomyClass = seats;
				return;
			default:
				return;
		}
	}

	
	public int getRegisteredSeatsForClassType(int classType) {
		
		switch(classType) {
			case ALLCLASS:
				return m_registeredSeatsFirstClass + m_registeredSeatsBuisnessClass + m_registeredSeatsEconomyClass;
			case FIRSTCLASS:	
				return m_registeredSeatsFirstClass;
			case BUISNESSCLASS:
				return m_registeredSeatsBuisnessClass;
			case ECONOMYCLASS:
				return m_registeredSeatsEconomyClass;
			default:
				return 0;
		}
		
		
	}
	
	public int getSeatsForClassType(int classType) {
		
		switch(classType) {
			case ALLCLASS:
				return m_seatsFirstClass + m_seatsBuisnessClass + m_seatsEconomyClass;
			case FIRSTCLASS:	
				return m_seatsFirstClass;
			case BUISNESSCLASS:
				return m_seatsBuisnessClass;
			case ECONOMYCLASS:
				return m_seatsEconomyClass;
			default:
				return 0;
		}
		
		
	}
	
	public static String seatConstantToString(int classType) {
		
		switch(classType) {
			case ALLCLASS:
				return "all classes";
			case FIRSTCLASS:	
				return "first class";
			case BUISNESSCLASS:
				return "buisness class";
			case ECONOMYCLASS:
				return "economy class";
			default:
				return "";
		}
		
	}
	
	
	public void setRegisteredSeatsFirstClass(int seats) {
		
		m_registeredSeatsFirstClass = seats;
		
	}
	
	
	public void setRegisteredSeatsBuisnessClass(int seats) {
		
		m_registeredSeatsBuisnessClass = seats;
		
	}
	
	public void setRegisteredSeatsEconomyClass(int seats) {
		
		m_registeredSeatsEconomyClass = seats;
		
	}
	
	
	public void setSeatsFirstClass(int seats) {
		
		m_registeredSeatsFirstClass = seats;
		
	}
	
	
	public void setSeatsBuisnessClass(int seats) {
		
		m_registeredSeatsBuisnessClass = seats;
		
	}
	
	public void setSeatsEconomyClass(int seats) {
		
		m_registeredSeatsEconomyClass = seats;
		
	}
	
	public void setTimeOfDeparture(Date timeOfDeparture) {
	
		m_timeOfDeparture = timeOfDeparture;
		
	}
	
	public void setLocation(String location) {
		
		m_location = location;
	}
	
	
	public void setDestination(String destination) {
		
		m_destination = destination;
	}
	
	public int getId() {
		
		return m_recordId;
		
	}
	
	public void addPassenger(Passenger passenger) {
		
		m_registeredPassengers.push(passenger);
		
	}
	
	public LinkedList<Passenger> getPassengers() {
		
		return m_registeredPassengers;
		
	}
	
	public String toString() {
		
		return "[ Dest: " + m_destination + ", SFC: " + m_seatsFirstClass + ", SBC: " + m_seatsBuisnessClass + ", SBC: " + m_seatsEconomyClass + ", Date: " + m_timeOfDeparture.toString() + " ]"; 
		
	}
		
}
