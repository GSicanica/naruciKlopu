/*
package com.appbosna.data.ui

import com.appbosna.data.config.ApiConfig
import com.appbosna.data.config.ApiType
import com.appbosna.data.usecase.PlaceOrderUseCase
import com.appbosna.shared.domain.CartItem

*/
/**
 * Example: API Switcher ViewModel
 *
 * Demonstrates how to switch between REST and GraphQL APIs at runtime
 *//*

class ApiSwitcherViewModel(
    private val placeOrderUseCase: PlaceOrderUseCase
) {
    // Current API type
    val currentApiType: ApiType
        get() = ApiConfig.currentApiType

    val currentApiName: String
        get() = ApiConfig.getCurrentApiName()

    */
/**
     * Switch to GraphQL API
     *//*

    fun switchToGraphQL() {
        ApiConfig.useGraphQL()
        println("✓ Now using: ${ApiConfig.getCurrentApiName()}")
    }

    */
/**
     * Switch to REST API
     *//*

    fun switchToRest() {
        ApiConfig.useRest()
        println("✓ Now using: ${ApiConfig.getCurrentApiName()}")
    }

    */
/**
     * Toggle between REST and GraphQL
     *//*

    fun toggleApi() {
        when (ApiConfig.currentApiType) {
            ApiType.REST -> switchToGraphQL()
            ApiType.GRAPHQL -> switchToRest()
        }
    }

    */
/**
     * Place order - automatically uses currently selected API
     *//*

    suspend fun placeOrder(
        items: List<CartItem>,
        totalAmount: Double,
        couponCode: String? = null,
        couponDiscount: Double? = null
    ): Result<String> {
        println("📡 Placing order via ${ApiConfig.getCurrentApiName()}")

        return placeOrderUseCase.execute(
            items = items,
            totalAmount = totalAmount,
            couponCode = couponCode,
            couponDiscount = couponDiscount
        )
    }

    */
/**
     * Place order with specific API (ignores global setting)
     *//*

    suspend fun placeOrderWithSpecificApi(
        apiType: ApiType,
        items: List<CartItem>,
        totalAmount: Double,
        couponCode: String? = null,
        couponDiscount: Double? = null
    ): Result<String> {
        println("📡 Placing order via ${apiType.name} (forced)")

        return when (apiType) {
            ApiType.REST -> placeOrderUseCase.executeWithRest(
                items, totalAmount, couponCode, couponDiscount
            )
            ApiType.GRAPHQL -> placeOrderUseCase.executeWithGraphQL(
                items, totalAmount, couponCode, couponDiscount
            )
        }
    }
}
*/
