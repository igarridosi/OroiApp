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
import kotlin.random.Random

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        // 1. Lortu harpidetzaren IDa, programatzerakoan pasatu dioguna
        val subscriptionId = inputData.getInt("SUBSCRIPTION_ID", -1)
        if (subscriptionId == -1) {
            return Result.failure()
        }

        // 2. Lortu DAO-a datu-basea kontsultatzeko
        val dao = OroiViewModelFactory.dao
        val subscription = dao.getSubscriptionById(subscriptionId) ?: return Result.failure()

        // 3. Kalkulatu urteko aurrezkia
        val annualCost = when (subscription.billingCycle) {
            BillingCycle.WEEKLY -> subscription.amount * 52
            BillingCycle.MONTHLY -> subscription.amount * 12
            BillingCycle.ANNUAL -> subscription.amount
        }

        // 4. Sortu eta erakutsi notifikazioa
        val title = "${subscription.name} berritzear dago"
        val content = "Bi egun barru berrituko da. Ezeztatuz gero, %.2f€ aurreztuko zenituzke urtean.".format(annualCost)

        showNotification(title, content)

        return Result.success()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(title: String, content: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Zure aplikazioaren ikonoa erabili
        val notification = NotificationCompat.Builder(context, OroiApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ziurtatu ikono hau existitzen dela
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Erabiltzaileak klik egitean ezabatzen da
            .setContentIntent(pendingIntent)
            .build()

        // Erakutsi notifikazioa (baimenak behar dira API 33+)
        try {
            NotificationManagerCompat.from(context).notify(Random.nextInt(), notification)
        } catch (e: SecurityException) {
            // Android 13+ bertsioan, erabiltzaileak notifikazio baimenak ukatu baditu
            // Aplikazioak ez du huts egingo, baina notifikazioa ez da agertuko
            e.printStackTrace()
        }
    }
}