package com.example.carcamerasandlightsbluetooth.di

import android.content.Context
import com.example.carcamerasandlightsbluetooth.presentation.MainActivity
import dagger.BindsInstance
import dagger.Component
import dagger.Provides
import javax.inject.Singleton

@Singleton
@Component(modules = [DaggerBluetoothBinderModule::class, DaggerBluetoothProviderModule::class])
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun inject(activity: MainActivity)
}