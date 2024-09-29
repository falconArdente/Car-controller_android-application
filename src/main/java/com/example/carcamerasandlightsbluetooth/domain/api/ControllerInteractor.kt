package com.example.carcamerasandlightsbluetooth.domain.api

import com.example.carcamerasandlightsbluetooth.domain.model.ControlCommand
import com.example.carcamerasandlightsbluetooth.domain.model.DeviceState
import com.example.carcamerasandlightsbluetooth.domain.model.Timings
import kotlinx.coroutines.flow.Flow

interface ControllerInteractor {

    fun getStateFlow(): Flow<DeviceState>
    fun sendCommand(command: ControlCommand)
    fun switchToTestMode(testIsOn: Boolean)
    fun requestTimings()
    fun sendTimings(newTimings: Timings)
    fun scanForDevice()
    fun stopScan()
    fun getServiceDataFlow(): Flow<String>
}