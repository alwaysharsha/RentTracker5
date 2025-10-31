package com.renttracker.app.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Utility functions for UI operations
 */

/**
 * Shows a toast message with consistent styling
 */
fun showToast(context: Context, message: String, duration: Int = Constants.TOAST_DURATION_SHORT) {
    Toast.makeText(context, message, duration).show()
}

/**
 * Shows an error toast message
 */
fun showErrorToast(context: Context, error: String? = null) {
    val message = if (error.isNullOrBlank()) {
        Constants.ERROR_UNKNOWN
    } else {
        Constants.ERROR_GENERIC_PREFIX + error
    }
    showToast(context, message, Constants.TOAST_DURATION_LONG)
}

/**
 * Composable function to get context safely
 */
@Composable
fun getContext(): Context {
    return LocalContext.current
}
