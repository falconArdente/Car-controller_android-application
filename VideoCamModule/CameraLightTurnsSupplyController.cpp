#include "Timings.h"
#include "CameraLightTurnsSupplyController.h"
#include "CommunicationUnit.h"
#include "Utils.cpp"
#include <EEPROM.h>

const int TIMINGS_ADDR = 0;
const int CAUTION_BUTTON_DELAY = 300;

CameraLightTurnsSupplyController::CameraLightTurnsSupplyController() {}

void CameraLightTurnsSupplyController::setCommunicationDevice(CommunicationUnit network) {
    this->network = network;
}

void CameraLightTurnsSupplyController::setChangeStateCallback(ChangeStateCallback callback) {
    changeStateCallback = callback;
}

void CameraLightTurnsSupplyController::initiate() {
    pinMode(outAngelEyeRight, OUTPUT);
    pinMode(outAngelEyeLeft, OUTPUT);
    pinMode(outCautionSignal, OUTPUT);
    pinMode(outDisplayOn, OUTPUT);
    pinMode(outLeftFogLight, OUTPUT);
    pinMode(outRightFogLight, OUTPUT);
    pinMode(outRelayCameraSwitch, OUTPUT);
    pinMode(outControllerLed, OUTPUT);
    digitalWrite(outAngelEyeRight, LOW);
    digitalWrite(outAngelEyeLeft, LOW);
    digitalWrite(outCautionSignal, LOW);
    digitalWrite(outDisplayOn, LOW);
    digitalWrite(outLeftFogLight, LOW);
    digitalWrite(outRightFogLight, LOW);
    digitalWrite(outRelayCameraSwitch, LOW);
    digitalWrite(outControllerLed, LOW);
    reverseGear = Lever(A1, &timings);
    leftTurnLever = Lever(A0, &timings);
    rightTurnLever = Lever(12, &timings);
    cautionIsTimeStamp = millis();
    setCameraState(CAMS_OFF);
    getTimingsFromStorage();
    getGearsState();
    sendCurrentState();
}

void CameraLightTurnsSupplyController::updateTimings(Timings newTimings) {
    timings = newTimings;
    putTimingsToStorage();
}

void
CameraLightTurnsSupplyController::executeCommand(CommunicationUnit::ControlCommandSet command) {
    if (cautionIsPressed != command.cautionIsOn)pushCautionButton();
    digitalWrite(outLeftFogLight, command.leftFogIsOn);
    digitalWrite(outRightFogLight, command.rightFogIsOn);
    digitalWrite(outRelayCameraSwitch, command.relayIsOn);
    digitalWrite(outAngelEyeRight, command.rightAngelEyeIsOn);
    digitalWrite(outAngelEyeLeft, command.leftAngelEyeIsOn);
    digitalWrite(outDisplayOn, command.displayIsOn);
    setCameraState(command.cameraState);
    sendCurrentState();
    if (command.leftAngelEyeIsOn)fogTintIsWhite=true; // temp for POC
}

void CameraLightTurnsSupplyController::sendUpTimings() {
    network.sendTimings(timings);
}

void CameraLightTurnsSupplyController::sendCurrentState() {
    CommunicationUnit::StateInfoSet state{
            leftTurnLever.isOn(),
            leftTurnLever.isDoubleClicked(),
            rightTurnLever.isOn(),
            rightTurnLever.isDoubleClicked(),
            reverseGear.isOn(),
            cautionIsPressed,
            digitalRead(outLeftFogLight),
            digitalRead(outRightFogLight),
            digitalRead(outRelayCameraSwitch),
            digitalRead(outAngelEyeRight),
            digitalRead(outAngelEyeLeft),
            digitalRead(outDisplayOn),
            cameraState,
    };
    network.sendState(state);
}

