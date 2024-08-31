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

    typedef void (CameraLightTurnsSupplyController::*UpdateTimingsCallback)(Timings newTimings);

    typedef void (CameraLightTurnsSupplyController::*ExecuteCommandCallback)(
            ControlCommandSet command);

    typedef void (CameraLightTurnsSupplyController::*SendUpTimingsCallback)();

    CommunicationUnit(CameraLightTurnsSupplyController *hostObject);

    CommunicationUnit();

    bool isAbstract = true;

    void sendState(StateInfoSet stateSet);

    void sendTimings(Timings timings);

    void checkForIncome();

    void setUpdateTimingsCallback(UpdateTimingsCallback timingsCallback);

    void setExecuteCommandCallback(ExecuteCommandCallback commandCallback);

    void setSendUpTimingsCallback(void (CameraLightTurnsSupplyController::* sendUpTimings)());
//inline virtual void checkForIncomingData();

//inline virtual void setTimingsIncome(byte p[9]);

//inline virtual void setStateIncome(byte p [2]);
    void sendUpTimings();

    UpdateTimingsCallback updateTimings;
    ExecuteCommandCallback executeCommand;

    //SendUpTimingsCallback sendUpTimingsCallback;
    void (CameraLightTurnsSupplyController::* sendUpTimingsCallback)();

private:
    CameraLightTurnsSupplyController *hostObject;


    void sendPackage(byte *packageToSend, int bytesToSend);

    byte packageToSend[9];
    int bytesToSend = 0;
};

#endif
