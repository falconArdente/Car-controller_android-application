package com.example.carcamerasandlightsbluetooth.data.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.bluetooth.BluetoothDevice.BOND_BONDING
import android.bluetooth.BluetoothDevice.BOND_NONE
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGatt.GATT_FAILURE
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class BLEssedCentral(private val context: Activity, private val scope: CoroutineScope) {
    private var bleHandler = Handler(Looper.getMainLooper())
    private var scanJob: Job? = null
    private var discoverServicesRunnable: Runnable? = null
    private var controllerDevice: BluetoothDevice? = null
    private var adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var scanner: BluetoothLeScanner = adapter.bluetoothLeScanner
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
        .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
        .setReportDelay(3L)
        .build()
    private var scanningRunnable: Runnable? = null

    suspend fun startRawScan(): Flow<List<ScanResult>> = callbackFlow {
        scanJob?.let { scanJob!!.cancel() }
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
        scanner.startScan(null, scanSettings, scanCallback)
        awaitClose {
            stopScan()
        }
    }

    suspend fun startScanByName(nameToScan: String): Flow<List<ScanResult>> = callbackFlow {
        scanJob?.let { scanJob!!.cancel() }
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
        scanner.startScan(null, scanSettings, scanCallback)
        awaitClose {
            stopScan()
        }
    }

    suspend fun startScanByAddress(macToScan: String): Flow<List<ScanResult>> = callbackFlow {
        scanJob?.let { scanJob!!.cancel() }
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
        scanner.startScan(null, scanSettings, scanCallback)
        awaitClose {
            stopScan()
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

    fun stopScan() {
        scanJob?.let { scanJob!!.cancel() }
    }

    private val connectionStateCallback = object : BluetoothGattCallback() {
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == GATT_FAILURE) {
                Log.d("BEL", "Service discovery failed");
                gatt?.disconnect()
                return
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            Log.d("BEL", "got Read $characteristic with value $value")
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    val bondstate: Int? = controllerDevice?.getBondState()

                    // Обрабатываем bondState
                    if (bondstate == BOND_NONE || bondstate == BOND_BONDED) {
                        // Подключились к устройству, вызываем discoverServices с задержкой
                        var delayWhenBonded = 0
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                            delayWhenBonded = 1000
                        }
                        val delay = if (bondstate == BOND_BONDED) delayWhenBonded else 0
                        discoverServicesRunnable = Runnable {
                            Log.d(
                                "BEL",
                                "discovering services of ${gatt.device} with delay of $delay ms"
                            )
                            val result = gatt.discoverServices()
                            if (!result) {
                                Log.e("BEL", "discoverServices failed to start")
                            }
                            discoverServicesRunnable = null
                        }
                        bleHandler?.postDelayed(discoverServicesRunnable!!, delay.toLong())
                    } else if (bondstate == BOND_BONDING) {
                        // Bonding в процессе, ждем когда закончится
                        Log.i("BEL", "waiting for bonding to complete")
                    }
                } else {
                    gatt.close()
                }
            } else {
                // Произошла ошибка... разбираемся, что случилось!

                gatt.close();
            }
        }
    }

    suspend fun connectTo(device: BluetoothDevice): Int {
        Log.d("BEL", "connecting to ${device.name}")
        controllerDevice = device
        val gatt: BluetoothGatt =
            controllerDevice!!.connectGatt(context, false, connectionStateCallback)
        return 0
    }

}