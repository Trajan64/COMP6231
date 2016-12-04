package FlightReservationApp;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class ServerInit extends Thread {
	
	private String m_city;
	private String m_name;
	
	public ServerInit(String city, String name) {
		
		m_city = city;
		m_name = name;
		
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ServerInformationExtractor servInfExtract = new ServerInformationExtractor();
		ServerInformation[] servers = servInfExtract.getServerInformations();
				
		System.out.println("Starting servers..");
		
		int i;
		for (i = 0; servers[i] != null; i++) {
								
			// Start the servers
			ServerInit serverInit = new ServerInit(servers[i].getCity(), "");
			serverInit.start();			
		}

		

	}
	
	
	public void run() {
		
		String city = m_city;
		String name = m_name;
		System.out.println("Attempting to start server at " + city + " with naming sevice name:" + name);
		
		try {
		
			String[] argv = {"-ORBInitialPort", "1050", "-ORBInitialHost", "localhost"};
			
			ORB orb = ORB.init(argv, null);
			POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootPOA.the_POAManager().activate();
			
			ServerImpl server = new ServerImpl(city);
			
			org.omg.CORBA.Object ref = rootPOA.servant_to_reference(server);
			server href = serverHelper.narrow(ref);
			
			
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			
			NameComponent path[] = ncRef.to_name(name);
			ncRef.rebind(path, href);
			
			server.start(orb);
						
			orb.run();
			System.out.println("ServerInit: Shutting down server at " + city + " with naming service: " + name);
			
		}
		
		catch (Exception e) {
			e.printStackTrace(System.out);
		}
		
	}

}
