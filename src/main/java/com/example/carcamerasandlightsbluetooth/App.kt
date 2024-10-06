package com.example.carcamerasandlightsbluetooth

import android.app.Application
import com.example.carcamerasandlightsbluetooth.di.AppComponent
import com.example.carcamerasandlightsbluetooth.di.DaggerAppComponent
import com.markodevcic.peko.PermissionRequester

open class App : Application() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        PermissionRequester.initialize(applicationContext)
    }
}