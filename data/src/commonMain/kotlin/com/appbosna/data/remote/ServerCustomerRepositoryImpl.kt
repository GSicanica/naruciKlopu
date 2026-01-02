package com.appbosna.data.remote

import TokenStore
import com.appbosna.data.domain.CustomerRepository
import com.appbosna.data.usecase.toIntSafe
import com.appbosna.shared.domain.CartItem
import com.appbosna.shared.domain.Customer
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
import kotlinx.coroutines.flow.shareIn

class ServerCustomerRepositoryImpl(
    private val api: UnifiedApiService,
    private val tokenStore: TokenStore
) : CustomerRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val refresh = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val customerShared: SharedFlow<RequestState<Customer>> =
        refresh
            .flatMapLatest { loadCustomerOnce() }
            .shareIn(scope, SharingStarted.WhileSubscribed(5000), replay = 1)

    override fun getCurrentUserId(): String? = tokenStore.getToken()

    override fun readCustomerFlow(): Flow<RequestState<Customer>> = customerShared

    private fun loadCustomerOnce(): Flow<RequestState<Customer>> = flow {
        val token = tokenStore.getToken()
        println("🔑 loadCustomerOnce: Token from store = ${token?.take(20)}...")
        
        if (token.isNullOrBlank()) {
            println("🔑 loadCustomerOnce: No token found - emitting 'Niste prijavljeni' error")
            emit(RequestState.Error("Niste prijavljeni."))
            return@flow
        }

        try {
            println("🔑 loadCustomerOnce: Calling api.me() with token...")
            val apiUser = api.me(token)
            
            if (apiUser == null) {
                println("🔑 loadCustomerOnce: api.me() returned null - network/API error")
                // API call failed - DON'T delete token, just emit error
                // Token might still be valid, could be a temporary network issue
                emit(RequestState.Error("Nije moguće dohvatiti korisničke podatke. Provjerite internet konekciju."))
                return@flow
            }

            println("🔑 loadCustomerOnce: api.me() success - user = ${apiUser.email}")
            val dto = api.getCustomer(token)

            emit(RequestState.Success(dto.toDomain(apiUser)))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(RequestState.Error(e.message ?: "Greška pri dohvaćanju korisnika."))
        }
    }

    private fun notifyCustomerChanged() {
        refresh.tryEmit(Unit)
    }

    override suspend fun createCustomer(
        user: ApiUser?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (user == null) {
            onError("Korisnik je null.")
            return
        }

        val token = tokenStore.getToken()
        if (token.isNullOrBlank()) {
            onError("Niste prijavljeni.")
            return
        }

        try {
            val (first, last) = splitName(user.name.ifBlank { user.displayName })

            val dto = CustomerDto(
                id = user.id,
                firstName = first,
                lastName = last,
                email = user.email,
                city = "",
                postalCode = "",
                address = "",
                phone = user.phoneNumber,
                cart = emptyList()
            )

            api.updateCustomer(dto, token)
            notifyCustomerChanged()
            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.message ?: "Greška pri kreiranju korisnika.")
        }
    }

    override suspend fun updateCustomer(
        customer: Customer,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val token = tokenStore.getToken()
        if (token.isNullOrBlank()) {
            onError("Niste prijavljeni.")
            return
        }

        try {
            api.updateCustomer(customer.toDto(), token)
            notifyCustomerChanged()
            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.message ?: "Greška pri ažuriranju profila.")
        }
    }

    override suspend fun addItemToCard(
        cartItem: CartItem,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val token = tokenStore.getToken()
        if (token.isNullOrBlank()) {
            onError("Niste prijavljeni.")
            return
        }

        try {
            api.addCartItem(cartItem.toDto(), token)
            notifyCustomerChanged()
            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.message ?: "Greška pri dodavanju u korpu.")
        }
    }

    override suspend fun updateCartItemQuantity(
        productId: String,
        quantity: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val token = tokenStore.getToken()
        if (token.isNullOrBlank()) {
            onError("Niste prijavljeni.")
            return
        }

        if (quantity.toIntSafe() < 1) {
            onError("Količina mora biti veća od nule.")
            return
        }

        try {
            val dto = CartItemDto(
                id = "",
                productId = productId,
                title = "",
                price = "",
                quantity = quantity,
                thumbnail = ""
            )

            api.updateCartItem(dto, token)
            notifyCustomerChanged()
            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.message ?: "Greška pri izmjeni količine.")
        }
    }

    override suspend fun deleteCartItem(
        productId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val token = tokenStore.getToken()
        if (token.isNullOrBlank()) {
            onError("Niste prijavljeni.")
            return
        }

        try {
            api.deleteCartItem(productId, token)
            notifyCustomerChanged()
            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.message ?: "Greška pri brisanju iz korpe.")
        }
    }

    override suspend fun deleteAllCartItems(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val token = tokenStore.getToken()
        if (token.isNullOrBlank()) {
            onError("Niste prijavljeni.")
            return
        }

        try {
            api.clearCart(token)
            notifyCustomerChanged()
            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.message ?: "Greška pri pražnjenju korpe.")
        }
    }

    override suspend fun signOut(): RequestState<Unit> {
        tokenStore.setToken(null)
        notifyCustomerChanged()
        return RequestState.Success(Unit)
    }

    private fun splitName(full: String): Pair<String, String> {
        val t = full.trim()
        if (t.isBlank()) return "" to ""

        val p = t.split("\\s+".toRegex())
        return when {
            p.size == 1 -> p[0] to ""
            else -> p.first() to p.drop(1).joinToString(" ")
        }
    }
}
