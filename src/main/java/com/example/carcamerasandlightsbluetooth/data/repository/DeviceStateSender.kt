package com.example.carcamerasandlightsbluetooth.data.repository

import com.example.carcamerasandlightsbluetooth.data.dto.DeviceReports
import com.example.carcamerasandlightsbluetooth.domain.model.DeviceState

interface DeviceStateSender {
    fun send(deviceReport: DeviceReports)
    fun send(connectionState: DeviceState.ConnectionState)
}