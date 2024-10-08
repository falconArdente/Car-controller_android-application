package com.example.carcamerasandlightsbluetooth.data.dto

import com.example.carcamerasandlightsbluetooth.data.CameraState

data class HardDeviceState(
    val leftPressed: Boolean,
    val leftDblPressed: Boolean,
    val rightPressed: Boolean,
    val rightDblPressed: Boolean,
    val reversePressed: Boolean,
    val cautionIsOn: Boolean,
    val leftFogIsOn: Boolean,
    val rightFogIsOn: Boolean,
    val relayIsOn: Boolean,
    val rightAngelEyeIsOn: Boolean,
    val leftAngelEyeIsOn: Boolean,
    val displayIsOn: Boolean,
    val cameraState: CameraState,
)
