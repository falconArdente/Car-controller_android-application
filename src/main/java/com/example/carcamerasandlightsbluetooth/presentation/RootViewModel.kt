package com.example.carcamerasandlightsbluetooth.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcamerasandlightsbluetooth.data.CameraState
import com.example.carcamerasandlightsbluetooth.domain.api.ControllerInteractor
import com.example.carcamerasandlightsbluetooth.domain.model.ControlCommand
import com.example.carcamerasandlightsbluetooth.domain.model.DeviceState
import com.example.carcamerasandlightsbluetooth.domain.model.Timings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import javax.inject.Inject

class RootViewModel @Inject constructor(
    private val deviceInteractor: ControllerInteractor
) : ViewModel() {
    private var deviceJob: Job? = null
    private var isTestMode = false

    init {
        deviceJob = viewModelScope.launch(Dispatchers.IO) {
            launch { deviceInteractor.scanForDevice() }
            launch { deviceInteractor.getStateFlow().collect(stateFlowCollector) }
            launch { deviceInteractor.getServiceDataFlow().collect(serviceLogCollector) }
            launch { deviceInteractor.getErrorsCountFlow().collect(errorCountCollector) }
        }
    }

    private val mutableStatesLiveData = MutableLiveData(
        MainState(
            deviceState = DeviceState.NOT_INITIALIZED,
            isLocked = true,
            isSetTimings = false
        )
    )
    val stateToObserve: LiveData<MainState> = mutableStatesLiveData
    private val mutableLogLiveData = MutableLiveData("")
    val serviceLogToObserve: LiveData<String> = mutableLogLiveData
    private var logStorage = ""
    private val stateFlowCollector = FlowCollector<DeviceState> { incomeState ->
        mutableStatesLiveData.postValue(
            mutableStatesLiveData.value?.copy(deviceState = incomeState)
        )
    }
    private val serviceLogCollector = FlowCollector<String> { message ->
        logStorage += message + '\n'
        mutableLogLiveData.postValue(logStorage)
    }
    private val errorCountCollector = FlowCollector<Int> {

    }

    override fun onCleared() {
        deviceInteractor.finish()
        super.onCleared()
    }

    fun clickFrontCam() {
        val state = currentStateToCommand()
        deviceInteractor.sendCommand(
            state.copy(relayIsOn = !state.relayIsOn)
        )
    }

    fun clickLeftFog() {
        val state = currentStateToCommand()
        deviceInteractor.sendCommand(
            state.copy(leftFogIsOn = !state.leftFogIsOn)
        )
    }

    fun clickRightAngelEye() {
        val state = currentStateToCommand()
        deviceInteractor.sendCommand(
            state.copy(rightAngelEyeIsOn = !state.rightAngelEyeIsOn)
        )
    }

    fun clickLeftAngelEye() {
        val state = currentStateToCommand()
        deviceInteractor.sendCommand(
            state.copy(leftAngelEyeIsOn = !state.leftAngelEyeIsOn)
        )
    }

    fun clickRightFog() {
        val state = currentStateToCommand()
        deviceInteractor.sendCommand(
            state.copy(rightFogIsOn = !state.rightFogIsOn)
        )
    }

    fun clickRearCam() {
        val state = currentStateToCommand()
        deviceInteractor.sendCommand(
            state.copy(rightAngelEyeIsOn = !state.rightAngelEyeIsOn)
        )
    }

    fun clickCaution() {
        val state = currentStateToCommand()
        deviceInteractor.sendCommand(
            state.copy(cautionIsOn = !state.cautionIsOn)
        )
    }


    private fun currentStateToCommand(): ControlCommand {
        with(mutableStatesLiveData.value!!.deviceState) {
            val cameraState = if (isTestMode) {
                CameraState.TEST_MODE
            } else {
                if (frontCameraIsShown) {
                    CameraState.FRONT_CAM_ON
                } else if (rightAngelEyeIsOn) {
                    CameraState.REAR_CAM_ON
                } else {
                    CameraState.CAMS_OFF
                }
            }
            return ControlCommand(
                cautionIsOn = cautionIsOn,
                leftFogIsOn = leftFogIsOn,
                rightFogIsOn = rightFogIsOn,
                relayIsOn = frontCameraIsShown,
                rightAngelEyeIsOn = rightAngelEyeIsOn,
                leftAngelEyeIsOn = leftAngelEyeIsOn,
                displayIsOn = (rearCameraIsShown || frontCameraIsShown),
                cameraState = cameraState
            )
        }
    }

    fun clickLock() {
        with(mutableStatesLiveData) {
            value?.let { deviceInteractor.switchToTestMode(!it.isLocked) }
            postValue(
                value?.copy(
                    isLocked = !value?.isLocked!!
                )
            )
        }
    }

    fun clickTimings() {
        with(mutableStatesLiveData) {
            if (!value?.isSetTimings!!) deviceInteractor.requestTimings()
            postValue(
                value?.copy(
                    isSetTimings = !value?.isSetTimings!!
                )
            )
        }
    }

    fun sendTimings(timings: Timings) {
        deviceInteractor.sendTimings(
            newTimings = timings
        )
        mutableStatesLiveData.postValue(
            mutableStatesLiveData.value!!.copy(isSetTimings = false)
        )
    }

    fun reScan() {
        viewModelScope.launch(Dispatchers.IO) {
            deviceInteractor.stopScan()
            delay(900L)
            deviceInteractor.scanForDevice()
        }

    }
}