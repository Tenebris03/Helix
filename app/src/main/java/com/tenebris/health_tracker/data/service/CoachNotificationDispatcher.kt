package com.tenebris.health_tracker.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tenebris.health_tracker.MainActivity
import com.tenebris.health_tracker.R

object CoachNotificationDispatcher {
    private const val CHANNEL_ID = "invisible_coach"
    private const val NOTIFICATION_ID = 1001

    fun triggerTactileAlert(
        context: Context,
        headline: String,
        body: String,
    ) {
        createChannel(context)
        sendNotification(context, headline, body)
        vibrateDoubleTap(context)
    }

    private fun createChannel(context: Context) {
        val name = "Invisible Coach"
        val description = "Behavioral nudges and pattern alerts"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel =
            NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun sendNotification(
        context: Context,
        headline: String,
        body: String,
    ) {
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(headline)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun vibrateDoubleTap(context: Context) {
        try {
            val vibrator =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    manager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                }

            val pattern =
                VibrationEffect.createWaveform(
                    longArrayOf(0, 15, 40, 25),
                    intArrayOf(0, 255, 0, 255),
                    -1,
                )
            vibrator?.vibrate(pattern)
        } catch (_: Exception) {
            // Silently fail if vibration not supported
        }
    }
}
