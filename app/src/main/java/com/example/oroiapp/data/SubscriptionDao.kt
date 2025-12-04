package com.example.oroiapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.oroiapp.model.Subscription
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY name ASC")
    fun getAllSubscriptions(): Flow<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getSubscriptionById(id: Int): Subscription?

    // CRUD Funtzioak
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(subscription: Subscription)
    @Update
    suspend fun update(subscription: Subscription)
    @Delete
    suspend fun delete(subscription: Subscription)
}