package com.appbosna.data.remote

import com.appbosna.data.domain.AdminRepository
import com.appbosna.shared.domain.Customer
import com.appbosna.shared.domain.Order
import com.appbosna.shared.domain.Restaurant
import com.appbosna.shared.util.RequestState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ServerAdminRepositoryImpl(
    private val api: UnifiedApiService,
    private val tokenProvider: () -> String,
) : AdminRepository {

    override fun getCurrentUserId(): String? = null

    override suspend fun createNewProduct(
        product: com.appbosna.shared.domain.Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val token = tokenProvider()
            if (token.isBlank()) return onError("Nema tokena (prijavi se ponovo).")

            val body = product.toDtoForCreate()
            val result = api.createProduct(body, token)
            if (result != null) {
                onSuccess()
            } else {
                onError("Greška pri kreiranju proizvoda")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onError("Greška pri kreiranju proizvoda: ${e.message ?: "nepoznata"}")
        }
    }

    override suspend fun uploadImageToStorage(file: KmpFile): String? {
        return null
    }

    override suspend fun deleteImageFromStorage(
        downloadUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        onSuccess()
    }

    override fun readLastTenProducts(): Flow<RequestState<List<com.appbosna.shared.domain.Product>>> = flow {
        emit(RequestState.Loading)
        try {
            val list = api.getAllProducts().map { it.toDomain() }
            emit(RequestState.Success(list.take(10)))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(RequestState.Error("Greška pri dohvaćanju proizvoda: ${e.message}"))
        }
    }

    override suspend fun readProductById(id: String): RequestState<com.appbosna.shared.domain.Product> {
        return try {
            val dto = api.getProductById(id)
            if (dto != null) {
                RequestState.Success(dto.toDomain())
            } else {
                RequestState.Error("Proizvod nije pronađen")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            RequestState.Error("Greška pri dohvaćanju proizvoda: ${e.message}")
        }
    }

    override suspend fun updateProduct(
        product: com.appbosna.shared.domain.Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val token = tokenProvider()
            if (token.isBlank()) return onError("Nema tokena.")

            api.updateProduct(product.id, product.toDtoForUpdate(), token)
            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            onError("Greška pri ažuriranju proizvoda: ${e.message}")
        }
    }

    override suspend fun deleteProduct(
        productId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val token = tokenProvider()
            if (token.isBlank()) return onError("Nema tokena.")
            api.deleteProduct(productId, token)
            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            onError("Greška pri brisanju proizvoda: ${e.message}")
        }
    }

    override fun searchProductsByTitle(
        searchQuery: String
    ): Flow<RequestState<List<com.appbosna.shared.domain.Product>>> = flow {
        emit(RequestState.Loading)
        try {
            val all = api.getAllProducts().map { it.toDomain() }
            emit(
                RequestState.Success(
                    all.filter { it.title.contains(searchQuery, ignoreCase = true) }
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emit(RequestState.Error("Greška pri pretraživanju: ${e.message}"))
        }
    }

    override suspend fun getOrdersByDateRange(
        startDate: Long,
        endDate: Long
    ): RequestState<List<Order>> {
        return try {
            val token = tokenProvider()
            if (token.isBlank()) return RequestState.Error("Niste prijavljeni.")

            val adminOrders = api.adminGetOrders(startDate, endDate, token)
            val list = adminOrders.map { it.toDomainOrder() }

            RequestState.Success(list)
        } catch (e: Exception) {
            e.printStackTrace()
            RequestState.Error("Greška pri dohvaćanju narudžbi: ${e.message}")
        }
    }

    override suspend fun getAllUsers(): RequestState<List<Customer>> {
        return try {
            val token = tokenProvider()
            if (token.isBlank()) return RequestState.Error("Niste prijavljeni.")

            val users = api.adminGetUsers(token).map { it.toDomainCustomer() }
            RequestState.Success(users)
        } catch (e: Exception) {
            e.printStackTrace()
            RequestState.Error("Greška pri dohvaćanju korisnika: ${e.message}")
        }
    }

    override suspend fun getTotalOrdersCount(): RequestState<Int> {
        return try {
            val token = tokenProvider()
            if (token.isBlank()) return RequestState.Error("Niste prijavljeni.")
            RequestState.Success(api.adminGetOrdersCount(token))
        } catch (e: Exception) {
            e.printStackTrace()
            RequestState.Error("Greška pri brojanju narudžbi: ${e.message}")
        }
    }

    override suspend fun getTotalUsersCount(): RequestState<Int> {
        return try {
            val token = tokenProvider()
            if (token.isBlank()) return RequestState.Error("Niste prijavljeni.")
            RequestState.Success(api.adminGetUsersCount(token))
        } catch (e: Exception) {
            e.printStackTrace()
            RequestState.Error("Greška pri brojanju korisnika: ${e.message}")
        }
    }

    override suspend fun getRestaurants(): List<Restaurant> {
        return try {
            api.getRestaurants().map { it.toDomain() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun updateProductThumbnail(
        productId: String,
        downloadUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        onSuccess()
    }

    override suspend fun getRevenueAnalytics(period: String): RequestState<com.appbosna.data.domain.RevenueAnalytics> {
        return try {
            val token = tokenProvider()
            if (token.isBlank()) return RequestState.Error("Niste prijavljeni.")
            
            val analytics = api.getRevenueAnalytics(period, token)
            RequestState.Success(analytics)
        } catch (e: Exception) {
            e.printStackTrace()
            RequestState.Error("Greška pri dohvaćanju revenue analytics: ${e.message}")
        }
    }

    override suspend fun getOrdersByHourAnalytics(period: String): RequestState<com.appbosna.data.domain.OrdersByHourAnalytics> {
        return try {
            val token = tokenProvider()
            if (token.isBlank()) return RequestState.Error("Niste prijavljeni.")
            
            val analytics = api.getOrdersByHourAnalytics(period, token)
            RequestState.Success(analytics)
        } catch (e: Exception) {
            e.printStackTrace()
            RequestState.Error("Greška pri dohvaćanju orders by hour analytics: ${e.message}")
        }
    }
}
