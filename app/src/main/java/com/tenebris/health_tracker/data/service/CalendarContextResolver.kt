package com.tenebris.health_tracker.data.service

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract

class CalendarContextResolver(private val context: Context) {

    fun getRecentCalendarEvents(windowHours: Long = 3): String {
        if (!hasCalendarPermission()) return ""

        val now = System.currentTimeMillis()
        val windowStart = now - windowHours * 3600000L
        val windowEnd = now + windowHours * 3600000L

        val uri = CalendarContract.Instances.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END
        )
        val selection = "${CalendarContract.Instances.BEGIN} >= ? AND ${CalendarContract.Instances.END} <= ?"
        val selectionArgs = arrayOf(windowStart.toString(), windowEnd.toString())

        val events = mutableListOf<String>()
        var cursor: Cursor? = null

        try {
            val instanceUri = Uri.parse(
                "content://com.android.calendar/instances/when/${windowStart}/${windowEnd}"
            )
            cursor = context.contentResolver.query(
                instanceUri,
                projection, null, null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            cursor?.use { c ->
                val titleIdx = c.getColumnIndex(CalendarContract.Instances.TITLE)
                while (c.moveToNext()) {
                    val title = c.getString(titleIdx) ?: continue
                    val tag = when {
                        title.contains("urgent", ignoreCase = true) -> "(urgent)"
                        title.contains("deadline", ignoreCase = true) -> "(deadline)"
                        title.contains("review", ignoreCase = true) -> "(review)"
                        else -> ""
                    }
                    events.add("\"$title\" $tag")
                }
            }
        } catch (_: SecurityException) {
            return ""
        }

        if (events.isEmpty()) return "No events in window"

        val backToBack = if (events.size >= 3) " | Back-to-back meetings detected" else ""
        return "${events.joinToString(", ")}$backToBack"
    }

    private fun hasCalendarPermission(): Boolean {
        return try {
            val permission = android.Manifest.permission.READ_CALENDAR
            context.checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    }
}
