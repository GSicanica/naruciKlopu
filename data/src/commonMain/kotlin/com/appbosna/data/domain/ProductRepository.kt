package com.appbosna.data.domain

import com.appbosna.shared.domain.Product
import com.appbosna.shared.util.RequestState
import kotlinx.coroutines.flow.Flow

interface ProductRepository {

    fun refresh()

    fun getCurrentUserId(): String?

    fun readDiscountedProducts(): Flow<RequestState<List<Product>>>

    fun readNewProducts(): Flow<RequestState<List<Product>>>

    fun readProductByIdFlow(id: String): Flow<RequestState<Product>>

    fun readProductsByIdsFlow(ids: List<String>): Flow<RequestState<List<Product>>>

    // NOVO – filtriranje po restoranu
    fun readProductsByRestaurantFlow(restaurantId: Int): Flow<RequestState<List<Product>>>
}
