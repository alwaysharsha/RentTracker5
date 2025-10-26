package com.renttracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.renttracker.app.ui.navigation.RentTrackerApp
import com.renttracker.app.ui.theme.RentTrackerTheme
import com.renttracker.app.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    private lateinit var ownerViewModel: OwnerViewModel
    private lateinit var buildingViewModel: BuildingViewModel
    private lateinit var tenantViewModel: TenantViewModel
    private lateinit var paymentViewModel: PaymentViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = application as RentTrackerApplication
        val viewModelFactory = ViewModelFactory(app.repository, app.preferencesManager)

        ownerViewModel = ViewModelProvider(this, viewModelFactory)[OwnerViewModel::class.java]
        buildingViewModel = ViewModelProvider(this, viewModelFactory)[BuildingViewModel::class.java]
        tenantViewModel = ViewModelProvider(this, viewModelFactory)[TenantViewModel::class.java]
        paymentViewModel = ViewModelProvider(this, viewModelFactory)[PaymentViewModel::class.java]
        settingsViewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]

        setContent {
            RentTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RentTrackerApp(
                        ownerViewModel = ownerViewModel,
                        buildingViewModel = buildingViewModel,
                        tenantViewModel = tenantViewModel,
                        paymentViewModel = paymentViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
