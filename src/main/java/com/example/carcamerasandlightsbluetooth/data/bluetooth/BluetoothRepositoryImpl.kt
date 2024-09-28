package com.example.carcamerasandlightsbluetooth.data.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.example.carcamerasandlightsbluetooth.R
import com.example.carcamerasandlightsbluetooth.data.bluetooth.SimpleBleConnectedController.ConnectionState
import com.example.carcamerasandlightsbluetooth.data.dto.DeviceReports
import com.example.carcamerasandlightsbluetooth.data.map.PacketsMapper
import com.example.carcamerasandlightsbluetooth.data.repository.BluetoothRepository
import com.example.carcamerasandlightsbluetooth.data.repository.ServiceMessageSender
import com.example.carcamerasandlightsbluetooth.domain.model.ControlCommand
import com.example.carcamerasandlightsbluetooth.domain.model.DeviceState
import com.example.carcamerasandlightsbluetooth.domain.model.Timings
import com.example.carcamerasandlightsbluetooth.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class BluetoothRepositoryImpl(
    private val communicator: SimpleBleConnectedController,
    private val defaultMAC: String = "",
    context: Context,
) : BluetoothRepository {
    private val gotTimingsMessage = context.getString(R.string.got_new_timings_message)
    private val gotPacketRecognitionErrorMessage =
        context.getString(R.string.got_packet_recognition_error_message)
    private val gotIncomeDataErrorMessage =
        context.getString(R.string.got_income_data_error_message)
    private val deviceIsReachable =
        context.getString(R.string.is_reachable)
    private val connectingToMessage = context.getString(R.string.connecting_to)
    private val remoteNotConnected = context.getString(R.string.remote_not_connected)
    private val remoteScanning = context.getString(R.string.remote_scanning)
    private val remoteConnected = context.getString(R.string.remote_connected)
    private val remoteConnectedNotifications =
        context.getString(R.string.remote_connected_notifications)
    private var previousRemoteState = ConnectionState.NOT_CONNECTED
    private var connectionReactionsJob: Job? = null
    private var scanJob: Job? = null
    private var connectionFlow: Flow<Result<DeviceState>>? = null
    private val stateFlow: MutableStateFlow<DeviceState> =
        MutableStateFlow(DeviceState.NOT_INITIALIZED)
    private var serviceSender: ServiceMessageSender? = null
    private var macAddress: String = ""
    private var connectionFlowCollector: FlowCollector<Result<DeviceState>> =
        FlowCollector { result ->
            serviceSender?.sendMessage(result.data.toString())
            if (result is Result.Log) {
                Log.d("repository", result.message.toString())
            }
        }

    // to make a log textView:
    private val scanFlowCollector = FlowCollector<Result<List<BluetoothDevice>>> { result ->
        when (result) {
            is Result.Success -> {
                result.data!!.forEach { device ->
                    serviceSender?.sendMessage("${device.address} $deviceIsReachable")
                    if (device.address == defaultMAC) {
                        serviceSender?.sendMessage("$connectingToMessage ${device.address}")
                        connectionFlow = connectToDevice(device)
                        connectionFlow!!.collect(connectionFlowCollector)
                        scanJob?.cancel()
                    }
                }
            }

            is Result.Error -> {
                serviceSender?.sendMessage(
                    if (result.errorCode != null) "${result.message}: ${result.errorCode}"
                    else result.message.toString()
                )
            }

            is Result.Log -> serviceSender?.sendMessage(result.message.toString())
        }
    }

    override var serviceFlow: Flow<String> = callbackFlow {
        serviceSender = ServiceMessageSender { message ->
            trySend(message)
        }
        awaitClose {}
    }

    private val deviceStatesCollector = FlowCollector<ConnectionState> { connectionState ->
        when (connectionState) {
            ConnectionState.NOT_CONNECTED -> {
                serviceSender?.sendMessage(remoteNotConnected)
                if (previousRemoteState == ConnectionState.CONNECTED_NOTIFICATIONS
                    || previousRemoteState == ConnectionState.CONNECTED
                ) scanForDevice()
            }

            ConnectionState.SCANNING -> serviceSender?.sendMessage(remoteScanning)
            ConnectionState.CONNECTED_NOTIFICATIONS -> serviceSender?.sendMessage(
                remoteConnectedNotifications
            )

            ConnectionState.CONNECTED -> serviceSender?.sendMessage(remoteConnected)
        }
        previousRemoteState = connectionState
    }

    override suspend fun getServiceDataFlow(): Flow<String> {
        return serviceFlow
    }

    override fun getState(): Flow<DeviceState> = stateFlow

    override fun sendCommand(command: ControlCommand) {
        TODO("Not yet implemented")
    }

    override fun getTimings(): Timings {
        TODO("Not yet implemented")
    }

    override fun sendTimings(newTimings: Timings) {
        TODO("Not yet implemented")
    }

    override suspend fun scanForDevice() {
        coroutineScope {
            if (connectionReactionsJob == null) {
                connectionReactionsJob =
                    launch(Dispatchers.IO) {
                        communicator.connectionStateFlow
                            .collect(deviceStatesCollector)
                    }
            }
            scanJob?.cancel()
            scanJob = launch(Dispatchers.IO) {
                delay(300L)
                if (defaultMAC.isNotEmpty())
                    communicator.startScanByAddress(defaultMAC).collect(scanFlowCollector)
                else
                    communicator.startRawScan().collect(scanFlowCollector)
            }
        }
    }

    override fun stopScan() {
        scanJob?.cancel()
    }

    private suspend fun connectToDevice(device: BluetoothDevice): Flow<Result<DeviceState>> {
        return flow {
            communicator.connectTo(device)
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            when (val report = PacketsMapper.toReport(result.data!!)) {
                                is DeviceReports.StateReport -> {
                                    emit(
                                        Result.Success(
                                            PacketsMapper.combineReportWithState(
                                                stateReport = report,
                                                deviceState = stateFlow.value
                                            )
                                        )
                                    )
                                }

                                is DeviceReports.TimingReport -> {
                                    TODO()
                                    serviceSender?.sendMessage(gotTimingsMessage)
                                }

                                is DeviceReports.Error -> {
                                    TODO()
                                    serviceSender?.sendMessage(gotPacketRecognitionErrorMessage)
                                }
                            }
                        }

                        is Result.Error -> {
                            serviceSender?.sendMessage(
                                "$gotIncomeDataErrorMessage ${result.message} "
                                        + result.errorCode?.toString()
                            )
                        }

                        is Result.Log -> {
                            serviceSender?.sendMessage(result.message.toString())
                        }
                    }
                }
        }
    }

    private fun parseDeviceDataFlow() {

    }

}