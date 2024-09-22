package com.example.carcamerasandlightsbluetooth.data.bluetooth

import android.Manifest.permission
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
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.carcamerasandlightsbluetooth.R
import com.example.carcamerasandlightsbluetooth.utils.runWithPermissionCheck
import com.example.carcamerasandlightsbluetooth.utils.showAppPermissionsFrame
import com.welie.blessed.supportsIndicate
import com.welie.blessed.supportsNotify
import com.welie.blessed.supportsReading
import com.welie.blessed.supportsWritingWithResponse
import com.welie.blessed.supportsWritingWithoutResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID

const val START_PACKAGE_SIGNATURE = 's'
const val BORDER_OF_PACKAGE_SIGN = '\n'

class SimpleBleRepository(
    private val context: Context,
    private val serviceToFindUUID: UUID,
    private val characteristicToFindUUID: UUID
) {
    enum class ConnectionState {
        NOT_CONNECTED, CONNECTED, CONNECTED_NOTIFICATIONS
    }

    private var mutableConnectionStateFlow: MutableStateFlow<ConnectionState> =
        MutableStateFlow(ConnectionState.NOT_CONNECTED)
    var connectionStateFlow: StateFlow<ConnectionState> = mutableConnectionStateFlow
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
                Log.d("SimpleBle", "scan failed")
            }
        }
        runPermissionSafe(rational = context.getString(R.string.coarse_permission_rationale)) {
            runBlocking {
                scanJob?.cancel()
                scanJob = launch { scanner.startScan(null, scanSettings, scanCallback) }
            }
        }
        awaitClose {
            stopScan()
        }
    }

    suspend fun startScanByAddress(macToScan: String): Flow<List<ScanResult>> = callbackFlow {
        val scanCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                Log.d("SimpleBle", "scan single")
                trySend(listOf(result)).isSuccess
            }

            override fun onBatchScanResults(results: List<ScanResult?>?) {
                if (!results.isNullOrEmpty()) {
                    Log.d("SimpleBle", "scan Batch")
                    trySend(results.mapNotNull { scanResult ->
                        scanResult!!
                    }).isSuccess
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.d("SimpleBle", "scan failed $errorCode")
            }
        }
        runPermissionSafe(rational = context.getString(R.string.coarse_permission_rationale)) {
            runBlocking {
                scanJob?.cancel()
                scanJob = launch {
                    scanner.startScan(
                        listOf(getFilterByAddress(macToScan)),
                        scanSettings,
                        scanCallback
                    )
                }
            }
        }
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
        stopScan()
        val connectionStateCallback = object : BluetoothGattCallback() {

            override fun onServicesDiscovered(gattProfile: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gattProfile, status)

                if (status == GATT_FAILURE) {
                    Log.d("SimpleBle", "Service discovery failed")
                    gattProfile?.disconnect()
                }

                gattProfile!!.services.forEach { gattService ->
                    Log.d("SimpleBle", "discovered ${gattService.uuid} ")
                    if (gattService.uuid == serviceToFindUUID) {
                        serviceToCommunicateWith = gattService
                        gattService.characteristics.forEach { characteristic ->
                            if (characteristic.uuid == characteristicToFindUUID) {
                                runBlocking {
                                    subscribeForNotifyAndWrite(
                                        gattProfile,
                                        characteristic
                                    )
                                }
                            }
                        }
                    }
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?
            ) {
                if (characteristic?.value != null) {
                    trySend(characteristic.value!!)
                }
            }

            override fun onConnectionStateChange(
                gatt: BluetoothGatt, status: Int, newState: Int
            ) {
                if (status == GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        mutableConnectionStateFlow.value = ConnectionState.CONNECTED
                        currentGattProfile = gatt
                        val bondState: Int? = controllerDevice?.getBondState()
                        if (bondState == BOND_NONE || bondState == BOND_BONDED) {
                            val delayWhenBonded = 0
                            val delay = if (bondState == BOND_BONDED) delayWhenBonded else 300L
                            discoverServicesRunnable = Runnable {
                                Log.d("SimpleBle", "discover ${gatt.device} delay $delay ms")
                                val result = gatt.discoverServices()
                                if (!result) {
                                    Log.e("SimpleBle", "discoverServices failed to start")
                                }
                            }
                            bleHandler.postDelayed(discoverServicesRunnable!!, delay.toLong())
                        } else if (bondState == BOND_BONDING) {
                            Log.d("SimpleBle", "waiting for bonding to complete")
                        }
                    } else {
                        mutableConnectionStateFlow.value = ConnectionState.NOT_CONNECTED
                        serviceToCommunicateWith = null
                        characteristicToNotifyOf = null
                        characteristicToWriteTo = null
                        Log.d("SimpleBle", "${gatt.device.name} is no connected now")
                        gatt.close()
                    }
                } else {
                    mutableConnectionStateFlow.value = ConnectionState.NOT_CONNECTED
                    Log.d("SimpleBle", "Failed to connect $status")
                    gatt.close()
                    gatt.disconnect()
                    gatt.connect()
                }
            }
        }
        runPermissionSafe {
            runBlocking {
                controllerDevice = device
                currentGattProfile =
                    controllerDevice!!.connectGatt(
                        context,
                        false,
                        connectionStateCallback
                    )
            }
        }
        awaitClose {
            stopScan()
        }
    }

    fun sendBytes(data: ByteArray) {
        if (mutableConnectionStateFlow.value == ConnectionState.NOT_CONNECTED) return
        if (currentGattProfile == null && characteristicToWriteTo == null) return
        val bytesToSend: ByteArray = byteArrayOf(
            BORDER_OF_PACKAGE_SIGN.code.toByte(), START_PACKAGE_SIGNATURE.code.toByte()
        ) + data + BORDER_OF_PACKAGE_SIGN.code.toByte()
        characteristicToWriteTo!!.setValue(bytesToSend)
        characteristicToWriteTo!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        currentGattProfile!!.writeCharacteristic(characteristicToWriteTo)
        Log.d("SimpleBle", "send ${bytesToSend.toList()}")
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


    suspend fun subscribeForNotifyAndWrite(
        gattProfile: BluetoothGatt, characteristic: BluetoothGattCharacteristic
    ) {
        var subscribed = false
        runPermissionSafe(rational = context.getString(R.string.coarse_permission_rationale_notifications)) {
            runBlocking {
                if (characteristic.supportsNotify()) {
                    subscribed = gattProfile.setCharacteristicNotification(characteristic, true)
                }
            }
            if (subscribed) mutableConnectionStateFlow.value = ConnectionState.CONNECTED_NOTIFICATIONS

            if (characteristic.supportsWritingWithResponse() || characteristic.supportsWritingWithoutResponse()) {
                characteristicToWriteTo = characteristic
            }
        }
    }

    fun onDestroy() {
        currentGattProfile?.disconnect()
        stopScan()
        scanJob?.cancel()
    }

    private suspend fun runPermissionSafe(
        rational: String = "",
        action: () -> Unit
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                runWithPermissionCheck(
                    run(action),
                    permission.BLUETOOTH_CONNECT,
                    context
                )
            } else {  // old versions
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    showAppPermissionsFrame(
                        context,
                        rational
                    )
                } else {// all permissions are on
                    run(action)
                }
            }
        }
    }
}