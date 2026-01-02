package com.appbosna.shared.network

import com.appbosna.shared.domain.PaymentIntentResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class StripeApiClient {
    
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    private companion object {
        const val STRIPE_API_BASE_URL = "https://api.stripe.com/v1"
        const val PAYMENT_INTENTS_ENDPOINT = "$STRIPE_API_BASE_URL/payment_intents"
    }
    
    suspend fun createPaymentIntent(
        amount: Long,
        currency: String = "usd"
    ): Result<PaymentIntentResponse> {
        return try {
            val response = httpClient.submitForm(
                url = PAYMENT_INTENTS_ENDPOINT,
                formParameters = Parameters.build {
                    append("amount", amount.toString())
                    append("currency", currency)
                    append("automatic_payment_methods[enabled]", "true")
                }
            ) {
                //header(HttpHeaders.Authorization, "Bearer ${Consts.STRIPE_SECRET_KEY}")
                header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            }
            
            if (response.status.isSuccess()) {
                val paymentIntent = response.body<PaymentIntentResponse>()
                Result.success(paymentIntent)
            } else {
                Result.failure(Exception("Failed to create payment intent: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun retrievePaymentIntent(paymentIntentId: String): Result<PaymentIntentResponse> {
        return try {
            val response = httpClient.get("$PAYMENT_INTENTS_ENDPOINT/$paymentIntentId") {
              //  header(HttpHeaders.Authorization, "Bearer ${Consts.STRIPE_SECRET_KEY}")
            }
            
            if (response.status.isSuccess()) {
                val paymentIntent = response.body<PaymentIntentResponse>()
                Result.success(paymentIntent)
            } else {
                Result.failure(Exception("Failed to retrieve payment intent: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun close() {
        httpClient.close()
    }
} 