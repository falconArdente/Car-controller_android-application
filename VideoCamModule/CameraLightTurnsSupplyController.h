#pragma once

#include "Timings.h"
#include "Lever.h"
#include "CommunicationUnit.h"
#include <Arduino.h>

#ifndef CAMERALIGHTTURNSSUPPLYYCONTROLLER_H
#define CAMERALIGHTTURNSSUPPLYYCONTROLLER_H

class CameraLightTurnsSupplyController {
public:
    CameraLightTurnsSupplyController(Timings appTimings);

    CameraLightTurnsSupplyController();

    const CameraLightTurnsSupplyController &operator=(const CameraLightTurnsSupplyController &B);

    typedef void (*ChangeStateCallback)(CameraStates);

    void initiate();

    bool isChangedFlag = false;

    void checkGearsLoopStep();

    void communicationLoopStep();

    void setChangeStateCallback(ChangeStateCallback callback);

    void setCommunicationDevice(CommunicationUnit network);

private:
    CommunicationUnit network;
    Timings *timings;
    ChangeStateCallback changeStateCallback;
    CameraStates cameraState = CAMS_OFF;
//input gears
    Lever reverseGear;
    Lever leftTurnLever;
    Lever rightTurnLever;
//output pins
    const int outRearCamPower = 7; // transistor to power on
    const int outAngelEye = 5;
    const int outCautionSignal = 2;
    const int outDisplayOn = A2;
    const int outLeftFogLight = 3;
    const int outRightFogLight = 9;
    const int outRelayCameraSwitch = 11;
    const int outControllerLed = 13; //direct board on
// service functions
    void getGearsState();

    bool isTimeOutForFront();

    bool isTimeOutForRear();

    void setCameraState(CameraStates state);

    void turnFogLightOn();

    void turnOffFogLight();
};

#endif
