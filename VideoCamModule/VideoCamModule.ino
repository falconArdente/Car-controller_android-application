
#include "Timings.h"
#include "CameraLightTurnsSupplyController.h"

CameraLightTurnsSupplyController device = CameraLightTurnsSupplyController();
CommunicationUnit network = CommunicationUnit(&device);

void setup() {
    Serial.begin(9600);
    Serial.setTimeout(50);
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
