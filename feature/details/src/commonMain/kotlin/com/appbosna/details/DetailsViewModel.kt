package com.appbosna.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appbosna.data.domain.CustomerRepository
import com.appbosna.data.domain.ProductRepository
import com.appbosna.shared.domain.CartItem
import com.appbosna.shared.util.RequestState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val productId: String,
    productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
) : ViewModel() {
    
    init {
        println("🔵 DetailsViewModel created with productId: $productId")
    }
    
    val product = productRepository.readProductByIdFlow(productId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RequestState.Loading
    )

    var quantity by mutableStateOf(1)
        private set

    var selectedFlavor: String? by mutableStateOf(null)
        private set

    fun updateQuantity(value: Int) {
        quantity = value
    }

    fun updateFlavor(value: String) {
        selectedFlavor = value
    }

    fun addItemToCart(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (productId.isBlank()) {
                onError("Product id is not found.")
                return@launch
            }

            val currentProduct = (product.value as? RequestState.Success)?.data
            if (currentProduct == null) {
                onError("Product details not loaded yet.")
                return@launch
            }

            val item = CartItem(
                id = currentProduct.id,
                productId = currentProduct.id,
                title = currentProduct.title,
                price = currentProduct.price,
                quantity = quantity.toString(),
                thumbnail = currentProduct.thumbnail,
                flavor = selectedFlavor
            )

            customerRepository.addItemToCard(
                cartItem = item,
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }

}
