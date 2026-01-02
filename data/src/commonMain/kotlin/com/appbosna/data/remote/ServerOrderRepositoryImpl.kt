package com.appbosna.data.remote

import com.appbosna.data.domain.OrderRepository
import com.appbosna.shared.domain.Order

class ServerOrderRepositoryImpl(
    private val api: UnifiedApiService,
    private val tokenProvider: () -> String
) : OrderRepository {

    override fun getCurrentUserId(): String? = null

    override suspend fun createTheOrder(
        order: Order,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // domain CartItem -> CartItemDto (extension u istom paketu)
            val cartItemsDto = order.items.map { it.toDto() }
            
            println("========================================")
            println("CREATE ORDER DEBUG:")
            println("customerId: ${order.customerId}")
            println("totalAmount: ${order.totalAmount}")
            println("items count: ${order.items.size}")
            order.items.forEachIndexed { index, item ->
                println("  Item $index: productId=${item.productId}, qty=${item.quantity}, price=${item.price}")
            }
            println("token: ${tokenProvider().take(20)}...")
            println("========================================")

            api.createOrder(
                cartItems = cartItemsDto,
                totalAmount = order.totalAmount.toString(),
                couponCode = null,
                couponDiscount = null,
                token = tokenProvider()
            )


            onSuccess()
        } catch (t: Throwable) {
            println("CREATE ORDER ERROR: ${t.message}")
            println("Error class: ${t::class.simpleName}")
            t.printStackTrace()
            onError(t.message ?: "Unknown error")
        }
    }
}
