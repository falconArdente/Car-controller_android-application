package com.example.carcamerasandlightsbluetooth.data.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.example.carcamerasandlightsbluetooth.R
import com.example.carcamerasandlightsbluetooth.data.CameraState
import com.example.carcamerasandlightsbluetooth.data.bluetooth.SimpleBleConnectedController.ConnectionState
import com.example.carcamerasandlightsbluetooth.data.dto.DeviceReports
import com.example.carcamerasandlightsbluetooth.data.map.PacketsMapper
import com.example.carcamerasandlightsbluetooth.data.repository.BluetoothRepository
import com.example.carcamerasandlightsbluetooth.data.repository.DeviceStateSender
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
import kotlinx.coroutines.launch
import javax.inject.Inject

class BluetoothRepositoryImpl @Inject constructor(
    private val communicator: SimpleBleConnectedController,
    private val defaultMAC: String = "",
    context: Context,
) : BluetoothRepository {
    private val gotTimingsMessage = context.getString(R.string.got_new_timings_message)
    private val gotDeviceErrorsCount = context.getString(R.string.got_device_error_count)
    private val gotPacketRecognitionErrorMessage =
        context.getString(R.string.got_packet_recognition_error_message)
    private val gotIncomeDataErrorMessage =
        context.getString(R.string.got_income_data_error_message)
    private val deviceIsReachable =
        context.getString(R.string.is_reachable)
    private val connectingToMessage = context.getString(R.string.connecting_to)
    private var previousRemoteState = ConnectionState.NOT_CONNECTED
    private var lastDeviceState: DeviceState = DeviceState.NOT_INITIALIZED
    private var connectionReactionsJob: Job? = null
    private var scanJob: Job? = null
    private var stateSender: DeviceStateSender? = null
    private var serviceSender: ServiceMessageSender? = null

    /**
     * Поток: число ошибок связи, обнаруженных контроллером
     */
    val communicationErrorsStateFlow = MutableStateFlow(0)

    /**
     * Передоставляет поток: число ошибок связи, обнаруженных контроллером
     */
    override fun getErrorsCountFlow(): Flow<Int> = communicationErrorsStateFlow

    /**
     * Распределяет данные, возникающие, при сканировании по потокам состояний и сервисным
     */
    private val scanFlowCollector = FlowCollector<Result<List<BluetoothDevice>>> { result ->
        when (result) {
            is Result.Success -> {
                result.data!!.forEach { device ->
                    serviceSender?.sendMessage("${device.address} $deviceIsReachable")
                    if (device.address == defaultMAC) {
                        serviceSender?.sendMessage("$connectingToMessage ${device.address}")
                        communicator.connectTo(device).collect(connectionFlowCollector)
                        stopScan()
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

    /**
     * Закидывает в состояния подключения контроллера в поток состояний
     */
    private val deviceStatesCollector = FlowCollector<ConnectionState> { connectionState ->
        when (connectionState) {
            ConnectionState.NOT_CONNECTED -> {
                stateSender?.send(DeviceState.ConnectionState.NOT_CONNECTED)
                if (previousRemoteState == ConnectionState.CONNECTED_NOTIFICATIONS
                    || previousRemoteState == ConnectionState.CONNECTED
                ) scanForDevice()
            }

            ConnectionState.SCANNING -> stateSender?.send(DeviceState.ConnectionState.SCANNING)
            ConnectionState.CONNECTED_NOTIFICATIONS ->
                stateSender?.send(DeviceState.ConnectionState.CONNECTED_NOTIFIED)

            ConnectionState.CONNECTED -> stateSender?.send(DeviceState.ConnectionState.CONNECTED)
        }
        previousRemoteState = connectionState
    }

    /**
     * Разделяет данные по потокам состояний и сервисным после соединения с контроллером
     */
    private val connectionFlowCollector = FlowCollector<Result<ByteArray>> { result ->
        when (result) {
            is Result.Success -> {
                stateSender?.send(PacketsMapper.toReport(result.data!!))
            }

            is Result.Error -> {
                serviceSender?.sendMessage(
                    "$gotIncomeDataErrorMessage ${result.message} "
                            + result.errorCode?.toString()
                )
            }

            is Result.Log -> serviceSender?.sendMessage(result.message.toString())
        }
    }


    override fun getServiceDataFlow(): Flow<String> {
        return callbackFlow {
            serviceSender = ServiceMessageSender { message ->
                trySend(message)
            }
            awaitClose {}
        }
    }

    override fun getStateFlow(): Flow<DeviceState> {
        return callbackFlow {
            stateSender = object : DeviceStateSender {
                override fun send(deviceReport: DeviceReports) {
                    when (deviceReport) {
                        is DeviceReports.StateReport -> {
                            lastDeviceState = PacketsMapper.combineReportWithState(
                                stateReport = deviceReport,
                                deviceState = lastDeviceState
                            )
                            trySend(lastDeviceState).isSuccess
                        }

                        is DeviceReports.TimingReport -> {
                            serviceSender?.sendMessage(gotTimingsMessage)
                            lastDeviceState = lastDeviceState.copy(timings = deviceReport.timings)
                            trySend(lastDeviceState).isSuccess
                        }

                        is DeviceReports.Error -> {
                            serviceSender?.sendMessage(gotPacketRecognitionErrorMessage)
                        }

                        is DeviceReports.AdditionalReport -> {
                            serviceSender?.sendMessage("$gotDeviceErrorsCount: ${deviceReport.errorsCount}")
                            communicationErrorsStateFlow.value = deviceReport.errorsCount
                        }
                    }
                }

                override fun send(connectionState: DeviceState.ConnectionState) {
                    lastDeviceState = lastDeviceState.copy(
                        connectionState = connectionState
                    )
                    trySend(lastDeviceState).isSuccess
                }
            }
            awaitClose {}
        }
    }

    /**
     * Отправка текущего состояния с модификатором камеры TEST_MODE
     */
    override fun switchToTestMode(testIsOn: Boolean) {
        with(lastDeviceState) {
            sendCommand(
                ControlCommand(
                    cautionIsOn = cautionIsOn,
                    leftFogIsOn = leftFogIsOn,
                    rightFogIsOn = rightFogIsOn,
                    relayIsOn = frontCameraIsShown,
                    rightAngelEyeIsOn = rightAngelEyeIsOn,
                    leftAngelEyeIsOn = leftAngelEyeIsOn,
                    displayIsOn = (frontCameraIsShown || rearCameraIsShown),
                    cameraState = if (testIsOn) CameraState.TEST_MODE else CameraState.CAMS_OFF
                )
            )
        }
    }

    override fun sendCommand(command: ControlCommand) {
        communicator.sendBytes(PacketsMapper.commandToPacket(command))
    }

    override fun requestTimings() {
        communicator.sendBytes(byteArrayOf(Constants.TIMINGS_REQUEST.toByte()))
    }

    override fun sendTimings(newTimings: Timings) {
        communicator.sendBytes(PacketsMapper.commandToPacket(newTimings))
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
                stateSender?.send(DeviceState.ConnectionState.SCANNING)
                communicator.startScanByAddress(defaultMAC).collect(scanFlowCollector)
            }
        }
    }

    override fun stopScan() {
        scanJob?.cancel()
    }

    override fun finish() {
        communicator.finish()
    }
}