package com.mutkuensert.airqualitymonitor.presentation

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.core.app.ActivityCompat.requestPermissions

object PermissionHandler {

    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            requestPermissions(activity, arrayOf(permission), 0)
        }
    }
}