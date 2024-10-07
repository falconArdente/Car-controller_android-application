package com.example.carcamerasandlightsbluetooth.data.map

import com.example.carcamerasandlightsbluetooth.data.bluetooth.Constants
import com.example.carcamerasandlightsbluetooth.data.dto.DeviceReports
import com.example.carcamerasandlightsbluetooth.domain.model.ControlCommand
import com.example.carcamerasandlightsbluetooth.domain.model.DeviceState
import com.example.carcamerasandlightsbluetooth.domain.model.Timings
import java.nio.ByteBuffer
import java.util.BitSet

/**
 * Соединяет массив байт контроллера и объекты в Android
 */
object PacketsMapper {
    fun toReport(data: ByteArray): DeviceReports {
        if (!(data[0] == Constants.BORDER_OF_PACKAGE_SIGN.code.toByte()
                    && data[1] == Constants.START_PACKAGE_SIGNATURE.code.toByte())
        ) return DeviceReports.Error
        val packet = reduceToContent(data)
        return when (packet.size) {
            2 -> {
                val bits = BitSet.valueOf(byteArrayOf(packet[0], packet[1]))
                if ((bits.get(0) || bits.get(1))) return DeviceReports.Error
                return DeviceReports.StateReport(
                    state = DeviceState(
                        connectionState = DeviceState.ConnectionState.CONNECTED_NOTIFIED,
                        leftPressed = bits.get(2),
                        leftDblPressed = bits.get(3),
                        rightPressed = bits.get(4),
                        rightDblPressed = bits.get(5),
                        reversePressed = bits.get(6),
                        cautionIsOn = bits.get(7),
                        leftFogIsOn = bits.get(8),
                        rightFogIsOn = bits.get(9),
                        frontCameraIsShown = bits.get(10),
                        rightAngelEyeIsOn = bits.get(11),
                        leftAngelEyeIsOn = bits.get(12),
                        displayIsOn = bits.get(13),
                        testModeIsOn = (bits.get(14) && bits.get(15)),
                        timings = Timings.NOT_INITIALIZED
                    )
                )
            }

            3 -> {
                val bits = BitSet.valueOf(byteArrayOf(packet[0]))
                if (!(bits.get(0) && !bits.get(1))) return DeviceReports.Error
                DeviceReports.AdditionalReport(errorsCount = twoBytesToInt(packet[1], packet[2]))
            }

            9 -> {
                if (packet[0].toInt() != 2) return DeviceReports.Error
                DeviceReports.TimingReport(
                    timings = Timings(
                        bounce = twoBytesToInt(packet[1], packet[2]),
                        repeater = twoBytesToInt(packet[3], packet[4]),
                        frontDelay = twoBytesToInt(packet[5], packet[6]),
                        rearDelay = twoBytesToInt(packet[7], packet[8])
                    )
                )
            }

            else -> {
                return DeviceReports.Error
            }
        }
    }

    private fun twoBytesToInt(leastByte: Byte, mostByte: Byte): Int {
        val array = byteArrayOf(leastByte, mostByte, 0, 0)
        array.reverse()
        return ByteBuffer.wrap(array).int
    }

    fun combineReportWithState(
        stateReport: DeviceReports.StateReport,
        deviceState: DeviceState
    ): DeviceState {
        with(stateReport.state) {
            return deviceState.copy(
                leftPressed = leftPressed,
                leftDblPressed = leftDblPressed,
                rightPressed = rightPressed,
                rightDblPressed = rightDblPressed,
                reversePressed = reversePressed,
                cautionIsOn = cautionIsOn,
                leftFogIsOn = leftFogIsOn,
                rightFogIsOn = rightFogIsOn,
                frontCameraIsShown = frontCameraIsShown,
                rightAngelEyeIsOn = rightAngelEyeIsOn,
                leftAngelEyeIsOn = leftAngelEyeIsOn,
                displayIsOn = displayIsOn
            )
        }
    }

    private fun reduceToContent(income: ByteArray): ByteArray {
        val startByte = 2
        for (i in startByte..income.size step 1) {
            if (income[i] == Constants.BORDER_OF_PACKAGE_SIGN.code.toByte()) {
                return income.copyOfRange(startByte, i)
            }
        }
        return income
    }

    fun commandToPacket(command: ControlCommand): ByteArray {
        val bits = BitSet(16)
        with(command) {
            val cameraBits = BitSet(cameraState.ordinal)
            bits[0] = true
            bits[1] = false
            bits[2] = cautionIsOn
            bits[3] = leftFogIsOn
            bits[4] = rightFogIsOn
            bits[5] = relayIsOn
            bits[6] = rightAngelEyeIsOn
            bits[7] = leftAngelEyeIsOn
            bits[8] = displayIsOn
            bits[9] = cameraBits[0]
            bits[10] = cameraBits[1]
            bits[15] = true
        }
        return bits.toByteArray()
    }

    fun commandToPacket(timings: Timings): ByteArray {
        val bytes = ByteArray(9)
        with(timings) {
            bytes[0] = 3
            bytes[1] = bounce.toByte()
            bytes[2] = (bounce shr (8)).toByte()
            bytes[3] = repeater.toByte()
            bytes[4] = (repeater shr (8)).toByte()
            bytes[5] = frontDelay.toByte()
            bytes[6] = (frontDelay shr (8)).toByte()
            bytes[7] = rearDelay.toByte()
            bytes[8] = (rearDelay shr (8)).toByte()
        }
        return bytes
    }
}