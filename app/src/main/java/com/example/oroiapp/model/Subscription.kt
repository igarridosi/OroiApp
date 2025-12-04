package com.example.oroiapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

// Fakturazio-ziklorako Enum-a
enum class BillingCycle {
    WEEKLY, // Astero
    MONTHLY, // Hilero
    ANNUAL // Urtero
}

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val amount: Double,
    val currency: String,
    val billingCycle: BillingCycle,
    val firstPaymentDate: Date,
)