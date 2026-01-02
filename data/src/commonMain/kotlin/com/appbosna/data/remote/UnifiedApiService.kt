package com.appbosna.data.remote

import com.appbosna.data.config.ApiConfig
import com.appbosna.data.config.ApiType
import com.appbosna.data.remote.graphql.GraphQLRepository

class UnifiedApiService(
    private val rest: ApiService,
    private val graphql: GraphQLRepository
) {

    private fun logApiCall(method: String) {
        println("🔵 UnifiedApiService.$method -> Using ${ApiConfig.currentApiType.name} API")
    }

    // ============================================================
    // AUTH
    // ============================================================

    suspend fun login(req: LoginReq): LoginRes {
        logApiCall("login")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.login(req)
            ApiType.GRAPHQL -> graphql.login(req)
        }
    }

    suspend fun register(req: RegisterReq): LoginRes {
        logApiCall("register")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.register(req)
            ApiType.GRAPHQL -> graphql.register(req)
        }
    }

    suspend fun loginOrRegisterWithGoogle(name: String, email: String, uid: String): LoginRes {
        logApiCall("loginOrRegisterWithGoogle")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.loginOrRegisterWithGoogle(name, email, uid)
            ApiType.GRAPHQL -> graphql.loginOrRegisterWithGoogle(name, email, uid)
        }
    }

    suspend fun me(token: String): ApiUser? {
        logApiCall("me")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.me(token)
            ApiType.GRAPHQL -> graphql.me(token)
        }
    }

    // ============================================================
    // SETTINGS
    // ============================================================

    suspend fun getSettings(): SettingsEnvelope {
        logApiCall("getSettings")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.getSettings()
            ApiType.GRAPHQL -> graphql.getSettings()
        }
    }

    // ============================================================
    // CATEGORIES
    // ============================================================

    suspend fun getCategories(): List<CategoryDto> {
        logApiCall("getCategories")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.getCategories()
            ApiType.GRAPHQL -> graphql.getCategories()
        }
    }

    // ============================================================
    // CUSTOMER
    // ============================================================

    suspend fun getCustomer(token: String): CustomerDto {
        logApiCall("getCustomer")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.getCustomer(token)
            ApiType.GRAPHQL -> graphql.getCustomer(token)
        }
    }

    suspend fun updateCustomer(customer: CustomerDto, token: String) {
        logApiCall("updateCustomer")

        when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.updateCustomer(customer, token)
            ApiType.GRAPHQL -> graphql.updateCustomer(customer, token)
        }
    }

    // ============================================================
    // CART
    // ============================================================

    suspend fun addCartItem(cartItem: CartItemDto, token: String) {
        logApiCall("addCartItem")

        when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.addCartItem(cartItem, token)
            ApiType.GRAPHQL -> graphql.addCartItem(cartItem, token)
        }
    }

    suspend fun updateCartItem(cartItem: CartItemDto, token: String) {
        logApiCall("updateCartItem")

        when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.updateCartItem(cartItem, token)
            ApiType.GRAPHQL -> graphql.updateCartItem(cartItem, token)
        }
    }

    suspend fun deleteCartItem(cartItemId: String, token: String) {
        logApiCall("deleteCartItem")

        when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.deleteCartItem(cartItemId, token)
            ApiType.GRAPHQL -> graphql.deleteCartItem(cartItemId, token)
        }
    }

    suspend fun clearCart(token: String) {
        logApiCall("clearCart")

        when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.clearCart(token)
            ApiType.GRAPHQL -> graphql.clearCart(token)
        }
    }

    // ============================================================
    // PRODUCTS
    // ============================================================

    suspend fun getAllProducts(): List<ProductDto> {
        logApiCall("getAllProducts")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.getAllProducts()
            ApiType.GRAPHQL -> graphql.getAllProducts()
        }
    }

    suspend fun getProductById(id: String): ProductDto? {
        logApiCall("getProductById")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.getProductById(id)
            ApiType.GRAPHQL -> graphql.getProductById(id)
        }
    }

    suspend fun createProduct(product: ProductDto, token: String): ProductDto? {
        logApiCall("createProduct")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.createProduct(product, token)
            ApiType.GRAPHQL -> graphql.createProduct(product, token)
        }
    }

    suspend fun updateProduct(id: String, product: ProductDto, token: String) {
        logApiCall("updateProduct")

        when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.updateProduct(id, product, token)
            ApiType.GRAPHQL -> graphql.updateProduct(id, product, token)
        }
    }

    suspend fun deleteProduct(id: String, token: String) {
        logApiCall("deleteProduct")

        when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.deleteProduct(id, token)
            ApiType.GRAPHQL -> graphql.deleteProduct(id, token)
        }
    }

    // ============================================================
    // MEDIA
    // ============================================================

    suspend fun uploadImage(
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String,
        token: String
    ): UploadResponse {
        logApiCall("uploadImage")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.uploadImage(fileBytes, fileName, mimeType, token)
            ApiType.GRAPHQL -> graphql.uploadImage(fileBytes, fileName, mimeType, token)
        }
    }

    suspend fun deleteImage(filename: String, token: String) {
        logApiCall("deleteImage")

        when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.deleteImage(filename, token)
            ApiType.GRAPHQL -> graphql.deleteImage(filename, token)
        }
    }

    // ============================================================
    // ORDERS
    // ============================================================

    suspend fun createOrder(
        cartItems: List<CartItemDto>,
        totalAmount: String,
        couponCode: String?,
        couponDiscount: String?,
        token: String
    ): String {
        logApiCall("createOrder")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.createOrder(cartItems, totalAmount, couponCode, couponDiscount, token)
            ApiType.GRAPHQL -> graphql.createOrder(cartItems, totalAmount, couponCode, couponDiscount, token)
        }
    }

    suspend fun getOrders(token: String): List<OrderDto> {
        logApiCall("getOrders")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.getOrders(token)
            ApiType.GRAPHQL -> graphql.getOrders(token)
        }
    }

    // ============================================================
    // RESTAURANTS
    // ============================================================

    suspend fun getRestaurants(): List<RestaurantDto> {
        logApiCall("getRestaurants")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.getRestaurants()
            ApiType.GRAPHQL -> graphql.getRestaurants()
        }
    }

    // ============================================================
    // ADMIN
    // ============================================================

    suspend fun adminGetUsers(token: String): List<AdminUserDto> {
        logApiCall("adminGetUsers")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.adminGetUsers(token)
            ApiType.GRAPHQL -> graphql.adminGetUsers(token)
        }
    }

    suspend fun adminGetUsersCount(token: String): Int {
        logApiCall("adminGetUsersCount")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.adminGetUsersCount(token)
            ApiType.GRAPHQL -> graphql.adminGetUsersCount(token)
        }
    }

    suspend fun adminGetOrders(start: Long, end: Long, token: String): List<AdminOrderDto> {
        logApiCall("adminGetOrders")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.adminGetOrders(start, end, token)
            ApiType.GRAPHQL -> graphql.adminGetOrders(start, end, token)
        }
    }

    suspend fun adminGetOrdersCount(token: String): Int {
        logApiCall("adminGetOrdersCount")

        return when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.adminGetOrdersCount(token)
            ApiType.GRAPHQL -> graphql.adminGetOrdersCount(token)
        }
    }

    // ============================================================
    // ANALYTICS
    // ============================================================

    suspend fun getRevenueAnalytics(period: String, token: String) =
        when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.getRevenueAnalytics(period, token)
            ApiType.GRAPHQL -> graphql.getRevenueAnalytics(period, token)
        }

    suspend fun getOrdersByHourAnalytics(period: String, token: String) =
        when (ApiConfig.currentApiType) {
            ApiType.REST -> rest.getOrdersByHourAnalytics(period, token)
            ApiType.GRAPHQL -> graphql.getOrdersByHourAnalytics(period, token)
        }
}
