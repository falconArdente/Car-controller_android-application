#include <EEPROM.h>
#include "Timings.h"
#include "CameraLightTurnsSupplyController.h"

const int BOUNCE_DELAY = 60;
const int REPEATER_DELAY = 600; // millis to discover double click
const int FRONT_CAM_SHOWTIME_DELAY = 3000; //millis to show front cam after signal gone off
const int REAR_CAM_SHOWTIME_DELAY = 3000;



void setup() {
      Serial.begin(9600);
Serial.println("RESTARTED");
  Timings appTimings{BOUNCE_DELAY, REPEATER_DELAY, FRONT_CAM_SHOWTIME_DELAY, REAR_CAM_SHOWTIME_DELAY};
CameraLightTurnsSupplyController device = CameraLightTurnsSupplyController(appTimings);    

    device.initiate();

}

void loop() {
   // device.checkGearsLoopStep();
    //checking version usability
    delay(50);
}
