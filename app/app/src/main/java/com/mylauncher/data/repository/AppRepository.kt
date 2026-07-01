package com.mylauncher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Process
import android.os.UserManager
import com.mylauncher.data.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val userManager =
        context.getSystemService(Context.USER_SERVICE) as UserManager

    /**
     * Queries LauncherApps for all launchable apps across every user profile
     * (personal + work), sorted alphabetically.  Work-profile apps are tagged
     * with [AppInfo.isWorkProfile] = true and carry the work-profile serial.
     */
    fun getInstalledApps(): List<AppInfo> {
        val mySerial = userManager.getSerialNumberForUser(Process.myUserHandle())
        val results = mutableListOf<AppInfo>()

        for (profile in userManager.userProfiles) {
            val serial = userManager.getSerialNumberForUser(profile)
            val isWork = serial != mySerial

            launcherApps.getActivityList(null, profile).forEach { lai ->
                results += AppInfo(
                    packageName = lai.componentName.packageName,
                    appName = lai.label.toString(),
                    icon = lai.getBadgedIcon(0),
                    userSerialNumber = serial,
                    isWorkProfile = isWork
                )
            }
        }

        return results
            .distinctBy { "${it.packageName}::${it.userSerialNumber}" }
            .sortedBy { it.appName.lowercase() }
    }

    /**
     * Returns the launch intent for a given personal-profile package.
     */
    fun getLaunchIntent(packageName: String): Intent? {
        return context.packageManager.getLaunchIntentForPackage(packageName)
    }

    /**
     * Launches an app.  For work-profile apps (serial ≠ personal) uses
     * [LauncherApps]; for personal-profile apps uses a regular Intent.
     */
    fun launchApp(packageName: String, userSerialNumber: Long) {
        val mySerial = userManager.getSerialNumberForUser(Process.myUserHandle())

        if (userSerialNumber == mySerial || userSerialNumber == 0L) {
            // Personal profile — regular intent
            val intent = getLaunchIntent(packageName) ?: return
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            // Work profile — LauncherApps API
            val userHandle = userManager.getUserForSerialNumber(userSerialNumber) ?: return
            val activities = launcherApps.getActivityList(packageName, userHandle)
            if (activities.isNotEmpty()) {
                launcherApps.startMainActivity(
                    activities[0].componentName, userHandle, null, null
                )
            }
        }
    }
}
