package com.example.carcamerasandlightsbluetooth.data.map

import com.example.carcamerasandlightsbluetooth.domain.model.DeviceState
import com.example.carcamerasandlightsbluetooth.domain.model.Timings

sealed class DeviceReports {
    data class TimingReport(
        val timings: Timings
    ) : DeviceReports()

    data class StateReport(
        val state: DeviceState
    ) : DeviceReports()

    data object Error : DeviceReports()
}