#include "CommunicationUnit.h"

#include "CameraLightTurnsSupplyController.h"

CommunicationUnit::CommunicationUnit(CameraLightTurnsSupplyController *hostPointer) {
    hostObject = hostPointer;
}

CommunicationUnit::CommunicationUnit() {}

void CommunicationUnit::checkForIncome() {
    sendUpTimings();
}

void CommunicationUnit::sendUpTimings() {
            hostObject->sendUpTimings();
    }

void CommunicationUnit::sendState(StateInfoSet stateSet) {

    bitWrite(packageToSend[0], 0, 0);
    bitWrite(packageToSend[0], 1, 0);
    bitWrite(packageToSend[0], 2, stateSet.leftPressed);
    bitWrite(packageToSend[0], 3, stateSet.leftDblPressed);
    bitWrite(packageToSend[0], 4, stateSet.rightPressed);
    bitWrite(packageToSend[0], 5, stateSet.rightDblPressed);
    bitWrite(packageToSend[0], 6, stateSet.reversePressed);
    bitWrite(packageToSend[0], 7, stateSet.cautionIsOn);
    bitWrite(packageToSend[1], 0, stateSet.leftFogIsOn);
    bitWrite(packageToSend[1], 1, stateSet.rightFogIsOn);
    bitWrite(packageToSend[1], 2, stateSet.relayIsOn);
    bitWrite(packageToSend[1], 3, stateSet.rearCameraIsOn);
    bitWrite(packageToSend[1], 4, stateSet.angelEyeIsOn);
    bitWrite(packageToSend[1], 5, stateSet.displayIsOn);
    bitWrite(packageToSend[1], 6, bitRead(stateSet.cameraState, 0));
    bitWrite(packageToSend[1], 7, bitRead(stateSet.cameraState, 1));
    sendPackage(packageToSend, 2);
}

void CommunicationUnit::sendPackage(byte *packageToSend, int bytesToSend) {
    Serial.println();
    for (int i = 0; i < bytesToSend; i++) {
        Serial.write(packageToSend[i]);
    }
    Serial.println();
}

void CommunicationUnit::sendTimings(Timings timings) {
    packageToSend[0] = 2;
    packageToSend[1] = timings.BOUNCE_DELAY;
    packageToSend[2] = (timings.BOUNCE_DELAY) >> 8;
    packageToSend[3] = timings.REPEATER_DELAY;
    packageToSend[4] = (timings.REPEATER_DELAY) >> 8;
    packageToSend[5] = timings.FRONT_CAM_SHOWTIME_DELAY;
    packageToSend[6] = (timings.FRONT_CAM_SHOWTIME_DELAY) >> 8;
    packageToSend[7] = timings.REAR_CAM_SHOWTIME_DELAY;
    packageToSend[8] = (timings.REAR_CAM_SHOWTIME_DELAY) >> 8;
    sendPackage(packageToSend, 9);
}
