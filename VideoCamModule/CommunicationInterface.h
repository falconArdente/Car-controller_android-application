#pragma once

#include <Arduino.h>

#ifndef COM_INT_H
#define COM_INT_H

class CommunicationInterface {
  
public:
    CommunicationInterface(){
      
      }

    byte lastStateCommand[2];
    byte lastTimingCommand[9];
    byte lastStateInfo[2];
    byte lastTimingInfo[2];

    virtual void sendState(byte (&stateInfo)(2));

    virtual void sendTimings(byte (&timingInfo)[9]);

    virtual void checkForIncomingData();

    virtual void setTimingsIncome(byte (&newTimingCommand)[9]);

    virtual void setStateIncome(byte (&newStateCommand)[2]);
};

#endif
