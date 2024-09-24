package com.example.carcamerasandlightsbluetooth.di

import com.example.carcamerasandlightsbluetooth.data.bluetooth.SimpleBleConnectedController
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import java.util.UUID

val bluetoothModule = module {

    single<SimpleBleConnectedController> {
        SimpleBleConnectedController(
            androidApplication(),
            serviceToFindUUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"),
            characteristicToFindUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
        )
    }
}