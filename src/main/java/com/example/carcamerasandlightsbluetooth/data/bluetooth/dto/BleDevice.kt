package com.example.carcamerasandlightsbluetooth.data.bluetooth.dto

import com.welie.blessed.PhyType

data class BleDevice(
    val mac:String,
    val scanRecord: ScanRecord,
    val rssi:Int,
    val primaryPhyType: PhyType,
    val secondaryPhyType: PhyType,
    val txPower:Int,
    val advertisingId:Int,
    val advertisingInterval:Int,
)
