/* This header file is created to define the message format for the messages received by the 
BaseStation from the Oscilloscope. The Oscilloscope application has a header file called Oscilloscope.h 
in which they have already provided the structure format for the messages from the Oscilloscope. We used 
the exact same message format in BaseStation also to define the stuctyre below. We use the id and readings 
field of this structure in our BaseStationP.nc file to different comparisons required for our logic. */

#ifndef BASESTATION_H
#define BaSESTATION_H

enum {
  /* Number of readings per message. If you increase this, you may have to
     increase the message_t size. */
  NREADINGS = 1
};

/* Defines a new structure with fields version, interval, id, count and readings for the BaseStationMsg */

typedef nx_struct BaseStationMsg
{
  nx_uint16_t version; /* Version of the interval. */
  nx_uint16_t interval; /* Samping period. */
  nx_uint16_t id; /* Mote id of sending mote. */
  nx_uint16_t count; /* The readings are samples count * NREADINGS onwards */
  nx_uint16_t readings[NREADINGS];

}BaseStationMsg;

#endif
