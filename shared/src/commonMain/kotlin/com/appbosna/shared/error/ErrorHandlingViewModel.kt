package com.appbosna.shared.error

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel with enhanced error handling
 */
abstract class ErrorHandlingViewModel : ViewModel() {

    private val _error = MutableStateFlow<AppError?>(null)
    val error: StateFlow<AppError?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Execute a suspend function with error handling
     */
    protected fun <T> executeWithErrorHandling(
        onSuccess: (T) -> Unit = {},
        onError: (AppError) -> Unit = { handleError(it) },
        block: suspend () -> Result<T>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = block()) {
                is Result.Success -> {
                    onSuccess(result.data)
                }
                is Result.Error -> {
                    onError(result.error)
                }
                is Result.Loading -> {
                    // Already loading
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * Execute with automatic retry
     */
    protected fun <T> executeWithRetry(
        maxAttempts: Int = 3,
        onSuccess: (T) -> Unit = {},
        onError: (AppError) -> Unit = { handleError(it) },
        block: suspend () -> Result<T>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = retryWithBackoff(
                strategy = RetryStrategy(maxAttempts = maxAttempts),
                block = block
            )

            when (result) {
                is Result.Success -> onSuccess(result.data)
                is Result.Error -> onError(result.error)
                is Result.Loading -> {}
            }

            _isLoading.value = false
        }
    }

    /**
     * Handle error (can be overridden)
     */
    protected open fun handleError(error: AppError) {
        _error.value = error

        // Auto-logout if token expired
        if (error.shouldLogout()) {
            onTokenExpired()
        }
    }

    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Handle token expiration (to be overridden)
     */
    protected open fun onTokenExpired() {
        // Override in child classes to handle logout
    }
}

/**
 * Composable helper for handling errors in ViewModels
 */
@Composable
fun ErrorHandler(
    error: AppError?,
    onDismiss: () -> Unit = {},
    onRetry: (() -> Unit)? = null
) {
    error?.let {
        ErrorView(
            error = it,
            onRetry = onRetry,
            modifier = Modifier.fillMaxSize()
        )
    }
}
