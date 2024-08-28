#include <EEPROM.h>
#include "Timings.h"
#include "CameraLightTurnsSupplyController.h"

const Timings appTimings{
        60, // BOUNCE_DELAY
        900, // REPEATER_DELAY
        3000, // FRONT_CAM_SHOWTIME_DELAY
        3000}; // REAR_CAM_SHOWTIME_DELAY
CameraLightTurnsSupplyController device;

void setup() {
    Serial.begin(9600);
    device = CameraLightTurnsSupplyController(appTimings);
    device.setChangeStateCallback(callbackTest);
    device.initiate();
    byte first;
    bitWrite(first,0,1);
    Serial.println(first);
}

void loop() {
    device.checkGearsLoopStep();
    delay(50);
}

void callbackTest(CameraStates state) {
    // empty for now
}
