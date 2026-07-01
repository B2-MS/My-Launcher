package com.mylauncher.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Data class representing live-tile badge information extracted from
 * active notifications for a single package.
 */
data class LiveTileInfo(
    val badgeCount: Int = 0,
    val line1: String? = null,   // primary line (e.g. "3 unread emails")
    val line2: String? = null    // secondary line (e.g. "5 activities")
)

/**
 * [NotificationListenerService] that reads active notifications and
 * publishes per-package [LiveTileInfo] for tiles that have `isLiveTile`
 * enabled.
 *
 * The user must grant "Notification access" in Android Settings for this
 * service to receive callbacks.
 */
class NotificationService : NotificationListenerService() {

    companion object {
        private val _liveTileData = MutableStateFlow<Map<String, LiveTileInfo>>(emptyMap())

        /** Observe per-package live tile data from any Composable / ViewModel. */
        val liveTileData: StateFlow<Map<String, LiveTileInfo>> = _liveTileData.asStateFlow()

        // Well-known package names
        private const val PKG_OUTLOOK = "com.microsoft.office.outlook"
        private const val PKG_TEAMS   = "com.microsoft.teams"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        rebuildLiveTileData()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        rebuildLiveTileData()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        rebuildLiveTileData()
    }

    /**
     * Scan all active notifications and build per-package [LiveTileInfo].
     * Called whenever a notification is added, removed, or the listener
     * first connects.
     */
    private fun rebuildLiveTileData() {
        val notifications = try {
            activeNotifications?.toList() ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        val result = mutableMapOf<String, LiveTileInfo>()

        // ── Outlook: unread email count ─────────────────────────────
        val outlookNotifs = notifications.filter { it.packageName == PKG_OUTLOOK }
        if (outlookNotifs.isNotEmpty()) {
            // Outlook's summary/group notification typically carries the total
            // unread count in Notification.number.  Fall back to counting
            // individual notification entries.
            val summaryCount = outlookNotifs
                .mapNotNull { it.notification?.number?.takeIf { n -> n > 0 } }
                .maxOrNull()
            val count = summaryCount ?: outlookNotifs.size

            result[PKG_OUTLOOK] = LiveTileInfo(
                badgeCount = count,
                line1 = if (count == 1) "1 unread email" else "$count unread emails"
            )
        }

        // ── Teams: unread IMs + activity count ──────────────────────
        val teamsNotifs = notifications.filter { it.packageName == PKG_TEAMS }
        if (teamsNotifs.isNotEmpty()) {
            // Teams posts separate notifications for chat messages vs.
            // activity feed items.  Chat notifications usually have the
            // "Messages" category or contain person icons; activity feed
            // items are typically in the "Activity" channel.  We use the
            // notification category tag when available, otherwise fall
            // back to counting them all as messages.
            var chatCount = 0
            var activityCount = 0

            for (sbn in teamsNotifs) {
                val category = sbn.notification?.category
                val tag = sbn.tag ?: ""
                val channelId = sbn.notification?.channelId ?: ""

                when {
                    // Teams categorises chat messages as "msg" category or
                    // channel IDs containing "chat" / "message"
                    channelId.contains("chat", ignoreCase = true) ||
                    channelId.contains("message", ignoreCase = true) ||
                    category == "msg" ||
                    tag.contains("chat", ignoreCase = true) -> chatCount++

                    // Activity feed
                    channelId.contains("activity", ignoreCase = true) ||
                    channelId.contains("feed", ignoreCase = true) ||
                    tag.contains("activity", ignoreCase = true) -> activityCount++

                    // Default: count as a chat message
                    else -> chatCount++
                }
            }

            val parts = mutableListOf<String>()
            if (chatCount > 0) parts.add("$chatCount unread IM${if (chatCount > 1) "s" else ""}")
            if (activityCount > 0) parts.add("$activityCount activit${if (activityCount > 1) "ies" else "y"}")

            result[PKG_TEAMS] = LiveTileInfo(
                badgeCount = chatCount + activityCount,
                line1 = parts.getOrNull(0),
                line2 = parts.getOrNull(1)
            )
        }

        // ── Generic: any other package with notifications ───────────
        val handledPackages = setOf(PKG_OUTLOOK, PKG_TEAMS)
        notifications
            .filter { it.packageName !in handledPackages }
            .groupBy { it.packageName }
            .forEach { (pkg, notifs) ->
                val count = notifs
                    .mapNotNull { it.notification?.number?.takeIf { n -> n > 0 } }
                    .maxOrNull() ?: notifs.size
                result[pkg] = LiveTileInfo(
                    badgeCount = count,
                    line1 = if (count == 1) "1 notification" else "$count notifications"
                )
            }

        _liveTileData.value = result
    }
}
