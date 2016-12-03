package com.replicamanager;

import java.net.InetAddress;
import java.util.concurrent.TimeoutException;

import com.reliableudp.OperationMessage;
import com.reliableudp.OperationMessageProcessorInterface;
import com.reliableudp.ReliableUDPSender;
import com.utils.ContactInformation;

public class ReplicaManagerHeartbeat extends Thread {

	private	ReplicaManagerInformation	m_replicaManagerInformation;
	private	ContactInformation			m_replicaClientInformation;
	
	private ReplicaManagerInformation	m_unmonitoredReplicaManager;
	
	private int							m_parentReplicaManagerId;
	
	private static final int			ALIVETIMETOTIMEOUT  = 1000;
	private static final int			TIMETOBEAT			= 2000;
	
	private	boolean						m_on;
	
	
	ReplicaManagerHeartbeat(ReplicaManagerInformation[] distantReplicaManagers, int replicaManagerToMonitor, int replicaManagerUnmonitored, int parentReplicaManagerId) {
		
		
		m_replicaManagerInformation = distantReplicaManagers[replicaManagerToMonitor];
		m_replicaClientInformation = distantReplicaManagers[replicaManagerToMonitor].getReplicaClientInformation();
		
		m_unmonitoredReplicaManager = distantReplicaManagers[replicaManagerUnmonitored];
		
		m_parentReplicaManagerId = parentReplicaManagerId;
		
		m_on = true;
	}
	
	
	public void setOn() {
		
		m_on = true;
		
	}
	
	public void setOff() {
		
		m_on = false;
		
	}
	
	
	
	public void run() {
		
		ReliableUDPSender sender = new ReliableUDPSender(m_replicaClientInformation.getAddress(), m_replicaClientInformation.getPort());
		sender.setMaxTimeouts(ALIVETIMETOTIMEOUT);
		
		OperationMessage aliveMessage = new OperationMessage(OperationMessage.ALIVE);
		OperationMessage reply;
				
		// Loop on sending ALIVE messages.
		while (true) {
			
			// The heartbeat can be switched to off in case of crash recovery.
			while (!m_on) {
				continue;
			}
			
			// Timer loop.
			long startTime = System.currentTimeMillis();
			while ((System.currentTimeMillis() - startTime) < TIMETOBEAT) {
				continue;
			}
			
			reply = null;
			
			// Send message.
			try {
				reply = sender.send(aliveMessage);
				System.out.println("Client: received response; " + "message has protocol id: " + reply.getOpid());
			} catch (TimeoutException e) {
				// We have not received a response within the specified time to timeout.
				// We can suppose that the replica client is unreachable.
				
				// Calling this method will inform that a crash may have happened on this replica client.
				// The parent ReplicaManager instance will be able to notice the crash if it receives a crash consensus agreement request.
				m_replicaManagerInformation.crashNoticed();
				
				// We engage in a crash consensus agreement with the unmonitored replica manager.
				OperationMessage notice = new OperationMessage(OperationMessage.FAILCONSENSUSREQ);
				
				// Add faulty replica manager id.
				notice.addMessageComponent(Integer.toString(m_replicaManagerInformation.getId()));
				// Add this replica manager id.
				
				// Sender to the other replica manager, which is not supposed to received alive messages from this instance.
				ReliableUDPSender sendToUnmonitoredReplicaManager = new ReliableUDPSender(m_unmonitoredReplicaManager.getAddress(), m_unmonitoredReplicaManager.getPort());
				OperationMessage crashAgreementResponse = null;
				try { crashAgreementResponse = sendToUnmonitoredReplicaManager.send(notice); } catch (TimeoutException e1) { e1.printStackTrace(); }
								
				// Process response.
				switch(crashAgreementResponse.getOpid()) {
				
					case OperationMessage.FAILCONSENSUSREPLYPOSITIVE:
						
						// The other replica manager has also noticed the crash.
						// We need to verify if the other replica manager has green-lit the crash resolution from our end.					
						boolean shouldResolveCrash;
						String stringBoolean = crashAgreementResponse.getContentComponents().get(0);
						if (stringBoolean.equals("1")) {
							shouldResolveCrash = true;
						}
						else { shouldResolveCrash = false; }
						
						if (shouldResolveCrash) {
							
							// We have priority.
							
							// Send restart message.
							// We have priority which means that we must be the one to send the restart signal to the faulty replica's replica manager.
							InetAddress address = m_replicaManagerInformation.getAddress();
							int			port	= m_replicaManagerInformation.getPort();
							ReliableUDPSender senderRestart = new ReliableUDPSender(address, port);
							try { senderRestart.send(new OperationMessage(OperationMessage.RESTART)); } catch (TimeoutException e2) { e2.printStackTrace(); }
							
							// Set off the heartbeat for now.
							setOff();
							
	
						}
						
						break;
						
					case OperationMessage.FAILCONSENSUSREPLYNEGATIVE:
						
						// No crash has been noticed.
						// The timeout may have been due to delay.
						continue;
				}
				
			
			}

			
		}
		
		
	}
	
	
	

}
