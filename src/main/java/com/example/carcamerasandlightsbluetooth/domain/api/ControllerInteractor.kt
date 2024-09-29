package com.example.carcamerasandlightsbluetooth.domain.api

import com.example.carcamerasandlightsbluetooth.domain.model.ControlCommand
import com.example.carcamerasandlightsbluetooth.domain.model.DeviceState
import com.example.carcamerasandlightsbluetooth.domain.model.Timings
import kotlinx.coroutines.flow.Flow

interface ControllerInteractor {
    /**
     * Запускает поиск контроллера автомобиля
     * По сути первая команда для начала работы
     */
    suspend fun scanForDevice()

    /**
     * Отправляет комманду управления контроллером автомобиля,
     * перечеь данных команды в domain.model.ControlCommand
     */
    fun sendCommand(command: ControlCommand)

    /**
     * Отключает на время тестирования реакцию на состояние органов управления автомобиля
     */
    fun switchToTestMode(testIsOn: Boolean)

    /**
     * Запрашивает у контроллера автомобиля действующий набор интервалов задержек
     */
    fun requestTimings()

    /**
     * Передает новый набор интервалов задержек для использования контроллером
     */
    fun sendTimings(newTimings: Timings)

    /**
     * Принудительно отключает процесс поиска контроллера автомобиля
     */
    fun stopScan()

    /**
     * Поток данных контроллера о состоянии органов управления и управляемых систем
     */
    fun getStateFlow(): Flow<DeviceState>

    /**
     * Поток сервисных данных: не нужны для пользователя, полезны для понимания что происходит
     */
    fun getServiceDataFlow(): Flow<String>

    /**
     * Выполняет набор действий для корректного завершения связи
     */
    fun finish()
}