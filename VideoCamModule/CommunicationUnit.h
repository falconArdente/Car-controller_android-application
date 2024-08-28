#pragma once

#include <Arduino.h>
#include "CameraStatesEnum.h"
#include "Timings.h"

#ifndef COMINT_H
#define COMINT_H

class CommunicationUnit {
public:
    CommunicationUnit() {
//
    }

    class StateInfo {
    public:
        StateInfo(
                bool leftPressed,
                bool leftDblPressed,
                bool rightPressed,
                bool rightDblPressed,
                bool reversePressed,
                bool cautionIsOn,
                bool leftFogIsOn,
                bool rightFogIsOn,
                bool relayIsOn,
                bool RearCameraIsOn,
                bool frontCameraIsOn,
                bool displayIsOn,
                CameraStates cameraState
        );

    private:
        byte package[2];
    };

    class TimingsPackage {
    public:
        TimingsPackage(bool isToWrite, Timings timings);

    private:
        byte package[9];
    };

    inline void sendState(StateInfo stateInfo);

    // inline virtual void sendTimings(byte p [9]);

//inline virtual void checkForIncomingData();

//inline virtual void setTimingsIncome(byte p[9]);

//inline virtual void setStateIncome(byte p [2]);
};

#endif
