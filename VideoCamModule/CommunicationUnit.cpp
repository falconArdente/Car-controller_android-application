#include "CommunicationUnit.h"
#include "CameraStatesEnum.h"

CommunicationUnit::StateInfo::StateInfo(
        bool leftPressed,
        bool leftDblPressed,
        bool rightPressed,
        bool rightDblPressed,
        bool reversePressed,
        bool cautionIsOn,
        bool leftFogIsOn,
        bool rightFogIsOn,
        bool relayIsOn,
        bool RearCameraIsOn,
        bool frontCameraIsOn,
        bool displayIsOn,
        CameraStates cameraState
) {
    bitWrite(package[0], 0, 0);
    bitWrite(package[0], 1, 0);
    bitWrite(package[0], 2, leftPressed);
    bitWrite(package[0], 3, leftDblPressed);
    bitWrite(package[0], 4, rightPressed);
    bitWrite(package[0], 5, rightDblPressed);
    bitWrite(package[0], 6, reversePressed);
    bitWrite(package[0], 7, cautionIsOn);
    bitWrite(package[1], 0, leftFogIsOn);
    bitWrite(package[1], 1, rightFogIsOn);
    bitWrite(package[1], 2, relayIsOn);
    bitWrite(package[1], 3, RearCameraIsOn);
    bitWrite(package[1], 4, frontCameraIsOn);
    bitWrite(package[1], 5, displayIsOn);
    bitWrite(package[1], 6, bitRead(cameraState, 0));
    bitWrite(package[1], 7, bitRead(cameraState, 1));
}

CommunicationUnit::TimingsPackage::TimingsPackage(bool isToWrite, Timings timings) {
    if (isToWrite) {
        package[0] = 3;
    } else {
        package[0] = 2;
    }
    package[1] = timings.BOUNCE_DELAY;
    package[2] = (timings.BOUNCE_DELAY) >> 8;
}
