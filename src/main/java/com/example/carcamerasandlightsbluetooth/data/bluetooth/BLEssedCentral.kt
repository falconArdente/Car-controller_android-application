package com.example.carcamerasandlightsbluetooth.data.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class BLEssedCentral {
    private var scanCallback: ScanCallback?=null
    private var adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var scanner: BluetoothLeScanner = adapter.bluetoothLeScanner
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
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
                Log.d("BLEssedScan", "fail")
            }
        }
        scanningJob = launch {
            Log.d("BEL", "launchScan")
            scanner.startScan(null, scanSettings, scanCallback)
        }
        awaitClose {
            scanningJob?.cancel()//  cleanup
        }
    }

    suspend fun startScanByName(nameToScan: String): Flow<List<ScanResult>> = callbackFlow {
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
            scanner.startScan(listOf(getFilterByName(nameToScan)), scanSettings, scanCallback)
        }
        awaitClose {
            scanningJob?.cancel()//  cleanup
        }
    }

    suspend fun startScanByAddress(macToScan: String): Flow<List<ScanResult>> = callbackFlow {
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
            scanner.startScan(listOf(getFilterByAddress(macToScan)), scanSettings, scanCallback)
        }
        awaitClose {
            scanningJob?.cancel()//  cleanup
        }
    }

    private fun getFilterByName(deviceName: String): ScanFilter {
        return ScanFilter.Builder()
            .setDeviceName(deviceName)
            .build()
    }

    private fun getFilterByAddress(deviceAddress: String): ScanFilter {
        return ScanFilter.Builder()
            .setDeviceAddress(deviceAddress)
            .build()
    }
    fun stopScan(){
        scanningJob?.cancel()
        scanner.stopScan(scanCallback)
    }
}