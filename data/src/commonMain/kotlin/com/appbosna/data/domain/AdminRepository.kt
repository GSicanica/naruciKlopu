package com.appbosna.data.domain

import com.appbosna.data.remote.KmpFile
import com.appbosna.shared.domain.Customer
import com.appbosna.shared.domain.Order
import com.appbosna.shared.domain.Product
import com.appbosna.shared.domain.Restaurant
import com.appbosna.shared.util.RequestState
import kotlinx.coroutines.flow.Flow

interface AdminRepository {

    fun getCurrentUserId(): String?

    suspend fun createNewProduct(
        product: Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )

    suspend fun uploadImageToStorage(file: KmpFile): String?

    suspend fun deleteImageFromStorage(
        downloadUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )

    fun readLastTenProducts(): Flow<RequestState<List<Product>>>

    suspend fun readProductById(id: String): RequestState<Product>

    suspend fun updateProductThumbnail(
        productId: String,
        downloadUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )

    suspend fun updateProduct(
        product: Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )

    suspend fun deleteProduct(
        productId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )

    fun searchProductsByTitle(
        searchQuery: String,
    ): Flow<RequestState<List<Product>>>

    suspend fun getOrdersByDateRange(
        startDate: Long,
        endDate: Long
    ): RequestState<List<Order>>

    suspend fun getAllUsers(): RequestState<List<Customer>>

    suspend fun getTotalOrdersCount(): RequestState<Int>

    suspend fun getTotalUsersCount(): RequestState<Int>

    // Analytics endpoints
    suspend fun getRevenueAnalytics(period: String): RequestState<RevenueAnalytics>
    
    suspend fun getOrdersByHourAnalytics(period: String): RequestState<OrdersByHourAnalytics>

    suspend fun getRestaurants(): List<Restaurant>
}