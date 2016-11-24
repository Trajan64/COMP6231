package dfrsObjects;

import java.util.ArrayList;

public class Flight {


	private String recordId;
	private String departureCity;
	private String destinationCity;
	private String departureDate;
	private String departureTime;
	private ArrayList<ArrayList<Passenger>> passengerList = new ArrayList<ArrayList<Passenger>>(3); 
	private int[] seatsAvailable = new int[3];

	
	public Flight(){
		this.recordId = java.util.UUID.randomUUID().toString();
		departureCity = "";
		destinationCity = "";
		departureDate = "";
		departureTime = "";
		
		this.passengerList.add(new ArrayList<Passenger>());
		this.passengerList.add(new ArrayList<Passenger>());
		this.passengerList.add(new ArrayList<Passenger>());
		
		seatsAvailable[0] = 0;
		seatsAvailable[1] = 0;
		seatsAvailable[2] = 0;
		

	}
	
	public Flight(String departureCity,String arrivalCity,String departureDate,String departureTime,
			int economySeats,int businessSeats,int firstSeats){
		
		this.recordId = java.util.UUID.randomUUID().toString();
		this.departureCity = departureCity;
		this.destinationCity = arrivalCity;
		this.departureDate = departureDate;
		this.departureTime = departureTime;
		this.passengerList.add(new ArrayList<Passenger>(economySeats));
		this.passengerList.add(new ArrayList<Passenger>(businessSeats));
		this.passengerList.add(new ArrayList<Passenger>(firstSeats));
		
		seatsAvailable[0] = economySeats;
		seatsAvailable[1] = businessSeats;
		seatsAvailable[2] = firstSeats;

		
	}
	public String getRecordId(){
		return this.recordId;
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
	public String getDepartureTime(){
		return this.departureTime;
	}
	public void setDepartureTime(String departureTime){
		this.departureTime = departureTime;
	}
	public int getNumberSeatsAvailable(String classType){
		switch(classType){
			case "Economy":
				return seatsAvailable[0];
			case "Business":
				return seatsAvailable[1];
			case "First class":
				return seatsAvailable[2];
			default:
				return 0;
		}
	}
	public void setNumberSeats(String classType, int seats){
		switch(classType){
			case "Economy":
				removePassengers(0, this.seatsAvailable[0] - seats);
				this.seatsAvailable[0] = seats;
				break;
			case "Business":
				removePassengers(1, this.seatsAvailable[1] - seats);
				this.seatsAvailable[1] = seats;
				break;
			case "First class":
				removePassengers(2, this.seatsAvailable[2] - seats);
				this.seatsAvailable[2] = seats;
			
		}
	}
	
	public void removePassengers(int classType, int remove){
		if(remove > 0){
			for(int i = 0 ; i < remove; i++){
				this.passengerList.get(classType).remove(this.passengerList.get(classType).size() - 1);
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
				if(passengerList.get(classType).get(i).equals(passengerId)) return true;
			}

			return false;
		}
		
	}
	
	public String toString(){
		return "Flight number: " + this.recordId + " Departure date: " + this.departureDate + " Departure time: " + this.departureTime;
	}
}
