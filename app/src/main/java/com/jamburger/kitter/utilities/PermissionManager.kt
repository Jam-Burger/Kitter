package com.jamburger.kitter.utilities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.jamburger.kitter.activities.StartActivity
import com.jamburger.kitter.utilities.Constants.TAG

object PermissionManager {
    fun askPermissions(activity: StartActivity) {
        val notificationPermissionLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { _: Boolean -> }

        val cameraPermissionLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { _: Boolean -> }

        val storagePermissionLauncher =
            activity.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { _: Map<String, Boolean> -> }

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "askPermissions: FCM SDK (and your app) can post notifications.")
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Toast.makeText(
                    activity,
                    "askPermissions: enabling notifications is cool bruh!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            storagePermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }
    }
}
