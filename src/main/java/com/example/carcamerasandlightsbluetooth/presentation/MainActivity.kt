package com.example.carcamerasandlightsbluetooth.presentation

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.carcamerasandlightsbluetooth.R
import java.util.UUID


class MainActivity : AppCompatActivity() {
    var adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    var scanner: BluetoothLeScanner = adapter.bluetoothLeScanner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        doScanStuff()
    }

    private fun doScanStuff() {
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .setReportDelay(0L)
            .build()
        val scanCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device: BluetoothDevice = result.getDevice()
                // ...do whatever you want with this found device
            }

            override fun onBatchScanResults(results: List<ScanResult?>?) {
                // Ignore for now
            }

            override fun onScanFailed(errorCode: Int) {
                // Ignore for now
            }
        }

        val BLP_SERVICE_UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb")
        val serviceUUIDs = arrayOf(BLP_SERVICE_UUID)
        var filters: MutableList<ScanFilter?>? = null
        if (serviceUUIDs != null) {
            filters = ArrayList()
            for (serviceUUID in serviceUUIDs) {
                val filter = ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(serviceUUID))
                    .build()
                filters.add(filter)
            }
        }

        if (scanner != null) {
            scanner.startScan(filters, scanSettings, scanCallback);
            Log.d(TAG, "scan started");
        } else {
            Log.e(TAG, "could not get scanner object");
        }

        //scanner.startScan(filters, scanSettings, scanCallback)
    }
}