package com.appbosna.data.remote

import TokenStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class HttpClientProvider(
    private val tokenStore: TokenStore,
    private val engine: HttpClientEngine
) {
    val client: HttpClient by lazy {
        HttpClient(engine) {
            expectSuccess = false

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = true
                        coerceInputValues = true
                    }
                )
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 20_000
                connectTimeoutMillis = 12_000
                socketTimeoutMillis = 20_000
            }

            install(Logging) {
                level = LogLevel.BODY
            }

            defaultRequest {
                header(HttpHeaders.Accept, ContentType.Application.Json)

                tokenStore.getToken()
                    ?.takeIf { it.isNotBlank() }
                    ?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }
        }
    }
}
