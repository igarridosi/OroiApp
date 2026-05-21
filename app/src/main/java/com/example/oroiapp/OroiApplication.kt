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
            // Streaming — video
            CancellationLink("Netflix", "https://www.netflix.com/cancelplan"),
            CancellationLink("HBO Max", "https://www.hbomax.com/account"),
            CancellationLink("Disney+", "https://www.disneyplus.com/account/cancel-subscription"),
            CancellationLink("Amazon Prime", "https://www.amazon.com/prime/cancel"),
            CancellationLink("Apple TV+", "https://tv.apple.com/settings"),
            CancellationLink("Hulu", "https://secure.hulu.com/account/cancel"),
            CancellationLink("Paramount+", "https://www.paramountplus.com/account/"),
            CancellationLink("Crunchyroll", "https://www.crunchyroll.com/acct/membership"),
            CancellationLink("YouTube Premium", "https://www.youtube.com/paid_memberships"),
            CancellationLink("Twitch", "https://www.twitch.tv/subscriptions"),
            // Streaming — music
            CancellationLink("Spotify", "https://www.spotify.com/account/subscription/cancel/"),
            CancellationLink("Apple Music", "https://music.apple.com/account/billing"),
            CancellationLink("Tidal", "https://account.tidal.com/"),
            CancellationLink("Deezer", "https://www.deezer.com/account/offers"),
            // Cloud & productivity
            CancellationLink("iCloud", "https://appleid.apple.com/account/manage/section/subscriptions"),
            CancellationLink("Google One", "https://one.google.com/storage"),
            CancellationLink("Dropbox", "https://www.dropbox.com/account/plan"),
            CancellationLink("Microsoft 365", "https://account.microsoft.com/services/"),
            CancellationLink("Adobe Creative Cloud", "https://account.adobe.com/plans"),
            CancellationLink("Grammarly", "https://account.grammarly.com/subscription"),
            // Gaming
            CancellationLink("Xbox Game Pass", "https://account.microsoft.com/services/"),
            CancellationLink("PlayStation Plus", "https://www.playstation.com/en-us/playstation-plus/"),
            CancellationLink("Nintendo Switch Online", "https://accounts.nintendo.com/profile/subscriptions"),
            CancellationLink("EA Play", "https://myaccount.ea.com/cp-ui/subscriptions/index"),
            // Fitness & wellness
            CancellationLink("Strava", "https://www.strava.com/account"),
            CancellationLink("Headspace", "https://www.headspace.com/settings/subscriptions"),
            CancellationLink("Calm", "https://www.calm.com/settings"),
            CancellationLink("MyFitnessPal", "https://www.myfitnesspal.com/account/subscription"),
            // VPN & security
            CancellationLink("NordVPN", "https://my.nordaccount.com/"),
            CancellationLink("ExpressVPN", "https://www.expressvpn.com/subscriptions/"),
            // News & reading
            CancellationLink("New York Times", "https://myaccount.nytimes.com/seg/subscription"),
            CancellationLink("Kindle Unlimited", "https://www.amazon.com/kindle-dbs/hz/subscriptions/manage")
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
