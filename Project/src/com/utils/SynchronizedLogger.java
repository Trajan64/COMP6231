package com.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SynchronizedLogger {

	private final String 	m_fileDirectory = "./logs/";
	private FileWriter 		m_fileWriter;
	private	String			m_logName;
	
	private static final String LINE = "-----------------";
	
	
	public SynchronizedLogger(String logName) {
		
		m_logName = logName;
		
	}
	
	public synchronized void addStartLine() {
		log(LINE);
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
