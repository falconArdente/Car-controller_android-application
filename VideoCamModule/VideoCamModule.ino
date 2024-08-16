#include <EEPROM.h>

class CameraLightTurnsSupplyController {
    enum CameraStates {
        CAMS_OFF,
        REAR_CAM_ON,
        FRONT_CAM_ON,
    };
public:
    CameraStates cameraState = CAMS_OFF;

private:
    //timings
    const uint16_t BOUNCE_DELAY = 60;
    const uint16_t REPEATER_DELAY = 600; // millis to discover command to turn front camera on
    const uint16_t FRONT_CAM_SHOWTIME_DELAY = 3000; //millis to show front cam after signal gone off
    //input pins
    const int inReverseGear = A1;
    const int inRightTurn = 12;
    const int inLeftTurn = A0;
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
    int stateRearSig = 0;
    int stateLeftSig = 0;
    int stateRightSig = 0;
    bool backIsOn = false;
    bool leftIsOn = false;
    bool rightIsOn = false;
    unsigned long lastTimeRearChanged = 0;
    unsigned long lastTimeLeftChanged = 0;
    unsigned long lastTimeRightChanged = 0;
    unsigned long leftTriggerOffMill = 0;
    unsigned long rightTriggerOffMill = 0;

    bool checkSignalState(
            int *lastSignalState,
            const int pinNumber,
            unsigned long *lastTimeChanged) {
        if (lastSignalState != digitalRead(pinNumber) &&
            lastTimeChanged <= millis() - BOUNCE_DELAY) {
            lastSignalState = digitalRead(pinNumber);
            lastTimeChanged = millis();
            if (lastSignalState == HIGH) {
                return true;
            } else {
                return false;
            }
        }
    }
};


void setup() {
    Serial.begin(9600);
}

void loop() {
    delay(50);
}
