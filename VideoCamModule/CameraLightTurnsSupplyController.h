#pragma once

#include "Timings.h"
#include "Lever.h"
#include <Arduino.h>
#include "CommunicationInterface.h"

#ifndef CAMERALIGHTTURNSSUPPLYYCONTROLLER_H
#define CAMERALIGHTTURNSSUPPLYYCONTROLLER_H

class CameraLightTurnsSupplyController {
public:
    enum CameraStates {
        CAMS_OFF,
        REAR_CAM_ON,
        FRONT_CAM_ON,
        TEST_MODE
    };

    typedef void (*ChangeStateCallback)(CameraStates);
 
    CameraLightTurnsSupplyController(Timings appTimings);

    CameraLightTurnsSupplyController();

    const CameraLightTurnsSupplyController &operator=(const CameraLightTurnsSupplyController &B);

    void initiate();

    void checkGearsLoopStep();
    void setChangeStateCallback(ChangeStateCallback callback);
    void setComunicationDevice(CommunicationInterface network);

private:
    CommunicationInterface network;
    Timings *timings;
    ChangeStateCallback changeStateCallback;
    CameraStates cameraState = CAMS_OFF;
//input gears
    Lever reverseGear;
    Lever leftTurnLever;
    Lever rightTurnLever;
//output pins
    const int outRearCamPower = 7; // transistor to power on
    const int outFrontCamPower = 5;
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
