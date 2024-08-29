#pragma once

#include <Arduino.h>
#include "CameraStatesEnum.h"
#include "Timings.h"

#ifndef COMINT_H
#define COMINT_H

class CommunicationUnit {
public:
    CommunicationUnit() {

    }
      struct StateInfoSet{
                bool leftPressed;
                bool leftDblPressed;
                bool rightPressed;
                bool rightDblPressed;
                bool reversePressed;
                bool cautionIsOn;
                bool leftFogIsOn;
                bool rightFogIsOn;
                bool relayIsOn;
                bool rearCameraIsOn;
                bool angelEyeIsOn;
                bool displayIsOn;
                CameraStates cameraState;
      };

        struct TimingSet{
          bool isToWrite; 
          Timings timings;
        };

        struct ControlCommandSet {
                bool cautionIsOn;
                bool leftFogIsOn;
                bool rightFogIsOn;
                bool relayIsOn;
                bool rearCameraIsOn;
                bool angelEyeIsOn;
                bool displayIsOn;
                CameraStates cameraState;
};
void sendPackage(byte* packageToSend, int bytesToSend);
    void package(StateInfoSet stateSet);

    inline  void sendTimings(TimingSet timingsSet);

//inline virtual void checkForIncomingData();

//inline virtual void setTimingsIncome(byte p[9]);

//inline virtual void setStateIncome(byte p [2]);
private:
byte packageToSend[9];
int bytesToSend=0;
};

#endif
