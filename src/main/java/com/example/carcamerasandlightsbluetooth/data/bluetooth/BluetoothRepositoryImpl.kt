package com.example.carcamerasandlightsbluetooth.data.bluetooth

import android.bluetooth.BluetoothDevice
import com.example.carcamerasandlightsbluetooth.data.repository.BluetoothRepository
import com.example.carcamerasandlightsbluetooth.data.repository.ServiceMessageSender
import com.example.carcamerasandlightsbluetooth.domain.model.ControlCommand
import com.example.carcamerasandlightsbluetooth.domain.model.DeviceState
import com.example.carcamerasandlightsbluetooth.domain.model.Timings
import com.example.carcamerasandlightsbluetooth.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect

class BluetoothRepositoryImpl(
    val deviceAdapter: SimpleBleConnectedController,
    val defaultMAC: String = ""
) : BluetoothRepository {
    private val stateFlow: MutableStateFlow<DeviceState> = MutableStateFlow(DeviceState.NOT_INITIALIZED)
    private var macAddress: String = ""

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

    override fun scanForDevice() {
        TODO("Not yet implemented")
    }

    override fun stopScan() {
        TODO("Not yet implemented")
    }

    override fun getServiceDataFlow(): Flow<String> {
        TODO("Not yet implemented")
    }

    private val serviceSender = ServiceMessageSender {

    }

    private suspend fun connectToDevice(device:BluetoothDevice) {
        deviceAdapter.connectTo(device)
            .collect{result->
                when(result){
                    is Result.Success->{byteArray->
                        if (state is
                        stateFlow.value.copy(
                            leftPressed = state
                        )
                    }

                    is Result.Error -> TODO()
                    is Result.Log -> TODO()
                }


            }
    }

    private fun parseDeviceDataFlow() {

    }

}