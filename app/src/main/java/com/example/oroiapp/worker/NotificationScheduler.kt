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

    /**
     * Schedules a one-time reminder that fires at 9:00 AM, 2 days before the
     * next renewal of [subscription].
     *
     * Logic:
     *  1. Start from firstPaymentDate and advance by billing cycle until the
     *     date is strictly in the future (this gives us the next renewal).
     *  2. Compute triggerTime = nextRenewal - 2 days, normalised to 09:00.
     *  3. If triggerTime is already in the past (payment is < 2 days away),
     *     advance one more billing cycle so the NEXT renewal gets a reminder.
     *  4. Enqueue a OneTimeWorkRequest with the exact delay.
     */
    fun scheduleReminder(context: Context, subscription: Subscription) {
        val workManager = WorkManager.getInstance(context)
        val workTag = "reminder_${subscription.id}"

        // Cancel any existing reminder for this subscription (e.g. after edits)
        workManager.cancelAllWorkByTag(workTag)

        val now = Calendar.getInstance()

        // Step 1 — find the next future renewal date
        val nextRenewal = Calendar.getInstance().apply { time = subscription.firstPaymentDate }
        while (!nextRenewal.after(now)) {
            nextRenewal.advanceByCycle(subscription.billingCycle)
        }

        // Step 2 — trigger at 09:00, two days before that renewal
        val triggerTime = (nextRenewal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -2)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Step 3 — if the trigger has already passed, jump to the next cycle
        if (!triggerTime.after(now)) {
            nextRenewal.advanceByCycle(subscription.billingCycle)
            triggerTime.apply {
                time = nextRenewal.time
                add(Calendar.DAY_OF_YEAR, -2)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }

        val delay = triggerTime.timeInMillis - now.timeInMillis
        if (delay <= 0) return // Should never happen after the step-3 advance, but guard anyway

        // Step 4 — enqueue
        val inputData = Data.Builder()
            .putInt("SUBSCRIPTION_ID", subscription.id)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(workTag)
            .build()

        workManager.enqueue(workRequest)
    }

    fun cancelReminder(context: Context, subscriptionId: Int) {
        WorkManager.getInstance(context).cancelAllWorkByTag("reminder_$subscriptionId")
    }

    private fun Calendar.advanceByCycle(cycle: BillingCycle) {
        when (cycle) {
            BillingCycle.WEEKLY  -> add(Calendar.WEEK_OF_YEAR, 1)
            BillingCycle.MONTHLY -> add(Calendar.MONTH, 1)
            BillingCycle.ANNUAL  -> add(Calendar.YEAR, 1)
        }
    }
}
