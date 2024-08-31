#pragma once

#include <Arduino.h>
#include "CameraStatesEnum.h"
#include "Timings.h"
//
#ifndef COMINT_H
#define COMINT_H

class CameraLightTurnsSupplyController;

class CommunicationUnit {
public:
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

    CommunicationUnit(CameraLightTurnsSupplyController *hostObject);

    CommunicationUnit();

    bool isAbstract = true;

    void checkForIncome();

    void sendState(StateInfoSet stateSet);

    void sendTimings(Timings timings);
// virtual void checkForIncomingData()const=0;
private:
    CameraLightTurnsSupplyController *hostObject;

    void sendUpTimings();

    void sendPackage(byte *packageToSend, int bytesToSend);

    byte packageToSend[9];
    int bytesToSend = 0;
};

#endif
