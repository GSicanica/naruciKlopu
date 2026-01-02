package com.appbosna.shared.error

import kotlinx.coroutines.delay
import kotlin.math.pow

/**
 * Retry strategy for handling transient errors
 */
class RetryStrategy(
    val maxAttempts: Int = 3,
    val initialDelayMs: Long = 1000,
    val maxDelayMs: Long = 10000,
    val factor: Double = 2.0,
    val shouldRetry: (AppError) -> Boolean = { it.isRetryable() }
)

/**
 * Retry a suspend function with exponential backoff
 */
suspend fun <T> retryWithBackoff(
    strategy: RetryStrategy = RetryStrategy(),
    block: suspend () -> Result<T>
): Result<T> {
    var currentDelay = strategy.initialDelayMs
    var lastError: AppError? = null

    repeat(strategy.maxAttempts) { attempt ->
        val result = block()

        when (result) {
            is Result.Success -> return result
            is Result.Error -> {
                lastError = result.error
                
                // Don't retry if error is not retryable or it's the last attempt
                if (!strategy.shouldRetry(result.error) || attempt == strategy.maxAttempts - 1) {
                    return result
                }

                // Wait before retry with exponential backoff
                delay(currentDelay)
                currentDelay = (currentDelay * strategy.factor).toLong()
                    .coerceAtMost(strategy.maxDelayMs)
            }
            is Result.Loading -> {
                // Continue to next attempt
            }
        }
    }

    return Result.Error(lastError ?: AppError.UnknownError())
}

/**
 * Extension function for easier retry
 */
suspend fun <T> (suspend () -> Result<T>).withRetry(
    maxAttempts: Int = 3,
    initialDelayMs: Long = 1000
): Result<T> = retryWithBackoff(
    strategy = RetryStrategy(maxAttempts = maxAttempts, initialDelayMs = initialDelayMs),
    block = this
)
