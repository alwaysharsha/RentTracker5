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
    fun fromPaymentMethod(value: PaymentMethod): String {
        return value.name
    }

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod {
        return PaymentMethod.valueOf(value)
    }

    @TypeConverter
    fun fromBankType(value: BankType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toBankType(value: String?): BankType? {
        return value?.let { BankType.valueOf(it) }
    }

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String {
        return value.name
    }

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus {
        return PaymentStatus.valueOf(value)
    }
}
