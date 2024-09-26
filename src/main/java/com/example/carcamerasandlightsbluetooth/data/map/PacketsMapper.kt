package com.example.carcamerasandlightsbluetooth.data.map

import com.example.carcamerasandlightsbluetooth.data.dto.DeviceReports
import com.example.carcamerasandlightsbluetooth.domain.model.DeviceState

object PacketsMapper {
    fun toReport(data: ByteArray): DeviceReports {
        //TODO parsing
        return DeviceReports.StateReport(
            state = DeviceState.NOT_INITIALIZED
        )
    }

    fun combineReportWithState(
        stateReport: DeviceReports.StateReport,
        deviceState: DeviceState
    ): DeviceState {
        with(stateReport.state) {
            return deviceState.copy(
                leftPressed = leftPressed,
                leftDblPressed = leftDblPressed,
                rightPressed = rightPressed,
                rightDblPressed = rightDblPressed,
                reversePressed = reversePressed,
                cautionIsOn = cautionIsOn,
                leftFogIsOn = leftFogIsOn,
                rightFogIsOn = rightFogIsOn,
                frontCameraIsShown = frontCameraIsShown,
                rearCameraIsOn = rearCameraIsOn,
                angelEyeIsOn = angelEyeIsOn,
                displayIsOn = displayIsOn
            )
        }
    }
}