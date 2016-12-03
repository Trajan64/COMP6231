package com.client;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Scanner;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class ClientInterface {

	private static ClientManager m_clientManager;
	private static final String m_testDirectoryPath = "./tests2";
	private static TestThread[] m_threads;
	
	
	// Returns the number of spawned threads.
	private static void spawnTestThreads(File directory) {
	
		// Setup the threads.
		m_threads = new TestThread[32];
		
		// Create and provide a test file to each of the threads.
		int i = 0;
		for (final File file : directory.listFiles()) {
			
			m_threads[i] = new TestThread(file);
			i++;
	    }
		
		// Launch the threads.
		int j;
		for (j = 0; j < i; j++) {
			
			m_threads[j].start();
			
		}
		
		// Wait and clean up.
		for (i = 0; i < j; i++) {
			
			// Wait.
			try {
				m_threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Destroy.
			m_threads[i] = null;
			
		}
					
	}
		
	
	public static void main(String args[]) throws MalformedURLException {
		
		String[] options = {"-ORBInitialPort", "1050", "-ORBInitialHost", "localhost"};
		
		if (args.length > 0) {
			// Option was supplied.
			
			if (args[0].equals("-multitest")) {
				
				
				if (!(args.length >= 2)) {
					
					// Set File with test directory.
					File testDirectory = new File(m_testDirectoryPath);
					
					// Iterate of each of the folders within test directory.
					for (final File file : testDirectory.listFiles()) {	
						
						System.out.println("Performing tests within test directory: " + file.getPath());
						spawnTestThreads(file);
						
					}
					
					
					//System.out.println("A test directory must be provided in order to perform the testing");
					//return;
				}
				else {
				
					String subTestDirectoryString = m_testDirectoryPath + args[1];
					
					File subTestDirectory = new File(subTestDirectoryString);
					
					spawnTestThreads(subTestDirectory);
					
				}
			}
			
			/*
			if (args[0].equals("-startservers")) {
				
				
				ServerInformationExtractor servInfExtract = new ServerInformationExtractor();
				ServerInformation[] servers = servInfExtract.getServerInformations();
				
				ServerImpl server;
				
				System.out.println("Starting servers..");
				
				int i;
				for (i = 0; servers[i] != null; i++) {
										
					// Start the servers
					server = new Server(servers[i].getCity());
					try {
						server.exportServer();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				
				System.out.println("Servers started.");
				
				
			}
			
			*/
			
			
			if (args[0].equals("-killservers")) {
				
				
				ServerInformationExtractor servInfExtract = new ServerInformationExtractor();
				ServerInformation[] servers = servInfExtract.getServerInformations();
				
				ORB orb = org.omg.CORBA.ORB.init(options, null);
				
				int i;
				for (i = 0; servers[i] != null; i++) {
					
					String city = servers[i].getCity();					
					
					try {
						org.omg.CORBA.Object objRef;
						objRef = orb.resolve_initial_references("NameService");
						NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
						server serverToKill = serverHelper.narrow(ncRef.resolve_str(city));
						serverToKill.stop();
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			}
			
			
		}
		
		else {
			
			ORB orb = org.omg.CORBA.ORB.init(options, null);
			
			// Run the console in normal mode.
			
			m_clientManager = new ClientManager(orb);
			
			boolean stop = false;
			
			m_clientManager.showLoginHelp();
			while (!stop) {
				
				@SuppressWarnings("resource")
				Scanner input = new Scanner(System.in);
				
				String inputString = input.nextLine();
				
				stop = m_clientManager.process(inputString);
				
			}
			
			
		}
		
	}
	
}
