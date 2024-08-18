#pragma once

#include "Timings.h"
#include "Lever.h"
#include <Arduino.h>

#ifndef CAMERALIGHTTURNSSUPPLYYCONTROLLER_H
#define CAMERALIGHTTURNSSUPPLYYCONTROLLER_H

class CameraLightTurnsSupplyController {
    enum CameraStates {
        CAMS_OFF,
        REAR_CAM_ON,
        FRONT_CAM_ON,
    };

public:
    CameraLightTurnsSupplyController(Timings appTimings) ;
    typedef void (*ChangeStateCallback)(CameraStates);
    void setChangeStateCallback(ChangeStateCallback callback);
    void initiate();
    void checkGearsLoopStep();

private:
    Timings *timings;
    ChangeStateCallback changeStateCallback;
    CameraStates cameraState = CAMS_OFF;
//input pins
    Lever reverseGear = Lever(A1, *timings);
    Lever leftTurnLever = Lever(A0, *timings, true);
    Lever rightTurnLever = Lever(12, *timings, true);
//output pins
    const int outRearCamPower = 7; // transistor to power on
    const int outFrontCamPower = 5;
    const int outCautionSignal = 2;
    const int outDisplayOn = A2;
    const int outLeftFogLight = 3;
    const int outRightFogLight = 9;
    const int outRelayCameraSwitch = 11;
    const int outControllerLed = 13; //direct board on
// states
    bool leftIsDoubleClicked = false;
    bool rightIsDoubleClicked = false;

    bool isTimeToOffFront();

    bool isTimeToOffRear();

    void getGearsState();

    void setCameraState(CameraStates state);
};
#endif