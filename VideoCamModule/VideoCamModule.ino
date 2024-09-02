#include "CameraLightTurnsSupplyController.h"
#include "DxBT18BluetoothModule.h"

CameraLightTurnsSupplyController device = CameraLightTurnsSupplyController();
// implements ControllerForCommUnitInterface
CommunicationUnit bt18BluetoothModule = DxBT18BluetoothModule(&device);
// extends CommunicationUnit class, that realize package interaction rules in single place

void setup() {
    Serial.begin(9600);
    Serial.setTimeout(50);
    device.setCommunicationDevice(bt18BluetoothModule);
    device.initiate();
}

void loop() {
    device.checkGearsLoopStep();
    device.communicationLoopStep();
    delay(50);
}