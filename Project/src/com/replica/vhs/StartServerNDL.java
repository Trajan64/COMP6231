package com.replica.vhs;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import com.config.Constants;

import FlightReservationApp.*;

public class StartServerNDL implements Runnable {
	
public String id = "";
	
	public StartServerNDL(String id) {
		this.id= id;
	}
	
	
	public static void main(String[] args) {
		StartServerNDL serverNDL = new StartServerNDL("2");
		Thread thread = new Thread(serverNDL);
		thread.start();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		String[] args = {"-ORBInitialPort", "1050", "-ORBInitialHost", "localhost"};
		try {
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);

			// get reference to rootpoa & activate the POAManager
			POA rootpoa = POAHelper.narrow(orb
					.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB
			ServerVHSImpl server = new ServerVHSImpl("NDL");
			server.start(orb);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(server);
			FlightReservation sref = FlightReservationHelper.narrow(ref);

			// get the root naming context
			// NameService invokes the name service
			org.omg.CORBA.Object objRef = orb
					.resolve_initial_references("NameService");
			// Use NamingContextExt which is part of the Interoperable
			// Naming Service (INS) specification.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// bind the Object Reference in Naming
			String name = Constants.NDL+"_"+id;
			System.out.println("NDL server ncref " + name);
			NameComponent path[] = ncRef.to_name(name);
			ncRef.rebind(path, sref);

			System.out.println("NDL Server ready and waiting ...");

			// wait for invocations from clients
			orb.run();
		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

		System.out.println("NDL Server Exiting ...");
	
		
	}
}
