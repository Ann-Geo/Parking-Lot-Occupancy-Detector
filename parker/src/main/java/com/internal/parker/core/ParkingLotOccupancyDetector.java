package com.internal.parker.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//*******************************************************************************************
//Class: ParkingLotOccupancyDetector
//This class is used to read the command from the command promt line by line and places the 
//content in SLOTS variable.
//*******************************************************************************************
@Service
public class ParkingLotOccupancyDetector {

	private PropertyService properties;
	public static String SLOTS = "";
	private static String cmd;
	private ProcessBuilder builder;
	
	@Autowired
	public ParkingLotOccupancyDetector(PropertyService properties) {
		this.properties = properties;
	}
//****************************************************************************************
//Function:getFreeSlots
//A new thread was created to ensure parallel processing of tasks in the project.
//Process builder constructs a process builder with program and arguments to execute the 
//commands in command line to get output.
//In the while loop, the readline tries to listen to the command line the comand prompt and 
//read it line by line in an infinite loop.The lines that were read are placed in the 
//variable "SLOTS".
//******************************************************************************************
	@PostConstruct
	public void getFreeSlots() {
		System.out.println("Initialized ParkingLotOccupancyDetector");

		new Thread(new Runnable() {   
			public void run() {
				cmd = properties.getProperty("cmd");
				String[] cmds = cmd.split(" ");
				builder = new ProcessBuilder(cmds); 
				builder.redirectErrorStream(true);
				try {
					while (true) {
						System.out.println("ParkingLotOccupancyDetector");
						Process process = builder.start();
						InputStream is = process.getInputStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(is));
						String line = null;
						while ((line = reader.readLine()) != null) { 
							SLOTS = line;                        
							System.out.println("SLOTS : " + SLOTS);

						}

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

	}
}
