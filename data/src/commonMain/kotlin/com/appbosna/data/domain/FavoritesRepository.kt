package com.appbosna.data.domain

import com.appbosna.shared.domain.Product
import com.appbosna.shared.util.RequestState
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun getCurrentUserId(): String?
    fun addProductToFavorites(productId: String): Flow<RequestState<Unit>>
    fun removeProductFromFavorites(productId: String): Flow<RequestState<Unit>>
    fun readFavoriteProductIds(): Flow<RequestState<List<String>>>
    fun readFavoriteProducts(): Flow<RequestState<List<Product>>>
} 