package com.appbosna.data.remote.graphql

import com.appbosna.data.remote.AdminOrderDto
import com.appbosna.data.remote.AdminUserDto
import com.appbosna.data.remote.ApiUser
import com.appbosna.data.remote.CartItemDto
import com.appbosna.data.remote.CategoryDto
import com.appbosna.data.remote.CustomerDto
import com.appbosna.data.remote.LoginReq
import com.appbosna.data.remote.LoginRes
import com.appbosna.data.remote.OrderDto
import com.appbosna.data.remote.ProductDto
import com.appbosna.data.remote.RegisterReq
import com.appbosna.data.remote.RestaurantDto
import com.appbosna.data.remote.SettingsEnvelope
import com.appbosna.data.remote.UploadResponse
import com.appbosna.shared.domain.OrderItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

/**
 * GraphQL Repository - Alternative to REST API
 *
 * Full feature parity s ApiService (postupno).
 */
class GraphQLRepository(
    private val client: GraphQLClient
) {

    // ============================================================
    // AUTH
    // ============================================================

    suspend fun login(req: LoginReq): LoginRes {
        // TODO: Implement GraphQL login mutation
        throw NotImplementedError("GraphQL login not yet implemented - use REST API for auth")
    }

    suspend fun register(req: RegisterReq): LoginRes {
        // TODO: Implement GraphQL register mutation
        throw NotImplementedError("GraphQL register not yet implemented - use REST API for auth")
    }

    suspend fun loginOrRegisterWithGoogle(
        name: String,
        email: String,
        uid: String
    ): LoginRes {
        val variables = mapOf(
            "name" to JsonPrimitive(name),
            "email" to JsonPrimitive(email),
            "uid" to JsonPrimitive(uid)
        )

        val result = client.mutate<LoginGoogleResponse>(
            mutation = GraphQLMutations.LOGIN_OR_REGISTER_WITH_GOOGLE,
            variables = variables
        )

        val response = result.getOrNull()
            ?: throw RuntimeException("Failed to login or register with Google")

        return response.loginOrRegisterWithGoogle
    }

    suspend fun me(token: String): ApiUser? {
        // Ako treba token u headeru, to se rješava u HttpClient konfiguraciji.
        val result = client.query<MeResponse>(
            query = GraphQLQueries.GET_ME,
            variables = null
        )

        val response = result.getOrNull() ?: return null
        val me = response.me ?: return null

        return ApiUser(
            id = me.id?.toString().orEmpty(),
            name = me.name.orEmpty(),
            email = me.email.orEmpty(),
            displayName = me.displayName.orEmpty(),
            photoUrl = me.photoUrl.orEmpty(),
            uid = me.uid.orEmpty(),
            phoneNumber = me.phoneNumber.orEmpty(),
            isAdmin = me.is_admin.orEmpty()
        )
    }

    // ============================================================
    // SETTINGS
    // ============================================================

    suspend fun getSettings(): SettingsEnvelope {
        // TODO: Implement GraphQL settings query
        throw NotImplementedError("GraphQL settings not yet implemented")
    }

    // ============================================================
    // CATEGORIES
    // ============================================================

    suspend fun getCategories(): List<CategoryDto> {
        val result = getCategoriesResult()
        return result.getOrElse { emptyList() }.map { it.toDto() }
    }

    // ============================================================
    // CUSTOMER
    // ============================================================

    suspend fun getCustomer(token: String): CustomerDto {
        val result = client.query<CustomerResponse>(
            GraphQLQueries.GET_CUSTOMER
        )

        val response = result.getOrElse { throw it }
        val customer = response.customer ?: throw RuntimeException("Customer not found")

        return customer.toDto()
    }

    suspend fun updateCustomer(customer: CustomerDto, token: String) {
        val variables = buildMap {
            put("firstName", JsonPrimitive(customer.firstName))
            put("lastName", JsonPrimitive(customer.lastName))
            put("email", JsonPrimitive(customer.email))
            put("city", JsonPrimitive(customer.city))
            put("postalCode", JsonPrimitive(customer.postalCode))
            put("address", JsonPrimitive(customer.address))
            put("phone", JsonPrimitive(customer.phone))
        }

        val result = client.mutate<Unit>(
            mutation = GraphQLMutations.UPDATE_CUSTOMER,
            variables = variables
        )

        result.getOrElse { throw it }
    }

    // ============================================================
    // CART
    // ============================================================

    suspend fun addCartItem(cartItem: CartItemDto, token: String) {
        val variables = mapOf(
            "productId" to JsonPrimitive(cartItem.productId.toIntOrNull() ?: 0),
            "quantity" to JsonPrimitive(cartItem.quantity.toIntOrNull() ?: 1)
        )

        val result = client.mutate<Unit>(
            mutation = GraphQLMutations.ADD_CART_ITEM,
            variables = variables
        )

        result.getOrElse { throw it }
    }

    suspend fun updateCartItem(cartItem: CartItemDto, token: String) {
        val variables = mapOf(
            "productId" to JsonPrimitive(cartItem.productId.toIntOrNull() ?: 0),
            "quantity" to JsonPrimitive(cartItem.quantity.toIntOrNull() ?: 1)
        )

        val result = client.mutate<Unit>(
            mutation = GraphQLMutations.UPDATE_CART_ITEM,
            variables = variables
        )

        result.getOrElse { throw it }
    }

    suspend fun deleteCartItem(cartItemId: String, token: String) {
        val variables = mapOf(
            "id" to JsonPrimitive(cartItemId.toIntOrNull() ?: 0)
        )

        val result = client.mutate<Unit>(
            mutation = GraphQLMutations.DELETE_CART_ITEM,
            variables = variables
        )

        result.getOrElse { throw it }
    }

    suspend fun clearCart(token: String) {
        val result = client.mutate<Unit>(
            mutation = GraphQLMutations.CLEAR_CART
        )

        result.getOrElse { throw it }
    }

    // ============================================================
    // PRODUCTS
    // ============================================================

    suspend fun getAllProducts(): List<ProductDto> {
        val result = getProducts()
        return result.getOrElse { emptyList() }.map { it.toDto() }
    }

    suspend fun getProductById(id: String): ProductDto? {
        return try {
            val result = getProduct(id.toIntOrNull() ?: 0)
            result.getOrNull()?.toDto()
        } catch (e: Exception) {
            println("❌ GraphQL getProductById failed: ${e.message}")
            null
        }
    }

    suspend fun createProduct(product: ProductDto, token: String): ProductDto? {
        // Ovisi o ProductInput shemi na backendu – za sada stub
        throw NotImplementedError("GraphQL createProduct not yet implemented")
    }

    suspend fun updateProduct(id: String, product: ProductDto, token: String) {
        throw NotImplementedError("GraphQL updateProduct not yet implemented")
    }

    suspend fun deleteProduct(id: String, token: String) {
        throw NotImplementedError("GraphQL deleteProduct not yet implemented")
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
        // TODO: Implement GraphQL file upload (multipart or base64)
        throw NotImplementedError("GraphQL uploadImage not yet implemented")
    }

    suspend fun deleteImage(filename: String, token: String) {
        // TODO: Implement GraphQL deleteImage mutation
        throw NotImplementedError("GraphQL deleteImage not yet implemented")
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
        // TODO: Implement GraphQL createOrder mutation returning orderId
        throw NotImplementedError("GraphQL createOrder not yet implemented")
    }

    suspend fun getOrders(token: String): List<OrderDto> {
        // TODO: Convert Result<List<GraphQLOrder>> to List<OrderDto>
        throw NotImplementedError("GraphQL getOrders needs DTO conversion")
    }

    // ============================================================
    // RESTAURANTS
    // ============================================================

    suspend fun getRestaurants(): List<RestaurantDto> {
        // TODO: Implement GraphQL restaurants query
        throw NotImplementedError("GraphQL getRestaurants not yet implemented")
    }

    // ============================================================
    // ADMIN
    // ============================================================

    suspend fun adminGetUsers(token: String): List<AdminUserDto> {
        // TODO: Implement GraphQL admin users query
        throw NotImplementedError("GraphQL adminGetUsers not yet implemented")
    }

    suspend fun adminGetUsersCount(token: String): Int {
        // TODO: Implement GraphQL admin users count query
        throw NotImplementedError("GraphQL adminGetUsersCount not yet implemented")
    }

    suspend fun adminGetOrders(start: Long, end: Long, token: String): List<AdminOrderDto> {
        // TODO: Implement GraphQL admin orders query
        throw NotImplementedError("GraphQL adminGetOrders not yet implemented")
    }

    suspend fun adminGetOrdersCount(token: String): Int {
        // TODO: Implement GraphQL admin orders count query
        throw NotImplementedError("GraphQL adminGetOrdersCount not yet implemented")
    }

    // ============================================================
    // LEGACY METHODS (for backward compatibility)
    // ============================================================

    // Products
    suspend fun getProducts(categoryId: Int? = null): Result<List<GraphQLProduct>> {
        val variables = categoryId?.let { mapOf("categoryId" to JsonPrimitive(it)) }
        return client.query<ProductsResponse>(
            GraphQLQueries.GET_PRODUCTS,
            variables
        ).map { it.products }
    }

    suspend fun getProduct(id: Int): Result<GraphQLProduct?> {
        return client.query<ProductResponse>(
            GraphQLQueries.GET_PRODUCT,
            mapOf("id" to JsonPrimitive(id))
        ).map { it.product }
    }

    // Categories
    suspend fun getCategoriesResult(): Result<List<GraphQLCategory>> {
        return client.query<CategoriesResponse>(
            GraphQLQueries.GET_CATEGORIES
        ).map { it.categories }
    }

    // Orders (requires authentication)
    suspend fun getMyOrders(): Result<List<GraphQLOrder>> {
        return client.query<OrdersResponse>(
            GraphQLQueries.GET_MY_ORDERS
        ).map { it.orders }
    }

    suspend fun getOrder(id: Int): Result<GraphQLOrder?> {
        return client.query<OrderResponse>(
            GraphQLQueries.GET_ORDER,
            mapOf("id" to JsonPrimitive(id))
        ).map { it.order }
    }

    // Create order (mutation - requires authentication)
    suspend fun createOrder(
        total: Double,
        items: List<OrderItemInput>,
        couponCode: String? = null,
        couponDiscount: Double? = null
    ): Result<GraphQLOrder> {
        val variables = buildMap {
            put("total", JsonPrimitive(total))
            // Serialize items list to JSON
            val itemsJson = Json.encodeToString(items)
            put("items", Json.parseToJsonElement(itemsJson))
            couponCode?.let { put("couponCode", JsonPrimitive(it)) }
            couponDiscount?.let { put("couponDiscount", JsonPrimitive(it)) }
        }

        return client.mutate<CreateOrderResponse>(
            GraphQLMutations.CREATE_ORDER,
            variables
        ).map { it.createOrder }
    }

    // Helper: Convert domain models to GraphQL input
    fun orderItemToInput(item: OrderItem): OrderItemInput {
        return OrderItemInput(
            productId = item.productId.toIntOrNull() ?: 0,
            quantity = item.quantity.toIntOrNull() ?: 1
        )
    }

    // Helper: Convert GraphQL order items to domain OrderItem
    fun graphQLOrderItemToDomain(item: GraphQLOrderItem): OrderItem {
        return OrderItem(
            id = item.id?.toString(),
            productId = item.product_id?.toString() ?: "",
            title = item.title ?: "",
            price = item.price?.toString() ?: "0",
            quantity = item.quantity?.toString() ?: "1",
            thumbnail = item.thumbnail
        )
    }

    // ============================================================
    // ANALYTICS
    // ============================================================

    suspend fun getRevenueAnalytics(period: String, token: String): com.appbosna.data.domain.RevenueAnalytics {
        // TODO: Implement GraphQL query for revenue analytics
        throw NotImplementedError("GraphQL analytics not yet implemented - use REST API")
    }

    suspend fun getOrdersByHourAnalytics(period: String, token: String): com.appbosna.data.domain.OrdersByHourAnalytics {
        // TODO: Implement GraphQL query for orders by hour analytics
        throw NotImplementedError("GraphQL analytics not yet implemented - use REST API")
    }
}
