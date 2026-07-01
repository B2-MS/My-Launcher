package com.mylauncher.data.model

import android.graphics.drawable.Drawable

/**
 * Represents an installed app discovered via PackageManager / LauncherApps.
 * Work-profile apps have a non-zero [userSerialNumber] that differs from
 * the personal profile's serial, and [isWorkProfile] is true.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val userSerialNumber: Long = 0L,
    val isWorkProfile: Boolean = false
)
