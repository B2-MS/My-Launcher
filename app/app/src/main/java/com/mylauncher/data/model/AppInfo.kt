package com.mylauncher.data.model

import android.graphics.drawable.Drawable

/**
 * Represents an installed app discovered via PackageManager.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)
