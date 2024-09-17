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
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE
import android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_SIGNED
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
import com.welie.blessed.supportsNotify
import com.welie.blessed.supportsNotifyOrIndicate
import com.welie.blessed.supportsReading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale


class BLEssedCentral(private val context: Activity, private val scope: CoroutineScope) {
    enum class BleStatus {
        NOT_CONNECTED, CONNECTED, CONNECTED_NOTIFICATIONS
    }

    private var status: BleStatus = BleStatus.NOT_CONNECTED
    private var bleHandler = Handler(Looper.getMainLooper())
    private var scanJob: Job? = null
    private var discoverServicesRunnable: Runnable? = null
    private var controllerDevice: BluetoothDevice? = null
    private var adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var scanner: BluetoothLeScanner = adapter.bluetoothLeScanner
    private var currentGattProfile: BluetoothGatt? = null
    private var currentCharacteristic: BluetoothGattCharacteristic? = null
    private val scanSettings =
        ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT).setReportDelay(3L).build()

    fun getStatus() = status
    suspend fun startRawScan(): Flow<List<ScanResult>> = callbackFlow {
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
        scanJob?.cancel()
        scanner.startScan(null, scanSettings, scanCallback)
        awaitClose {
            stopScan()
        }
    }

    suspend fun startScanByAddress(macToScan: String): Flow<List<ScanResult>> = callbackFlow {
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
        scanJob?.cancel()
        scanner.startScan(null, scanSettings, scanCallback)
        awaitClose {
            stopScan()
        }
    }

    private fun getFilterByAddress(deviceAddress: String): ScanFilter {
        return ScanFilter.Builder().setDeviceAddress(deviceAddress).build()
    }

    fun stopScan() {
        scanJob?.cancel()
    }

    suspend fun connectTo(device: BluetoothDevice): Flow<ByteArray> = callbackFlow {
        val connectionStateCallback = object : BluetoothGattCallback() {

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                if (status == GATT_FAILURE) {
                    Log.d("BEL", "Service discovery failed")
                    gatt?.disconnect()
                }
                gatt!!.services.forEach { gattService ->
                    Log.d(
                        "BEL", "Discovered ${gattService.uuid} service:"
                    )
                    if (gattService.uuid.toString() == "0000ffe0-0000-1000-8000-00805f9b34fb") {
                        gattService.characteristics.forEach { characteristic ->
                            Log.d(
                                "BEL",
                                "characteristic ${characteristic.uuid} read ${characteristic.supportsReading()} : notify or indicate ${characteristic.supportsNotifyOrIndicate()} "
                            )
                            Log.d("BEL", "has properties ${characteristic.properties}")
                            if (characteristic.supportsNotify()) {
                                Log.d("BEL", "try notify to ${characteristic.uuid}")
                                gatt.setCharacteristicNotification(
                                    characteristic, true
                                )
                            }
                        }
                    }
                }

            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?
            ) {
                if (characteristic?.value != null) {
                    this@BLEssedCentral.status = BleStatus.CONNECTED_NOTIFICATIONS
                    trySend(characteristic.value!!)
                }
            }

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (status == GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        this@BLEssedCentral.status = BleStatus.CONNECTED
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
                            Log.d("BEL", "waiting for bonding to complete")
                        }
                    } else {
                        this@BLEssedCentral.status = BleStatus.NOT_CONNECTED
                        Log.d("BEL", "! STATE_CONNECTED")
                        gatt.close()
                    }
                } else {
                    this@BLEssedCentral.status = BleStatus.NOT_CONNECTED
                    Log.d("BEL", "Произошла ошибка... разбираемся, что случилось!")
                    gatt.close()
                    gatt.disconnect()
                    gatt.connect()
                }
            }
        }
        Log.d("BEL", "connecting to ${device.name}")
        controllerDevice = device
        controllerDevice!!.connectGatt(context, false, connectionStateCallback)
        awaitClose {
            stopScan()
        }
    }

    fun sendBytes(data: ByteArray) {
        if (status == BleStatus.NOT_CONNECTED) return



    }
}