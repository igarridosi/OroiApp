package com.example.oroiapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cancellation_links")
data class CancellationLink(
    @PrimaryKey
    val serviceName: String, // Adibidez, "Netflix", "Spotify", "Amazon Prime"
    val url: String          // Ezeztapen-orriaren URL-a
)