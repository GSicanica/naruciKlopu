package com.appbosna.shared.error

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

/**
 * Error Response Model from API
 */
@Serializable
data class ApiErrorResponse(
    val success: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val errors: Map<String, String>? = null,
    val context: Map<String, String>? = null,
    val retry_after: Int? = null
)

/**
 * Result wrapper with enhanced error handling
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: AppError) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun errorOrNull(): AppError? = when (this) {
        is Error -> error
        else -> null
    }

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (AppError) -> Unit): Result<T> {
        if (this is Error) action(error)
        return this
    }

    inline fun onLoading(action: () -> Unit): Result<T> {
        if (this is Loading) action()
        return this
    }

    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(error)
        is Loading -> Loading
    }

    companion object {
        /**
         * Parse HTTP response to Result
         */
        suspend inline fun <reified T> fromHttpResponse(response: HttpResponse): Result<T> {
            return try {
                if (response.status.isSuccess()) {
                    val data: T = response.body()
                    Success(data)
                } else {
                    val errorResponse = try {
                        response.body<ApiErrorResponse>()
                    } catch (e: Exception) {
                        // Fallback error response
                        ApiErrorResponse(
                            error = "http_error",
                            message = "HTTP ${response.status.value}"
                        )
                    }

                    val appError = AppError.fromApiResponse(
                        errorCode = errorResponse.error,
                        message = errorResponse.message,
                        statusCode = response.status.value,
                        additionalData = buildMap {
                            errorResponse.errors?.let { put("errors", it) }
                            errorResponse.retry_after?.let { put("retry_after", it) }
                        }
                    )

                    Error(appError)
                }
            } catch (e: Exception) {
                Error(AppError.fromException(e))
            }
        }

        /**
         * Wrap suspend function with error handling
         */
        suspend inline fun <T> catch(block: () -> T): Result<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                Error(AppError.fromException(e))
            }
        }
    }
}

/**
 * Extension function to convert Result to RequestState (for UI)
 * RequestState is the existing UI state wrapper with DisplayResult extension
 */
fun <T> Result<T>.toRequestState(): com.appbosna.shared.util.RequestState<T> {
    return when (this) {
        is Result.Success -> com.appbosna.shared.util.RequestState.Success(data)
        is Result.Error -> com.appbosna.shared.util.RequestState.Error(error.displayMessage)
        is Result.Loading -> com.appbosna.shared.util.RequestState.Loading
    }
}
