package com.example.carcamerasandlightsbluetooth.data.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class BLEssedCentral {
    private var adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var scanner: BluetoothLeScanner = adapter.bluetoothLeScanner
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
        .setReportDelay(3L)
        .build()

    private var scanningJob: Job? = null
    suspend fun startRawScan(): Flow<List<ScanResult>> = callbackFlow {
        scanningJob?.cancel()
        val scanCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                trySend(listOf(result)).isSuccess
            }

            override fun onBatchScanResults(results: List<ScanResult?>?) {
                if (results != null && !results.isNullOrEmpty()) {
                    trySend(results.mapNotNull { scanResult ->
                        scanResult!!
                    }).isSuccess
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.d("BLEssedScann", "fail")
            }
        }
        scanningJob = launch {
            scanner.startScan(null, scanSettings, scanCallback)
        }
        awaitClose {
            scanningJob?.cancel()//  cleanup
        }
    }
}