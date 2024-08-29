#include "CommunicationUnit.h"
#include "CameraStatesEnum.h"

void CommunicationUnit::package(StateInfoSet stateSet) {
    
    bitWrite(packageToSend[0], 0, 0);
    bitWrite(packageToSend[0], 1, 0);
    bitWrite(packageToSend[0], 2, stateSet.leftPressed);
    bitWrite(packageToSend[0], 3, stateSet.leftDblPressed);
    bitWrite(packageToSend[0], 4, stateSet.rightPressed);
    bitWrite(packageToSend[0], 5, stateSet.rightDblPressed);
    bitWrite(packageToSend[0], 6, stateSet.reversePressed);
    bitWrite(packageToSend[0], 7, stateSet.cautionIsOn);
    bitWrite(packageToSend[1], 0, stateSet.leftFogIsOn);
    bitWrite(packageToSend[1], 1, stateSet.rightFogIsOn);
    bitWrite(packageToSend[1], 2, stateSet.relayIsOn);
    bitWrite(packageToSend[1], 3, stateSet.rearCameraIsOn);
    bitWrite(packageToSend[1], 4, stateSet.angelEyeIsOn);
    bitWrite(packageToSend[1], 5, stateSet.displayIsOn);
    bitWrite(packageToSend[1], 6, bitRead(stateSet.cameraState, 0));
    bitWrite(packageToSend[1], 7, bitRead(stateSet.cameraState, 1));
    sendPackage(packageToSend,2);
}
void CommunicationUnit::sendPackage(byte* packageToSend, int bytesToSend){
  Serial.println();
  for(int i=0;i<bytesToSend;i++){
    Serial.print("byte ");
    Serial.println(i);
      Serial.write(packageToSend[i]);
  }
  Serial.println();
}
//CommunicationUnit::TimingsPackage::TimingsPackage(bool isToWrite, Timings timings) {
//    if (isToWrite) {
//        package[0] = 3;
//    } else {
//        package[0] = 2;
//    }
//    package[1] = timings.BOUNCE_DELAY;
//    package[2] = (timings.BOUNCE_DELAY) >> 8;
//}
//CommunicationUnit::ControlCommand::ControlCommand(
//                bool cautionIsOn,
//                bool leftFogIsOn,
//                bool rightFogIsOn,
//                bool relayIsOn,
//                bool rearCameraIsOn,
//                bool angelEyeIsOn,
//                bool displayIsOn,
//                CameraStates cameraState){
//                  controlCommandSet.cautionIsOn=cautionIsOn;
//}
//byte[2] CommunicationUnit::ControlCommand::makePackage(){
//        byte package[2];
//        bitWrite(package[0], 0, 1);
//        bitWrite(package[0], 1, 0);
//
//        return &package;
//}
