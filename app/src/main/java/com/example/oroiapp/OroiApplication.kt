package com.example.oroiapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.oroiapp.data.AppDatabase
import com.example.oroiapp.data.SubscriptionDao
import com.example.oroiapp.viewmodel.OroiViewModelFactory
import com.example.oroiapp.data.UserPreferencesRepository
import com.example.oroiapp.model.CancellationLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OroiApplication : Application() {
    companion object {
        const val CHANNEL_ID = "subscription_reminders"
    }

    override fun onCreate() {
        super.onCreate()
        val databaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Coroutine bat abiarazi hasierako datuak (estekak) txertatzeko
                CoroutineScope(Dispatchers.IO).launch {
                    // Oharra: Hemen zuzenean datu-basearen instantzia berria lortuko dugu,
                    // Factory-a une honetan oraindik ez baitago guztiz konfiguratuta.
                    val database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "oroi_database").build()
                    val dao = database.cancellationLinkDao()
                    dao.insertAll(getInitialCancellationLinks())
                }
            }
        }
        // Datu-basea sortu
        val database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "oroi_database"
        ).addCallback(databaseCallback).build()

        val userPreferencesRepository = UserPreferencesRepository(applicationContext)

        // Factory-ari DAO-aren instantzia eman aplikazioa hasten denean
        OroiViewModelFactory.dao = database.subscriptionDao()

        OroiViewModelFactory.cancellationDao = database.cancellationLinkDao()

        OroiViewModelFactory.userPrefs = userPreferencesRepository

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
            // Gehitu nahi dituzun beste esteka guztiak hemen
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Harpidetzen Gogorarazpenak"
            val descriptionText = "Harpidetzak berritu baino lehen abisuak jasotzeko kanala."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Erregistratu kanala sistemarekin
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
