package com.example.carcamerasandlightsbluetooth.presentation

import com.example.carcamerasandlightsbluetooth.domain.model.DeviceState

data class MainState(
    val deviceState: DeviceState,
    val isLocked: Boolean,
    val isSetTimings: Boolean
)
