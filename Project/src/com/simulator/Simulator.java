package com.simulator;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.reliableudp.OperationMessage;
import com.reliableudp.ReliableUDPSender;
import com.systeminitializer.SystemInitializer;

public class Simulator {

	
	public Simulator() {
		
		
		
	}
	
	
	public static void main(String args[]) throws Exception {
		
		String mode = args[0];
		int replicaId = Integer.parseInt(args[1]);

		// pas the replica id as argument
		int replicaPort;
		int replicaManagerPort=0;
		
		
		int port;
		switch(replicaId) {
		
			case 1:
				replicaPort = SystemInitializer.REPLICA_1_PORT;
				replicaManagerPort = SystemInitializer.REPLICA_MANAGER_1_PORT;
				break;
			case 2:
				replicaPort = SystemInitializer.REPLICA_2_PORT;
				replicaManagerPort = SystemInitializer.REPLICA_MANAGER_2_PORT;
				break;
			case 3:
				replicaPort = SystemInitializer.REPLICA_3_PORT;
				replicaManagerPort = SystemInitializer.REPLICA_MANAGER_3_PORT;
				break;
		
		}		
		
		InetAddress localhost = InetAddress.getByName("localhost");
		
		if (mode == "kill") {
			
			
			OperationMessage killMessage = new OperationMessage(OperationMessage.KILL_TEST);
			
			ReliableUDPSender sender = new ReliableUDPSender(localhost, replicaManagerPort);
			sender.send(killMessage);
			
		}
		
		if (mode == "forceError") {
			
			OperationMessage errorMessage = new OperationMessage(OperationMessage.TEST_ERROR);
			
			ReliableUDPSender sender = new ReliableUDPSender(localhost, replicaManagerPort);
			sender.send(errorMessage);
			
		}
		
	}
	
	
	
	
	
}
