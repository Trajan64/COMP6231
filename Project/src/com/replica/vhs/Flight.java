package com.replica.vhs;

import java.util.ArrayList;


public class Flight {


	private String flightId;
	private String departureCity;
	private String destinationCity;
	private String departureDate;
	
	private ArrayList<ArrayList<Passenger>> passengerList = new ArrayList<ArrayList<Passenger>>(3); 
	private int[] seatsAvailable = new int[3];

	
	public Flight(){
		this.flightId = "";
		departureCity = "";
		destinationCity = "";
		departureDate = "";
		
		this.passengerList.add(new ArrayList<Passenger>());
		this.passengerList.add(new ArrayList<Passenger>());
		this.passengerList.add(new ArrayList<Passenger>());
		
		seatsAvailable[0] = 0;
		seatsAvailable[1] = 0;
		seatsAvailable[2] = 0;
		

	}
	
	public Flight(String recordId, String departureCity,String arrivalCity,String departureDate,
			int businessSeats, int economySeats,int firstSeats){
		
		this.flightId = recordId ;
		this.departureCity = departureCity;
		this.destinationCity = arrivalCity;
		this.departureDate = departureDate;
		
		this.passengerList.add(new ArrayList<Passenger>(economySeats));
		this.passengerList.add(new ArrayList<Passenger>(businessSeats));
		this.passengerList.add(new ArrayList<Passenger>(firstSeats));
		
		seatsAvailable[0] = economySeats;
		seatsAvailable[1] = businessSeats;
		seatsAvailable[2] = firstSeats;

		
	}
	public String getFlightId(){
		return this.flightId;
	}
	//No setter as manager can only make modifications to flights in their location
	public String getDepartureCity(){
		return this.departureCity;
	}
	public String getDestinationCity(){
		return this.destinationCity;
	}
	public void setDestinationCity(String destinationCity){
		this.destinationCity = destinationCity;
	}
	public String getDepartureDate(){
		return this.departureDate;
	}
	public void setDepartureDate(String departureDate){
		this.departureDate = departureDate;
	}

	public int getNumberSeatsAvailable(String classType){
		switch(classType){
			case "Economy":
				return this.seatsAvailable[0];
			case "Business":
				return this.seatsAvailable[1];
			case "First class":
				return this.seatsAvailable[2];
			default:
				return 0;
		}
	}
	public int getNumSeatsBooked(int classType){
		return this.passengerList.get(classType).size();
	}
	public void setNumberSeats(String classType, int remove){
		
		
		switch(classType){
			case "Economy":
				System.out.println("About to remove passengers Economy");
				removePassengers(0,remove);
				this.seatsAvailable[0] = (remove > 0 && this.seatsAvailable[0] - remove >= 0) ? this.seatsAvailable[0] - remove : (remove < 0) ? -1 * remove : 0;
				break;
			case "Business":
				System.out.println("About to remove passengers Business");
				removePassengers(1, remove);
				this.seatsAvailable[1] = (remove > 0 && this.seatsAvailable[1] - remove >= 0) ? this.seatsAvailable[1] - remove : (remove < 0) ? -1 * remove : 0;
				break;
			case "First class":
				System.out.println("About to remove passengers First Class");
				removePassengers(2, remove);
				this.seatsAvailable[2] = (remove > 0 && this.seatsAvailable[2] - remove >= 0) ? this.seatsAvailable[2] - remove : (remove < 0) ? -1 * remove : 0;		
		}
		
		
	}
	
	public void removePassengers(int classType, int remove){
		if(remove > 0){
			for(int i = 0 ; i < remove; i++){
				if(this.passengerList.get(classType).size() > 0)
					this.passengerList.get(classType).remove(this.passengerList.get(classType).size() - 1);
				System.out.println("Removing passengers from flight");
			}
		}
	}
	public void removePassenger(int classType, String passengerId){
		for(int i = 0; i < this.passengerList.get(classType).size(); i++){
			if(this.passengerList.get(classType).get(i).getRecordId().equals(passengerId)){
				this.passengerList.get(classType).remove(i);
				this.seatsAvailable[classType] += 1;
			}
		}
	}
	
	public void addPassengers(int classType, Passenger person){
		this.passengerList.get(classType).add(person);
	}
	
	public boolean passengerExists(String passengerId, int classType){
		if(classType == -1){
			for(int i = 0 ; i < 3 ; i++){
				for(int j = 0; j < passengerList.get(i).size(); j++){
					if(passengerList.get(i).get(j).getRecordId().equals(passengerId)) return true;
				}
			}
			return false;
		}
		else{

			for(int i = 0; i < passengerList.get(classType).size(); i++){
				if(passengerList.get(classType).get(i).getRecordId().equals(passengerId)) return true;
			}

			return false;
		}
		
	}
	
	public String toString(){
		return "Flight number: " + this.flightId + " Departure date: " + this.departureDate +
				" Seats " + seatsAvailable[0] + " " + seatsAvailable[1] + " " + seatsAvailable[2];
	}
}