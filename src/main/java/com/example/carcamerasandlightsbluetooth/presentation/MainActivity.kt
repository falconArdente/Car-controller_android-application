package com.example.carcamerasandlightsbluetooth.presentation

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.carcamerasandlightsbluetooth.R
import com.example.carcamerasandlightsbluetooth.data.bluetooth.SimpleBleConnectedController
import com.example.carcamerasandlightsbluetooth.data.bluetooth.SimpleBleConnectedController.ConnectionState
import com.example.carcamerasandlightsbluetooth.data.repository.BluetoothRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {

    private var countOfConnectedDevices: Int = 0
    private var scanJob: Job? = null
    private var sendJob: Job? = null
    private val blessed: SimpleBleConnectedController by inject<SimpleBleConnectedController>()
    private val repo: BluetoothRepository by inject()
    private var handler: Handler? = null
    private var switch = true
    override fun onDestroy() {
        super.onDestroy()
        blessed.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(mainLooper)
        setContentView(R.layout.activity_main)
        lifecycleScope.launch(Dispatchers.IO) {
            blessed.connectionStateFlow.collect {
                //parseNewConnectionState(it)
            }
        }
        doScanStuff3()
    }

    private fun doScanStuff3() {
        Log.d("repository", "Launch It")
        lifecycleScope.launch {
            launch {
                repo.getServiceDataFlow()
                    .collect { string ->
                        Log.d("repository", "service: $string")

                    }
            }
            //delay(3000L)
            repo.scanForDevice()
        }
//        lifecycleScope.launch {
//            launch {
//                repo.getServiceDataFlow()
//                    .collect { string ->
//                        Log.d("repository", "INIT subcr $string")
//                    }
//            }
//
//            var counter = 0
//            while (true) {
//                delay(15000L)
//                Log.d("repository", "go for service $counter")
//                launch {
//                    repo.getServiceDataFlow()
//                        .collect { string ->
//
//                            Log.d("repository", "at $counter flow $string")
//                        }
//                }
//                Log.d("repository", "subscribed service on $counter")
//                counter++
//            }
//        }
    }

    private fun doScanStuff2() {
        scanJob = lifecycleScope.launch(Dispatchers.IO) {
            Log.d("SimpleBle", "new Scan run")
            blessed.startScanByAddress("00:15:A5:02:0A:24")
                .collect { listItem ->
                    Log.d("SimpleBle", "collecting")
                    if (!listItem.isNullOrEmpty()) {
                        listItem.forEach { device ->
                            Log.d("SimpleBle", listItem.toString())
                            if (device.address == "00:15:A5:02:0A:24") {
                                blessed.stopScan()
                                Log.d("SimpleBle", "connecting")
                                if (countOfConnectedDevices == 0) connectTo(device)
                                countOfConnectedDevices++
                                coroutineContext.cancel()
                            }
                        }
                    }
                }
        }
    }

    private fun parseNewConnectionState(state: ConnectionState) {
        when (state) {
            ConnectionState.CONNECTED -> {
                Log.d("SimpleBle", "A connected")
                senderLauncher()
            }

            ConnectionState.NOT_CONNECTED, ConnectionState.SCANNING -> Log.d(
                "SimpleBle",
                "A NOT con"
            )

            ConnectionState.CONNECTED_NOTIFICATIONS -> Log.d(
                "SimpleBle",
                "A CON with NOTIFY"
            )
        }
    }

    private var sender = Runnable {
        if (switch) {
            blessed.sendBytes(byteArrayOf(1, 1))
            switch = false
        } else {
            blessed.sendBytes(byteArrayOf(1, 2))
            switch = true
        }
        senderLauncher()
    }

    private fun senderLauncher() {
        handler?.removeCallbacks(sender)
        handler?.postDelayed(sender, 3000L)
    }

    private fun connectTo(device: BluetoothDevice) {

        lifecycleScope.launch(Dispatchers.IO) {
            blessed.connectTo(device)
                .collect { result ->
                    Log.d("SimpleBle", (result.data as ByteArray).toList().toString())
                }
        }
    }
}