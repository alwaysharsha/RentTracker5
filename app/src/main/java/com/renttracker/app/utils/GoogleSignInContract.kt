package com.renttracker.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

class GoogleSignInContract : ActivityResultContract<Unit, GoogleSignInAccount?>() {
    
    override fun createIntent(context: Context, input: Unit): Intent {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        
        val client = GoogleSignIn.getClient(context, signInOptions)
        return client.signInIntent
    }
    
    override fun parseResult(resultCode: Int, intent: Intent?): GoogleSignInAccount? {
        return if (resultCode == Activity.RESULT_OK && intent != null) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                task.getResult(ApiException::class.java)
            } catch (e: ApiException) {
                null
            }
        } else {
            null
        }
    }
}
