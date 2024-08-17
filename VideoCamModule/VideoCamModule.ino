#include <EEPROM.h>

class CameraLightTurnsSupplyController {
    enum CameraStates {
        CAMS_OFF,
        REAR_CAM_ON,
        FRONT_CAM_ON,
    };

    typedef void (*ChangeStateCallback)(CameraStates);

public:
    void setChangeStateCallback(ChangeStateCallback callback) {
        this->changeStateCallback = callback;
    }

    void initiate() {
        pinMode(inReverseGear, INPUT);
        pinMode(inRightTurn, INPUT);
        pinMode(inLeftTurn, INPUT);
        pinMode(outRearCamPower, OUTPUT);
        pinMode(outFrontCamPower, OUTPUT);
        pinMode(outCautionSignal, OUTPUT);
        pinMode(outDisplayOn, OUTPUT);
        pinMode(outLeftFogLight, OUTPUT);
        pinMode(outRightFogLight, OUTPUT);
        pinMode(outRelayCameraSwitch, OUTPUT);
        pinMode(outControllerLed, OUTPUT);
        setCameraState(CAMS_OFF);
        getGearsState();
    }

    void checkGearsLoopStep() {
        getGearsState();
        if (reverseIsOn) {
            setCameraState(REAR_CAM_ON);
            return;
        }
        if (leftIsOn) {
            if (leftIsDoublePressed())setCameraState(FRONT_CAM_ON);
            return;
        }
        if (rightIsOn) {
            if (rightIsDoublePressed())setCameraState(FRONT_CAM_ON);
            return;
        }
        switch (cameraState) {
            REAR_CAM_ON:
                if (isTimeToOffRear())setCameraState(CAMS_OFF);
            FRONT_CAM_ON:
                if (isTimeToOffFront())setCameraState(CAMS_OFF);
        }
    }

private:
    bool isTimeToOffFront() {

    }

    bool isTimeToOffRear() {

    }

    bool leftIsDoublePressed() {

    }

    bool rightIsDoublePressed() {

    }

    ChangeStateCallback changeStateCallback;
    CameraStates cameraState = CAMS_OFF;
    //timings
    uint16_t BOUNCE_DELAY = 60;
    uint16_t REPEATER_DELAY = 600; // millis to discover command to turn front camera on
    uint16_t FRONT_CAM_SHOWTIME_DELAY = 3000; //millis to show front cam after signal gone off
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
    bool lastReverseState = false;
    bool lastLeftState = false;
    bool lastRightState = false;
    bool reverseIsOn = false;
    bool leftIsOn = false;
    bool rightIsOn = false;
    bool leftIsDoubleClicked = false;
    bool rightIsDoubleClicked = false;
    unsigned long lastTimeReverseChanged = 0;
    unsigned long lastTimeReverseOff = 0;
    unsigned long lastTimeLeftChanged = 0;
    unsigned long lastTimeRightChanged = 0;
    unsigned long lastTimeLeftOn;
    unsigned long lastTimeRightOn;
    unsigned long lastTimeLeftOff = 0;
    unsigned long lastTimeRightOff = 0;

    void getGearsState() {
        getReverseState();
        getLeftState();
        getRightState();
    }

    bool getReverseState() {
        reverseIsOn = checkSignalState(&lastReverseState, inReverseGear, &lastTimeReverseChanged);
        if (!reverseIsOn)lastTimeReverseOff = millis();
        return reverseIsOn;
    }

    bool getLeftState() {
        leftIsOn = !checkSignalState(&lastLeftState, inLeftTurn, &lastTimeLeftChanged);
        if (leftIsOn) {
            leftIsDoubleClicked =
            lastTimeLeftOn = millis();
        } else {
            lastTimeLeftOff = millis();
        }
        return leftIsOn;
    }

    bool getRightState() {
        return rightIsOn = !checkSignalState(&lastRightState,
                                             inRightTurn,
                                             &lastTimeRightChanged);
    }

    void setCameraState(CameraStates state) {
        if (this->cameraState == state)return;
        switch (state) {
            case CAMS_OFF:
                digitalWrite(outDisplayOn, LOW);
                digitalWrite(outFrontCamPower, LOW);
                digitalWrite(outRearCamPower, LOW);
                digitalWrite(outRelayCameraSwitch, LOW);
                digitalWrite(outControllerLed, LOW);
            case REAR_CAM_ON:
                digitalWrite(outDisplayOn, HIGH);
                digitalWrite(outFrontCamPower, LOW);
                digitalWrite(outRearCamPower, HIGH);
                digitalWrite(outRelayCameraSwitch, LOW);
                digitalWrite(outControllerLed, HIGH);
            case FRONT_CAM_ON:
                digitalWrite(outDisplayOn, HIGH);
                digitalWrite(outFrontCamPower, HIGH);
                digitalWrite(outRearCamPower, LOW);
                digitalWrite(outRelayCameraSwitch, HIGH);
                digitalWrite(outControllerLed, HIGH);
        }
        this->cameraState = state;
        changeStateCallback(state);
    }

    bool checkSignalState(
            bool *lastSignalState,
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

CameraLightTurnsSupplyController device = CameraLightTurnsSupplyController();

void setup() {
    device.initiate();
    Serial.begin(9600);
}

void loop() {
    device.checkGearsLoopStep();
    delay(50);
}