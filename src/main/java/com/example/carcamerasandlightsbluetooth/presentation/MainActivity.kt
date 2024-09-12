package com.example.carcamerasandlightsbluetooth.presentation

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.carcamerasandlightsbluetooth.R
import com.example.carcamerasandlightsbluetooth.data.bluetooth.BLEssedCentral
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private var countOfConnectedDevices:Int=0

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
            .collect { listItem ->
                if (!listItem.isNullOrEmpty()) {
                    var device: BluetoothDevice? = null
                    listItem.forEach { scannedResult ->

                        Log.d("BEL", listItem.toString())
                        if (scannedResult.device.address == "00:15:A5:02:0A:24") {
                            device = scannedResult.device
                        }
                        device?.let {
                            blessed.stopScan()
                            connectTo(it)
                        }
                    }
                }
            }
    }

    private val connectionStateCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BEL", "connected to ${gatt.device}")
                lifecycleScope.launch {
                    Log.d("BEL", "starting searching descovery")
                    gatt.discoverServices()
                    delay(1200L)
                    gatt.services.forEach {
                        Log.d("BEL", "service ${it.uuid}")
                    }
                }

            } else {
                gatt.close()
            }
        }
    }

    private suspend fun connectTo(device: BluetoothDevice) {
        Log.d("BEL", "connecting to ${device.name}")

        val gatt: BluetoothGatt =
            device.connectGatt(this@MainActivity, false, connectionStateCallback)
    }
}