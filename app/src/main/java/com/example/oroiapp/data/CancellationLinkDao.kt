package com.example.oroiapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.oroiapp.model.CancellationLink
import kotlinx.coroutines.flow.Flow

@Dao
interface CancellationLinkDao {
    // Zerbitzu baten ezeztapen-esteka bere izenaren bidez bilatzen du
    @Query("SELECT * FROM cancellation_links WHERE serviceName LIKE :serviceName LIMIT 1")
    suspend fun findLinkByName(serviceName: String): CancellationLink?

    // Estekak txertatzeko (aplikazioa lehen aldiz hasten denean erabiliko dugu)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(links: List<CancellationLink>)

    @Query("SELECT serviceName FROM cancellation_links ORDER BY serviceName ASC")
    fun getAllServiceNames(): Flow<List<String>>
}