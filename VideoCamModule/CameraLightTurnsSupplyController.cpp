#include "Timings.h"
#include "CameraLightTurnsSupplyController.h"

CameraLightTurnsSupplyController::CameraLightTurnsSupplyController(Timings appTimings) {
    this->timings = &appTimings;
}

void CameraLightTurnsSupplyController::setCommunicationDevice(CommunicationUnit network){
   this->network = network;
}
CameraLightTurnsSupplyController::CameraLightTurnsSupplyController() {}

const CameraLightTurnsSupplyController &CameraLightTurnsSupplyController::operator=
        (const CameraLightTurnsSupplyController &B) {
    timings = B.timings;
    return *this;
}

void CameraLightTurnsSupplyController::setChangeStateCallback(ChangeStateCallback callback) {
    changeStateCallback = callback;
}

void CameraLightTurnsSupplyController::initiate() {
    pinMode(outRearCamPower, OUTPUT);
    pinMode(outAngelEye, OUTPUT);
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
    void CameraLightTurnsSupplyController::communicationLoopStep(){
      if(!reverseGear.isChangedFlag&&!leftTurnLever.isChangedFlag&&!rightTurnLever.isChangedFlag&&!isChangedFlag) return;
             CommunicationUnit::StateInfoSet state{
                leftTurnLever.isOn(),
                leftTurnLever.isDoubleClicked(),
                rightTurnLever.isOn(),
                rightTurnLever.isDoubleClicked(),
                reverseGear.isOn(),
                digitalRead(outCautionSignal),
                digitalRead(outLeftFogLight),
                digitalRead(outRightFogLight),
                digitalRead(outRelayCameraSwitch),
                digitalRead(outRearCamPower),
                digitalRead(outAngelEye),
                digitalRead(outDisplayOn),
                cameraState,
      };
     network.package(state); 
     reverseGear.isChangedFlag=false;
    leftTurnLever.isChangedFlag=false;
    rightTurnLever.isChangedFlag=false;
    isChangedFlag=false;
      
    }
void CameraLightTurnsSupplyController::checkGearsLoopStep() {
    getGearsState();
    switch (cameraState) { // logic by diagram https://github.com/falconArdente/Car-controller_android-application/wiki/CameraStatesDiagram
        case CAMS_OFF:
            if (reverseGear.isOn()) {
                setCameraState(REAR_CAM_ON);
            } else if (leftTurnLever.isDoubleClicked() || rightTurnLever.isDoubleClicked())
                setCameraState(FRONT_CAM_ON);
                this->isChangedFlag=true;
            break;
        case FRONT_CAM_ON:
            if (reverseGear.isOn()) {
                setCameraState(REAR_CAM_ON);
            } else if (!leftTurnLever.isDoubleClicked()
                       && !rightTurnLever.isDoubleClicked()
                       && isTimeOutForFront()
                       && isTimeOutForRear())
                setCameraState(CAMS_OFF);
                //this->isChangedFlag=true;
            break;
        case REAR_CAM_ON:
            if ((!isTimeOutForFront() || leftTurnLever.isDoubleClicked() ||
                 rightTurnLever.isDoubleClicked())
                && !reverseGear.isOn()) {
                setCameraState(FRONT_CAM_ON);
            } else if (isTimeOutForRear())
                setCameraState(CAMS_OFF);
               // this->isChangedFlag=true;
            break;
        case TEST_MODE:
            //this->isChangedFlag=true;
            break;
    }
    if (!reverseGear.isOn() && cameraState != TEST_MODE)digitalWrite(outCautionSignal, LOW);
}

bool CameraLightTurnsSupplyController::isTimeOutForFront() {
    unsigned long timeStamp = millis() - timings->FRONT_CAM_SHOWTIME_DELAY;
    if (leftTurnLever.getLastTimeTurnedOff() < timeStamp
        && rightTurnLever.getLastTimeTurnedOff() < timeStamp)
        return true;
    else
        return false;
}

bool CameraLightTurnsSupplyController::isTimeOutForRear() {
    if (reverseGear.getLastTimeTurnedOn() + timings->REAR_CAM_SHOWTIME_DELAY < millis())
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
            digitalWrite(outAngelEye, LOW);
            digitalWrite(outRearCamPower, LOW);
            digitalWrite(outRelayCameraSwitch, LOW);
            digitalWrite(outControllerLed, LOW);
            digitalWrite(outCautionSignal, LOW);
            turnOffFogLight();
            break;
        case REAR_CAM_ON:
            digitalWrite(outDisplayOn, HIGH);
            digitalWrite(outAngelEye, LOW);
            digitalWrite(outRearCamPower, HIGH);
            digitalWrite(outRelayCameraSwitch, LOW);
            digitalWrite(outControllerLed, HIGH);
            turnOffFogLight();
            if (!leftTurnLever.isOn() && !rightTurnLever.isOn())
                digitalWrite(outCautionSignal, HIGH);
            break;
        case FRONT_CAM_ON:
            digitalWrite(outDisplayOn, HIGH);
            digitalWrite(outAngelEye, HIGH);
            digitalWrite(outRearCamPower, LOW);
            digitalWrite(outRelayCameraSwitch, HIGH);
            digitalWrite(outControllerLed, HIGH);
            digitalWrite(outCautionSignal, LOW);
            turnFogLightOn();
            break;
    }

    cameraState = state;
    if (changeStateCallback != NULL)changeStateCallback(state);
}

void CameraLightTurnsSupplyController::turnFogLightOn() {
    if (leftTurnLever.isOn()) {
        digitalWrite(outLeftFogLight, HIGH);
    } else {
        digitalWrite(outRightFogLight, HIGH);
    }
}

void CameraLightTurnsSupplyController::turnOffFogLight() {
    digitalWrite(outLeftFogLight, LOW);
    digitalWrite(outRightFogLight, LOW);
}
