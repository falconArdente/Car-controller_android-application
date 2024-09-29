package com.example.carcamerasandlightsbluetooth.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcamerasandlightsbluetooth.domain.api.ControllerInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RootViewModel(
    private val device: ControllerInteractor,
    private val context: Context
) : ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.IO) {
            device.scanForDevice()
        }
    }


}