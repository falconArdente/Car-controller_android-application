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

    bool isAbstract = true;
    struct StateInfoSet {
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

    typedef void (*UpdateTimingsCallback)(Timings newTimings);

    typedef void (*ExecuteCommandCallback)(ControlCommandSet command);

    typedef void (*SendUpTimingsCallback)();

    void sendState(StateInfoSet stateSet);

    void sendTimings(Timings timings);

    void checkForIncome();

    void setUpdateTimingsCallback(UpdateTimingsCallback timingsCallback);

    void setExecuteCommandCallback(ExecuteCommandCallback commandCallback);

    void setSendUpTimingsCallback(SendUpTimingsCallback sendUpTimings);
//inline virtual void checkForIncomingData();

//inline virtual void setTimingsIncome(byte p[9]);

//inline virtual void setStateIncome(byte p [2]);
private:
    UpdateTimingsCallback updateTimings;
    ExecuteCommandCallback executeCommand;
    SendUpTimingsCallback sendUpTimings;

    void sendPackage(byte *packageToSend, int bytesToSend);

    byte packageToSend[9];
    int bytesToSend = 0;
};

#endif
