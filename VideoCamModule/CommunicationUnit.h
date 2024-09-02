#pragma once

#include <Arduino.h>
#include "CameraStatesEnum.h"
#include "Timings.h"

#ifndef COMINT_H
#define COMINT_H

class ControllerForCommUnitInterface;

class CommunicationUnit {
public:
    static const char START_PACKAGE_SIGNATURE = '█';
    static const char BORDER_OF_PACKAGE_SIGN = '\n';
    static const int MAX_PACKAGE_SIZE = 9;
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

    CommunicationUnit(ControllerForCommUnitInterface *hostObject);

    CommunicationUnit();

    bool isAbstract = true;

    void checkForIncome();

    void sendState(StateInfoSet stateSet);

    void sendTimings(Timings timings);

    void sendAdditionalInfo();

protected:
    ControllerForCommUnitInterface *hostObject;
    byte packageToSend[MAX_PACKAGE_SIZE];
    int bytesToSend = 0;
    int errorsCount = 0;

    void parseIncomingPackage(byte package[MAX_PACKAGE_SIZE], int packetSize);

    void commandToControllerDevice(byte package[MAX_PACKAGE_SIZE]);

    void newTimingsToControllerDevice(byte package[MAX_PACKAGE_SIZE]);

    void sendPackage(byte *packageToSend, int bytesToSend);
};

#endif