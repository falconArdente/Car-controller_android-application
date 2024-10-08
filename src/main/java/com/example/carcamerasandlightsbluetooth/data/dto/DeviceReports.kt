package com.example.carcamerasandlightsbluetooth.data.dto

import com.example.carcamerasandlightsbluetooth.domain.model.Timings

sealed class DeviceReports {
    data class TimingReport(
        val timings: Timings
    ) : DeviceReports()

    data class StateReport(
        val state: HardDeviceState
    ) : DeviceReports()

    data class AdditionalReport(
        val errorsCount: Int
    ) : DeviceReports()

    data object Error : DeviceReports()
}