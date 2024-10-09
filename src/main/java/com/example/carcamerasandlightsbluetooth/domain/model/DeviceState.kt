package com.example.carcamerasandlightsbluetooth.domain.model

data class DeviceState(
    val connectionState: ConnectionState,
    val leftPressed: Boolean,
    val leftDblPressed: Boolean,
    val rightPressed: Boolean,
    val rightDblPressed: Boolean,
    val reversePressed: Boolean,
    val cautionIsOn: Boolean,
    val leftFogIsOn: Boolean,
    val rightFogIsOn: Boolean,
    val frontCameraIsShown: Boolean,
    val rightAngelEyeIsOn: Boolean,
    val leftAngelEyeIsOn: Boolean,
    val rearCameraIsShown: Boolean,
    val testModeIsOn: Boolean,
    val timings: Timings
) {
    enum class ConnectionState {
        NOT_CONNECTED,
        SCANNING,
        CONNECTED,
        CONNECTED_NOTIFIED,
    }

    companion object {
        val NOT_INITIALIZED = DeviceState(
            connectionState = ConnectionState.NOT_CONNECTED,
            leftPressed = false,
            leftDblPressed = false,
            rightPressed = false,
            rightDblPressed = false,
            reversePressed = false,
            cautionIsOn = false,
            leftFogIsOn = false,
            rightFogIsOn = false,
            frontCameraIsShown = false,
            rightAngelEyeIsOn = false,
            leftAngelEyeIsOn = false,
            rearCameraIsShown = false,
            testModeIsOn = false,
            timings = Timings.NOT_INITIALIZED
        )
    }

}

