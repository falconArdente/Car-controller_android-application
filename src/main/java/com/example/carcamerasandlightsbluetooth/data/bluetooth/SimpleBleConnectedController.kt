@file:Suppress("DEPRECATION", "DeprecatedCallableAddReplaceWith")

package com.example.carcamerasandlightsbluetooth.data.bluetooth

import android.Manifest.permission
import android.annotation.SuppressLint
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
import com.example.carcamerasandlightsbluetooth.utils.Result
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

@SuppressLint("LogNotTimber")
class SimpleBleConnectedController(
    private val context: Context,
    private val serviceToFindUUID: UUID,
    private val characteristicToFindUUID: UUID,
) {
    enum class ConnectionState {
        NOT_CONNECTED, SCANNING, CONNECTED_NOTIFICATIONS, CONNECTED,
    }

    private val serviceDiscovered = context.getString(R.string.service_discovered)
    private val serviceDiscoveryFailed = context.getString(R.string.service_discovery_failed)
    private val discoverService = context.getString(R.string.discover_service)
    private val delay = context.getString(R.string.delay)
    private val discoverServiceFailedToStart =
        context.getString(R.string.discover_service_failed_to_start)
    private val isNotConnectedNow = context.getString(R.string.is_not_connected_now)
    private val failedToConnect = context.getString(R.string.failed_to_connect)
    private val scanFailedWithError = context.getString(R.string.scan_failed_with_error)
    private val waitingBondingCompletion = context.getString(R.string.waiting_bonding_completion)

    private var mutableConnectionStateFlow: MutableStateFlow<ConnectionState> =
        MutableStateFlow(ConnectionState.NOT_CONNECTED)
    var connectionStateFlow: StateFlow<ConnectionState> = mutableConnectionStateFlow
    private var previousConnectionState = ConnectionState.NOT_CONNECTED
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

    @SuppressLint("MissingPermission")
    suspend fun startRawScan(): Flow<Result<List<BluetoothDevice>>> = callbackFlow {
        val scanCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                trySend(Result.Success(listOf(result.device))).isSuccess
            }

            override fun onBatchScanResults(results: List<ScanResult?>?) {
                if (!results.isNullOrEmpty()) {
                    trySend(
                        Result.Success(
                            results.mapNotNull { scanResult ->
                                scanResult!!.device
                            })
                    ).isSuccess
                }
            }

            override fun onScanFailed(errorCode: Int) {
                trySend(Result.Error(scanFailedWithError, errorCode)).isSuccess
            }
        }
        runPermissionSafe(rational = context.getString(R.string.coarse_permission_rationale)) {
            scanJob?.cancel()
            scanJob = launch {
                previousConnectionState = mutableConnectionStateFlow.value
                mutableConnectionStateFlow.value = ConnectionState.SCANNING
                scanner.startScan(null, scanSettings, scanCallback)
            }
        }
        awaitClose {
            stopScan()
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun startScanByAddress(macToScan: String): Flow<Result<List<BluetoothDevice>>> =
        callbackFlow {
            val scanCallback: ScanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    trySend(Result.Success(listOf(result.device))).isSuccess
                }

                override fun onBatchScanResults(results: List<ScanResult?>?) {
                    if (!results.isNullOrEmpty()) {
                        trySend(Result.Success(results.mapNotNull { scanResult ->
                            scanResult!!.device
                        })).isSuccess
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    trySend(Result.Error(scanFailedWithError, errorCode)).isSuccess
                }
            }
            runPermissionSafe(rational = context.getString(R.string.coarse_permission_rationale)) {
                scanJob?.cancel()
                scanJob = launch {
                    previousConnectionState = mutableConnectionStateFlow.value
                    mutableConnectionStateFlow.value = ConnectionState.SCANNING
                    scanner.startScan(
                        listOf(getFilterByAddress(macToScan)),
                        scanSettings,
                        scanCallback
                    )
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
        mutableConnectionStateFlow.value = previousConnectionState
    }

    @SuppressLint("MissingPermission")
    suspend fun connectTo(device: BluetoothDevice): Flow<Result<ByteArray>> = callbackFlow {
        stopScan()
        val connectionStateCallback = object : BluetoothGattCallback() {

            override fun onServicesDiscovered(gattProfile: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gattProfile, status)
                if (status == GATT_FAILURE) {
                    trySend(
                        Result.Error(serviceDiscoveryFailed, status)
                    ).isSuccess
                    onDisconnect()
                    runPermissionSafe { gattProfile?.disconnect() }
                }

                gattProfile?.services?.forEach { gattService ->
                    if (gattService.uuid == serviceToFindUUID) {
                        trySend(
                            Result.Log("$serviceDiscovered ${gattService.uuid}")
                        ).isSuccess
                        serviceToCommunicateWith = gattService
                        gattService.characteristics?.forEach { characteristic ->
                            if (characteristic.uuid == characteristicToFindUUID) {
                                subscribeForNotifyAndWrite(
                                    gattProfile,
                                    characteristic
                                )
                            }
                        }
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?
            ) {
                if (characteristic?.value != null) {
                    trySend(
                        Result.Success(
                            characteristic.value!!
                        )
                    ).isSuccess
                }
            }

            @SuppressLint("MissingPermission")
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
                                trySend(
                                    Result.Log("$discoverService ${gatt.device} $delay $delay ms")
                                ).isSuccess
                                val result = gatt.discoverServices()
                                if (!result) {
                                    trySend(
                                        Result.Error(discoverServiceFailedToStart)
                                    ).isSuccess
                                }
                            }
                            bleHandler.postDelayed(discoverServicesRunnable!!, delay.toLong())
                        } else if (bondState == BOND_BONDING) {
                            trySend(Result.Log(waitingBondingCompletion)).isSuccess
                        }
                    } else {
                        onDisconnect()
                        trySend(
                            Result.Error("${gatt.device.name} $isNotConnectedNow")
                        ).isSuccess
                        runPermissionSafe(rational = context.getString(R.string.coarse_permission_rationale)) {
                            gatt.close()
                        }
                    }
                } else {
                    trySend(
                        Result.Error(failedToConnect, status)
                    ).isSuccess
                    onDisconnect()
                    runPermissionSafe(rational = context.getString(R.string.coarse_permission_rationale)) {
                        gatt.close()
                        gatt.disconnect()
                        gatt.connect()
                    }
                }
            }
        }

        runPermissionSafe(rational = context.getString(R.string.coarse_permission_rationale)) {
            controllerDevice = device
            currentGattProfile =
                controllerDevice!!.connectGatt(
                    context,
                    false,
                    connectionStateCallback
                )
        }
        awaitClose {
            stopScan()
        }
    }

    private fun onDisconnect() {
        mutableConnectionStateFlow.value = ConnectionState.NOT_CONNECTED
        serviceToCommunicateWith = null
        characteristicToNotifyOf = null
        characteristicToWriteTo = null
    }

    @SuppressLint("MissingPermission")
    fun sendBytes(data: ByteArray) {
        if (mutableConnectionStateFlow.value <= ConnectionState.SCANNING) return
        if (currentGattProfile == null && characteristicToWriteTo == null) return
        val bytesToSend: ByteArray = byteArrayOf(
            Constants.BORDER_OF_PACKAGE_SIGN.code.toByte(),
            Constants.START_PACKAGE_SIGNATURE.code.toByte()
        ) + data + Constants.BORDER_OF_PACKAGE_SIGN.code.toByte()
        runPermissionSafe {
            characteristicToWriteTo?.setValue(bytesToSend)
            characteristicToWriteTo?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            val sendSuccess =
                currentGattProfile?.writeCharacteristic(characteristicToWriteTo) ?: false
            if (sendSuccess)
                Log.d("SimpleBle", "send ${bytesToSend.toList()}")
            else
                Log.d("SimpleBle", "Failed to send ${bytesToSend.toList()}")
        }
    }

    fun showGattContents(profile: BluetoothGatt): String {
        var outLog = ""
        runPermissionSafe {
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
        }
        return outLog
    }

    @SuppressLint("MissingPermission")
    fun subscribeForNotifyAndWrite(
        gattProfile: BluetoothGatt, characteristic: BluetoothGattCharacteristic?
    ) {
        var subscribed = false
        runPermissionSafe(rational = context.getString(R.string.coarse_permission_rationale_notifications)) {
            if (characteristic?.supportsNotify() == true) {
                subscribed = gattProfile.setCharacteristicNotification(characteristic, true)
            }
        }
        if (subscribed) mutableConnectionStateFlow.value =
            ConnectionState.CONNECTED_NOTIFICATIONS

        if (characteristic?.supportsWritingWithResponse() == true || characteristic?.supportsWritingWithoutResponse() == true) {
            characteristicToWriteTo = characteristic
        }
    }

    @SuppressLint("MissingPermission")
    fun onDestroy() {
        currentGattProfile?.disconnect()
        stopScan()
        scanJob?.cancel()
    }

    private fun runPermissionSafe(
        rational: String = "",
        action: () -> Unit
    ) {
        runBlocking {
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
}