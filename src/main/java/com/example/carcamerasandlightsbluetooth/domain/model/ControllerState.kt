package com.example.carcamerasandlightsbluetooth.domain.model

data class ControllerState(
    var leftPressed: Boolean,
    var leftDblPressed: Boolean,
    var rightPressed: Boolean,
    var rightDblPressed: Boolean,
    var reversePressed: Boolean,
    var cautionIsOn: Boolean,
    var leftFogIsOn: Boolean,
    var rightFogIsOn: Boolean,
    var relayIsOn: Boolean,
    var rearCameraIsOn: Boolean,
    var angelEyeIsOn: Boolean,
    var displayIsOn: Boolean,
    var cameraState: CameraState,
)