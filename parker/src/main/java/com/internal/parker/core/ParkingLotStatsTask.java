package com.internal.parker.core;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.internal.parker.utils.DropBoxUtil;
//***********************************************************************************************
//Class: ParkingLotStatsTask
//This class will do the task of copying the contents of variable SLOTS and calls the function 
//droboxUtil.upload
//***********************************************************************************************
@Service
public class ParkingLotStatsTask {

	private DropBoxUtil dropBoxUtil;
	private String slots = "";

	@Autowired
	public ParkingLotStatsTask(DropBoxUtil dropBoxUtil) {
		this.dropBoxUtil = dropBoxUtil;
	}

	@PostConstruct
	public void save() {
		System.out.println("Initialized ParkingLotStatsTask");
//**************************************************************************************************
//This thread will take the slots variable output and is giving that as input to dropboxutil
//Here also the concept of multi threading is used and while (true) will run this block infinite
//times.It will aso call the function dropboxutil.upload.
//**************************************************************************************************
		new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						while (!slots.equals(ParkingLotOccupancyDetector.SLOTS)) {
							System.out.println("Uploading to DropBox");
							dropBoxUtil.upload(ParkingLotOccupancyDetector.SLOTS);
							slots = ParkingLotOccupancyDetector.SLOTS;
							sleep();
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void sleep() {
		try {
			Thread.sleep(30 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
