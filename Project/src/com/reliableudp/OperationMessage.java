package com.reliableudp;
import java.util.LinkedList;


public class OperationMessage {

	
	private int m_opid;
	private int m_id = 0;
	
	private LinkedList<String> 	m_contentComponents;
	private	String				m_messageString;
	
	int 						m_conversionStatus;
	
	
	// Protocol specification.
	public static final int 	ACK =								0;
	public static final String 	ACK_STR =							"ACK";

	public static final int 	REQUEST =							1;
	public static final String 	REQUEST_STR =						"REQUEST";
	
	public static final int 	REPLY = 							2;
	public static final String 	REPLY_STR =							"REPLY";
	
	public static final int 	SOFTWAREFAILURE = 					3;
	public static final String 	SOFTWAREFAILURE_STR =				"SOFTWAREFAILURE";
	
	public static final int 	ALIVE =								4;
	public static final String 	ALIVE_STR =							"ALIVE";
	
	public static final int 	RESTART = 							5;
	public static final String 	RESTART_STR =						"RESTART";
	
	public static final int 	FAILCONSENSUSREQ = 					6;
	public static final String 	FAILCONSENSREQ_STR =				"FAILCONSENSUSREQ";
	
	public static final int 	FAILCONSENSUSREPLYPOSITIVE = 		7;
	public static final String 	FAILCONSENSUSREPLYPOSITIVE_STR =	"FAILCONSENSUSREPLYPOSITIVE";
	
	public static final int 	FAILCONSENSUSREPLYNEGATIVE  =		8;
	public static final String 	FAILCONSENSUSREPLYNEGATIVE_STR =	"FAILCONSENSUSREPLYNEGATIVE";
	
	public static final int 	RESPONSE  =							9;
	public static final String 	RESPONSE_STR =						"RESPONSE";
	
	public static final int		STATEUPDATEREQUEST	=				10;
	public static final String 	STATEUPDATEREQUEST_STR =			"STATEUPDATEREQUEST";
	
	public static final int		RESTARTED =							11;
	public static final String 	RESTARTED_STR =						"RESTARTED";
	
	public static final int		RMUNAVAILABLE =						12;
	public static final String 	RMUNAVAILABLE_STR =					"RMUNAVAILABLE";

	
	private static final String	SEPERATORTOKEN = " ";
	
	public OperationMessage(int opid) {
		
		m_opid = opid;
		
		m_contentComponents = new LinkedList<String>();
		
	}
	
	
	public static final int CONVERSION_SUCCESS = 0;
	public static final int CONVERSION_FAILURE_NOT_ENOUGH_COMPS_IN_MESSAGE = 1;
	public static final int CONVERSION_FAILURE_OPID_NOT_AN_INT = 2;
	public static final int CONVERSION_FAILURE_ID_NOT_AN_INT = 3;
	public OperationMessage(byte[] rawMessage) {
		
		m_contentComponents = new LinkedList<String>();
		
		// Properly convert to String.
		String message = new String(rawMessage).trim();
		
		String[] components = message.split(SEPERATORTOKEN);
		
		// If number of components in the message is lesser than 2, then we are assured to have a badly formed message.
		// Message must at least contain an opid and an id.
		if (components.length < 2) {
			
			m_conversionStatus = CONVERSION_FAILURE_NOT_ENOUGH_COMPS_IN_MESSAGE;
			return;
		}
		
		// Extract opid.
		try {
			m_opid = Integer.parseInt(components[0]);
		} catch (Exception e) {
			m_conversionStatus = CONVERSION_FAILURE_OPID_NOT_AN_INT;
			return;
		}

		
		// Extract id.
		try {
			m_id = Integer.parseInt(components[1]);
		} catch (Exception e) {
			m_conversionStatus = CONVERSION_FAILURE_ID_NOT_AN_INT;
			return;
		}

		if (components.length > 2) {
						
			// Extract each of the message components.
			for (int i = 2; i < components.length; i++) {
				m_contentComponents.add(components[i]);
				
			}
			
		}
		
		buildMessage();

		m_conversionStatus = CONVERSION_SUCCESS;
		return;
		
	}
	
	
	public void setId(int id) {
		
		m_id = id;
		
	}
	
	
	public void addMessageComponent(String component) {
		
		m_contentComponents.add(component);
		
	}
	
	public int getOpid() {
		
		return m_opid;
	}
	
	
	public int getId() {
		
		return m_id;
	}
	
	public LinkedList<String> getContentComponents() {
		
		return m_contentComponents;
		
	}
	
	public String getMessage() {
		
		return m_messageString;
		
	}
	
	
	public byte[] toBytes() {
		
		return m_messageString.getBytes();
		
	}
	
	public void buildMessage() {
		
		m_messageString = m_opid + SEPERATORTOKEN + m_id;
		for (int i = 0; i < m_contentComponents.size(); i++) {
			
			m_messageString += SEPERATORTOKEN + m_contentComponents.get(i);
			
		}
		
	}
	
	// This method can be used to change the operation id of an already built message.
	public void setOpid(int newOpid) {
		
		m_opid = newOpid;
		
	}
	
	
	
	
}
