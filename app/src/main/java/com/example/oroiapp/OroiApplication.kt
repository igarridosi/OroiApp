package com.example.oroiapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.Room
import com.example.oroiapp.data.AppDatabase
import com.example.oroiapp.data.UserPreferencesRepository
import com.example.oroiapp.model.CancellationLink
import com.example.oroiapp.viewmodel.OroiViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OroiApplication : Application() {
    companion object {
        const val CHANNEL_ID = "subscription_reminders"
    }

    override fun onCreate() {
        super.onCreate()

        val database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "oroi_database"
        ).build()

        val userPreferencesRepository = UserPreferencesRepository(applicationContext)

        OroiViewModelFactory.dao = database.subscriptionDao()
        OroiViewModelFactory.cancellationDao = database.cancellationLinkDao()
        OroiViewModelFactory.userPrefs = userPreferencesRepository

        // Insert initial cancellation links; IGNORE strategy skips duplicates silently
        CoroutineScope(Dispatchers.IO).launch {
            database.cancellationLinkDao().insertAll(getInitialCancellationLinks())
        }

        createNotificationChannel()
    }

    private fun getInitialCancellationLinks(): List<CancellationLink> {
        return listOf(
            CancellationLink("Netflix", "https://www.netflix.com/cancelplan"),
            CancellationLink("Spotify", "https://www.spotify.com/account/subscription/cancel/"),
            CancellationLink("Amazon Prime", "https://www.amazon.com/prime/cancel"),
            CancellationLink("HBO Max", "https://www.hbomax.com/account"),
            CancellationLink("Disney+", "https://www.disneyplus.com/account/cancel-subscription"),
            CancellationLink("Strava", "https://www.strava.com/account")
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
