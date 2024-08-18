#include <EEPROM.h>
#include "Timings.h"
#include "CameraLightTurnsSupplyController.h"

const uint16_t BOUNCE_DELAY = 60;
const uint16_t REPEATER_DELAY = 600; // millis to discover double click
const uint16_t FRONT_CAM_SHOWTIME_DELAY = 3000; //millis to show front cam after signal gone off
const uint16_t REAR_CAM_SHOWTIME_DELAY = 3000;

Timings appTimings{BOUNCE_DELAY, REPEATER_DELAY, FRONT_CAM_SHOWTIME_DELAY, REAR_CAM_SHOWTIME_DELAY};
CameraLightTurnsSupplyController device = CameraLightTurnsSupplyController(appTimings);

void setup() {
    device.initiate();
    Serial.begin(9600);
}

void loop() {
    device.checkGearsLoopStep();
    delay(50);
}