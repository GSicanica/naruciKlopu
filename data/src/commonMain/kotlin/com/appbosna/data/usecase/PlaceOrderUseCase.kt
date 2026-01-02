package com.appbosna.data.usecase

import com.appbosna.data.config.ApiConfig
import com.appbosna.data.config.ApiType
import com.appbosna.data.remote.ApiService
import com.appbosna.data.remote.graphql.GraphQLRepository
import com.appbosna.data.remote.graphql.OrderItemInput
import com.appbosna.shared.domain.CartItem

/**
 * Unified Order Use Case
 *
 * Automatically switches between REST and GraphQL APIs based on ApiConfig
 *
 * Usage:
 * ```
 * // Switch API at runtime
 * ApiConfig.useGraphQL()  // or ApiConfig.useRest()
 *
 * // Place order - will use currently configured API
 * val result = placeOrderUseCase.execute(items, total)
 * ```
 */
class PlaceOrderUseCase(
    private val restApi: ApiService,
    private val graphqlRepo: GraphQLRepository,
    private val tokenProvider: () -> String
) {
    /**
     * Place an order using the currently configured API
     *
     * @param items List of cart items
     * @param totalAmount Total order amount
     * @param couponCode Optional coupon code
     * @param couponDiscount Optional coupon discount amount
     * @return Result with order ID or error
     */
    suspend fun execute(
        items: List<CartItem>,
        totalAmount: Double,
        couponCode: String? = null,
        couponDiscount: Double? = null
    ): Result<String> {
        return when (ApiConfig.currentApiType) {
            ApiType.REST -> executeWithRest(items, totalAmount, couponCode, couponDiscount)
            ApiType.GRAPHQL -> executeWithGraphQL(items, totalAmount, couponCode, couponDiscount)
        }
    }

    /**
     * Force execution with REST API (ignores global config)
     */
    suspend fun executeWithRest(
        items: List<CartItem>,
        totalAmount: Double,
        couponCode: String? = null,
        couponDiscount: Double? = null
    ): Result<String> {
        return try {
            val cartItemsDto = items.map { it.toDto() }

            val orderId = restApi.createOrder(
                cartItems = cartItemsDto,
                totalAmount = totalAmount.toString(),
                couponCode = couponCode,
                couponDiscount = couponDiscount?.toString(),
                token = tokenProvider()
            )

            Result.success("Order #${orderId} created successfully (REST)")
        } catch (e: Exception) {
            Result.failure(Exception("REST API Error: ${e.message}"))
        }
    }

    /**
     * Force execution with GraphQL API (ignores global config)
     */
    suspend fun executeWithGraphQL(
        items: List<CartItem>,
        totalAmount: Double,
        couponCode: String? = null,
        couponDiscount: Double? = null
    ): Result<String> {
        val graphqlItems = items.map { item ->
            OrderItemInput(
                productId = item.productId.toInt(),
                quantity = item.quantity.toInt(),
            )
        }

        return graphqlRepo.createOrder(
            total = totalAmount,
            items = graphqlItems,
            couponCode = couponCode,
            couponDiscount = couponDiscount
        ).map { order ->
            "Order #${order.id} created successfully (GraphQL)"
        }.recover { error ->
            throw Exception("GraphQL API Error: ${error.message}")
        }
    }

    /**
     * Get currently used API type
     */
    fun getCurrentApiType(): ApiType = ApiConfig.currentApiType

    /**
     * Get current API name for display
     */
    fun getCurrentApiName(): String = ApiConfig.getCurrentApiName()
}

// Extension function to convert CartItem to DTO
private fun CartItem.toDto(): com.appbosna.data.remote.CartItemDto {
    return com.appbosna.data.remote.CartItemDto(
        id = this.id,
        productId = this.productId,
        title = this.title,
        price = this.price,
        quantity = this.quantity,
        thumbnail = this.thumbnail ?: ""
    )
}
