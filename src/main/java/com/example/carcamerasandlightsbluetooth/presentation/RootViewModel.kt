package com.example.carcamerasandlightsbluetooth.presentation

import android.content.Context
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
    private val deviceInteractor: ControllerInteractor,
    private val context: Context
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

    private val mutableStatesLiveData = MutableLiveData(DeviceState.NOT_INITIALIZED)
    val stateToObserve: LiveData<DeviceState> = mutableStatesLiveData
    private val stateFlowCollector = FlowCollector<DeviceState> { incomeState ->
        mutableStatesLiveData.postValue(incomeState)
        Log.d("SimpleBle", incomeState.toString())
    }
    private val serviceLogCollector = FlowCollector<String> { message ->
        Log.d("SimpleBle", "service: $message")
    }
    private val errorCountCollector = FlowCollector<Int> {

    }

    override fun onCleared() {
        deviceInteractor.finish()
        super.onCleared()
    }
}