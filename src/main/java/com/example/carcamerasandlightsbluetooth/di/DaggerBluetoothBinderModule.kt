package com.example.carcamerasandlightsbluetooth.di

import com.example.carcamerasandlightsbluetooth.data.bluetooth.BluetoothRepositoryImpl
import com.example.carcamerasandlightsbluetooth.data.repository.BasicLightCamInteractor
import com.example.carcamerasandlightsbluetooth.data.repository.BluetoothRepository
import com.example.carcamerasandlightsbluetooth.domain.api.ControllerInteractor
import dagger.Binds
import dagger.Module

@Module()
abstract class DaggerBluetoothBinderModule {
    @Binds
    abstract fun interactorToUse(implementation: BasicLightCamInteractor): ControllerInteractor

    @Binds
    abstract fun repositoryToUse(implementation: BluetoothRepositoryImpl): BluetoothRepository
}