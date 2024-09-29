package com.example.carcamerasandlightsbluetooth.data.repository

import com.example.carcamerasandlightsbluetooth.domain.model.ControlCommand
import com.example.carcamerasandlightsbluetooth.domain.model.DeviceState
import com.example.carcamerasandlightsbluetooth.domain.model.Timings
import kotlinx.coroutines.flow.Flow

interface BluetoothRepository {
    fun getState(): Flow<DeviceState>
    fun sendCommand(command: ControlCommand)
    fun requestTimings()
    fun sendTimings(newTimings: Timings)
    suspend fun scanForDevice()
    fun stopScan()
    suspend fun getServiceDataFlow(): Flow<String>
    val serviceFlow: Flow<String>
}