package com.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.omg.CORBA.ORB;

import FlightReservationApp.FlightReservationPOA;

import com.beans.FlightRecord;
import com.beans.PassengerRecord;
import com.config.Constants;
import com.logging.Logger;

public class FlightReservationImpl extends FlightReservationPOA implements Runnable{

	private HashMap<Character, ArrayList<PassengerRecord>> recordMap = new HashMap<Character, ArrayList<PassengerRecord>>();
	private HashMap<String, FlightRecord> flightRecordMap = new HashMap<String, FlightRecord>();
	
	private Thread t;

	private Logger log;
	private static long ids = 0;
	private String udpServerThreadName;
	private int udpServerPort;
	private String serverName;
	private int udpPeerServerPort1;
	private int udpPeerServerPort2;
	private Map<Integer,String> serverMapping = new HashMap<Integer,String>();
	private String resultGetBookedFlightCount = "fail";
	private String res1;
	private String res2;
	private String res3;
	private ORB orb;
	
	
	
	public void setORB(ORB orb_val) {
		orb = orb_val;
	}
	
	


	
	/*
	 * generates sequence id for record
	 */
	public synchronized static long getNextId() {
		ids++;
		return ids;
	}
	
	
	/*
	 * return the first char in upper case
	 */
	private Character getFirstChar(String str) {
		Character ch = new Character(Character.toUpperCase(str.charAt(0)));
		return ch;
	}

	
	/*
	 * Logs 
	 */
	public void log(String logInfo) {
		logInfo = serverName + " server ::: " + logInfo;
		log.info(logInfo);
	}
	
	

	
	public FlightReservationImpl(String serverName, int udpServerPort, int udpPeerServerPort1, int udpPeerServerPort2) {
		log = new Logger("logs/server/FlightReservation"+serverName+".log");
		for (int i = 65; i <= 90; i++) {
			recordMap.put(new Character((char)i), new ArrayList<PassengerRecord>());
		}
		this.serverName = serverName;
		this.udpServerPort = udpServerPort;
		this.udpServerThreadName = serverName + "UdpThread";
		this.udpPeerServerPort1 = udpPeerServerPort1;
		this.udpPeerServerPort2 = udpPeerServerPort2;
		serverMapping.put(Constants.MTL_UDP_SERVER_PORT, "MTL ");
		serverMapping.put(Constants.WST_UDP_SERVER_PORT, "WST ");
		serverMapping.put(Constants.NDL_UDP_SERVER_PORT, "NDL ");
	}
	
	@Override
	public String bookFlight(String firstName, String lastName, String address,
			String phone, String destination, String date, String flightClass) {
		
		return bookFlightInfo(firstName, lastName, address, phone, destination, date, flightClass);
		
	}

