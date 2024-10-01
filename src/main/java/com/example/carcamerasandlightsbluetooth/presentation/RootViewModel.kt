package com.example.carcamerasandlightsbluetooth.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcamerasandlightsbluetooth.domain.api.ControllerInteractor
import com.example.carcamerasandlightsbluetooth.domain.model.DeviceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

class RootViewModel(
    private val deviceInteractor: ControllerInteractor
) : ViewModel() {
    private var deviceJob: Job? = null

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
            isLocked = false,
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
        Log.d("SimpleBle", incomeState.toString())
    }
    private val serviceLogCollector = FlowCollector<String> { message ->
        logStorage += message + '\n'
        mutableLogLiveData.postValue(logStorage)
        Log.d("SimpleBle", "service: $message")
    }
    private val errorCountCollector = FlowCollector<Int> {

    }

    override fun onCleared() {
        deviceInteractor.finish()
        super.onCleared()
    }
}