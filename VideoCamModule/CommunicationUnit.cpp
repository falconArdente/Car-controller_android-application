#include "CommunicationUnit.h"
#include "ControllerForCommUnitInterface.h"

CommunicationUnit::CommunicationUnit(ControllerForCommUnitInterface *hostPointer) {
    hostObject = hostPointer;
}

CommunicationUnit::CommunicationUnit() {}

void CommunicationUnit::checkForIncome() {
    char target[2] = {BORDER_OF_PACKAGE_SIGN, START_PACKAGE_SIGNATURE};
    if (Serial.available()) {
        if (Serial.find(target, sizeof(target))) {
            byte package[9];
            int packageByteCursor = 0;
            byte inByte = 0;
            while (packageByteCursor < MAX_PACKAGE_SIZE) {
                inByte = Serial.read();
                if (inByte == BORDER_OF_PACKAGE_SIGN)break;
                package[packageByteCursor++] = inByte;
            }
            if (packageByteCursor > 0) parseIncomingPackage(package, packageByteCursor);
        }
    }
}

void CommunicationUnit::parseIncomingPackage(byte package[MAX_PACKAGE_SIZE], int packetSize) {
    switch (packetSize) {
        case 1: //timings Request?
            // Serial.println("1 Byte parsing");
            if (bitRead(!package[0], 0) && bitRead(package[0], 1)) {
                hostObject->sendUpTimings();
            } else errorsCount++;
            break;
        case 2: //controlCommand?
            // Serial.println("2 Byte parsing");
            if (bitRead(package[0], 0) && !bitRead(package[0], 1)) {
                commandToControllerDevice(package);
            }
            errorsCount++;
            break;
        case 9: //newTimings?
            // Serial.println("9 Byte parsing");
            if (bitRead(package[0], 0) && bitRead(package[0], 1)) {
                newTimingsToControllerDevice(package);
            }
            errorsCount++;
            break;
        default:
            errorsCount++;
            //Serial.print("Errors: ");
            //Serial.println(errorsCount);
    }
}

void CommunicationUnit::newTimingsToControllerDevice(byte package[MAX_PACKAGE_SIZE]) {
    Timings newTimings{
            package[1] | (package[2] << 8), // bounce
            package[3] | (package[4] << 8), // repeater
            package[5] | (package[6] << 8), // front
            package[7] | (package[8] << 8)}; // rear
    hostObject->updateTimings(newTimings);
}

void CommunicationUnit::commandToControllerDevice(byte package[MAX_PACKAGE_SIZE]) {
    CameraStates cameraState = 0;
    bitWrite(cameraState, 0, bitRead(package[1], 1));
    bitWrite(cameraState, 1, bitRead(package[1], 2));
    ControlCommandSet command{
            bitRead(package[0], 2),
            bitRead(package[0], 3),
            bitRead(package[0], 4),
            bitRead(package[0], 5),
            bitRead(package[0], 6),
            bitRead(package[0], 7),
            bitRead(package[1], 0),
            cameraState};
    hostObject->executeCommand(command);
}

void CommunicationUnit::sendPackage(byte *packageToSend, int bytesToSend) {
    Serial.write(BORDER_OF_PACKAGE_SIGN);
    Serial.write(START_PACKAGE_SIGNATURE);
    for (int i = 0; i < bytesToSend; i++) {
        Serial.write(packageToSend[i]);
    }
    Serial.write(BORDER_OF_PACKAGE_SIGN);
    //TODO crc8 byte add
}

void CommunicationUnit::sendState(StateInfoSet stateSet) {
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
    sendPackage(packageToSend, 2);
}

void CommunicationUnit::sendTimings(Timings timings) {
    packageToSend[0] = 2;
    packageToSend[1] = timings.BOUNCE_DELAY;
    packageToSend[2] = (timings.BOUNCE_DELAY) >> 8;
    packageToSend[3] = timings.REPEATER_DELAY;
    packageToSend[4] = (timings.REPEATER_DELAY) >> 8;
    packageToSend[5] = timings.FRONT_CAM_SHOWTIME_DELAY;
    packageToSend[6] = (timings.FRONT_CAM_SHOWTIME_DELAY) >> 8;
    packageToSend[7] = timings.REAR_CAM_SHOWTIME_DELAY;
    packageToSend[8] = (timings.REAR_CAM_SHOWTIME_DELAY) >> 8;
    sendPackage(packageToSend, 9);
}

void CommunicationUnit::sendAdditionalInfo() {
    byte addInfo[3];
    addInfo[0] = 1; // sign of additional info
    addInfo[1] = errorsCount;
    addInfo[2] = errorsCount >> 8;
    sendPackage(addInfo, 3);
}
