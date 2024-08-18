#include <EEPROM.h>

struct Timings {
    uint16_t BOUNCE_DELAY = 60;
    uint16_t REPEATER_DELAY = 600; // millis to discover double click
    uint16_t FRONT_CAM_SHOWTIME_DELAY = 3000; //millis to show front cam after signal gone off
    uint16_t REAR_CAM_SHOWTIME_DELAY = 3000;
} appTimings;

class CameraLightTurnsSupplyController {
    enum CameraStates {
        CAMS_OFF,
        REAR_CAM_ON,
        FRONT_CAM_ON,
    };

public:
    CameraLightTurnsSupplyController(Timings timings) {
        this->timings = &timings;
    }

    class Lever {
    public:
        Lever(int pinNumber, Timings timings, bool isLowLevelToTurnOn = false) {
            this->timings = &timings;
            this->pinNumber = pinNumber;
            pinMode(pinNumber, INPUT);
            this->isLowLevelToTurnOn = isLowLevelToTurnOn;
        }


        bool isOn() {
            return state;
        }

        bool isDoubleClicked() {
            return doubleClicked;
        }

        unsigned long getLastTimeTurnedOff() {
            return lastTimeTurnedOff;
        }

        void checkState() {
            if (isLowLevelToTurnOn)state = !state;
            unsigned long timeStamp = millis();
            if (state != digitalRead(pinNumber) &&
                lastTimeChanged <= timeStamp - timings->BOUNCE_DELAY) {
                state = digitalRead(pinNumber);
                lastTimeChanged = timeStamp;
            }
            if (isLowLevelToTurnOn)state = !state;
            if (lastTimeChanged == timeStamp) {// is time to set on/off stamp
                if (state) {
                    doubleClicked = isDoubleClicking(timeStamp);
                    lastTimeTurnedOn = timeStamp;
                } else {
                    lastTimeTurnedOff = timeStamp;
                }
            }
        }

    private:
        Timings *timings;
        bool state = false;
        bool doubleClicked = false;
        int pinNumber;
        unsigned long lastTimeChanged = 0;
        unsigned long lastTimeTurnedOn = 0;
        unsigned long lastTimeTurnedOff = 0;
        bool isLowLevelToTurnOn;

        bool isDoubleClicking(unsigned long timeStamp) {
            if (lastTimeTurnedOn + timings->REPEATER_DELAY < timeStamp &&
                lastTimeTurnedOn < lastTimeTurnedOff)
                return true;
            else
                return false;
        }
    };

    typedef void (*ChangeStateCallback)(CameraStates);

public:

    void setChangeStateCallback(ChangeStateCallback callback) {
        this->changeStateCallback = callback;
    }

    void initiate() {
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
            REAR_CAM_ON:
                if (isTimeToOffRear())setCameraState(CAMS_OFF);
            FRONT_CAM_ON:
                if (isTimeToOffFront())setCameraState(CAMS_OFF);
        }
    }

private:
    Timings *timings;

    bool isTimeToOffFront() {
        unsigned long timeStamp = millis() - timings->FRONT_CAM_SHOWTIME_DELAY;
        if (leftTurnLever.getLastTimeTurnedOff() < timeStamp
            && rightTurnLever.getLastTimeTurnedOff() < timeStamp)
            return true;
        else
            return false;
    }

    bool isTimeToOffRear() {
        if (reverseGear.getLastTimeTurnedOff() + timings->REAR_CAM_SHOWTIME_DELAY > millis())
            return true;
        else
            return false;
    }

    ChangeStateCallback changeStateCallback;
    CameraStates cameraState = CAMS_OFF;
//input pins
    Lever reverseGear = Lever(A1, appTimings);
    Lever leftTurnLever = Lever(A0, appTimings, true);
    Lever rightTurnLever = Lever(12, appTimings, true);

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
    bool leftIsDoubleClicked = false;
    bool rightIsDoubleClicked = false;

    void getGearsState() {
        reverseGear.checkState();
        leftTurnLever.checkState();
        rightTurnLever.checkState();
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

};

CameraLightTurnsSupplyController device = CameraLightTurnsSupplyController(appTimings);

void setup() {
    device.initiate();
    Serial.begin(9600);
}

void loop() {
    device.checkGearsLoopStep();
    delay(50);
}