package com.example.carcamerasandlightsbluetooth.utils

sealed class Result<T>(
    val data: T? = null,
    val message: String? = null,
    val errorCode: Int? = null
) {
    class Success<T>(data: T) : Result<T>(data)
    class Error<T>(message: String, errorCode: Int? = null) :
        Result<T>(message = message, errorCode = errorCode)

    class Log<T>(message: String) : Result<T>(message = message)
}