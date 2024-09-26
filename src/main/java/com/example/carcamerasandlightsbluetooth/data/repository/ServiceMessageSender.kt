package com.example.carcamerasandlightsbluetooth.data.repository

fun interface ServiceMessageSender {
    fun sendMessage(text: String)
}