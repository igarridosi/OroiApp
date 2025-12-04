package com.example.oroiapp.data

import androidx.room.TypeConverter
import com.example.oroiapp.model.BillingCycle
import java.util.Date

class Converters {
    // 'Long' motatik 'Date' motara bihurtzen du
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    // 'Date' motatik 'Long' motara bihurtzen du
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // 'String' motatik 'BillingCycle' motara bihurtzen du
    @TypeConverter
    fun fromBillingCycle(value: String?): BillingCycle? {
        return value?.let { BillingCycle.valueOf(it) }
    }

    // 'BillingCycle' motatik 'String' motara bihurtzen du
    @TypeConverter
    fun billingCycleToString(billingCycle: BillingCycle?): String? {
        return billingCycle?.name
    }
}