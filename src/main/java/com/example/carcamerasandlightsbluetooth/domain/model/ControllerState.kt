package com.example.carcamerasandlightsbluetooth.domain.model

data class ControllerState(
    val connectionState: ConnectionState,
    val leftPressed: Boolean,
    val leftDblPressed: Boolean,
    val rightPressed: Boolean,
    val rightDblPressed: Boolean,
    val reversePressed: Boolean,
    val cautionIsOn: Boolean,
    val leftFogIsOn: Boolean,
    val rightFogIsOn: Boolean,
    val relayIsOn: Boolean,
    val rearCameraIsOn: Boolean,
    val angelEyeIsOn: Boolean,
    val displayIsOn: Boolean,
    val cameraState: CameraState,
) {
    enum class ConnectionState {
        NOT_CONNECTED,
        SCANNING,
        CONNECTED,
        CONNECTED_NOTIFIED,
    }
}