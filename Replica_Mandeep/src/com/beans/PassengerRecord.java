package com.beans;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/** 
 * @author Mandeep
 * @StudentId 27849559
 */
public class PassengerRecord  implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1234L;
	private String recordId;
	private String firstName;
	private String lastName;
	private String address;
	private String phone;
	private String source;
	private String destination;
	private Date date;
	private String flightClass;
	
	public static final String LOCATION_MONTREAL = "mtl";
	public static final String LOCATION_WASHINGTON = "wst";
	public static final String LOCATION_NEWDELHI = "ndl";

	public static final String FIELDNAME_LOCATION = "Location";
	public static final String FIELDNAME_ADDRESS = "Address";
	public static final String FIELDNAME_PHONE = "Phone";
	
	
	
	
	public PassengerRecord(String recordId, String firstName, String lastName,
			String address, String phone, String source, String destination,
			Date date, String flightClass) {
		super();
		this.recordId = recordId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.phone = phone;
		this.source = source;
		this.destination = destination;
		this.date = date;
		this.flightClass = flightClass;
	}
	public String getRecordId() {
		return recordId;
	}
	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getFlightClass() {
		return flightClass;
	}
	public void setFlightClass(String flightClass) {
		this.flightClass = flightClass;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result
				+ ((destination == null) ? 0 : destination.hashCode());
		result = prime * result
				+ ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result
				+ ((flightClass == null) ? 0 : flightClass.hashCode());
		result = prime * result
				+ ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + ((phone == null) ? 0 : phone.hashCode());
		result = prime * result
				+ ((recordId == null) ? 0 : recordId.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PassengerRecord other = (PassengerRecord) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (flightClass == null) {
			if (other.flightClass != null)
				return false;
		} else if (!flightClass.equals(other.flightClass))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (phone == null) {
			if (other.phone != null)
				return false;
		} else if (!phone.equals(other.phone))
			return false;
		if (recordId == null) {
			if (other.recordId != null)
				return false;
		} else if (!recordId.equals(other.recordId))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "PassengerRecord [recordId=" + recordId + ", firstName="
				+ firstName + ", lastName=" + lastName + ", address=" + address
				+ ", phone=" + phone + ", source=" + source + ", destination="
				+ destination + ", date=" + date + ", flightClass="
				+ flightClass + "]";
	}
	
	
	

	

	
	
	
	
	
}
