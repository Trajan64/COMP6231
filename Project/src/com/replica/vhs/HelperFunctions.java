package com.replica.vhs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class HelperFunctions {

	public static int verifyValidInt(){
		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		int userChoice = 0;
		Boolean valid = false;
		
		// Enforces a valid integer input.
		while(!valid)
		{
			try{
				userChoice=keyboard.nextInt();
				valid=true;
			}
			catch(Exception e)
			{
				System.out.println("Invalid Input, please enter an Integer");
				valid=false;
				keyboard.nextLine();
			}
		}
		return userChoice;

	}
	
	public static boolean isValidTime(String inTime){
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	    timeFormat.setLenient(false);
	    try {
	     timeFormat.parse(inTime.trim());
	    } catch (ParseException pe) {
	      return false;
	    }
	    return true;
	}
	
	public static boolean isValidDate(String inDate) {
	    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	    dateFormat.setLenient(false);
	    try {
	      dateFormat.parse(inDate.trim());
	    } catch (ParseException pe) {
	      return false;
	    }
	    return true;
	 }
	
	public static boolean isValidDestination(String departure, String destination){
		if((destination.equals("MTL") || destination.equals("NDL") || destination.equals("WST")) &&
				!destination.equals("departure"))
			return true;
		return false;
	}
	
	protected static boolean writeToLog(String logFile,String text){
		
		try {
			if(Files.notExists(Paths.get("./Logs/"+ logFile + "Log.txt"))){
				Files.write(Paths.get("./Logs/"+ logFile + "Log.txt"), 
						(new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()).toString() + text ).getBytes());
			}
			else{
				Files.write(Paths.get("./Logs/"+ logFile + "Log.txt"), 
					(new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()).toString() + text).getBytes(), StandardOpenOption.APPEND);
			}
		}catch (IOException e) {
			return false;
		    //exception handling left as an exercise for the reader
		}
		return true;
		
	}
}
