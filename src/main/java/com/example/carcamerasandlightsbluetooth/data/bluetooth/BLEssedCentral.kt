package com.example.carcamerasandlightsbluetooth.data.bluetooth

import android.annotation.SuppressLint
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
import android.bluetooth.BluetoothGattService
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
import com.welie.blessed.supportsIndicate
import com.welie.blessed.supportsNotify
import com.welie.blessed.supportsReading
import com.welie.blessed.supportsWritingWithResponse
import com.welie.blessed.supportsWritingWithoutResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

const val START_PACKAGE_SIGNATURE = 's'
const val BORDER_OF_PACKAGE_SIGN = 'b'

class BLEssedCentral(
    private val context: Activity,
    private val serviceToFindUUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"),
    private val characteristicToFindUUID: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
) {
    enum class BleStatus {
        NOT_CONNECTED, CONNECTED, CONNECTED_NOTIFICATIONS
    }

    private var status: BleStatus = BleStatus.NOT_CONNECTED

    //        set(value) {
//            when (value) {
//                BleStatus.NOT_CONNECTED -> currentGattProfile = null
//                BleStatus.CONNECTED -> TODO()
//                BleStatus.CONNECTED_NOTIFICATIONS -> TODO()
//            }
//            field = value
//        }
    private var bleHandler = Handler(Looper.getMainLooper())
    private var scanJob: Job? = null
    private var discoverServicesRunnable: Runnable? = null
    private var controllerDevice: BluetoothDevice? = null
    private var adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var scanner: BluetoothLeScanner = adapter.bluetoothLeScanner
    private var currentGattProfile: BluetoothGatt? = null
    private var serviceToCommunicateWith: BluetoothGattService? = null
    private var characteristicToWriteTo: BluetoothGattCharacteristic? = null
    private var characteristicToNotifyOf: BluetoothGattCharacteristic? = null
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
                if (!results.isNullOrEmpty()) {
                    trySend(results.mapNotNull { scanResult ->
                        scanResult!!
                    }).isSuccess
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.d("BLEssed", "scan failed")
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
                if (!results.isNullOrEmpty()) {
                    trySend(results.mapNotNull { scanResult ->
                        scanResult!!
                    }).isSuccess
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.d("BLEssed", "scan failed")
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

            override fun onServicesDiscovered(gattProfile: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gattProfile, status)
                if (status == GATT_FAILURE) {
                    Log.d("BLEssed", "Service discovery failed")
                    gattProfile?.disconnect()
                }

                gattProfile!!.services.forEach { gattService ->
                    Log.d("BLEssed", "discovered ${gattService.uuid} ")
                    if (gattService.uuid == serviceToFindUUID) {
                        serviceToCommunicateWith = gattService
                        gattService.characteristics.forEach { characteristic ->
                            if (characteristic.uuid == characteristicToFindUUID) {
                                subscribeForNotifyAndWrite(gattProfile, characteristic)
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

            override fun onConnectionStateChange(
                gatt: BluetoothGatt, status: Int, newState: Int
            ) {
                if (status == GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        this@BLEssedCentral.status = BleStatus.CONNECTED
                        currentGattProfile = gatt
                        val bondState: Int? = controllerDevice?.getBondState()
                        if (bondState == BOND_NONE || bondState == BOND_BONDED) {
                            var delayWhenBonded = 0
                            val delay = if (bondState == BOND_BONDED) delayWhenBonded else 0
                            discoverServicesRunnable = Runnable {
                                Log.d("BLEssed", "discover ${gatt.device} delay $delay ms")
                                val result = gatt.discoverServices()
                                if (!result) {
                                    Log.e("BLEssed", "discoverServices failed to start")
                                }
                                discoverServicesRunnable = null
                            }
                            bleHandler.postDelayed(discoverServicesRunnable!!, delay.toLong())
                        } else if (bondState == BOND_BONDING) {
                            Log.d("BLEssed", "waiting for bonding to complete")
                        }
                    } else {
                        this@BLEssedCentral.status = BleStatus.NOT_CONNECTED
                        serviceToCommunicateWith = null
                        characteristicToNotifyOf = null
                        characteristicToWriteTo = null
                        Log.d("BLEssed", "${gatt.device.name} is no connected")
                        gatt.close()
                    }
                } else {
                    this@BLEssedCentral.status = BleStatus.NOT_CONNECTED
                    Log.d("BLEssed", "Failed to connect")
                    gatt.close()
                    gatt.disconnect()
                    gatt.connect()
                }
            }
        }
        controllerDevice = device
        controllerDevice!!.connectGatt(context, false, connectionStateCallback)
        awaitClose {
            stopScan()
        }
    }

    @SuppressLint("MissingPermission")
    fun sendBytes(data: ByteArray) {
        if (status == BleStatus.NOT_CONNECTED) return
        if (currentGattProfile == null && characteristicToWriteTo == null) return
        val bytesToSend: ByteArray = byteArrayOf(
            BORDER_OF_PACKAGE_SIGN.code.toByte(), START_PACKAGE_SIGNATURE.code.toByte()
        ) + data + BORDER_OF_PACKAGE_SIGN.code.toByte()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            currentGattProfile!!.writeCharacteristic(
                characteristicToWriteTo!!,
                bytesToSend,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            )
        } else {
            characteristicToWriteTo!!.setValue(bytesToSend)
            characteristicToWriteTo!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            currentGattProfile!!.writeCharacteristic(characteristicToWriteTo)
        }

        Log.d("BLEssed", "send ${bytesToSend.toList()}")

    }

    fun showGattContents(profile: BluetoothGatt): String {
        var outLog = ""
        profile.services.forEach { gattService ->
            outLog += "Discovered ${gattService.uuid} service:\n"
            gattService.characteristics.forEach { characteristic ->
                outLog += " Characteristic ${characteristic.uuid}\n  "
                if (characteristic.supportsReading()) outLog += "read |"
                if (characteristic.supportsWritingWithResponse()) outLog += "write with response |"
                if (characteristic.supportsWritingWithoutResponse()) outLog += "write NO response |"
                if (characteristic.supportsIndicate()) outLog += "indicate |"
                if (characteristic.supportsNotify()) outLog += "notify |"
                outLog += " \n"
            }
        }
        return outLog
    }

    fun subscribeForNotifyAndWrite(
        gattProfile: BluetoothGatt, characteristic: BluetoothGattCharacteristic
    ) {
        if (characteristic.supportsNotify()) {
            characteristicToNotifyOf = characteristic
            gattProfile.setCharacteristicNotification(characteristic, true)
        }
        if (characteristic.supportsWritingWithResponse() || characteristic.supportsWritingWithoutResponse()) {
            characteristicToWriteTo = characteristic
        }
    }
}