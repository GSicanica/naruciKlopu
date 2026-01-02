package com.appbosna.data.usecase

import com.appbosna.data.remote.graphql.GraphQLRepository
import com.appbosna.data.remote.graphql.OrderItemInput

/**
 * Use Case for placing orders via GraphQL
 * 
 * Benefits over REST:
 * - Single request for order creation
 * - Returns exactly the data we need
 * - Type-safe with compile-time checking
 * - Automatic authentication via Bearer token
 */
class PlaceOrderWithGraphQLUseCase(
    private val graphqlRepo: GraphQLRepository
) {
    suspend fun execute(
        total: Double,
        items: List<OrderItemInput>,
        couponCode: String? = null,
        couponDiscount: Double? = null
    ): Result<String> {
        return graphqlRepo.createOrder(
            total = total,
            items = items,
            couponCode = couponCode,
            couponDiscount = couponDiscount
        ).map { order ->
            // Return order ID as success message
            "Order #${order.id} created successfully"
        }.recover { error ->
            // Handle GraphQL errors
            throw Exception("Failed to create order: ${error.message}")
        }
    }
}
