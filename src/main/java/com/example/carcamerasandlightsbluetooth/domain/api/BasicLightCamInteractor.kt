package com.example.carcamerasandlightsbluetooth.domain.api

import com.example.carcamerasandlightsbluetooth.domain.model.ControlCommand
import com.example.carcamerasandlightsbluetooth.domain.model.ControllerState
import com.example.carcamerasandlightsbluetooth.domain.model.Timings
import kotlinx.coroutines.flow.Flow

class BasicLightCamInteractor:ControllerInteractor {
    override fun getState(): Flow <ControllerState> {
        TODO("Not yet implemented")
    }

    override fun sendCommand(command: ControlCommand) {
        TODO("Not yet implemented")
    }

    override fun getTimings(): Timings {
        TODO("Not yet implemented")
    }

    override fun sendTimings(newTimings: Timings) {
        TODO("Not yet implemented")
    }
}