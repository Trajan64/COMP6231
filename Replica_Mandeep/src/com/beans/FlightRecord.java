package com.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class FlightRecord implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String recordId;
	private String source;
	private String destination;
	private Calendar date;
	private Map<String , Integer> flightClassMap;
	
	private List<PassengerRecord> listPassenger;
	
	
	public String getRecordId() {
		return recordId;
	}
	public void setRecordId(String recordId) {
		this.recordId = recordId;
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
	public Calendar getDate() {
		return date;
	}
	public void setDate(Calendar date) {
		this.date = date;
	}
	public Map<String, Integer> getFlightClassMap() {
		return flightClassMap;
	}
	public void setFlightClassMap(Map<String, Integer> flightClassMap) {
		this.flightClassMap = flightClassMap;
	}
	public List<PassengerRecord> getListPassenger() {
        if (listPassenger == null) {
            listPassenger = new ArrayList<PassengerRecord>();
        }
        return this.listPassenger;
    }
	/*public void setListPassenger(List<PassengerRecord> listPassenger) {
		this.listPassenger = listPassenger;
	}*/
	
	
	@Override
	public String toString() {
		return "FlightRecord [recordId=" + recordId + ", source=" + source + ", destination=" + destination + ", date="
				+ ((null != date)?date.getTime():"") + ", flightClassMap=" + flightClassMap + ", listPassenger=" + listPassenger + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result
				+ ((destination == null) ? 0 : destination.hashCode());
		result = prime * result
				+ ((flightClassMap == null) ? 0 : flightClassMap.hashCode());
		result = prime * result
				+ ((listPassenger == null) ? 0 : listPassenger.hashCode());
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
		FlightRecord other = (FlightRecord) obj;
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
		if (flightClassMap == null) {
			if (other.flightClassMap != null)
				return false;
		} else if (!flightClassMap.equals(other.flightClassMap))
			return false;
		if (listPassenger == null) {
			if (other.listPassenger != null)
				return false;
		} else if (!listPassenger.equals(other.listPassenger))
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
	
	
	
	
	
	
}
