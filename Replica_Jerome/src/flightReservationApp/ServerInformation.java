package flightReservationApp;

public class ServerInformation {

	String m_city;
	int m_port;
	
	ServerInformation(String city, int port) {
		
		m_city = city;
		m_port = port;
		
	}
	
	public String getCity() {
		return m_city;
	}
	
	
	public int getPort() {
		return m_port;
	}
	
	
}
