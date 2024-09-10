package com.example.carcamerasandlightsbluetooth.presentation

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.carcamerasandlightsbluetooth.R
import com.example.carcamerasandlightsbluetooth.data.bluetooth.BLEssedCentral
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleScope.launch {
            doScanStuff2()
        }
    }

    private suspend fun doScanStuff2() {
        val blessed = BLEssedCentral()
        blessed.startRawScan()
            .collect {
                if (!it.isNullOrEmpty()) {
                    Log.d("BEL", it.toString())
                }
            }
    }
}