#include "Timings.h"
#include "CameraLightTurnsSupplyController.h"

CameraLightTurnsSupplyController::CameraLightTurnsSupplyController(Timings appTimings) {
    this->timings = &appTimings;
}

CameraLightTurnsSupplyController::CameraLightTurnsSupplyController() {}

const CameraLightTurnsSupplyController &CameraLightTurnsSupplyController::operator=
        (const CameraLightTurnsSupplyController &B) {
    timings = B.timings;
    return *this;
}

void CameraLightTurnsSupplyController::setChangeStateCallback(ChangeStateCallback callback) {
    this->changeStateCallback = callback;
}

void CameraLightTurnsSupplyController::initiate() {
    pinMode(outRearCamPower, OUTPUT);
    pinMode(outFrontCamPower, OUTPUT);
    pinMode(outCautionSignal, OUTPUT);
    pinMode(outDisplayOn, OUTPUT);
    pinMode(outLeftFogLight, OUTPUT);
    pinMode(outRightFogLight, OUTPUT);
    pinMode(outRelayCameraSwitch, OUTPUT);
    pinMode(outControllerLed, OUTPUT);
    reverseGear = Lever(A1, timings);
    leftTurnLever = Lever(A0, timings);
    rightTurnLever = Lever(12, timings);
    setCameraState(CAMS_OFF);
    getGearsState();
    
}

void CameraLightTurnsSupplyController::checkGearsLoopStep() {
    getGearsState();
    if (reverseGear.isOn()) {
        setCameraState(REAR_CAM_ON);
        return;
    }
    if (leftTurnLever.isOn()) {
        if (leftTurnLever.isDoubleClicked())setCameraState(FRONT_CAM_ON);
        return;
    }
    if (rightTurnLever.isOn()) {
        if (rightTurnLever.isDoubleClicked())setCameraState(FRONT_CAM_ON);
        return;
    }

    switch (cameraState) {
        case REAR_CAM_ON:
            if (isTimeToOffRear())setCameraState(CAMS_OFF);
            break;
        case FRONT_CAM_ON:
            if (isTimeToOffFront())setCameraState(CAMS_OFF);
    }
}

bool CameraLightTurnsSupplyController::isTimeToOffFront() {
    unsigned long timeStamp = millis() - timings->FRONT_CAM_SHOWTIME_DELAY;
    if (leftTurnLever.getLastTimeTurnedOff() < timeStamp
        && rightTurnLever.getLastTimeTurnedOff() < timeStamp)
        return true;
    else
        return false;
}

bool CameraLightTurnsSupplyController::isTimeToOffRear() {
    if (reverseGear.getLastTimeTurnedOff() + timings->REAR_CAM_SHOWTIME_DELAY < millis())
        return true;
    else
        return false;
}

void CameraLightTurnsSupplyController::getGearsState() {
    reverseGear.checkState();
    leftTurnLever.checkState();
    rightTurnLever.checkState();
}

void CameraLightTurnsSupplyController::setCameraState(CameraStates state) {
    if (cameraState == state)return;
    switch (state) {
        case CAMS_OFF:
            digitalWrite(outDisplayOn, LOW);
            digitalWrite(outFrontCamPower, LOW);
            digitalWrite(outRearCamPower, LOW);
            digitalWrite(outRelayCameraSwitch, LOW);
            digitalWrite(outControllerLed, LOW);
            digitalWrite(outCautionSignal, LOW);
            break;
        case REAR_CAM_ON:
            digitalWrite(outDisplayOn, HIGH);
            digitalWrite(outFrontCamPower, LOW);
            digitalWrite(outRearCamPower, HIGH);
            digitalWrite(outRelayCameraSwitch, LOW);
            digitalWrite(outControllerLed, HIGH);
            if (!leftTurnLever.isOn()&&!rightTurnLever.isOn())digitalWrite(outCautionSignal, HIGH);
            break;
        case FRONT_CAM_ON:
            digitalWrite(outDisplayOn, HIGH);
            digitalWrite(outFrontCamPower, HIGH);
            digitalWrite(outRearCamPower, LOW);
            digitalWrite(outRelayCameraSwitch, HIGH);
            digitalWrite(outControllerLed, HIGH);
            digitalWrite(outCautionSignal, LOW);
            break;
    }
    cameraState = state;
    if(changeStateCallback!=NULL)changeStateCallback(state);
}
