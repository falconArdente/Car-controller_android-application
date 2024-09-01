#include "Timings.h"
#include "CameraLightTurnsSupplyController.h"
#include "CommunicationUnit.h"
#include <EEPROM.h>

 const int TIMINGS_ADDR = 0;

CameraLightTurnsSupplyController::CameraLightTurnsSupplyController() {}

void CameraLightTurnsSupplyController::setCommunicationDevice(CommunicationUnit network) {
    this->network = network;
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
    reverseGear = Lever(A1, &timings);
    leftTurnLever = Lever(A0, &timings);
    rightTurnLever = Lever(12, &timings);
    setCameraState(CAMS_OFF);
    getTimingsFromStorage();
    getGearsState();
}

void CameraLightTurnsSupplyController::updateTimings(Timings newTimings) {
    Serial.println("updatingTimingsPayload");
}

void
CameraLightTurnsSupplyController::executeCommand(CommunicationUnit::ControlCommandSet command) {
    digitalWrite(outCautionSignal, command.cautionIsOn);
    digitalWrite(outLeftFogLight, command.leftFogIsOn);
    digitalWrite(outRightFogLight, command.rightFogIsOn);
    digitalWrite(outRelayCameraSwitch, command.relayIsOn);
    digitalWrite(outRearCamPower, command.rearCameraIsOn);
    digitalWrite(outAngelEye, command.angelEyeIsOn);
    digitalWrite(outDisplayOn, command.displayIsOn);
    setCameraState(command.cameraState);
}

void CameraLightTurnsSupplyController::sendUpTimings() {
    network.sendTimings(timings);
}


void CameraLightTurnsSupplyController::communicationLoopStep() {
    if (!reverseGear.isChangedFlag && !leftTurnLever.isChangedFlag &&
        !rightTurnLever.isChangedFlag && !isChangedFlag) {
        network.checkForIncome();
    } else {
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
        network.sendState(state);
        reverseGear.isChangedFlag = false;
        leftTurnLever.isChangedFlag = false;
        rightTurnLever.isChangedFlag = false;
        isChangedFlag = false;
    }
}

void CameraLightTurnsSupplyController::checkGearsLoopStep() {
    getGearsState();
    switch (cameraState) { // logic by diagram https://github.com/falconArdente/Car-controller_android-application/wiki/CameraStatesDiagram
        case CAMS_OFF:
            if (reverseGear.isOn()) {
                setCameraState(REAR_CAM_ON);
            } else if (leftTurnLever.isDoubleClicked() || rightTurnLever.isDoubleClicked())
                setCameraState(FRONT_CAM_ON);
            break;
        case FRONT_CAM_ON:
            if (reverseGear.isOn()) {
                setCameraState(REAR_CAM_ON);
            } else if (!leftTurnLever.isDoubleClicked()
                       && !rightTurnLever.isDoubleClicked()
                       && isTimeOutForFront())
                setCameraState(CAMS_OFF);
            break;
        case REAR_CAM_ON:
            if ((leftTurnLever.isDoubleClicked() ||
                 rightTurnLever.isDoubleClicked())
                && !reverseGear.isOn()) {
                setCameraState(FRONT_CAM_ON);
            } else if (isTimeOutForRear() && !reverseGear.isOn())
                setCameraState(CAMS_OFF);
            break;
        case TEST_MODE:
            break;
    }
    if (!reverseGear.isOn() && cameraState != TEST_MODE)digitalWrite(outCautionSignal, LOW);
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
    this->isChangedFlag = true;
    cameraState = state;
    if (changeStateCallback != NULL)changeStateCallback(state);
}

void CameraLightTurnsSupplyController::turnFogLightOn() {
    if (leftTurnLever.isOn()) {
        digitalWrite(outLeftFogLight, HIGH);
    } else if (rightTurnLever.isOn()) {
        digitalWrite(outRightFogLight, HIGH);
    } else {
        digitalWrite(outLeftFogLight, HIGH);
        digitalWrite(outRightFogLight, HIGH);
    }
}

void CameraLightTurnsSupplyController::turnOffFogLight() {
    digitalWrite(outLeftFogLight, LOW);
    digitalWrite(outRightFogLight, LOW);
}

bool CameraLightTurnsSupplyController::isTimeOutForFront() {
    unsigned long timeStamp = millis() - timings.FRONT_CAM_SHOWTIME_DELAY;
    if (timeStamp < leftTurnLever.getLastTimeTurnedOn()
        || timeStamp < rightTurnLever.getLastTimeTurnedOn())
        return false;
    else
        return true;
}

bool CameraLightTurnsSupplyController::isTimeOutForRear() {
    if (reverseGear.getLastTimeTurnedOn() + timings.REAR_CAM_SHOWTIME_DELAY < millis())
        return true;
    else
        return false;
}

void CameraLightTurnsSupplyController::getGearsState() {
    reverseGear.checkState();
    leftTurnLever.checkState();
    rightTurnLever.checkState();
}
    void CameraLightTurnsSupplyController::getTimingsFromStorage(){
     Timings timingsFromStorage;
     EEPROM.get(TIMINGS_ADDR, timingsFromStorage); 
     byte checkByte=crc8((byte*) &timingsFromStorage, sizeof(timingsFromStorage));
     timingsFromStorage.crc=0;
     if (checkByte==0){
      if(!(timings==timingsFromStorage)) timings=timingsFromStorage;
     }else{
      setCameraState(TEST_MODE);
      digitalWrite(outCautionSignal, HIGH);
      delay(1500);
      digitalWrite(outCautionSignal, LOW);
      setCameraState(CAMS_OFF);
     }
    }
    void CameraLightTurnsSupplyController::putTimingsToStorage(){
     timings.crc=crc8((byte*) &timings, sizeof(timings));
      EEPROM.put(TIMINGS_ADDR, timings);
    }
    byte CameraLightTurnsSupplyController::crc8(byte *buffer, byte size) {
  byte crc = 0;
  for (byte i = 0; i < size; i++) {
    byte data = buffer[i];
    for (int j = 8; j > 0; j--) {
      crc = ((crc ^ data) & 1) ? (crc >> 1) ^ 0x8C : (crc >> 1);
      data >>= 1;
    }
  }
  return crc;
}
