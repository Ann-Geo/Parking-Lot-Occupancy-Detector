/*
 * Copyright (c) 2006 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */

/**
 * Oscilloscope demo application. See README.txt file in this directory.
 *
 * @author David Gay
 */
#include "Timer.h"
#include "Oscilloscope.h"

module OscilloscopeC @safe()
{
  uses {
    interface Boot;
    interface SplitControl as RadioControl;
    interface AMSend;
    interface Receive;
    interface Timer<TMilli>;
    interface Read<uint16_t>;
    interface Leds;
	
/* We used the interface Mag in our to make use of the command gainAdjustY. */
    interface Mag;
  }
}
implementation
{
  message_t sendBuf;
  bool sendBusy;
  
  
/* We declared a variable called calibration_success and initialized it to 0. This variable is just a 
flag to indicate that our gain adjustment has successfully done. Instead of this we can also toggle an 
LED to indicate the success of the gain adjustment. */

	uint8_t calibration_success = 0;


/* We implemented a task to do the gain adjustment operation of the magnetometer. Here we called the 
command gainAdjustY from the Mag interface and if it is success we assigned the calibration_success variable 
to 1. The value we use to call the gainAdjustY command will go to the digital potentiometer connected to the 
second stage amplifier via I2C bus and is used to adjust the gain of the second stage amplifier in the 
magnetometer circuit. The value 55 is not a magic number, but we found it by analyzing the oscilloscope 
graph each time we changed the value from 1 to 255. Finally, we fixed the value at 55 because by using 
this value we were able to detect the metallic objects within range of 7 inches and also the presence 
of a various such objects. We called this task when the timer got fired in the oscilloscope code. 
We are not using the MagXC sensor in our code so the gainAdjustX command is also not used in our code. 
We added this in our code at the initial stage of our calibration phase to check in which axis we 
would get more sensitive readings from the magnetometer. */

	
task void adjustGain()
  {

		atomic
		{
			if(call Mag.gainAdjustX(35) == SUCCESS)
			{
 				calibration_success = 1;
 			}
 			else
 			{
 				calibration_success = 0;
 			}

 			if(call Mag.gainAdjustY(55) == SUCCESS)
 			{
 				calibration_success = 1;
 				
 			}
 		}

 post adjustGain();

   }




  /* Current local state - interval, version and accumulated readings */
  oscilloscope_t local;

  uint8_t reading; /* 0 to NREADINGS */

  /* When we head an Oscilloscope message, we check it's sample count. If
     it's ahead of ours, we "jump" forwards (set our count to the received
     count). However, we must then suppress our next count increment. This
     is a very simple form of "time" synchronization (for an abstract
     notion of time). */
  bool suppressCountChange;

  // Use LEDs to report various status issues.
  void report_problem() { call Leds.led0Toggle(); }
  void report_sent() { call Leds.led1Toggle(); }
  void report_received() { call Leds.led2Toggle(); }

  event void Boot.booted() {
    local.interval = DEFAULT_INTERVAL;
    local.id = TOS_NODE_ID;
    if (call RadioControl.start() != SUCCESS)
      report_problem();
  }

  void startTimer() {
    call Timer.startPeriodic(local.interval);
    reading = 0;
  }

  event void RadioControl.startDone(error_t error) {
    startTimer();
  }

  event void RadioControl.stopDone(error_t error) {
  }

  event message_t* Receive.receive(message_t* msg, void* payload, uint8_t len) {
    oscilloscope_t *omsg = payload;

    report_received();

    /* If we receive a newer version, update our interval. 
       If we hear from a future count, jump ahead but suppress our own change
    */
    if (omsg->version > local.version)
      {
	local.version = omsg->version;
	local.interval = omsg->interval;
	startTimer();
      }
    if (omsg->count > local.count)
      {
	local.count = omsg->count;
	suppressCountChange = TRUE;
      }

    return msg;
  }

  /* At each sample period:
     - if local sample buffer is full, send accumulated samples
     - read next sample
  */
  event void Timer.fired() {
  
  post adjustGain();
    if (reading == NREADINGS)
      {
	if (!sendBusy && sizeof local <= call AMSend.maxPayloadLength())
	  {
	    // Don't need to check for null because we've already checked length
	    // above
	    memcpy(call AMSend.getPayload(&sendBuf, sizeof(local)), &local, sizeof local);
	    if (call AMSend.send(AM_BROADCAST_ADDR, &sendBuf, sizeof local) == SUCCESS)
	      sendBusy = TRUE;
	  }
	if (!sendBusy)
	  report_problem();

	reading = 0;
	/* Part 2 of cheap "time sync": increment our count if we didn't
	   jump ahead. */
	if (!suppressCountChange)
	  local.count++;
	suppressCountChange = FALSE;
      }
    if (call Read.read() != SUCCESS)
      report_problem();
  }



  event void AMSend.sendDone(message_t* msg, error_t error) {
    if (error == SUCCESS)
      report_sent();
    else
      report_problem();

    sendBusy = FALSE;
  }

  event void Read.readDone(error_t result, uint16_t data) {
    if (result != SUCCESS)
      {
	data = 0xffff;
	report_problem();
      }
    if (reading < NREADINGS) 
      local.readings[reading++] = data;
  }


/* We called these events provided by the Mag interface to make sure that the gain adjustment was successfully done.*/

	event void Mag.gainAdjustXDone(error_t result)
 	{

 		return SUCCESS;
 	}

 	event void Mag.gainAdjustYDone(error_t result)
 	{

 		return SUCCESS;
 	}
}
