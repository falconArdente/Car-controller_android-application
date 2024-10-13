#pragma once

#include <Arduino.h>
#include "Timings.h"
#include "Lever.h"
#include "CameraStatesEnum.h"
#include "CommunicationUnit.h"
#include "ControllerForCommUnitInterface.h"

#ifndef CAMERALIGHTTURNSSUPPLYYCONTROLLER_H
#define CAMERALIGHTTURNSSUPPLYYCONTROLLER_H

class CameraLightTurnsSupplyController : public ControllerForCommUnitInterface {
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

    void sendCurrentState();

private:
    CommunicationUnit network;
    Timings timings = Timings( //default one
            60, // BOUNCE_DELAY
            900, // REPEATER_DELAY
            3000, // FRONT_CAM_SHOWTIME_DELAY
            1500); // REAR_CAM_SHOWTIME_DELAY

    ChangeStateCallback changeStateCallback;
    CameraStates cameraState = CAMS_OFF;
    enum FogLightState {
        ALL_OFF,
        LEFT_ON,
        RIGHT_ON,
        BOTH_ON
    } fogLightsState = ALL_OFF;
    bool cautionIsPressed = false;
    long cautionIsTimeStamp = 0;
//input gears
    Lever reverseGear;
    Lever leftTurnLever;
    Lever rightTurnLever;
//output pins
    const int outAngelEyeRight = 7; // transistor to power on pin8
    const int outAngelEyeLeft = 5; // transistor to power on pin16
    const int outCautionSignal = 2;
    const int outDisplayOn = A2;
    const int outLeftFogLight = 3;
    const int outRightFogLight = 9;
    const int outRelayCameraSwitch = 11;
    const int outControllerLed = 13; //direct board on
// service functions
    void getGearsState();

    bool isTimeOutForFront();

    void pushCautionButton();

    bool isTimeOutForRear();

    void setCameraState(CameraStates state);

    void turnFogLightOn();

    void turnOffFogLight();

    void getTimingsFromStorage();

    void putTimingsToStorage();
};

#endif