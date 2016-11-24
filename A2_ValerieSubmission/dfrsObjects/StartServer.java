package dfrsObjects;
import dfrsObjects.ManagerServer;

import org.omg.CosNaming.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

import DFRSApp.DFRS;
import DFRSApp.DFRSHelper;

import java.util.Scanner;


public class StartServer {

	public static void main(String[] args) {
		try{
			ORB orb = ORB.init(args, null);
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();
			
			
			Scanner keyboard = new Scanner(System.in);
			System.out.println("Please select a server");
			System.out.println("1.Montreal\n2.New Delhi\n3.Washington");
			

			int userChoice = 0;
			int udpPort = 0;
			boolean valid = false;
			String server = "";
			
			while(!valid)
			{
				try{
					userChoice=keyboard.nextInt();
					valid=true;
				}
				catch(Exception e)
				{
					System.out.println("Invalid Input, please enter an Integer");
					valid=false;
					keyboard.nextLine();
				}
			}
			
			while(server == ""){
				switch(userChoice){
					case 1:
						server = "MTL";
						udpPort = 6789;
						break;
					case 2:
						
						server = "NDL";
						udpPort = 6790;
						break;
					case 3:
						server = "WST";
						udpPort = 6791;
						break;
					default:
						System.out.println("Invalid input. Please enter a digit between 1 and 3");
					
						
				}
					
			}
			keyboard.close();
			
			
			ManagerServer serverObj = new ManagerServer(server);
			serverObj.setORB(orb);
			
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(serverObj);
			DFRS href = DFRSHelper.narrow(ref);
			
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef); 

			NameComponent path[] = ncRef.to_name(server);
			ncRef.rebind(path, href);
			
			System.out.println(server + " server ready and waiting...");
			serverObj.startUDPServer(udpPort);
			
			for(;;){
				orb.run();
			}
			
		} catch(Exception e){
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
		System.out.println("Server exiting...");
	}
}
