package flightReservationApp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ServerInformationExtractor {

	private final File m_serverInformationFolder = new File("./servers");
	private ServerInformation m_servers[];
	private int m_count;
	
	ServerInformationExtractor() {
		
		m_servers = new ServerInformation[32];
		
		processFiles();
		
	}
	
	
	private void processFiles() {
		
		int i = 0;
		for (final File file : m_serverInformationFolder.listFiles()) {
			m_servers[i] = extractServerInformation(file);
	    	i++;
	    }
	    
		m_count = i;
		
	}

	
	private ServerInformation extractServerInformation(File fileServerInformation) {
		
		String name = fileServerInformation.getName();
		
		
		int port;
		
		try {
			
			BufferedReader reader = new BufferedReader(new FileReader(fileServerInformation));
			port = Integer.parseInt(reader.readLine());
			reader.close();
			
			return new ServerInformation(name, port);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public ServerInformation[] getServerInformations() {
		
		return m_servers;
		
	}
	
	public int getCount() {
		
		return m_count;
	}
	
	
	
	
}
