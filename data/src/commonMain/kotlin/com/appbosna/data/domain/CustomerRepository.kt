package com.appbosna.data.domain

import com.appbosna.data.remote.ApiUser
import com.appbosna.shared.domain.CartItem
import com.appbosna.shared.domain.Customer
import com.appbosna.shared.util.RequestState
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {

    fun getCurrentUserId(): String?

    suspend fun createCustomer(
        user: ApiUser?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )
    fun readCustomerFlow(): Flow<RequestState<Customer>>
    suspend fun updateCustomer(
        customer: Customer,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    suspend fun addItemToCard(
        cartItem: CartItem,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    suspend fun updateCartItemQuantity(
        productId: String,
        quantity: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    suspend fun deleteCartItem(
        productId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    suspend fun deleteAllCartItems(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    suspend fun signOut(): RequestState<Unit>
}