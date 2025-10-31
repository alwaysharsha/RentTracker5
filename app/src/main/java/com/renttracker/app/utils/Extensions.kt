package com.renttracker.app.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Extension functions for common operations
 */

/**
 * Extension function to get the first element from a Flow safely
 */
suspend fun <T> Flow<T>.firstOrNull(): T? {
    return try {
        first()
    } catch (e: Exception) {
        null
    }
}

/**
 * Extension function to execute a list of operations with error handling
 */
suspend fun executeWithErrorHandling(
    operations: List<suspend () -> Unit>,
    onError: (Exception, String) -> Unit = { _, _ -> }
) {
    operations.forEachIndexed { index, operation ->
        try {
            operation()
        } catch (e: Exception) {
            onError(e, "Operation $index failed")
        }
    }
}
