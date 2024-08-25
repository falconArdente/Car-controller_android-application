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
    Serial.println("RESTARTED");
    device=CameraLightTurnsSupplyController(appTimings);
    //device.changeStateCallback=callbackTest;
    device.initiate();
}

void loop() {
    device.checkGearsLoopStep();
    delay(50);
}

void callbackTest(CameraLightTurnsSupplyController::CameraStates state){
    Serial.print("camera state is ");
    Serial.println(state);
}
