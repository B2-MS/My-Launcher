package com.mylauncher.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the AppWidgetHost lifecycle and widget allocation.
 * The host ID is a unique constant for this launcher.
 */
@Singleton
class LauncherWidgetHost @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val HOST_ID = 1024
    }

    val appWidgetHost: AppWidgetHost = AppWidgetHost(context, HOST_ID)
    val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)

    /** Start listening for widget updates. Call from Activity.onStart(). */
    fun startListening() {
        try {
            appWidgetHost.startListening()
        } catch (_: Exception) {
            // May throw if widgets aren't supported
        }
    }

    /** Stop listening for widget updates. Call from Activity.onStop(). */
    fun stopListening() {
        try {
            appWidgetHost.stopListening()
        } catch (_: Exception) { }
    }

    /** Allocate a new widget ID for the pick flow. */
    fun allocateWidgetId(): Int = appWidgetHost.allocateAppWidgetId()

    /** Delete a widget ID when a widget tile is removed. */
    fun deleteWidgetId(appWidgetId: Int) {
        appWidgetHost.deleteAppWidgetId(appWidgetId)
    }

    /** Create a host view for a bound widget. */
    fun createView(appWidgetId: Int): android.appwidget.AppWidgetHostView {
        val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
        return appWidgetHost.createView(context, appWidgetId, info)
    }

    /** Get info for a bound widget. */
    fun getWidgetInfo(appWidgetId: Int): AppWidgetProviderInfo? {
        return appWidgetManager.getAppWidgetInfo(appWidgetId)
    }

    /** Get all installed widget providers. */
    fun getInstalledProviders(): List<AppWidgetProviderInfo> {
        return appWidgetManager.installedProviders
    }
}
