package com.example.carcamerasandlightsbluetooth.presentation

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import com.example.carcamerasandlightsbluetooth.R
import com.example.carcamerasandlightsbluetooth.databinding.ActivityMainBinding
import com.example.carcamerasandlightsbluetooth.domain.model.DeviceState
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {
    private val viewModel by viewModel<RootViewModel>()
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.stateToObserve.observe(this) { renderMainState(it) }
        binding.LogView.movementMethod = ScrollingMovementMethod()
        viewModel.serviceLogToObserve.observe(this) {
            binding.LogView.text = it
        }
    }

    private fun renderMainState(mainState: MainState) {
        renderShifts(mainState.deviceState)
        renderBluetoothSign(mainState.deviceState)
        renderCommandSet(mainState.deviceState)

    }

    private fun renderCommandSet(state: DeviceState) {
        with(binding.commandsBlock) {
            backCamText.isVisible = state.rearCameraIsOn
            frontCamText.isVisible = state.frontCameraIsShown
            backCamBack.setBackgroundDrawable(
                AppCompatResources.getDrawable(
                    this@MainActivity,
                    if (state.rearCameraIsOn) R.drawable.camera_back_back_on else R.drawable.camera_back_back
                )
            )
            frontCamBack.setBackgroundDrawable(
                AppCompatResources.getDrawable(
                    this@MainActivity,
                    if (state.frontCameraIsShown) R.drawable.camera_back_back_on else R.drawable.camera_back_back
                )
            )
            leftFog.setBackgroundDrawable(
                AppCompatResources.getDrawable(
                    this@MainActivity,
                    if (state.leftFogIsOn) R.drawable.fog_lamp_on else R.drawable.fog_lamp
                )
            )
            rightFog.setBackgroundDrawable(
                AppCompatResources.getDrawable(
                    this@MainActivity,
                    if (state.rightFogIsOn) R.drawable.fog_lamp_on else R.drawable.fog_lamp
                )
            )
        }
        binding.columnSet.cautionButton.setBackgroundDrawable(
            AppCompatResources.getDrawable(
                this@MainActivity,
                if (state.cautionIsOn) R.drawable.caution_sign_on else R.drawable.caution_sign
            )
        )
    }

    private fun renderBluetoothSign(state: DeviceState) {
        binding.bluetoothSign.setImageDrawable(
            AppCompatResources.getDrawable(
                this,
                when (state.connectionState) {
                    DeviceState.ConnectionState.NOT_CONNECTED -> R.drawable.b_disconnected
                    DeviceState.ConnectionState.SCANNING -> R.drawable.b_scaning
                    DeviceState.ConnectionState.CONNECTED -> R.drawable.b_connected
                    DeviceState.ConnectionState.CONNECTED_NOTIFIED -> R.drawable.b_notified
                }
            )
        )
    }

    private fun renderShifts(state: DeviceState) {
        with(binding.shifts) {
            leftClickImg.setImageDrawable(
                AppCompatResources.getDrawable(
                    this@MainActivity,
                    if (state.leftPressed) R.drawable.turn_arrow_is_on else R.drawable.turn_arrow
                )
            )
            leftDblClickImg.setImageDrawable(
                AppCompatResources.getDrawable(
                    this@MainActivity,
                    if (state.leftDblPressed) R.drawable.dbl_click_is_on else R.drawable.dbl_click
                )
            )
            rightClickImg.setImageDrawable(
                AppCompatResources.getDrawable(
                    this@MainActivity,
                    if (state.rightPressed) R.drawable.turn_arrow_is_on else R.drawable.turn_arrow
                )
            )
            rightDblClickImg.setImageDrawable(
                AppCompatResources.getDrawable(
                    this@MainActivity,
                    if (state.rightDblPressed) R.drawable.dbl_click_is_on else R.drawable.dbl_click
                )
            )
            reverseImg.setImageDrawable(
                AppCompatResources.getDrawable(
                    this@MainActivity,
                    if (state.reversePressed) R.drawable.reverse_is_on else R.drawable.reverse
                )
            )
        }
    }
}