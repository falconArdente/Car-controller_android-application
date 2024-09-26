package com.example.carcamerasandlightsbluetooth.data.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.util.Log
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class BluetoothRepositoryImpl(
    private val communicator: SimpleBleConnectedController,
    private val defaultMAC: String = "",
) : BluetoothRepository {
    private var scanJob: Job? = null
    private val stateFlow: MutableStateFlow<DeviceState> =
        MutableStateFlow(DeviceState.NOT_INITIALIZED)
    val serviceFlow: Flow<String> = flow<String> {
        emit("service flow connected")
    }.flowOn(Dispatchers.IO)


    private var connectionFlow: Flow<Result<DeviceState>>? = null
    private var macAddress: String = ""
    private val scanFlowCollector = FlowCollector<List<ScanResult>> { list ->
        list.forEach {

            Log.d("repository", it.toString())
        }
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
            scanJob?.cancel()
            scanJob = if (defaultMAC.isNotEmpty())
                launch(Dispatchers.IO) {
                    communicator.startScanByAddress(defaultMAC)
                        .collect(scanFlowCollector)
                }
            else launch(Dispatchers.IO) { communicator.startRawScan().collect(scanFlowCollector) }
        }
    }

    override fun stopScan() {
        TODO("Not yet implemented")
    }

    override fun getServiceDataFlow(): Flow<String> {
        TODO("Not yet implemented")
    }

    private val serviceSender = ServiceMessageSender {

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
                                }

                                is DeviceReports.Error -> {
                                    TODO()
                                }
                            }
                        }

                        is Result.Error -> TODO()
                        is Result.Log -> TODO()
                    }
                }
        }
    }

    private fun parseDeviceDataFlow() {

    }

}