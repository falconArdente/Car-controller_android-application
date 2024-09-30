package com.example.carcamerasandlightsbluetooth.di

import com.example.carcamerasandlightsbluetooth.data.bluetooth.BluetoothRepositoryImpl
import com.example.carcamerasandlightsbluetooth.data.bluetooth.SimpleBleConnectedController
import com.example.carcamerasandlightsbluetooth.data.repository.BasicLightCamInteractor
import com.example.carcamerasandlightsbluetooth.data.repository.BluetoothRepository
import com.example.carcamerasandlightsbluetooth.domain.api.ControllerInteractor
import com.example.carcamerasandlightsbluetooth.presentation.RootViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.util.UUID

val bluetoothModule = module {
    viewModel<RootViewModel> {
        RootViewModel(deviceInteractor = get(), androidContext())
    }
    single<SimpleBleConnectedController> {
        SimpleBleConnectedController(
            androidApplication(),
            serviceToFindUUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"),
            characteristicToFindUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
        )
    }
    factory<BluetoothRepository> {
        BluetoothRepositoryImpl(
            communicator = get(),
            defaultMAC = "00:15:A5:02:0A:24",
            androidApplication()
        )
    }
    factory<ControllerInteractor> {
        BasicLightCamInteractor(get())
    }
}