	@Override
	public String getBookedFlightCount(final String recordType) {
		log.info("Start FlightReservationImpl:: getBookedFlightCount() method");
		log.info("RecordType : " + recordType);

		Thread myThreads[] = new Thread[2];
		
		//UDP Client thread for NDL
		myThreads[0] = new Thread(
	            new Runnable() {
	                public void run() {
	                	DatagramSocket aSocket = null;
	                	try {
		                	//Send request to NDL Server for getBookedFlightCount
		        			log.info("Send request to "+serverMapping.get(udpPeerServerPort1)+" Server for getBookedFlightCount");
		        			aSocket = new DatagramSocket();
		        			
		        			String requestmessage = "REQ "+recordType; 
		        			byte[] m = requestmessage.getBytes();
		        			
		        			InetAddress aHost = InetAddress.getByName("localhost");
		        			int serverPort = udpPeerServerPort1;
		        			DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
		        			aSocket.send(request);
		        			byte[] buffer = new byte[1000];
		        			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
		        			aSocket.receive(reply);
		        			res2 = new String(reply.getData(), 0, reply.getLength());
		        			log.info(serverMapping.get(udpPeerServerPort1)+ " Server RecordCount : " + res2);
	                	} catch (SocketException e){
		        			log.info("FlightReservationImplNdl:: getBookedFlightCount(): " + e.getMessage());
		        			resultGetBookedFlightCount = "fail";
		        		} catch (IOException e){
		        			log.info("FlightReservationImplNdl:: getBookedFlightCount(): " + e.getMessage());
		        			resultGetBookedFlightCount = "fail";
		        		} finally {
		        			if(aSocket != null) 
		        				aSocket.close();
		        		}
	                }
	            }
	        );
		
		//UDP Client thread for WST
		myThreads[1] = new Thread(
	            new Runnable() {
	                public void run() {
	                	DatagramSocket aSocket = null;
	                	try {
							//Send request to WST Server for getBookedFlightCount
							log.info("Send request to "+serverMapping.get(udpPeerServerPort2)+" Server for getBookedFlightCount");
							aSocket = new DatagramSocket();
							
							String requestmessage = "REQ "+recordType; 
							
							byte[] m = requestmessage.getBytes();
							InetAddress aHost = InetAddress.getByName("localhost");
							int serverPort = udpPeerServerPort2;
							DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
							aSocket.send(request);
							byte[] buffer = new byte[1000];
							DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
							aSocket.receive(reply);
							res3 = new String(reply.getData(), 0, reply.getLength());
							log.info(serverMapping.get(udpPeerServerPort2)+ " Server RecordCount : " + res3);
		                } catch (SocketException e){
		        			log.info("FlightReservationImplWst:: getBookedFlightCount(): " + e.getMessage());
		        			resultGetBookedFlightCount = "fail";
		        		} catch (IOException e){
		        			log.info("FlightReservationImplWst:: getBookedFlightCount(): " + e.getMessage());
		        			resultGetBookedFlightCount = "fail";
		        		} finally {
		        			if(aSocket != null) 
		        				aSocket.close();
		        		}
					}
			    }
			);

			//starts the thread
			for (Thread thread : myThreads) {
				thread.start();
			}
			
			//to make current thread wait for the completion of the client threads to get the respective count
			for (Thread thread : myThreads) {
				try {
					thread.join();
				} catch (InterruptedException e) {
			        e.printStackTrace();
			    }
			}
			
			res1 = Integer.toString(getLocalRecordCount(recordType));
			log.info("Mtl Server RecordCount : " + res1);
			resultGetBookedFlightCount = serverMapping.get(udpServerPort)+ res1 + ";"+serverMapping.get(udpPeerServerPort1) + res2 + ";"+serverMapping.get(udpPeerServerPort2) + res3;
		
		log.info("Result Total Record Count : " + resultGetBookedFlightCount);
		log.info("End FlightReservationImpl:: getBookedFlightCount() method");
		return resultGetBookedFlightCount;
	}

	

	@Override
	public String transferReservation(String passengerID, String currentCity,
			String otherCity) {
		log.info("Start FlightReservationImpl:: transferRecord() method");
		log.info("PassengerID : " + passengerID);
		log.info("currentCity : " + currentCity);
		log.info("otherCity : " + otherCity);
		String result = "fail";
		int otherCityUdpServerPort;
		
		if(currentCity.equalsIgnoreCase(otherCity)){
			log.info("Current City and other city should be different");
			return result;
		}
		synchronized (flightRecordMap) {
			
		
		PassengerRecord passengerRecord = getPassengerRecordUsingID(passengerID);
		log.info("Passenger Record is :: "+passengerRecord);
		//PassengerRecord passengerRecord = new PassengerRecord("First1", "firstName", "lastName", "address", "phone", "mtl", "ndl", new Date(), "First");
		if(null != passengerRecord){
			synchronized (passengerRecord) {
				
			
			switch(otherCity){
			
			case "mtl":
				otherCityUdpServerPort=Constants.MTL_UDP_SERVER_PORT;
				break;
			case "wst":
				otherCityUdpServerPort=Constants.WST_UDP_SERVER_PORT;
				break;
			case "ndl":
				otherCityUdpServerPort=Constants.NDL_UDP_SERVER_PORT;
				break;
			
				default:
					return "Invalid city";
			}
			
			if(udpTransfer(passengerRecord, otherCityUdpServerPort)){
				List<PassengerRecord> listPassenger = new ArrayList<PassengerRecord>();
				listPassenger.add(passengerRecord);
				deRegisterFromPassengerTable(listPassenger);
				
				removePassengerFromFlightTable(passengerRecord);
				
				
				return passengerID;
			}
			}
		}else{
			log.info("Passenger Id sent in the request "+passengerID + " is not present in DB.");
		}
		}
		
		return result;
	}
	
