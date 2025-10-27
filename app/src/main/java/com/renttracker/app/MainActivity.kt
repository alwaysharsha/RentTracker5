package com.renttracker.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.renttracker.app.ui.navigation.RentTrackerApp
import com.renttracker.app.ui.theme.RentTrackerTheme
import com.renttracker.app.ui.viewmodel.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private lateinit var ownerViewModel: OwnerViewModel
    private lateinit var buildingViewModel: BuildingViewModel
    private lateinit var tenantViewModel: TenantViewModel
    private lateinit var paymentViewModel: PaymentViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var documentViewModel: DocumentViewModel
    private lateinit var exportImportViewModel: ExportImportViewModel
    private var isAuthenticated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = application as RentTrackerApplication
        val viewModelFactory = ViewModelFactory(app.repository, app.preferencesManager, applicationContext)

        ownerViewModel = ViewModelProvider(this, viewModelFactory)[OwnerViewModel::class.java]
        buildingViewModel = ViewModelProvider(this, viewModelFactory)[BuildingViewModel::class.java]
        tenantViewModel = ViewModelProvider(this, viewModelFactory)[TenantViewModel::class.java]
        paymentViewModel = ViewModelProvider(this, viewModelFactory)[PaymentViewModel::class.java]
        settingsViewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]
        documentViewModel = ViewModelProvider(this, viewModelFactory)[DocumentViewModel::class.java]
        exportImportViewModel = ViewModelProvider(this, viewModelFactory)[ExportImportViewModel::class.java]

        // Check if app lock is enabled
        lifecycleScope.launch {
            val appLockEnabled = app.preferencesManager.appLockFlow.first()
            if (appLockEnabled) {
                authenticateUser()
            } else {
                isAuthenticated = true
                initializeApp()
            }
        }
    }

    private fun authenticateUser() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Biometric not available, proceed without authentication
                isAuthenticated = true
                initializeApp()
            }
            else -> {
                isAuthenticated = true
                initializeApp()
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        // User cancelled, close the app
                        finish()
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isAuthenticated = true
                    initializeApp()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Authentication failed, but user can try again
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Rent Tracker")
            .setSubtitle("Authenticate to access the app")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun initializeApp() {
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
                        settingsViewModel = settingsViewModel,
                        documentViewModel = documentViewModel,
                        exportImportViewModel = exportImportViewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check authentication when app resumes from background
        if (!isAuthenticated) {
            lifecycleScope.launch {
                val app = application as RentTrackerApplication
                val appLockEnabled = app.preferencesManager.appLockFlow.first()
                if (appLockEnabled) {
                    authenticateUser()
                }
            }
        }
    }
}
