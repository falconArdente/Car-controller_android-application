package com.example.carcamerasandlightsbluetooth.domain.model

data class Timings(
    val bounce: Int,
    val repeater: Int,
    val frontDelay: Int,
    val rearDelay: Int,
) {
    companion object {
        val NOT_INITIALIZED = Timings(-1, -1, -1, -1)
    }
}
