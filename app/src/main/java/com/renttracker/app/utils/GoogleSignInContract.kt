package com.renttracker.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException

class GoogleSignInContract : ActivityResultContract<GoogleDriveBackupManager, GoogleSignInAccount?>() {
    
    override fun createIntent(context: Context, input: GoogleDriveBackupManager): Intent {
        // Use the backup manager's sign-in client to ensure consistent configuration
        return input.getSignInIntent()
    }
    
    override fun parseResult(resultCode: Int, intent: Intent?): GoogleSignInAccount? {
        return when {
            resultCode == Activity.RESULT_OK && intent != null -> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                    task.getResult(ApiException::class.java)
                } catch (e: ApiException) {
                    android.util.Log.e("GoogleSignInContract", "Sign-in failed: ${e.statusCode}", e)
                    null
                }
            }
            resultCode == Activity.RESULT_CANCELED -> {
                android.util.Log.d("GoogleSignInContract", "Sign-in cancelled by user")
                null
            }
            else -> {
                android.util.Log.e("GoogleSignInContract", "Unexpected result code: $resultCode")
                null
            }
        }
    }
    
    override fun getSynchronousResult(
        context: Context,
        input: GoogleDriveBackupManager
    ): SynchronousResult<GoogleSignInAccount?>? {
        // Check if already signed in
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return if (account != null && account.grantedScopes.isNotEmpty()) {
            SynchronousResult(account)
        } else {
            null
        }
    }
}
