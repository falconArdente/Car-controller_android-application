#pragma once

#include "Timings.h"
#include "Lever.h"
#include <Arduino.h>

#ifndef CAMERALIGHTTURNSSUPPLYYCONTROLLER_H
#define CAMERALIGHTTURNSSUPPLYYCONTROLLER_H

class CameraLightTurnsSupplyController {
public:
    enum CameraStates {
        CAMS_OFF,
        REAR_CAM_ON,
        FRONT_CAM_ON,
    };
    typedef void (*ChangeStateCallback)(CameraStates);
    CameraLightTurnsSupplyController(Timings appTimings);

    CameraLightTurnsSupplyController();

    const CameraLightTurnsSupplyController &operator=(const CameraLightTurnsSupplyController &B);

    void setChangeStateCallback(ChangeStateCallback callback);

    void initiate();

    void checkGearsLoopStep();
    ChangeStateCallback changeStateCallback;

private:
    Timings *timings;
    
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
// states
    bool isTimeToOffFront();

    bool isTimeToOffRear();

    void getGearsState();

    void setCameraState(CameraStates state);
};

#endif
