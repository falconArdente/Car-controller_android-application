package com.example.carcamerasandlightsbluetooth.domain.api

import com.example.carcamerasandlightsbluetooth.domain.model.ControlCommand
import com.example.carcamerasandlightsbluetooth.domain.model.ControllerState
import com.example.carcamerasandlightsbluetooth.domain.model.Timings
import kotlinx.coroutines.flow.Flow

interface ControllerInteractor {

    fun getState(): Flow<ControllerState>
    fun sendCommand(command: ControlCommand)
    fun getTimings(): Timings
    fun sendTimings(newTimings: Timings)
}