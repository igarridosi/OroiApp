package com.example.oroiapp.worker

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.oroiapp.model.BillingCycle
import com.example.oroiapp.model.Subscription
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleReminder(context: Context, subscription: Subscription) {
        val workManager = WorkManager.getInstance(context)
        val workTag = "reminder_${subscription.id}" // Ataza bakoitzarentzat etiketa bakarra

        // 1. Aurretik zegoen edozein abisu ezeztatu harpidetza honentzat (editatzean ezinbestekoa)
        workManager.cancelAllWorkByTag(workTag)

        // 2. Kalkulatu atzerapena: hurrengo ordainketa - 2 egun - orain
        val now = Calendar.getInstance()
        val nextPayment = Calendar.getInstance().apply { time = subscription.firstPaymentDate }

        // Hurrengo ordainketa benetan noiz den kalkulatu
        while (nextPayment.before(now)) {
            when (subscription.billingCycle) {
                BillingCycle.WEEKLY -> nextPayment.add(Calendar.WEEK_OF_YEAR, 1)
                BillingCycle.MONTHLY -> nextPayment.add(Calendar.MONTH, 1)
                BillingCycle.ANNUAL -> nextPayment.add(Calendar.YEAR, 1)
            }
        }

        nextPayment.add(Calendar.DAY_OF_YEAR, -2) // Kendu 2 egun
        val delay = nextPayment.timeInMillis - now.timeInMillis

        if (delay > 0) {
            // 3. Datuak prestatu (Worker-ari IDa pasatzeko)
            val inputData = Data.Builder()
                .putInt("SUBSCRIPTION_ID", subscription.id)
                .build()

            // 4. Ataza sortu eta programatu
            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                // .setInitialDelay(5, TimeUnit.SECONDS)
                .setInputData(inputData)
                .addTag(workTag)
                .build()

            workManager.enqueue(workRequest)
        }
    }

    fun cancelReminder(context: Context, subscriptionId: Int) {
        WorkManager.getInstance(context).cancelAllWorkByTag("reminder_$subscriptionId")
    }
}