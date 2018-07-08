/*
 * Copyright (c) 2006 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */

// @author David Gay

#ifndef OSCILLOSCOPE_H
#define OSCILLOSCOPE_H

/* We added the following lines in the Oscilloscope.h file */
#ifndef MTS300CB
#define MTS300CB
#endif

/* We modified the NREADINGS from 10 to 1. When NREADINGS was 10 the Oscilloscope message contained 10 sensor 
readings in one packet. We changed the NREADINGS to 1 to get a single reading in each packet. We also modified 
the DEFAULT_INTERVAL to 5 from 256. This is the timer interval used in the oscilloscope code to send messages 
to the base station. We modified this to 5ms because we wanted the base station to receive the messages faster 
than the previous case. If base station receives readings one by one faster, we get more accurate and real time 
plot of the magnetometer sensor readings according to our observation. */

enum {
  /* Number of readings per message. If you increase this, you may have to
     increase the message_t size. */
  NREADINGS = 1,

  /* Default sampling period. */
  DEFAULT_INTERVAL = 5,

  AM_OSCILLOSCOPE = 0x93
};

typedef nx_struct oscilloscope {
  nx_uint16_t version; /* Version of the interval. */
  nx_uint16_t interval; /* Samping period. */
  nx_uint16_t id; /* Mote id of sending mote. */
  nx_uint16_t count; /* The readings are samples count * NREADINGS onwards */
  nx_uint16_t readings[NREADINGS];
} oscilloscope_t;

#endif
