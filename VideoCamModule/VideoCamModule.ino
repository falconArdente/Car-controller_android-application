#include <EEPROM.h>
#include "Timings.h"
#include "CameraLightTurnsSupplyController.h"

const Timings appTimings{
        60, // BOUNCE_DELAY
        900, // REPEATER_DELAY
        3000, // FRONT_CAM_SHOWTIME_DELAY
        1500}; // REAR_CAM_SHOWTIME_DELAY
CameraLightTurnsSupplyController device;
CommunicationUnit network = CommunicationUnit(&device);

void setup() {

    Serial.begin(9600);
    Serial.setTimeout(50);
    device = CameraLightTurnsSupplyController(appTimings);
    device.setChangeStateCallback(callbackTest);
    device.setCommunicationDevice(network);
    device.initiate();
}

void loop() {
    device.checkGearsLoopStep();
    device.communicationLoopStep();
    delay(50);
}

void callbackTest(CameraStates state) {
    // empty for now
}
