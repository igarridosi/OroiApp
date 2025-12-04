package com.example.oroiapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.oroiapp.model.CancellationLink
import com.example.oroiapp.model.Subscription

@Database(entities = [Subscription::class, CancellationLink::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun cancellationLinkDao(): CancellationLinkDao
}