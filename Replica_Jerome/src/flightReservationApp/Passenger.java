package flightReservationApp;
import java.io.Serializable;
import java.util.Date;

public class Passenger implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int	   	m_id;
	private String 	m_firstName;
	private String 	m_lastName;
	private String 	m_address;
	private String 	m_phone;
	private String 	m_destination;
	private Date 	m_date;
	private int		m_classType;
	
	Passenger(int id, String firstName, String lastName, String address, String phone, String destination, Date date, int classType) {

		/*
		synchronized (Passenger.class) {
			m_id = m_nextId;
			m_nextId++;
		}
		*/
		
		m_id = id;
		
		m_firstName = firstName;
		m_lastName = lastName;
		m_address = address;
		m_phone = phone;
		m_destination = destination;
		m_date = date;
		m_classType = classType;
		
		
	}
	
	public String getFirstName() {
		
		return m_firstName;
	}
	
	public String getLastName() {
		
		return m_lastName;
		
	}
	
	public String getAddress() {
		
		return m_address;
	
	}
	
	
	public String getPhone() {
		
		return m_phone;
	}
	
	public String getDestination() {
		
		return m_destination;
	}
	
	public Date getDate() {
		
		return m_date;
	}
	
	public int getClassType() {
		
		return m_classType;
		
	}
	
	public int getId() {
		
		return m_id;
	}
	
	
}
