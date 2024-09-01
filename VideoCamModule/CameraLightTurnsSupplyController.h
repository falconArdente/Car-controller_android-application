#pragma once

#include <Arduino.h>

#include "Timings.h"
#include "Lever.h"
#include "CameraStatesEnum.h"
#include "CommunicationUnit.h"

#ifndef CAMERALIGHTTURNSSUPPLYYCONTROLLER_H
#define CAMERALIGHTTURNSSUPPLYYCONTROLLER_H

class CameraLightTurnsSupplyController {
public:
    CameraLightTurnsSupplyController();
    typedef void (*ChangeStateCallback)(CameraStates);

    void initiate();

    bool isChangedFlag = false;
    
    void checkGearsLoopStep();

    void communicationLoopStep();

    void setChangeStateCallback(ChangeStateCallback callback);

    void setCommunicationDevice(CommunicationUnit network);

    void updateTimings(Timings newTimings);

    void executeCommand(CommunicationUnit::ControlCommandSet command);

    void sendUpTimings();

    CommunicationUnit network;
private:
    Timings timings=Timings( //default one
        60, // BOUNCE_DELAY
        900, // REPEATER_DELAY
        3000, // FRONT_CAM_SHOWTIME_DELAY
        1500); // REAR_CAM_SHOWTIME_DELAY
       
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
    void getTimingsFromStorage();
    void putTimingsToStorage();
    byte crc8(byte *buffer, byte size);
};

#endif
