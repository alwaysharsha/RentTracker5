package com.renttracker.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.renttracker.app.ui.navigation.RentTrackerApp
import com.renttracker.app.ui.theme.RentTrackerTheme
import com.renttracker.app.ui.viewmodel.*
import com.renttracker.app.utils.Constants
import com.renttracker.app.utils.showToast
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private lateinit var app: RentTrackerApplication
    private lateinit var ownerViewModel: OwnerViewModel
    private lateinit var buildingViewModel: BuildingViewModel
    private lateinit var tenantViewModel: TenantViewModel
    private lateinit var paymentViewModel: PaymentViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var documentViewModel: DocumentViewModel
    private lateinit var vendorViewModel: VendorViewModel
    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var exportImportViewModel: ExportImportViewModel
    private var isAuthenticated = false
    
    companion object {
        private const val IMPORT_FILE_PICKER_REQUEST_CODE = Constants.IMPORT_FILE_PICKER_REQUEST_CODE
        private const val DOCUMENT_FILE_PICKER_REQUEST_CODE = 1001
        private const val DOCUMENT_CAMERA_REQUEST_CODE = 1002
        const val GOOGLE_SIGN_IN_REQUEST_CODE = 9001 // Using high number to avoid conflicts
    }
    
    // Google Sign-In callback
    var onGoogleSignInResult: ((com.google.android.gms.auth.api.signin.GoogleSignInAccount?) -> Unit)? = null
    
    // Callback function to trigger import from Settings screen
    fun launchImportFilePicker() {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/zip", "application/x-zip-compressed", "application/json", "text/plain"))
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(Intent.createChooser(intent, Constants.BACKUP_FILE_CHOOSER_TITLE), IMPORT_FILE_PICKER_REQUEST_CODE)
        } catch (e: Exception) {
            android.util.Log.e(Constants.TAG_MAIN_ACTIVITY, "Exception launching file picker", e)
            showToast(this, Constants.ERROR_GENERIC_PREFIX + e.message, Constants.TOAST_DURATION_LONG)
        }
    }
    
    // Callback functions for document upload
    private var pendingDocumentName: String? = null
    private var pendingEntityType: com.renttracker.app.data.model.EntityType = com.renttracker.app.data.model.EntityType.TENANT
    private var pendingEntityId: Long? = null
    private var pendingNotes: String? = null
    private var pendingCameraUri: Uri? = null
    
    fun launchDocumentFilePicker(documentName: String? = null, entityType: com.renttracker.app.data.model.EntityType = com.renttracker.app.data.model.EntityType.TENANT, notes: String? = null, entityId: Long? = null) {
        try {
            pendingDocumentName = documentName
            pendingEntityType = entityType
            pendingEntityId = entityId
            pendingNotes = notes
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(Intent.createChooser(intent, "Select a file"), DOCUMENT_FILE_PICKER_REQUEST_CODE)
        } catch (e: Exception) {
            android.util.Log.e(Constants.TAG_MAIN_ACTIVITY, "Exception launching document file picker", e)
            showToast(this, "Error launching file picker: ${e.message}", Constants.TOAST_DURATION_LONG)
        }
    }
    
    fun launchDocumentCamera(documentName: String? = null, entityType: com.renttracker.app.data.model.EntityType = com.renttracker.app.data.model.EntityType.TENANT, notes: String? = null, entityId: Long? = null) {
        try {
            pendingDocumentName = documentName
            pendingEntityType = entityType
            pendingEntityId = entityId
            pendingNotes = notes
            val photoUri = createImageUri()
            photoUri?.let { uri ->
                pendingCameraUri = uri // Store the camera URI
                android.util.Log.d("MainActivity", "Camera URI stored: $uri")
                val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri)
                    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivityForResult(cameraIntent, DOCUMENT_CAMERA_REQUEST_CODE)
            }
        } catch (e: Exception) {
            android.util.Log.e(Constants.TAG_MAIN_ACTIVITY, "Exception launching document camera", e)
            showToast(this, "Error launching camera: ${e.message}", Constants.TOAST_DURATION_LONG)
        }
    }
    
    private fun createImageUri(): Uri? {
        return try {
            val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val imageFileName = "IMG_$timeStamp.jpg"
            val storageDir = java.io.File(cacheDir, "images")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            val imageFile = java.io.File(storageDir, imageFileName)
            
            if (imageFile.exists() || imageFile.createNewFile()) {
                androidx.core.content.FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    imageFile
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == IMPORT_FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                // Handle the import
                exportImportViewModel.resetImportStatus()
                exportImportViewModel.importData(uri) { success ->
                    if (success) {
                        showToast(this, Constants.SUCCESS_IMPORT_COMPLETED, Constants.TOAST_DURATION_SHORT)
                    } else {
                        showToast(this, Constants.ERROR_INVALID_FORMAT, Constants.TOAST_DURATION_LONG)
                    }
                }
            }
        }
        
        // Handle document file picker result
        if (requestCode == DOCUMENT_FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                handleDocumentUpload(uri)
            }
        }
        
        // Handle document camera result
        if (requestCode == DOCUMENT_CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            android.util.Log.d("MainActivity", "Camera result received, using stored URI: $pendingCameraUri")
            handleDocumentUpload(pendingCameraUri) // Use the stored camera URI
        }
        
        // Handle Google Sign-In result
        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            try {
                val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                android.util.Log.d("MainActivity", "Google Sign-In successful: ${account?.email}")
                onGoogleSignInResult?.invoke(account)
            } catch (e: com.google.android.gms.common.api.ApiException) {
                android.util.Log.e("MainActivity", "Google Sign-In failed: ${e.statusCode}", e)
                onGoogleSignInResult?.invoke(null)
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Google Sign-In error", e)
                onGoogleSignInResult?.invoke(null)
            }
        }
    }
    
    private fun handleDocumentUpload(uri: Uri?) {
        try {
            android.util.Log.d("MainActivity", "handleDocumentUpload called with URI: $uri")
            
            if (uri == null) {
                android.util.Log.e("MainActivity", "URI is null, cannot upload document")
                showToast(this, "Error: No file to upload", Constants.TOAST_DURATION_LONG)
                return
            }
            
            val fileName = pendingDocumentName ?: getFileName(uri) ?: "document_${System.currentTimeMillis()}"
            
            // Upload document using the pending values
            documentViewModel.uploadDocument(
                uri = uri,
                documentName = fileName,
                entityType = pendingEntityType,
                entityId = pendingEntityId ?: 0L, // Use 0 for general documents not tied to specific entity
                notes = pendingNotes
            ) { success ->
                if (success) {
                    showToast(this, "Document uploaded successfully", Constants.TOAST_DURATION_SHORT)
                } else {
                    showToast(this, "Failed to upload document", Constants.TOAST_DURATION_LONG)
                }
            }
            
            // Clear pending values
            pendingDocumentName = null
            pendingEntityId = null
            pendingNotes = null
            pendingCameraUri = null
        } catch (e: Exception) {
            android.util.Log.e(Constants.TAG_MAIN_ACTIVITY, "Exception handling document upload", e)
            showToast(this, "Error uploading document: ${e.message}", Constants.TOAST_DURATION_LONG)
        }
    }
    
    private fun getFileName(uri: Uri): String? {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        app = application as RentTrackerApplication
        val viewModelFactory = ViewModelFactory(app.repository, app.preferencesManager, applicationContext, app.database)

        ownerViewModel = ViewModelProvider(this, viewModelFactory)[OwnerViewModel::class.java]
        buildingViewModel = ViewModelProvider(this, viewModelFactory)[BuildingViewModel::class.java]
        tenantViewModel = ViewModelProvider(this, viewModelFactory)[TenantViewModel::class.java]
        paymentViewModel = ViewModelProvider(this, viewModelFactory)[PaymentViewModel::class.java]
        settingsViewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]
        documentViewModel = ViewModelProvider(this, viewModelFactory)[DocumentViewModel::class.java]
        vendorViewModel = ViewModelProvider(this, viewModelFactory)[VendorViewModel::class.java]
        expenseViewModel = ViewModelProvider(this, viewModelFactory)[ExpenseViewModel::class.java]
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
            val themeMode by app.preferencesManager.themeModeFlow.collectAsState(initial = com.renttracker.app.data.preferences.PreferencesManager.THEME_MODE_SYSTEM)
            
            RentTrackerTheme(themeMode = themeMode) {
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
                        vendorViewModel = vendorViewModel,
                        expenseViewModel = expenseViewModel,
                        exportImportViewModel = exportImportViewModel,
                        mainActivity = this,
                        database = app.database,
                        preferencesManager = app.preferencesManager
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
