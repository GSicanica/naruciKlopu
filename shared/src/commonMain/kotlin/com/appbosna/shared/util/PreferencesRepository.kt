package com.appbosna.shared.util

import com.appbosna.shared.navigation.Screen
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.observable.makeObservable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@OptIn(ExperimentalSettingsApi::class)
object PreferencesRepository {
    private val settings: ObservableSettings = Settings().makeObservable()

    private const val IS_SUCCESS = "isSuccess_paypal"
    private const val ERROR = "error_paypal"
    private const val TOKEN = "token_paypal"
    private const val LAST_LOGIN_EMAIL = "last_login_email"
    private const val LAST_LOGIN_NAME = "last_login_name"

    fun savePayPalData(
        isSuccess: Boolean?,
        error: String?,
        token: String?,
    ) {
        isSuccess?.let { settings.putBoolean(IS_SUCCESS, it) }
        error?.let { settings.putString(ERROR, it) }
        token?.let { settings.putString(TOKEN, it) }
    }

    fun readPayPalDataFlow(): Flow<Screen.PaymentCompleted?> = callbackFlow {
        fun getCurrentPaymentProcessed(): Screen.PaymentCompleted {
            return Screen.PaymentCompleted(
                isSuccess = settings.getBooleanOrNull(IS_SUCCESS),
                error = settings.getStringOrNull(ERROR),
                token = settings.getStringOrNull(TOKEN)
            )
        }

        while (true) {
            this.send(getCurrentPaymentProcessed())
            delay(1000) // Check for updates every second
        }
    }

    fun reset() {
        settings.clear()
    }

    fun saveLastLoginCredentials(email: String, name: String) {
        settings.putString(LAST_LOGIN_EMAIL, email)
        settings.putString(LAST_LOGIN_NAME, name)
    }

    fun getLastLoginEmail(): String? {
        return settings.getStringOrNull(LAST_LOGIN_EMAIL)
    }

    fun getLastLoginName(): String? {
        return settings.getStringOrNull(LAST_LOGIN_NAME)
    }
}