package com.example.carcamerasandlightsbluetooth.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.markodevcic.peko.PermissionRequester
import com.markodevcic.peko.PermissionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

suspend fun runWithPermissionCheck(
    action: Unit,
    permissionName: String,
    context: Context,
    rationaleMessage: String = ""
) {
    val requester = PermissionRequester.instance()
    requester.request(permissionName).collect { result ->
        when (result) {
            is PermissionResult.Granted -> {
                run { action }
            }

            is PermissionResult.Denied.DeniedPermanently -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.data =
                    Uri.fromParts("package", context.packageName, null)
                ContextCompat.startActivity(context, intent, null)
            }

            is PermissionResult.Denied.NeedsRationale -> {
                Toast.makeText(
                    context,
                    rationaleMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }

            is PermissionResult.Cancelled -> {
                return@collect
            }
        }
    }
}

fun showAppPermissionsFrame(context: Context, message: String = "") {
    if (context is Activity && message != "") {
        runBlocking(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            delay(1500L)
        }
    }
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.data = Uri.fromParts("package", context.packageName, null)
    context.startActivity(intent)
}