void CameraLightTurnsSupplyController::communicationLoopStep() {
    if (!reverseGear.isChangedFlag && !leftTurnLever.isChangedFlag &&
        !rightTurnLever.isChangedFlag && !isChangedFlag) {
        network.checkForIncome();
    } else {
        sendCurrentState();
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
            turnFogLightOn();
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
    if (!reverseGear.isOn() && cameraState != TEST_MODE && cautionIsPressed) {
        pushCautionButton();
        isChangedFlag = true;
    }
// for caution no fixation sequence
    if (digitalRead(outCautionSignal) == HIGH
        && (millis() > cautionIsTimeStamp + CAUTION_BUTTON_DELAY)) {
        digitalWrite(outCautionSignal, LOW);
    }
}

void CameraLightTurnsSupplyController::pushCautionButton() {
    if (cautionIsPressed) cautionIsPressed = false; else cautionIsPressed = true;
    cautionIsTimeStamp = millis();
    digitalWrite(outCautionSignal, HIGH);
}

void CameraLightTurnsSupplyController::setCameraState(CameraStates state) {
    if (cameraState == state)return;

    switch (state) {
        case CAMS_OFF:
            if (cameraState == TEST_MODE)asm volatile("jmp 0x00"); //return to code start
            digitalWrite(outDisplayOn, LOW);
            digitalWrite(outRelayCameraSwitch, LOW);
            if (cautionIsPressed) pushCautionButton();
            turnOffFogLight();
            break;
        case REAR_CAM_ON:
            digitalWrite(outDisplayOn, HIGH);
            digitalWrite(outRelayCameraSwitch, LOW);
            turnOffFogLight();
            if (!leftTurnLever.isOn() && !rightTurnLever.isOn())
                if (!cautionIsPressed) pushCautionButton();
            break;
        case FRONT_CAM_ON: // Fog lights turn on logic is inside checkGearsLoopStep case
            digitalWrite(outDisplayOn, HIGH);
            digitalWrite(outRelayCameraSwitch, HIGH);
            if (cautionIsPressed) pushCautionButton();
            break;
    }
    cameraState = state;
    isChangedFlag = true;
    if (changeStateCallback != NULL)changeStateCallback(state);
}

void CameraLightTurnsSupplyController::turnFogLightOn() {
    FogLightState newFogsState = ALL_OFF;
    if (leftTurnLever.isOn()) {
        digitalWrite(outLeftFogLight, HIGH);
        digitalWrite(outRightFogLight, LOW);
        if(fogTintIsWhite){
        digitalWrite(outAngelEyeLeft, HIGH);
        digitalWrite(outAngelEyeRight, LOW);
        }
        newFogsState = LEFT_ON;
    } else if (rightTurnLever.isOn()) {
              digitalWrite(outLeftFogLight, LOW);
              digitalWrite(outRightFogLight, HIGH);
                if(fogTintIsWhite){
        digitalWrite(outAngelEyeLeft, LOW);
        digitalWrite(outAngelEyeRight, HIGH);
        }
        newFogsState = RIGHT_ON;
    } else {
        digitalWrite(outLeftFogLight, HIGH);
        digitalWrite(outRightFogLight, HIGH);
                        if(fogTintIsWhite){
        digitalWrite(outAngelEyeLeft, HIGH);
        digitalWrite(outAngelEyeRight, HIGH);
        }
        newFogsState = BOTH_ON;
    }
    if (newFogsState != fogLightsState) {
        fogLightsState = newFogsState;
        isChangedFlag = true;
    }
}

void CameraLightTurnsSupplyController::turnOffFogLight() {
    digitalWrite(outLeftFogLight, LOW);
    digitalWrite(outRightFogLight, LOW);
                    if(fogTintIsWhite){
        digitalWrite(outAngelEyeLeft, LOW);
        digitalWrite(outAngelEyeRight, LOW);
        }
    isChangedFlag = true;
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

void CameraLightTurnsSupplyController::getTimingsFromStorage() {
    Timings timingsFromStorage;
    EEPROM.get(TIMINGS_ADDR, timingsFromStorage);
    byte checkByte = utils::crc8((byte * ) & timingsFromStorage, sizeof(timingsFromStorage));
    timingsFromStorage.crc = 0;
    if (checkByte == 0) {
        if (!(timings == timingsFromStorage)) timings = timingsFromStorage;
    } else {
        // Some way to signalise
    }
}

void CameraLightTurnsSupplyController::putTimingsToStorage() {
    timings.crc = utils::crc8((byte * ) & timings, sizeof(timings));
    EEPROM.put(TIMINGS_ADDR, timings);
}
