package com.renttracker.app

import android.app.Application
import androidx.room.Room
import com.renttracker.app.data.database.RentTrackerDatabase
import com.renttracker.app.data.preferences.PreferencesManager
import com.renttracker.app.data.repository.RentTrackerRepository

class RentTrackerApplication : Application() {
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            RentTrackerDatabase::class.java,
            "rent_tracker_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    val repository by lazy {
        RentTrackerRepository(
            ownerDao = database.ownerDao(),
            buildingDao = database.buildingDao(),
            tenantDao = database.tenantDao(),
            paymentDao = database.paymentDao(),
            documentDao = database.documentDao()
        )
    }

    val preferencesManager by lazy {
        PreferencesManager(applicationContext)
    }
}
