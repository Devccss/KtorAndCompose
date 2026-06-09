package org.example.project

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.core.app.ActivityCompat

object AndroidActivityProvider {
    lateinit var activity: Activity
}

actual fun requestNotificationPermission() {

    val activity = AndroidActivityProvider.activity

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            100
        )
    }
}