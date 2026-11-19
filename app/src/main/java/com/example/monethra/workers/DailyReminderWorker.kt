package com.example.monethra.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.monethra.BudgetPulseApplication
import com.example.monethra.utils.NotificationHelper
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class DailyReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "daily_budgetpulse_reminder"

        fun schedule(context: Context) {
            val reminderWork = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                reminderWork
            )
            NotificationHelper.logD(NotificationHelper.TAG, "Scheduled 24h periodic WorkManager job: $WORK_NAME")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            NotificationHelper.logD(NotificationHelper.TAG, "Cancelled WorkManager job: $WORK_NAME")
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as? BudgetPulseApplication
            val prefs = app?.userPreferencesRepository
            val notificationsEnabled = prefs?.notificationsEnabled?.first() ?: false

            if (notificationsEnabled) {
                NotificationHelper.logD(NotificationHelper.TAG, "DailyReminderWorker executing reminder")
                NotificationHelper(applicationContext).showDailyReminder()
            } else {
                NotificationHelper.logD(NotificationHelper.TAG, "DailyReminderWorker skipped: notifications disabled in preferences")
            }
            Result.success()
        } catch (e: Exception) {
            NotificationHelper.logE(NotificationHelper.TAG, "DailyReminderWorker error: ${e.message}", e)
            Result.failure()
        }
    }
}
