package com.systeminitializer;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.replica.ReplicaClient;
import com.replicamanager.ReplicaManager;
import com.replicamanager.ReplicaManagerInformation;
import com.sequencer.Sequencer;
import com.utils.ContactInformation;

public class SystemInitializer {

	public static final int	MODE_ERROR_RECOVERY = 0;
	public static final int MODE_HIGH_AVAILABILITY = 1;
	
	private static final int REPLICA_1_PORT	= 7750;
	private static final int REPLICA_2_PORT	= 7751;
	private static final int REPLICA_3_PORT	= 7752;
	
	private static final int REPLICA_MANAGER_1_PORT	= 8000;
	private static final int REPLICA_MANAGER_2_PORT	= 8001;
	private static final int REPLICA_MANAGER_3_PORT	= 8002;
	
	private static final int SEQUENCER_PORT = 8060;

	private static int m_mode;

	private static final int STARTING_IMPLEMENTATION_REPLICA_1 = ReplicaClient.IMPLEMENTATION_MANDEEP;
	private static final int STARTING_IMPLEMENTATION_REPLICA_2 = ReplicaClient.IMPLEMENTATION_MANDEEP;
	private static final int STARTING_IMPLEMENTATION_REPLICA_3 = ReplicaClient.IMPLEMENTATION_MANDEEP;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//public ReplicaManagerInformation(InetAddress address, int port, int id, ContactInformation replicaClientInformation) {

		//	ReplicaManager(int mode, int id, int outPort, int startingImplementationId, ContactInformation replicaInformation, ReplicaManagerInformation[] replicaManagerInformations, ContactInformation sequencerInformation) {
		
		if (args[0].equals("-error_recovery")) {
			m_mode = MODE_ERROR_RECOVERY;
			System.out.println("SystemInitializer: Error recovery mode selected.");
		}
		else { 
			m_mode = MODE_HIGH_AVAILABILITY; 
			System.out.println("SystemInitializer: High availability mode selected.");	
		}
		
		String mode = args[0];
		
		InetAddress localhost = null;
		try { localhost = InetAddress.getByName("localhost"); } catch (UnknownHostException e) { e.printStackTrace(); }

		// Create the information needed about the replicas.
		
		ContactInformation replica1 = new ContactInformation(localhost, REPLICA_1_PORT);
		ContactInformation replica2 = new ContactInformation(localhost, REPLICA_2_PORT);
		ContactInformation replica3 = new ContactInformation(localhost, REPLICA_3_PORT);

		
		// Create the information needed about the replica managers.
		ReplicaManagerInformation[] replicaManagerInformations = new ReplicaManagerInformation[3];
		
		
		ReplicaManagerInformation replicaManagerInformation1 = new ReplicaManagerInformation(localhost, REPLICA_MANAGER_1_PORT, 0, replica1);
		ReplicaManagerInformation replicaManagerInformation2 = new ReplicaManagerInformation(localhost, REPLICA_MANAGER_2_PORT, 1, replica2);
		ReplicaManagerInformation replicaManagerInformation3 = new ReplicaManagerInformation(localhost, REPLICA_MANAGER_3_PORT, 2, replica3);
		
		replicaManagerInformations[0] = replicaManagerInformation1;
		replicaManagerInformations[1] = replicaManagerInformation2;
		replicaManagerInformations[2] = replicaManagerInformation3;
		
		ContactInformation sequencerInformation = new ContactInformation(localhost, SEQUENCER_PORT);
		Sequencer sequencer = new Sequencer(replicaManagerInformations, SEQUENCER_PORT);
		
		ReplicaManager replicaManager1 = new ReplicaManager(m_mode, 0, REPLICA_MANAGER_1_PORT, STARTING_IMPLEMENTATION_REPLICA_1, replica1, replicaManagerInformations, sequencerInformation);
		ReplicaManager replicaManager2 = new ReplicaManager(m_mode, 1, REPLICA_MANAGER_2_PORT, STARTING_IMPLEMENTATION_REPLICA_2, replica2, replicaManagerInformations, sequencerInformation);
		ReplicaManager replicaManager3 = new ReplicaManager(m_mode, 2, REPLICA_MANAGER_3_PORT, STARTING_IMPLEMENTATION_REPLICA_3, replica3, replicaManagerInformations, sequencerInformation);
		
		ReplicaManager[] replicaManagers = new ReplicaManager[3];
		replicaManagers[0] = replicaManager1;
		replicaManagers[1] = replicaManager2;
		replicaManagers[2] = replicaManager3;
		
		// Start all the replicas first.
		for (int i = 0; i < 3; i++) {
			ReplicaManager replicaManager =  replicaManagers[i];
			replicaManager.startReplica();
		}
		
		// Then setup the heartbeats.
		for (int i = 0; i < 3; i++) {
			ReplicaManager replicaManager =  replicaManagers[i];
			replicaManager.initializeHeartbeats();
		}
		
		// Thread each replica managers.
		for (int i = 0; i < 3; i++) {
			ReplicaManager replicaManager =  replicaManagers[i];
			replicaManager.start();
		}

	}

}
