package com.replicamanager;

import java.net.InetAddress;

import com.utils.ContactInformation;

public class ReplicaManagerInformation extends ContactInformation {
	
	private	int								m_id;
	private	boolean							m_available;
	private	ContactInformation				m_replicaClientInformation;
	
	private ReplicaManagerHeartbeat			m_heartbeat;
	private	boolean							m_hasCrashed;
	
	
	public ReplicaManagerInformation(InetAddress address, int port, int id, ContactInformation replicaClientInformation) {
		
		super(address, port);

		m_id = id;
		
		m_available = true;
		
		m_replicaClientInformation = replicaClientInformation;
		
	}
	
	
	public int getId() {
		
		return m_id;
		
	}
	
	
	public boolean isUnaivable() {
		
		return m_available;
		
	}
	
	
	public void setAvaiable() {
		
		m_available = true;
		
	}
	
	
	public void setUnavailable() {
	
		m_available = false;
		
	}
	
	public ContactInformation getReplicaClientInformation() {
		
		return m_replicaClientInformation;
		
	}
	
	
	public void setHeartbeat(ReplicaManagerHeartbeat heartbeat) {
		
		m_heartbeat = heartbeat;
		
	}
	
	
	public ReplicaManagerHeartbeat getHeartbeat() {
		
		return m_heartbeat;
		
	}
	
	public boolean hasCrashed() {
		
		return m_hasCrashed;
		
	}
	
	public void crashNoticed() {
		
		m_hasCrashed = true;
		
	}
	
	public void resetCrashNotice() {
		
		m_hasCrashed = false;
		
	}
	
}
