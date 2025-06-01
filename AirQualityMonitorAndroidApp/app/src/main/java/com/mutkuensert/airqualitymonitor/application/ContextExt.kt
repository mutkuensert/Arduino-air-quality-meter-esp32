package com.mutkuensert.airqualitymonitor.application

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.core.view.WindowCompat.getInsetsController
import androidx.core.view.WindowInsetsControllerCompat

fun Context.setStatusBarAppearance(isLight: Boolean) {
    getInsetsController()?.isAppearanceLightStatusBars = isLight
}

fun Context.getInsetsController(): WindowInsetsControllerCompat? {
    val activity = asActivity() ?: return null
    return getInsetsController(
        activity.window,
        activity.window.decorView
    )
}

fun Context.asActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> this.baseContext.asActivity()
        else -> null
    }
}