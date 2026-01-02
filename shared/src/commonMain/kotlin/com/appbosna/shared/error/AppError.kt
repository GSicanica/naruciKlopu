package com.appbosna.shared.error

/**
 * Unified Error Model for the entire app
 * Provides consistent error handling across all features
 */
sealed class AppError {
    abstract val message: String
    abstract val errorCode: String?
    abstract val displayMessage: String

    /**
     * Network errors - connection, timeout, etc.
     */
    data class NetworkError(
        override val message: String = "Network connection failed",
        override val errorCode: String? = "NETWORK_ERROR",
        val isTimeout: Boolean = false,
        val isNoConnection: Boolean = false
    ) : AppError() {
        override val displayMessage: String
            get() = when {
                isNoConnection -> "Nema internet veze. Provjerite vašu konekciju."
                isTimeout -> "Zahtjev je istekao. Pokušajte ponovo."
                else -> "Greška pri povezivanju. Provjerite internet vezu."
            }
    }

    /**
     * Authentication errors - invalid token, expired, etc.
     */
    data class AuthenticationError(
        override val message: String = "Authentication failed",
        override val errorCode: String? = "AUTH_ERROR",
        val isTokenExpired: Boolean = false,
        val isInvalidCredentials: Boolean = false
    ) : AppError() {
        override val displayMessage: String
            get() = when {
                isTokenExpired -> "Vaša sesija je istekla. Molimo prijavite se ponovo."
                isInvalidCredentials -> "Pogrešan email ili lozinka."
                else -> "Greška pri autentifikaciji. Prijavite se ponovo."
            }
    }

    /**
     * Authorization errors - insufficient permissions
     */
    data class AuthorizationError(
        override val message: String = "Access denied",
        override val errorCode: String? = "ACCESS_DENIED",
        val requiredRole: String? = null
    ) : AppError() {
        override val displayMessage: String
            get() = "Nemate dozvolu za pristup ovom resursu."
    }

    /**
     * Validation errors - invalid input
     */
    data class ValidationError(
        override val message: String = "Validation failed",
        override val errorCode: String? = "VALIDATION_ERROR",
        val errors: Map<String, String> = emptyMap()
    ) : AppError() {
        override val displayMessage: String
            get() = errors.values.firstOrNull() ?: "Podaci nisu ispravni. Provjerite unos."
    }

    /**
     * Server errors - 500, 502, 503, etc.
     */
    data class ServerError(
        override val message: String = "Server error",
        override val errorCode: String? = "SERVER_ERROR",
        val statusCode: Int = 500
    ) : AppError() {
        override val displayMessage: String
            get() = "Došlo je do greške na serveru. Pokušajte kasnije."
    }

    /**
     * Resource not found - 404
     */
    data class NotFoundError(
        override val message: String = "Resource not found",
        override val errorCode: String? = "NOT_FOUND",
        val resource: String? = null,
        val id: String? = null
    ) : AppError() {
        override val displayMessage: String
            get() = resource?.let { "Traženi $it nije pronađen." }
                ?: "Traženi resurs nije pronađen."
    }

    /**
     * Rate limit exceeded - 429
     */
    data class RateLimitError(
        override val message: String = "Too many requests",
        override val errorCode: String? = "RATE_LIMIT",
        val retryAfter: Int? = null
    ) : AppError() {
        override val displayMessage: String
            get() = retryAfter?.let {
                "Previše zahtjeva. Pokušajte ponovo za $it sekundi."
            } ?: "Previše zahtjeva. Pokušajte kasnije."
    }

    /**
     * Business logic errors - invalid operation
     */
    data class BusinessError(
        override val message: String = "Operation not allowed",
        override val errorCode: String? = "BUSINESS_ERROR",
        val context: Map<String, Any> = emptyMap()
    ) : AppError() {
        override val displayMessage: String
            get() = message
    }

    /**
     * Unknown/Generic error
     */
    data class UnknownError(
        override val message: String = "An unexpected error occurred",
        override val errorCode: String? = "UNKNOWN_ERROR",
        val exception: Throwable? = null
    ) : AppError() {
        override val displayMessage: String
            get() = "Došlo je do neočekivane greške. Pokušajte ponovo."
    }

    /**
     * Empty/No data error
     */
    data class EmptyError(
        override val message: String = "No data available",
        override val errorCode: String? = "EMPTY_DATA"
    ) : AppError() {
        override val displayMessage: String
            get() = "Nema dostupnih podataka."
    }

    companion object {
        /**
         * Parse API error response to AppError
         */
        fun fromApiResponse(
            errorCode: String?,
            message: String?,
            statusCode: Int,
            additionalData: Map<String, Any> = emptyMap()
        ): AppError {
            return when (errorCode) {
                "validation_error" -> ValidationError(
                    message = message ?: "Validation failed",
                    errorCode = errorCode,
                    errors = (additionalData["errors"] as? Map<String, String>) ?: emptyMap()
                )
                "authentication_required", "invalid_token", "token_expired" -> AuthenticationError(
                    message = message ?: "Authentication failed",
                    errorCode = errorCode,
                    isTokenExpired = errorCode == "token_expired"
                )
                "access_denied", "forbidden" -> AuthorizationError(
                    message = message ?: "Access denied",
                    errorCode = errorCode,
                    requiredRole = additionalData["required_role"] as? String
                )
                "not_found" -> NotFoundError(
                    message = message ?: "Resource not found",
                    errorCode = errorCode,
                    resource = additionalData["resource"] as? String,
                    id = additionalData["id"]?.toString()
                )
                "rate_limit_exceeded" -> RateLimitError(
                    message = message ?: "Too many requests",
                    errorCode = errorCode,
                    retryAfter = (additionalData["retry_after"] as? Number)?.toInt()
                )
                "business_logic_error", "insufficient_stock", "order_cancelled" -> BusinessError(
                    message = message ?: "Operation not allowed",
                    errorCode = errorCode,
                    context = additionalData
                )
                else -> when (statusCode) {
                    401 -> AuthenticationError(message = message ?: "Authentication required")
                    403 -> AuthorizationError(message = message ?: "Access denied")
                    404 -> NotFoundError(message = message ?: "Not found")
                    429 -> RateLimitError(message = message ?: "Too many requests")
                    in 500..599 -> ServerError(message = message ?: "Server error", statusCode = statusCode)
                    else -> UnknownError(message = message ?: "Unknown error")
                }
            }
        }

        /**
         * Create error from exception
         */
        fun fromException(exception: Throwable): AppError {
            return when (exception) {
                is java.net.UnknownHostException,
                is java.net.SocketTimeoutException,
                is java.io.IOException -> NetworkError(
                    message = exception.message ?: "Network error",
                    isTimeout = exception is java.net.SocketTimeoutException,
                    isNoConnection = exception is java.net.UnknownHostException
                )
                else -> UnknownError(
                    message = exception.message ?: "An error occurred",
                    exception = exception
                )
            }
        }
    }
}

/**
 * Extension functions for easier error handling
 */
fun AppError.isRetryable(): Boolean = when (this) {
    is AppError.NetworkError -> true
    is AppError.ServerError -> statusCode in listOf(502, 503, 504)
    is AppError.RateLimitError -> true
    else -> false
}

fun AppError.shouldLogout(): Boolean = when (this) {
    is AppError.AuthenticationError -> isTokenExpired
    else -> false
}

fun AppError.toUserMessage(): String = displayMessage
