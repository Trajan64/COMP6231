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
	
	private static long ids = 0;
	
	public synchronized static long getNextId() {
		ids++;
		return ids;
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
