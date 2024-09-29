package com.example.carcamerasandlightsbluetooth.data.repository

import com.example.carcamerasandlightsbluetooth.domain.api.ControllerInteractor
import com.example.carcamerasandlightsbluetooth.domain.model.ControlCommand
import com.example.carcamerasandlightsbluetooth.domain.model.DeviceState
import com.example.carcamerasandlightsbluetooth.domain.model.Timings
import kotlinx.coroutines.flow.Flow

class BasicLightCamInteractor: ControllerInteractor {
    override fun getStateFlow(): Flow <DeviceState> {
        TODO("Not yet implemented")
    }

    override fun sendCommand(command: ControlCommand) {
        TODO("Not yet implemented")
    }

    override fun switchToTestMode(testIsOn: Boolean) {
        TODO("Not yet implemented")
    }

    override fun requestTimings() {
        TODO("Not yet implemented")
    }

    override fun sendTimings(newTimings: Timings) {
        TODO("Not yet implemented")
    }

    override fun scanForDevice() {
        TODO("Not yet implemented")
    }

    override fun stopScan() {
        TODO("Not yet implemented")
    }

    override fun getServiceDataFlow(): Flow<String> {
        TODO("Not yet implemented")
    }
}