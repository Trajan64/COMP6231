package FlightReservationApp;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyLogger {

	
	private final String 	m_fileDirectory = "./logs/";
	private FileWriter 		m_fileWriter;
	private	String			m_logName;
	
	
	MyLogger(String logName) {
		
//		try {
//			fileWriter = new FileWriter(fileDirectory + logName + ".txt");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		m_logName = logName;
		
	}
	
	
	public synchronized void log(String info) {
		
		// Get the date.
		//String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()); 
		String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()); 
				
		try {
			m_fileWriter = new FileWriter(m_fileDirectory + m_logName + ".txt", true);
			m_fileWriter.write(timeStamp + " " + info + '\n');
			m_fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
}
