package com.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.omg.CORBA.ORB;

public class TestThread extends Thread {

	//private File			m_testFile;
	private BufferedReader	m_reader;
	private ClientManager	m_clientManager;
	
	public TestThread(File testFile) {
		
		try {
			m_reader = new BufferedReader(new FileReader(testFile));			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String[] options = {"-ORBInitialPort", "1050", "-ORBInitialHost", "localhost"};
		ORB orb = org.omg.CORBA.ORB.init(options, null);
		
		m_clientManager = new ClientManager(orb);
		
	}
	
	public void run() {
		
		System.out.println("Thread running..");
		
		String line;
		try {
			
			while ( (line = m_reader.readLine()) != null) {
				m_clientManager.process(line);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Thread exitting..");
	}
	
	
}
