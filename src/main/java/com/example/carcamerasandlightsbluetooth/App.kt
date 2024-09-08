package com.example.carcamerasandlightsbluetooth

import android.app.Application
import com.example.carcamerasandlightsbluetooth.di.bluetoothModule
import com.markodevcic.peko.PermissionRequester
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        GlobalContext.startKoin {
            androidContext(this@App)
            modules(bluetoothModule)
        }
        PermissionRequester.initialize(applicationContext)
    }
}