package com.example.carcamerasandlightsbluetooth.data.map

import android.util.Log
import com.example.carcamerasandlightsbluetooth.data.bluetooth.Constants
import com.example.carcamerasandlightsbluetooth.data.dto.DeviceReports
import com.example.carcamerasandlightsbluetooth.domain.model.DeviceState
import com.example.carcamerasandlightsbluetooth.domain.model.Timings
import java.util.BitSet

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
                        rearCameraIsOn = bits.get(11),
                        angelEyeIsOn = bits.get(12),
                        displayIsOn = bits.get(13)
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
                Log.d("repository", "looks as ELSE")
                return DeviceReports.Error
            }
        }
    }

    private fun twoBytesToInt(leastByte: Byte, mostByte: Byte): Int {
        return (leastByte.toInt() and (0xFF).toInt()) + mostByte.toInt()
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
                rearCameraIsOn = rearCameraIsOn,
                angelEyeIsOn = angelEyeIsOn,
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
}