package com.example.carcamerasandlightsbluetooth.di

import android.content.Context
import com.example.carcamerasandlightsbluetooth.data.bluetooth.BluetoothRepositoryImpl
import com.example.carcamerasandlightsbluetooth.data.bluetooth.SimpleBleConnectedController
import com.example.carcamerasandlightsbluetooth.domain.api.ControllerInteractor
import com.example.carcamerasandlightsbluetooth.presentation.RootViewModel
import dagger.Module
import dagger.Provides
import java.util.UUID
import javax.inject.Singleton

@Module
class DaggerBluetoothProviderModule {
    @Singleton
    @Provides
    fun provideSimpleBle(context: Context): SimpleBleConnectedController {
        return SimpleBleConnectedController(
            context = context,
            serviceToFindUUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"),
            characteristicToFindUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
        )
    }

    @Singleton
    @Provides
    fun provideBluetoothRepositoryImpl(context: Context): BluetoothRepositoryImpl {
        return BluetoothRepositoryImpl(
            communicator = provideSimpleBle(context),
            defaultMAC = "00:15:A5:02:0A:24",
            context = context
        )
    }

    @Singleton
    @Provides
    fun provideViewModel(
        deviceInteractor: ControllerInteractor
    ): RootViewModel {
        return RootViewModel(deviceInteractor)
    }
}