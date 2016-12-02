package com.vhs.server;

import java.util.Scanner;

import javax.xml.ws.Endpoint;


public class ManagerServerPublisher {

	public static void main(String[] args) {

			
			
			Scanner keyboard = new Scanner(System.in);
			System.out.println("Please select a server");
			System.out.println("1.Montreal\n2.New Delhi\n3.Washington");
			

			int userChoice = 0;
			int udpPort = 0;
			boolean valid = false;
			String server = "";
			String url = "";
			
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
			
			while(server == ""){
				switch(userChoice){
					case 1:
						server = "MTL";
						udpPort = 6789;
						url = "http://localhost:8080/mtl";
						break;
					case 2:
						
						server = "NDL";
						udpPort = 6790;
						url = "http://localhost:8081/ndl";
						break;
					case 3:
						server = "WST";
						udpPort = 6791;
						url = "http://localhost:8082/wst";
						break;
					default:
						System.out.println("Invalid input. Please enter a digit between 1 and 3");
					
						
				}
					
			}
			keyboard.close();
			
			
			ManagerServerImpl serverObj = new ManagerServerImpl(server);
			
			Endpoint endpoint = Endpoint.publish(url, serverObj);
			
			String result = (endpoint.isPublished()) ? server + " server ready and waiting..." : server + " server failed to start";
			System.out.println(result);
			serverObj.startUDPServer(udpPort);
	}
}
