package com.appbosna.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ApiService(
    private val client: HttpClient,
    private val apiKeyProvider: () -> String
) {
    private val base = ApiConfig.baseUrl
    
    // Safe JSON parser that ignores unknown keys
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    /**
     * Safely parse response body with error handling
     * Returns null if parsing fails or HTTP status is error
     */
    private suspend inline fun <reified T> HttpResponse.safeParse(): T? {
        return try {
            if (!status.isSuccess()) {
                val errorBody = bodyAsText()
                println("❌ API Error (${status.value}): $errorBody")
                return null
            }
            
            val raw = bodyAsText()
            json.decodeFromString<T>(raw)
        } catch (e: Exception) {
            println("❌ Failed to parse response: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Extract error message from failed HTTP response
     * Handles different error response formats (rate limiting, validation errors, etc.)
     */
    private suspend fun HttpResponse.extractErrorMessage(): String {
        return try {
            val errorBody = bodyAsText()
            // Try to parse as error response
            @Serializable
            data class ErrorResponse(
                val error: String? = null,
                val message: String? = null,
                @SerialName("retry_after") val retryAfter: Long? = null
            )
            
            val errorRes = json.decodeFromString<ErrorResponse>(errorBody)
            when {
                errorRes.message != null && errorRes.retryAfter != null -> {
                    "${errorRes.message} (retry after: ${errorRes.retryAfter})"
                }
                errorRes.message != null -> errorRes.message
                errorRes.error != null -> errorRes.error
                else -> "HTTP ${status.value}: ${status.description}"
            }
        } catch (e: Exception) {
            "HTTP ${status.value}: ${status.description}"
        }
    }


    // ============================================================
    // AUTH
    // ============================================================

    suspend fun login(req: LoginReq): LoginRes {
        val url = "$base/auth/login"
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        return response.safeParse<LoginRes>() ?: LoginRes(error = response.extractErrorMessage())
    }

    // /auth/register nije u OpenAPI, ali ostavljeno ako backend ipak ima rutu.
    suspend fun register(req: RegisterReq): LoginRes {
        val url = "$base/auth/register"
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        return response.safeParse<LoginRes>() ?: LoginRes(error = response.extractErrorMessage())
    }

    suspend fun loginOrRegisterWithGoogle(
        name: String,
        email: String,
        uid: String
    ): LoginRes {
        val url = "$base/google-login"
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "uid" to uid
                )
            )
        }
        return response.safeParse<LoginRes>() ?: LoginRes(error = response.extractErrorMessage())
    }

    suspend fun me(token: String): ApiUser? {
        val url = "$base/auth/me"
        val response = client.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        // stvarni response: { "user": { ... }, "token_expires_at": ... }
        return runCatching { response.body<MeRes>().user }.getOrNull()
    }

    // ============================================================
    // SETTINGS
    // ============================================================

    suspend fun getSettings(): SettingsEnvelope {
        val url = "$base/settings"
        val response = client.get(url)
        return response.body()
    }

    // ============================================================
    // CATEGORIES
    // ============================================================

    suspend fun getCategories(): List<CategoryDto> {
        val url = "$base/categories"
        val response = client.get(url)
        return response.body<CategoriesEnvelope>().categories
    }

    // ============================================================
    // CUSTOMER
    // ============================================================

    suspend fun getCustomer(token: String): CustomerDto {
        val url = "$base/customer"
        val response = client.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return response.body()
    }

    suspend fun updateCustomer(customer: CustomerDto, token: String) {
        val url = "$base/customer"
        val response = client.put(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(customer)
        }
    }

    // ============================================================
    // CART
    // ============================================================

    suspend fun addCartItem(cartItem: CartItemDto, token: String) {
        val url = "$base/cart/add"
        val payload = mapOf(
            "product_id" to (cartItem.productId.toIntOrNull() ?: 0),
            "quantity" to (cartItem.quantity.toIntOrNull() ?: 1)
        )

        val response = client.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
    }

    suspend fun updateCartItem(cartItem: CartItemDto, token: String) {
        val url = "$base/cart/updateQty"
        val payload = mapOf(
            "product_id" to (cartItem.productId.toIntOrNull() ?: 0),
            "quantity" to (cartItem.quantity.toIntOrNull() ?: 1)
        )

        val response = client.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
    }

    suspend fun deleteCartItem(cartItemId: String, token: String) {
        val url = "$base/cart/delete"
        val payload = mapOf(
            "id" to (cartItemId.toIntOrNull() ?: 0)
        )

        val response = client.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
    }

    suspend fun clearCart(token: String) {
        val url = "$base/cart/clear"
        val response = client.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    // ============================================================
    // PRODUCTS
    // ============================================================

    suspend fun getAllProducts(): List<ProductDto> {
        return try {
            val response = client.get("$base/products")
            val envelope = response.safeParse<ProductsEnvelope>()
            envelope?.products ?: emptyList()
        } catch (e: Exception) {
            println("❌ Failed to fetch products: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getProductById(id: String): ProductDto? {
        return try {
            val url = "$base/products/$id"
            val response = client.get(url)
            response.safeParse<ProductEnvelope>()?.product
        } catch (e: Exception) {
            println("❌ Failed to fetch product $id: ${e.message}")
            null
        }
    }

    suspend fun createProduct(product: ProductDto, token: String): ProductDto? {
        return try {
            val url = "$base/products"
            val response = client.post(url) {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(product)
            }
            response.safeParse<ProductEnvelope>()?.product
        } catch (e: Exception) {
            println("❌ Failed to create product: ${e.message}")
            null
        }
    }

    suspend fun updateProduct(id: String, product: ProductDto, token: String) {
        val url = "$base/products/$id"
        val response = client.put(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(product)
        }
    }

    suspend fun deleteProduct(id: String, token: String) {
        val url = "$base/products/$id"
        val response = client.delete(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    // ============================================================
    // MEDIA UPLOAD
    // ============================================================

    suspend fun uploadImage(
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String,
        token: String
    ): UploadResponse {
        val url = "$base/media/upload"
        
        val response = client.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                io.ktor.client.request.forms.MultiPartFormDataContent(
                    io.ktor.client.request.forms.formData {
                        append("file", fileBytes, io.ktor.http.Headers.build {
                            append(HttpHeaders.ContentType, mimeType)
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        })
                    }
                )
            )
        }
        
        return response.body()
    }

    suspend fun deleteImage(filename: String, token: String) {
        val url = "$base/media/$filename"
        val response = client.delete(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    // ============================================================
    // ORDERS
    // ============================================================

    suspend fun createOrder(
        cartItems: List<CartItemDto>,
        totalAmount: String,
        couponCode: String? = null,
        couponDiscount: String? = null,
        token: String
    ): String {
        val url = "$base/orders"

        val cartPayload = cartItems.map {
            OrderCartItemRequest(
                productId = it.productId.toIntOrNull() ?: 0,
                title = it.title,
                price = it.price,
                quantity = it.quantity.toIntOrNull() ?: 1,
                thumbnail = it.thumbnail.takeIf { url -> url.isNotBlank() }
            )
        }

        val request = OrderCreateRequest(
            total = totalAmount,
            items = cartPayload,
            couponCode = couponCode?.takeIf { it.isNotBlank() },
            couponDiscount = couponDiscount?.takeIf { it.isNotBlank() }
        )

        val response = client.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)   // 👈 više nema Map<String, Any>
        }

        // Debug: print raw response for easier troubleshooting when server returns errors
        val raw = response.bodyAsText()
        try {
            println("CREATE ORDER RESPONSE STATUS: ${response.status}")
            println("CREATE ORDER RESPONSE BODY: $raw")
        } catch (t: Throwable) {
            println("CREATE ORDER: failed to print response: ${t.message}")
        }

        return runCatching { response.body<OrderCreateResponse>().orderId }
            .getOrElse {
                throw IllegalStateException("Failed to parse createOrder response: ${it.message}. Raw body: $raw")
            }
    }


    suspend fun getOrders(token: String): List<OrderDto> {
        val url = "$base/orders"
        val response = client.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return response.body<OrdersEnvelope>().orders
    }

    // ============================================================
    // RESTAURANTS
    // ============================================================

    suspend fun getRestaurants(): List<RestaurantDto> {
        val url = "$base/restaurants"
        val response = client.get(url)
        return response.body<RestaurantsEnvelope>().restaurants
    }

    // ============================================================
    // ADMIN
    // ============================================================

    suspend fun adminGetUsers(token: String): List<AdminUserDto> {
        val url = "$base/admin/users"
        val response = client.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return response.body<AdminUsersEnvelope>().users
    }

    suspend fun adminGetUsersCount(token: String): Int {
        val url = "$base/admin/users/count"
        val response = client.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return response.body<CountEnvelope>().count
    }

    suspend fun adminGetOrders(start: Long, end: Long, token: String): List<AdminOrderDto> {
        val url = "$base/admin/orders"
        val response = client.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("start", start)
            parameter("end", end)
        }
        return response.body<AdminOrdersEnvelope>().orders
    }

    suspend fun adminGetOrdersCount(token: String): Int {
        val url = "$base/admin/orders/count"
        val response = client.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return response.body<CountEnvelope>().count
    }

    // ============================================================
    // ANALYTICS
    // ============================================================

    suspend fun getRevenueAnalytics(period: String, token: String): com.appbosna.data.domain.RevenueAnalytics {
        val url = "$base/analytics/revenue"
        val response = client.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("period", period)
        }
        return response.body()
    }

    suspend fun getOrdersByHourAnalytics(period: String, token: String): com.appbosna.data.domain.OrdersByHourAnalytics {
        val url = "$base/analytics/orders-by-hour"
        val response = client.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("period", period)
        }
        return response.body()
    }
}

/* ============================================================
 * INTERNAL ENVELOPES
 * ============================================================ */

@Serializable
data class CategoriesEnvelope(
    val categories: List<CategoryDto> = emptyList()
)

@Serializable
data class ProductsEnvelope(
    val products: List<ProductDto> = emptyList()  // Default to empty list if missing
)

@Serializable
data class ProductEnvelope(
    val product: ProductDto? = null  // Nullable for safety
)

@Serializable
data class OrdersEnvelope(
    val orders: List<OrderDto> = emptyList()
)

@Serializable
data class RestaurantsEnvelope(
    val restaurants: List<RestaurantDto> = emptyList()
)

@Serializable
data class AdminUsersEnvelope(
    val users: List<AdminUserDto> = emptyList()
)

@Serializable
data class AdminOrdersEnvelope(
    val orders: List<AdminOrderDto> = emptyList()
)

@Serializable
data class CountEnvelope(
    val count: Int = 0
)

@Serializable
data class OrderCreateResponse(
    @SerialName("order_id") val orderId: String = ""
)

@Serializable
data class SettingsEnvelope(val settings: Map<String, String> = emptyMap())
