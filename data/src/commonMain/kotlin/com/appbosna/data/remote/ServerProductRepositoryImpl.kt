package com.appbosna.data.remote

import com.appbosna.data.domain.ProductRepository
import com.appbosna.shared.domain.Product
import com.appbosna.shared.util.RequestState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.plus

class ServerProductRepositoryImpl(
    private val api: UnifiedApiService
) : ProductRepository {

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob()) + Dispatchers.IO

    private val refresh = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val allProductsShared: SharedFlow<RequestState<List<Product>>> =
        refresh
            .flatMapLatest {
                flow {
                    emit(RequestState.Loading)
                    try {
                        val list = api.getAllProducts().map { it.toDomain() }
                        emit(RequestState.Success(list))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        emit(
                            RequestState.Error(
                                "Ne mogu se spojiti na server. Provjeri internet vezu ili pokušaj ponovo."
                            )
                        )
                    }
                }
            }
            .shareIn(scope, started = SharingStarted.WhileSubscribed(5_000), replay = 1)


    override fun refresh() {
        refresh.tryEmit(Unit)
    }

    override fun getCurrentUserId(): String? = null

    override fun readDiscountedProducts(): Flow<RequestState<List<Product>>> =
        allProductsShared.map { state ->
            when (state) {
                is RequestState.Success -> RequestState.Success(state.data.filter { it.isActive }) // Show all active products
                is RequestState.Error   -> state
                is RequestState.Loading -> state
                is RequestState.Idle    -> RequestState.Loading
            }
        }

    override fun readNewProducts(): Flow<RequestState<List<Product>>> =
        allProductsShared.map { state ->
            when (state) {
                is RequestState.Success -> RequestState.Success(state.data.filter { it.isActive }) // Show all active products
                is RequestState.Error   -> state
                is RequestState.Loading -> state
                is RequestState.Idle    -> RequestState.Loading
            }
        }

    override fun readProductByIdFlow(id: String): Flow<RequestState<Product>> =
        allProductsShared.map { state ->
            println("🔍 readProductByIdFlow called with id: $id")
            when (state) {
                is RequestState.Success -> {
                    val found = state.data.find { it.id == id }
                    println("🔍 Found product: ${found?.title} (id: ${found?.id})")
                    found?.let { RequestState.Success(it) }
                        ?: RequestState.Error("Proizvod ($id) nije pronađen.")
                }
                is RequestState.Error   -> RequestState.Error(state.message)
                is RequestState.Loading -> RequestState.Loading
                is RequestState.Idle    -> RequestState.Loading
            }
        }

    override fun readProductsByIdsFlow(ids: List<String>): Flow<RequestState<List<Product>>> =
        allProductsShared.map { state ->
            when (state) {
                is RequestState.Success ->
                    RequestState.Success(state.data.filter { it.id in ids.toSet() })
                is RequestState.Error   -> state
                is RequestState.Loading -> state
                is RequestState.Idle    -> RequestState.Loading
            }
        }

    override fun readProductsByRestaurantFlow(restaurantId: Int): Flow<RequestState<List<Product>>> =
        allProductsShared.map { state ->
            when (state) {
                is RequestState.Success ->
                    RequestState.Success(state.data.filter { it.restaurantId == restaurantId })
                is RequestState.Error   -> state
                is RequestState.Loading -> RequestState.Loading
                is RequestState.Idle    -> RequestState.Loading
            }
        }
}