	private Boolean udpTransfer(PassengerRecord passengerRecord,int otherCityServer) {
		

    	DatagramSocket aSocket = null;
    	try {
			//Send request to WST Server for getBookedFlightCount
			log.info("Send request to "+ serverMapping.get(otherCityServer)+" Server for transfer records with otherCity port "+otherCityServer);
			aSocket = new DatagramSocket();
			
			String requestMessage = "ABC ";
			
			// Serialize the passenger record and get the bytes.
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			outputStream.write(requestMessage.getBytes());
			
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			os.writeObject(passengerRecord);
			byte[] data = outputStream.toByteArray();
			
			InetAddress aHost = InetAddress.getByName("localhost");
			int serverPort = otherCityServer;
			DatagramPacket request = new DatagramPacket(data, data.length, aHost, serverPort);
			aSocket.send(request);
			
			log.info("Transfer request sent through UDP to " + serverMapping.get(otherCityServer) + " for passenger with ID " + passengerRecord.getRecordId());
			
			
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);
			
			String response = new String(reply.getData()).trim();
			
			log.info("Reply from UDP transfer method " + response);
			
			if(!response.equalsIgnoreCase("fail")){
				return true;
			}
        } catch (SocketException e){
			log.info("FlightReservationImplWst:: getBookedFlightCount(): " + e.getMessage());
			resultGetBookedFlightCount = "fail";
		} catch (IOException e){
			log.info("FlightReservationImplWst:: getBookedFlightCount(): " + e.getMessage());
			resultGetBookedFlightCount = "fail";
		} finally {
			if(aSocket != null) 
				aSocket.close();
		}
	
		
		return false;
	}
	public void start() {
		log("Start FlightReservationImpl:: start() method");

		if (t == null) {
			t = new Thread(this, udpServerThreadName);
			t.start();
			log("FlightReservationImpl:: start() ::UDP Server Thread Started");
		}
		log("End FlightReservationImpl:: start() method");
	}

	/*
	 * Thread for UDP Server
	 */
	public void run() {
		
		log.info("Start FlightReservationImpl:: run() method");
		DatagramSocket asocket = null;
		try{
			asocket = new DatagramSocket(udpServerPort);
			log.info("udpServerPort---"+udpServerPort);
			while(true) {
				byte[] incomingData = new byte[1000];
				DatagramPacket request = new DatagramPacket(incomingData, incomingData.length);
				asocket.receive(request);
				
				byte[] message = new byte[1000];
				message = request.getData();
				String req = new String(message).trim();
				
				//String reqStr = new String(request.getData(), 0, request.getLength());
				//log.info("FlightReservationImpl:: run() :: Request for getBookedFlightCount of type " + reqStr);
				int count = 0;
				
				String[] array = req.split(" ",2);
				if("REQ".equals(array[0])){
				
				log.info("Record Type is :: " + req);
				count = getLocalRecordCount(array[1].trim());
				
				byte[] buffer = new byte[1000];
				log.info("FlightReservationImpl:: run() :: RecordCount sent: " + count);
				buffer = Integer.toString(count).getBytes();
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());
				asocket.send(reply);
				}else{
					log.info("Inside udp transfer Server---");

					
					int byteSerializedObjectIndex = "ABC ".length();
					
					byte[] serializedObjectData = Arrays.copyOfRange(request.getData(), byteSerializedObjectIndex, request.getData().length);
					
					ByteArrayInputStream in = new ByteArrayInputStream(serializedObjectData);
					ObjectInputStream is = new ObjectInputStream(in);
					
					PassengerRecord newPassenger = null;
					
					try {
						
						newPassenger = (PassengerRecord) is.readObject();
						//Object o =  is.readObject();
						log.info("NEW PASSENGER Object is---"+newPassenger);
						String toSend = bookFlightInfo(newPassenger.getFirstName(),
								newPassenger.getLastName(),
								newPassenger.getAddress(),
								newPassenger.getPhone(),
								newPassenger.getDestination(),
								FlightReservationUtil.dateToString(newPassenger.getDate()),
								newPassenger.getFlightClass());
						log.info("RESPONSE from Book flight method---------" + toSend);
						byte[] buffer = new byte[1000];
						
						buffer = toSend.getBytes();
						DatagramPacket reply = new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());
						asocket.send(reply);
						log.info("RESPONSE from UDP Server--------- " + toSend);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					
						
					
					
					
				
				}
			}
		} catch(SocketException e) {
			e.printStackTrace();
			log.info("FlightReservationImpl:: run() method:" + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			log.info("FlightReservationImpl:: run() method:" + e.getMessage());
		} finally {
			if (asocket != null) {
				asocket.close();
			}
		}
		log.info("End FlightReservationImpl:: run() method");
	}
	private List<FlightRecord> fetchTheFlightRecord(String destination, Calendar date, String flightClass) {
		List<FlightRecord> listFlightRecords = new ArrayList<FlightRecord>();
		Collection<FlightRecord> values = flightRecordMap.values();
		for(FlightRecord flightRecord : values){
			// to check flight going to a destination on that day
			if(flightRecord.getDestination().equalsIgnoreCase(destination) && DateUtils.isSameDay(flightRecord.getDate(), date)){
				
				listFlightRecords.add(flightRecord);
			}
		}
		
		return  listFlightRecords;
	}
	
	public synchronized int  getLocalRecordCount(String recordType) {
		log.info("Start FlightReservationImpl:: getLocalRecordCount() method");
		int count = 0;
		log.info("FlightReservationImpl:: getLocalRecordCount() ::recordType" + recordType);
		ArrayList<ArrayList<PassengerRecord>> listList = new ArrayList<ArrayList<PassengerRecord>>(recordMap.values());
		if(recordType.equals(FlightReservationConstants.ALL_CLASS)){
			for (Iterator iterator = listList.iterator(); iterator.hasNext();) {
				ArrayList<PassengerRecord> arrayList = (ArrayList<PassengerRecord>) iterator.next();
				//synchronized block for list
				synchronized (arrayList) {
					for (Iterator iterator2 = arrayList.iterator(); iterator2.hasNext();) {
						iterator2.next();
							count++;
						
					}
				}
			}
		}else{
		for (Iterator iterator = listList.iterator(); iterator.hasNext();) {
			ArrayList<PassengerRecord> arrayList = (ArrayList<PassengerRecord>) iterator.next();
			//synchronized block for list
			synchronized (arrayList) {
				for (Iterator iterator2 = arrayList.iterator(); iterator2.hasNext();) {
					PassengerRecord record = (PassengerRecord) iterator2.next();
					if(record.getRecordId().contains(recordType))
					{
						count++;
					}
				}
			}
		}
		}
		log.info("FlightReservationImpl:: getLocalRecordCount() :: Local Record count ("+recordType+")" + count);
		log.info("End FlightReservationImpl:: getLocalRecordCount() method");
		return count;
	}
	
	private void deRegisterFromPassengerTable(List<PassengerRecord> list){
		log.info("Passenger Data base before: "+recordMap);
		List<ArrayList<PassengerRecord>> listPassengerDB = new ArrayList<ArrayList<PassengerRecord>>(recordMap.values());
		log.info("Passenger DB size "+listPassengerDB.size());
		
		synchronized (list) {
			for(PassengerRecord passengerRecord : list){
				
				for(ArrayList<PassengerRecord> array : listPassengerDB){
					
					for(Iterator<PassengerRecord> iterator = array.iterator(); iterator.hasNext();){
						
						PassengerRecord passenger = iterator.next();
						if(passengerRecord.equals(passenger)){
							iterator.remove();
						}
					}
				}
					
				}
		}
		
		log.info("Passenger Data base after: "+recordMap);
	}


	private void removePassengerFromFlightTable(PassengerRecord passengerRecord){
		
		log.info("Flight Data base before: "+flightRecordMap);
		
		synchronized (flightRecordMap) {
			
		
		List<FlightRecord> listFlightRecord = new ArrayList<FlightRecord>(flightRecordMap.values());
		
		
		
			for(FlightRecord flightRecord:listFlightRecord){
				
			//	synchronized (flightRecord) {
				List<PassengerRecord> array = flightRecord.getListPassenger();
				
				for(Iterator<PassengerRecord> iterator = array.iterator(); iterator.hasNext();){
					
					PassengerRecord passenger = iterator.next();
					if(passengerRecord.equals(passenger)){
						int count = flightRecord.getFlightClassMap().get(passengerRecord.getFlightClass())+1;
						
						flightRecord.getFlightClassMap().put(passengerRecord.getFlightClass(), count);
						
						iterator.remove();
						log.info("Flight Datbase after removing passenger :: "+flightRecordMap);
					}
				}
				
			//}
				
				
		}
	
		}
	}


	@Override
	public String editFlightRecord(String recordID, String fieldName,
			String newValue) {
		

		
		log.info("Start FlightReservationImpl:: editFlightRecord() method");
		log.info("recordID:" + recordID);
		log.info("fieldName:" + fieldName);
		log.info("newValue:" + newValue);
		FlightRecord flightRecord = null;
		String result = "fail";
		
		switch(fieldName){
		
		case FlightReservationConstants.CREATE_FLIGHT :
				
				if(null == flightRecordMap.get(recordID)){
					String array[]=newValue.split(",");
					
				flightRecord = createFlightRecord(array);
				synchronized (flightRecordMap) {
					
					flightRecordMap.put(recordID, flightRecord);
					result=recordID;
				}
				}else{
					log.info("Flight Record Already Exists with the flight Id :: "+recordID);
				}
				break;
						
		case FlightReservationConstants.FLIGHT_DATE:

			 flightRecord = flightRecordMap.get(recordID);
			if (null != flightRecord) {
				synchronized (flightRecord) {
					try {
						deRegisterFromPassengerTable(flightRecord.getListPassenger());
						int countBusiness=0;
						int countEconomy=0;
						int countFirst=0;
						
						for(PassengerRecord passengerRecord : flightRecord.getListPassenger()){
							if(passengerRecord.getFlightClass().equals(FlightReservationConstants.BUSINESS_CLASS)){
								countBusiness++;
							}
							if(passengerRecord.getFlightClass().equals(FlightReservationConstants.ECONOMY_CLASS)){
								countEconomy++;
							}
							if(passengerRecord.getFlightClass().equals(FlightReservationConstants.FIRST_CLASS)){
								countFirst++;
							}
						}
						
						countBusiness = flightRecord.getFlightClassMap().get(FlightReservationConstants.BUSINESS_CLASS)+countBusiness;
						countEconomy = flightRecord.getFlightClassMap().get(FlightReservationConstants.ECONOMY_CLASS)+countEconomy;
						countFirst = flightRecord.getFlightClassMap().get(FlightReservationConstants.FIRST_CLASS)+countFirst;
						
						Map<String,Integer> map = new HashMap<String,Integer>();
						map.put(FlightReservationConstants.BUSINESS_CLASS, countBusiness);
						map.put(FlightReservationConstants.ECONOMY_CLASS, countEconomy);
						map.put(FlightReservationConstants.FIRST_CLASS, countFirst);
						
						//updating the seats 
						flightRecord.setFlightClassMap(map);
						flightRecord.setDate(FlightReservationUtil.stringToDate((String)newValue));
						//removing passengers from flight table
						flightRecord.getListPassenger().removeAll(flightRecord.getListPassenger());
					} catch (ParseException e) {
						log.info("Error while parsing the date string in request "+e.getMessage());
					}

					flightRecordMap.put(recordID, flightRecord);
					result = recordID;
				}
			} else {
				log.info("Flight Id: " + recordID + " doesnt exits in database");
			}
			break;	
		case FlightReservationConstants.BUSINESS_CLASS:
			flightRecord = flightRecordMap.get(recordID);
			if(null!=flightRecord){
				synchronized (flightRecord) {
				Integer updateValue = Integer.parseInt((String) newValue);
				List<PassengerRecord> listOfPassengerFlighClass = countPassenger(FlightReservationConstants.BUSINESS_CLASS, flightRecord);
				if(listOfPassengerFlighClass.size() <= updateValue){
					
					flightRecord.getFlightClassMap().put(FlightReservationConstants.BUSINESS_CLASS, updateValue-listOfPassengerFlighClass.size());
					result = recordID;
				}else{
					
					int difference = listOfPassengerFlighClass.size() - updateValue;
					log.info("Reducing the new of seat by :: "+difference);
					
					List<PassengerRecord> subList = listOfPassengerFlighClass.subList(0, difference);
					deRegisterFromPassengerTable(subList);
					
					flightRecord.getListPassenger().removeAll(subList);
					flightRecord.getFlightClassMap().put(FlightReservationConstants.BUSINESS_CLASS, 0);
					result = recordID;
				}
				}
			}else {
				log.info("Flight Id: " + recordID + " doesnt exits in database");
			}
			break;
		case FlightReservationConstants.ECONOMY_CLASS:
			flightRecord = flightRecordMap.get(recordID);
			if(null!=flightRecord){
				
				synchronized (flightRecord) {
				Integer updateValue = Integer.parseInt((String) newValue);
					
				List<PassengerRecord> listOfPassengerFlighClass = countPassenger(FlightReservationConstants.ECONOMY_CLASS, flightRecord);
				if(listOfPassengerFlighClass.size() <= updateValue){
					
					flightRecord.getFlightClassMap().put(FlightReservationConstants.ECONOMY_CLASS, updateValue-listOfPassengerFlighClass.size());
					result = recordID;
				}else{
					
					int difference = listOfPassengerFlighClass.size() - updateValue;
					log.info("Reducing the new of seat by :: "+difference);
					
					List<PassengerRecord> subList = listOfPassengerFlighClass.subList(0, difference);
					deRegisterFromPassengerTable(subList);
					
					flightRecord.getListPassenger().removeAll(subList);
					flightRecord.getFlightClassMap().put(FlightReservationConstants.ECONOMY_CLASS, 0);
					result = recordID;
				}
				}
			} else {
				log.info("Flight Id: " + recordID + " doesnt exits in database");
			}
			break;
		case FlightReservationConstants.FIRST_CLASS:
			flightRecord = flightRecordMap.get(recordID);
			if(null!=flightRecord){
				synchronized (flightRecord) {
				Integer updateValue = Integer.parseInt((String) newValue);	
				List<PassengerRecord> listOfPassengerFlighClass = countPassenger(FlightReservationConstants.FIRST_CLASS, flightRecord);
				if(listOfPassengerFlighClass.size() <= updateValue){
					
						flightRecord.getFlightClassMap().put(FlightReservationConstants.FIRST_CLASS, updateValue-listOfPassengerFlighClass.size());
						result = recordID;
					
					
				}else{
					
					int difference = listOfPassengerFlighClass.size() - updateValue;
					log.info("Reducing the new of seat by :: "+difference);
					
					List<PassengerRecord> subList = listOfPassengerFlighClass.subList(0, difference);
					deRegisterFromPassengerTable(subList);
					
						flightRecord.getListPassenger().removeAll(subList);
						flightRecord.getFlightClassMap().put(FlightReservationConstants.FIRST_CLASS, 0);
						result = recordID;
					
				
				}
				}
			} else {
				log.info("Flight Id: " + recordID + " doesnt exits in database");
			}
			break;
			
		case FlightReservationConstants.DELETE_FLIGHT:
			flightRecord = flightRecordMap.get(recordID);
			if(null !=flightRecord){
			synchronized (flightRecord) {
			 
				log.info("Flight Record before deletion : "+flightRecord);
				if(null != flightRecord.getListPassenger() && flightRecord.getListPassenger().size()>0)
				deRegisterFromPassengerTable(flightRecord.getListPassenger());
				
				flightRecordMap.remove(recordID);
				result=recordID;
			}
			}else{
				log.info("Flight Record to be deleted is not present in FlightDatabase ");
			}
			break;
			
			default:
				break;
			
		
		}
		log.info("Flight Details for "+ serverMapping.get(udpServerPort)+" Database :: "+flightRecordMap);
		return result;
	
	}
	
	private List<PassengerRecord> countPassenger(String flightClass,FlightRecord flightRecord){
		List<PassengerRecord> listPassenger = new ArrayList<PassengerRecord>();
		for(PassengerRecord passengerRecord : flightRecord.getListPassenger()){
			
			if (passengerRecord.getFlightClass().equalsIgnoreCase(flightClass)){
				
				listPassenger.add(passengerRecord);
			}
		}
		log.info("countPassenger : PassengerList : "+listPassenger);
		return listPassenger;
	}

	private FlightRecord createFlightRecord(String[] array){
		System.out.println(array);
		FlightRecord flightRecord = new FlightRecord();
		flightRecord.setRecordId(array[0]);
		flightRecord.setSource(array[1]);
		flightRecord.setDestination(array[2]);
		try {
			flightRecord.setDate(FlightReservationUtil.stringToDate(array[3]));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, Integer> flightClassMap = new HashMap<String,Integer>();
		flightClassMap.put(FlightReservationConstants.BUSINESS_CLASS, Integer.parseInt(array[4]));
		flightClassMap.put(FlightReservationConstants.ECONOMY_CLASS, Integer.parseInt(array[5]));
		flightClassMap.put(FlightReservationConstants.FIRST_CLASS, Integer.parseInt(array[6]));
		flightRecord.setFlightClassMap(flightClassMap);
		return flightRecord;
	}
	
	private PassengerRecord getPassengerRecordUsingID(String passengerID) {
		
		int i;
		PassengerRecord currentPassenger;
		log.info("Passenger Record :: "+ recordMap);
		for (ArrayList<PassengerRecord> list : recordMap.values()) {
			
			synchronized (list) {
				
				for (i = 0; i < list.size(); i++) {
					
					currentPassenger = list.get(i);
					if (list.get(i).getRecordId().equals(passengerID)) {
						return currentPassenger;
					}
					
				}
				
			}
			
		}
		
		return null;
		
	}
	
	private String bookFlightInfo(String firstName, String lastName, String address,
			String phone, String destination, String date, String flightClass){

		log.info("Start FlightReservationImpl:: bookFlight() method");
		log.info("firstName : " + firstName);
		log.info("lastName : " + lastName);
		log.info("address : " + address);
		log.info("phone : " + phone);
		log.info("destination : " + destination);
		log.info("Date : " + date);
		log.info("FlightClass : "+ flightClass);
		String result = "fail";
		Calendar calDate =null;
		try {
			 calDate = FlightReservationUtil.stringToDate(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (destination.equals(PassengerRecord.LOCATION_MONTREAL) ||
				destination.equals(PassengerRecord.LOCATION_WASHINGTON) ||
				destination.equals(PassengerRecord.LOCATION_NEWDELHI)) {
			String recordId =null;
			switch(flightClass){
			
			case FlightReservationConstants.BUSINESS_CLASS:
				recordId = FlightReservationConstants.BUSINESS_CLASS +getNextId();
				break;
				
			case FlightReservationConstants.ECONOMY_CLASS:
			recordId = FlightReservationConstants.ECONOMY_CLASS +getNextId();
			break;
			
			case FlightReservationConstants.FIRST_CLASS:
				recordId = FlightReservationConstants.FIRST_CLASS+getNextId();
				break; 
				
				default:
					recordId = "Default"+getNextId();
					break;
			}
			
			
			log.info("Record Id for passenger :: "+recordId);
			PassengerRecord passsengerRecord = new PassengerRecord(recordId, firstName, lastName, address, phone,PassengerRecord.LOCATION_MONTREAL,destination, calDate.getTime(),flightClass);
			
			ArrayList<PassengerRecord> list = recordMap.get(getFirstChar(lastName));
			
			// list of flight to be returned
			List<FlightRecord> listFlightRecord = fetchTheFlightRecord(destination, calDate, flightClass);
						
			if (!listFlightRecord.isEmpty()) {
				for (FlightRecord flightRecord : listFlightRecord) {
					if (null != flightRecord) {
						log.info("Fetched Flight Record : " + flightRecord);
						synchronized (flightRecord) {
							if ((null != flightRecord.getFlightClassMap().get(flightClass))
									&& (flightRecord.getFlightClassMap().get(flightClass) > 0)) {

								flightRecord.getFlightClassMap().put(flightClass,
										(flightRecord.getFlightClassMap().get(flightClass) - 1));
								flightRecord.getListPassenger().add(passsengerRecord);
								log.info("Flight Record after succesful addition of passenger : " + flightRecord);

								synchronized (list) {
									list.add(passsengerRecord);
									result = recordId;
									log.info("Passenger Record created with id : " + recordId);
								}
								break;
							} else {
								log.info("Passenger booking failed as " + flightClass
										+ " class seats are not available in Flight Id " + flightRecord.getRecordId());
							}
						}

					} else {
						log.info("No flight available for Destination=" + destination + " on date=" + date);
					}
				}
			} else {
				log.info("No flight available for Destination=" + destination + " on date=" + date);
			}

		}
		log.info("End FlightReservationImpl:: bookFlight() method");
		return result;
	
	}
}
