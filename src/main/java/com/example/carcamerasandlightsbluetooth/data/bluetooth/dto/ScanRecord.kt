package com.example.carcamerasandlightsbluetooth.data.bluetooth.dto

data class ScanRecord(
    val advertiseFlag: Int,
    val mServiceUuids: List<String>,
    val powerLevel: Int,
    val deviceName: String,
)
