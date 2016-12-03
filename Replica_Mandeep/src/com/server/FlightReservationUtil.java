package com.server;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FlightReservationUtil {

	
	public static Calendar stringToDate(String dateInString) throws ParseException
	{
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
         Date date = formatter.parse(dateInString);
            System.out.println(date);
            System.out.println(formatter.format(date));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        
	}
	
	public static String dateToString(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String stringDate = sdf.format(date);
		System.out.println(stringDate);
		return stringDate;
	}
	
	
}
