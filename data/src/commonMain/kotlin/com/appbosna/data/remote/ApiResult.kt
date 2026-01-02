package com.appbosna.data.remote

/**
 * Comprehensive error handling for API operations
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val exception: ApiException) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}

/**
 * Typed API exceptions with detailed error information
 */
sealed class ApiException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    
    /**
     * Network-related errors (no connection, timeout, etc.)
     */
    data class NetworkError(
        override val message: String = "Network connection failed",
        override val cause: Throwable? = null
    ) : ApiException(message, cause)
    
    /**
     * HTTP errors (4xx, 5xx)
     */
    data class HttpError(
        val statusCode: Int,
        override val message: String,
        val errorBody: String? = null
    ) : ApiException(message)
    
    /**
     * Parsing/Serialization errors
     */
    data class ParseError(
        override val message: String = "Failed to parse response",
        override val cause: Throwable? = null
    ) : ApiException(message, cause)
    
    /**
     * Authentication/Authorization errors
     */
    data class AuthError(
        override val message: String = "Authentication failed",
        val requiresLogin: Boolean = true
    ) : ApiException(message)
    
    /**
     * Validation errors (invalid input)
     */
    data class ValidationError(
        override val message: String,
        val errors: List<String> = emptyList()
    ) : ApiException(message)
    
    /**
     * Server errors (500+)
     */
    data class ServerError(
        override val message: String = "Server error occurred",
        val statusCode: Int = 500
    ) : ApiException(message)
    
    /**
     * Unknown/Unexpected errors
     */
    data class UnknownError(
        override val message: String = "An unexpected error occurred",
        override val cause: Throwable? = null
    ) : ApiException(message, cause)
    
    /**
     * Get user-friendly error message
     */
    fun getUserMessage(): String = when (this) {
        is NetworkError -> "No internet connection. Please check your network and try again."
        is HttpError -> when (statusCode) {
            400 -> "Invalid request. Please check your input."
            401 -> "Please log in to continue."
            403 -> "You don't have permission to perform this action."
            404 -> "The requested resource was not found."
            429 -> "Too many requests. Please try again later."
            in 500..599 -> "Server error. Please try again later."
            else -> message
        }
        is AuthError -> if (requiresLogin) {
            "Your session has expired. Please log in again."
        } else {
            "Authentication failed. Please check your credentials."
        }
        is ValidationError -> errors.firstOrNull() ?: message
        is ParseError -> "Unable to process server response. Please try again."
        is ServerError -> "Server is experiencing issues. Please try again later."
        is UnknownError -> "Something went wrong. Please try again."
    }
    
    /**
     * Check if error is retryable
     */
    fun isRetryable(): Boolean = when (this) {
        is NetworkError -> true
        is HttpError -> statusCode in listOf(408, 429, 500, 502, 503, 504)
        is ServerError -> true
        else -> false
    }
}

/**
 * Extension functions for error handling
 */
fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) {
        action(data)
    }
    return this
}

fun <T> ApiResult<T>.onError(action: (ApiException) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) {
        action(exception)
    }
    return this
}

fun <T> ApiResult<T>.onLoading(action: () -> Unit): ApiResult<T> {
    if (this is ApiResult.Loading) {
        action()
    }
    return this
}

/**
 * Get data or null
 */
fun <T> ApiResult<T>.getOrNull(): T? = when (this) {
    is ApiResult.Success -> data
    else -> null
}

/**
 * Get data or default value
 */
fun <T> ApiResult<T>.getOrDefault(default: T): T = when (this) {
    is ApiResult.Success -> data
    else -> default
}

/**
 * Get data or throw exception
 */
fun <T> ApiResult<T>.getOrThrow(): T = when (this) {
    is ApiResult.Success -> data
    is ApiResult.Error -> throw exception
    is ApiResult.Loading -> throw IllegalStateException("Result is still loading")
}

/**
 * Map success data to another type
 */
fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.Error -> ApiResult.Error(exception)
    is ApiResult.Loading -> ApiResult.Loading
}

/**
 * Flat map for chaining operations
 */
fun <T, R> ApiResult<T>.flatMap(transform: (T) -> ApiResult<R>): ApiResult<R> = when (this) {
    is ApiResult.Success -> transform(data)
    is ApiResult.Error -> ApiResult.Error(exception)
    is ApiResult.Loading -> ApiResult.Loading
}

/**
 * Check if result is success
 */
fun <T> ApiResult<T>.isSuccess(): Boolean = this is ApiResult.Success

/**
 * Check if result is error
 */
fun <T> ApiResult<T>.isError(): Boolean = this is ApiResult.Error

/**
 * Check if result is loading
 */
fun <T> ApiResult<T>.isLoading(): Boolean = this is ApiResult.Loading

/**
 * Retry configuration for failed requests
 */
data class RetryConfig(
    val maxAttempts: Int = 3,
    val initialDelayMs: Long = 1000,
    val maxDelayMs: Long = 10000,
    val backoffMultiplier: Double = 2.0,
    val retryableErrors: Set<Class<out ApiException>> = setOf(
        ApiException.NetworkError::class.java,
        ApiException.ServerError::class.java
    )
)

/**
 * Retry helper for API calls
 */
suspend fun <T> retryWithBackoff(
    config: RetryConfig = RetryConfig(),
    block: suspend () -> ApiResult<T>
): ApiResult<T> {
    var currentDelay = config.initialDelayMs
    var lastError: ApiException? = null
    
    repeat(config.maxAttempts) { attempt ->
        when (val result = block()) {
            is ApiResult.Success -> return result
            is ApiResult.Error -> {
                lastError = result.exception
                
                // Check if error is retryable
                if (!result.exception.isRetryable() || attempt == config.maxAttempts - 1) {
                    return result
                }
                
                // Wait before retry with exponential backoff
                kotlinx.coroutines.delay(currentDelay)
                currentDelay = minOf(
                    (currentDelay * config.backoffMultiplier).toLong(),
                    config.maxDelayMs
                )
            }
            is ApiResult.Loading -> {
                // Continue to next attempt
            }
        }
    }
    
    return ApiResult.Error(
        lastError ?: ApiException.UnknownError("Max retry attempts reached")
    )
}
