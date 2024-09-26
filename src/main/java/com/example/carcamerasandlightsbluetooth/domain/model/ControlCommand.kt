package com.example.carcamerasandlightsbluetooth.domain.model

import com.example.carcamerasandlightsbluetooth.data.CameraState

data class ControlCommand(
    var cautionIsOn: Boolean,
    var leftFogIsOn: Boolean,
    var rightFogIsOn: Boolean,
    var relayIsOn: Boolean,
    var rearCameraIsOn: Boolean,
    var angelEyeIsOn: Boolean,
    var displayIsOn: Boolean,
    var cameraState: CameraState,
)