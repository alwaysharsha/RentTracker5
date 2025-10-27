package com.renttracker.app.data.database

import androidx.room.TypeConverter
import com.renttracker.app.data.model.*

class Converters {
    @TypeConverter
    fun fromPropertyType(value: PropertyType): String {
        return value.name
    }

    @TypeConverter
    fun toPropertyType(value: String): PropertyType {
        return PropertyType.valueOf(value)
    }

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String {
        return value.name
    }

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus {
        return PaymentStatus.valueOf(value)
    }

    @TypeConverter
    fun fromEntityType(value: EntityType): String {
        return value.name
    }

    @TypeConverter
    fun toEntityType(value: String): EntityType {
        return EntityType.valueOf(value)
    }
}
