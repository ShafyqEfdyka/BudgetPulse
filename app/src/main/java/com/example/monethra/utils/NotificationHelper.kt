package com.example.monethra.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.monethra.MainActivity
import com.example.monethra.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val TAG = "BudgetPulseNotification"
        const val CHANNEL_ID = "budgetpulse_alerts"
        const val CHANNEL_NAME = "Budget Alerts & Reminders"

        const val ID_CONFIRMATION = 1000
        const val ID_DAILY_REMINDER = 1001
        const val ID_BUDGET_ALERT = 1002

        fun logD(tag: String, message: String) {
            try {
                Log.d(tag, message)
            } catch (_: Throwable) {
                println("[$tag] $message")
            }
        }

        fun logE(tag: String, message: String, throwable: Throwable? = null) {
            try {
                if (throwable != null) {
                    Log.e(tag, message, throwable)
                } else {
                    Log.e(tag, message)
                }
            } catch (_: Throwable) {
                println("[$tag] ERROR: $message")
            }
        }
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for budget threshold alerts and daily reminders"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showBudgetAlert(title: String, message: String) {
        if (!hasNotificationPermission()) {
            logD(TAG, "Suppressed notification ($title): POST_NOTIFICATIONS permission not granted")
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createPendingIntent())
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(ID_BUDGET_ALERT, notification)
            logD(TAG, "Triggered budget alert notification: $title — $message")
        } catch (e: SecurityException) {
            logE(TAG, "Failed to show budget alert notification: ${e.message}")
        }
    }

    fun showDailyReminder() {
        if (!hasNotificationPermission()) {
            logD(TAG, "Suppressed daily reminder: POST_NOTIFICATIONS permission not granted")
            return
        }

        val title = "Track Today's Expenses"
        val message = "Don't forget to log your daily expenses in BudgetPulse to keep your monthly budget accurate!"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createPendingIntent())
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(ID_DAILY_REMINDER, notification)
            logD(TAG, "Triggered daily reminder notification: $title")
        } catch (e: SecurityException) {
            logE(TAG, "Failed to show daily reminder notification: ${e.message}")
        }
    }

    fun showConfirmationNotification() {
        if (!hasNotificationPermission()) {
            logD(TAG, "Suppressed confirmation: POST_NOTIFICATIONS permission not granted")
            return
        }

        val title = "BudgetPulse Alerts Active"
        val message = "You'll be notified when category spending reaches 80% or exceeds its limit."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createPendingIntent())
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(ID_CONFIRMATION, notification)
            logD(TAG, "Triggered confirmation notification: $title — $message")
        } catch (e: SecurityException) {
            logE(TAG, "Failed to show confirmation notification: ${e.message}")
        }
    }
}
