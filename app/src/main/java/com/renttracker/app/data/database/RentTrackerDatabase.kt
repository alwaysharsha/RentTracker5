package com.renttracker.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.renttracker.app.data.dao.*
import com.renttracker.app.data.model.*

@Database(
    entities = [
        Owner::class,
        Building::class,
        Tenant::class,
        Payment::class,
        Document::class,
        Vendor::class,
        Expense::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RentTrackerDatabase : RoomDatabase() {
    abstract fun ownerDao(): OwnerDao
    abstract fun buildingDao(): BuildingDao
    abstract fun tenantDao(): TenantDao
    abstract fun paymentDao(): PaymentDao
    abstract fun documentDao(): DocumentDao
    abstract fun vendorDao(): VendorDao
    abstract fun expenseDao(): ExpenseDao
}
