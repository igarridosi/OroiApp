package com.example.oroiapp.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.oroiapp.MainActivity
import com.example.oroiapp.OroiApplication
import com.example.oroiapp.R
import com.example.oroiapp.model.BillingCycle
import com.example.oroiapp.viewmodel.OroiViewModelFactory

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val subscriptionId = inputData.getInt("SUBSCRIPTION_ID", -1)
        if (subscriptionId == -1) return Result.failure()

        val dao = OroiViewModelFactory.dao
        val subscription = dao.getSubscriptionById(subscriptionId) ?: return Result.failure()

        val annualCost = when (subscription.billingCycle) {
            BillingCycle.WEEKLY -> subscription.amount * 52
            BillingCycle.MONTHLY -> subscription.amount * 12
            BillingCycle.ANNUAL -> subscription.amount
        }

        val title = context.getString(R.string.notification_title, subscription.name)
        val content = context.getString(R.string.notification_content, annualCost)

        showNotification(subscriptionId, title, content)
        return Result.success()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(notificationId: Int, title: String, content: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, OroiApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
