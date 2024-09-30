package com.example.carcamerasandlightsbluetooth.data.repository

import com.example.carcamerasandlightsbluetooth.domain.api.ControllerInteractor
import com.example.carcamerasandlightsbluetooth.domain.model.ControlCommand
import com.example.carcamerasandlightsbluetooth.domain.model.DeviceState
import com.example.carcamerasandlightsbluetooth.domain.model.Timings
import kotlinx.coroutines.flow.Flow

class BasicLightCamInteractor(private val repository: BluetoothRepository) : ControllerInteractor {
    override fun getStateFlow(): Flow<DeviceState> {
        return repository.getStateFlow()
    }

    override fun sendCommand(command: ControlCommand) {
        repository.sendCommand(command)
    }

    override fun switchToTestMode(testIsOn: Boolean) {
        repository.switchToTestMode(testIsOn)
    }

    override fun requestTimings() {
        repository.requestTimings()
    }

    override fun sendTimings(newTimings: Timings) {
        repository.sendTimings(newTimings)
    }

    override suspend fun scanForDevice() {
        repository.scanForDevice()
    }

    override fun stopScan() {
        repository.stopScan()
    }

    override fun getServiceDataFlow(): Flow<String> {
        return repository.getServiceDataFlow()
    }

    override fun getErrorsCountFlow(): Flow<Int> {
        return repository.getErrorsCountFlow()
    }

    override fun finish() {
        repository.finish()
    }
}