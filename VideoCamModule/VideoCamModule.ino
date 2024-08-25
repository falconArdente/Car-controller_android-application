#include <EEPROM.h>
#include "Timings.h"
#include "CameraLightTurnsSupplyController.h"

const int BOUNCE_DELAY = 60;
const int REPEATER_DELAY = 900; // millis to discover double click
const int FRONT_CAM_SHOWTIME_DELAY = 3000; //millis to show front cam after signal gone off
const int REAR_CAM_SHOWTIME_DELAY = 3000;

Timings appTimings{BOUNCE_DELAY, REPEATER_DELAY, FRONT_CAM_SHOWTIME_DELAY, REAR_CAM_SHOWTIME_DELAY};
CameraLightTurnsSupplyController device;

void setup() {

    Serial.begin(9600);
    Serial.println("RESTARTED");
    device.timings = &appTimings;


    device.initiate();

}

void loop() {
    device.checkGearsLoopStep();
    //checking version usability
    delay(50);
}
