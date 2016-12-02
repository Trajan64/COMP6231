package com.vhs.helper;


public class Passenger {

	private String passengerId;
	private String firstName; 
	private String lastName; 
	private String address; 
	private String phone; 
	private String destination;
	private String date;
	private String classType;
	private String flightNo;
	
	
	
	public Passenger(){
		this.firstName = "";
		this.lastName = "";
		this.address = "";
		this.phone = "";
		this.destination = "";
		this.date = "";
		this.classType = "";
		this.flightNo = "";
		passengerId = "@" + java.util.UUID.randomUUID().toString();
	}
	
	public Passenger(String firstName, String lastName, String address, String phone, String destination,
			String date, String classType, String flightNo){
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.phone = phone;
		this.destination = destination;
		this.date = date;
		this.classType = classType;
		this.flightNo = flightNo;
		passengerId = lastName.charAt(0) + java.util.UUID.randomUUID().toString();
	}
	
	public String getFirstName(){
		return this.firstName;
	}
	public String getLastName(){
		return this.lastName;
	}
	public String getAddress(){
		return this.address;
	}
	public String getPhone(){
		return this.phone;
	}
	public String getDestination(){
		return this.destination;
	}
	public String getDate(){
		return this.date;
	}
	public void setDate(String date){
		this.date = date;
	}
	
	public String getClassType(){
		return this.classType;
	}
	
	public String getRecordId(){
		return this.passengerId;
	}
	
	public String getFlightNo(){
		return this.flightNo;
	}
	public void setFlightNo(String flightNo){
		this.flightNo = flightNo;
	}
	
	public String toString(){
		return passengerId + ": " + firstName + " " + lastName + " " + address + " " + phone + " " + destination +
				" " + date + " Flight: "+ flightNo + " " + classType; 
	}
	public String recordTransferString(){
		
		return this.firstName.replaceAll(" ", "") + " " + this.lastName.replaceAll(" ", "") + " " +
		this.address.replaceAll(" ", "_") + " " + this.phone + " " + this.destination.replaceAll(" ", "") + 
		" " + this.date + " " + this.classType + " " + this.flightNo;
	}
	
}
