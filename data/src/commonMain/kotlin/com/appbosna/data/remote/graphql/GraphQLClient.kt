package com.appbosna.data.remote.graphql

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/* ============================================================
 * GRAPHQL REQUEST/RESPONSE MODELS
 * ============================================================ */

@Serializable
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, JsonElement>? = null
)

@Serializable
data class GraphQLResponse<T>(
    val data: T? = null,
    val errors: List<GraphQLError>? = null
)

@Serializable
data class GraphQLError(
    val message: String,
    val locations: List<GraphQLLocation>? = null,
    val path: List<String>? = null
)

@Serializable
data class GraphQLLocation(
    val line: Int,
    val column: Int
)

/* ============================================================
 * GRAPHQL CLIENT
 * ============================================================ */

class GraphQLClient(
    val client: HttpClient,
    baseUrl: String
) {
    // NE SMIJE BITI private zbog public inline funkcija
    val endpoint = "$baseUrl/graphql"

    suspend inline fun <reified T> query(
        query: String,
        variables: Map<String, JsonElement>? = null
    ): Result<T> = execute(query, variables)

    suspend inline fun <reified T> mutate(
        mutation: String,
        variables: Map<String, JsonElement>? = null
    ): Result<T> = execute(mutation, variables)

    suspend inline fun <reified T> execute(
        query: String,
        variables: Map<String, JsonElement>? = null
    ): Result<T> {
        return try {
            val response = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(GraphQLRequest(query, variables))
            }.body<GraphQLResponse<T>>()

            when {
                response.errors != null && response.errors!!.isNotEmpty() -> {
                    val msg = response.errors!!.joinToString { it.message }
                    Result.failure(GraphQLException(msg, response.errors!!))
                }

                response.data != null -> Result.success(response.data!!)
                else -> Result.failure(GraphQLException("GraphQL: Empty response from server"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GraphQLException(
    message: String,
    val graphQLErrors: List<GraphQLError>? = null
) : Exception(message)
