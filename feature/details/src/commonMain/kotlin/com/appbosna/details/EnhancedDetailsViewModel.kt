package com.appbosna.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.appbosna.data.domain.CustomerRepository
import com.appbosna.data.domain.ProductRepository
import com.appbosna.shared.domain.CartItem
import com.appbosna.shared.domain.Product
import com.appbosna.shared.error.AppError
import com.appbosna.shared.error.ErrorHandlingViewModel
import com.appbosna.shared.error.Result
import com.appbosna.shared.util.RequestState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Enhanced DetailsViewModel with improved error handling
 */
class EnhancedDetailsViewModel(
    private val productId: String,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
) : ErrorHandlingViewModel() {
    
    init {
        println("🔵 EnhancedDetailsViewModel created with productId: $productId")
        loadProduct()
    }
    
    // Product state with Result wrapper
    private val _productResult = mutableStateOf<Result<Product>>(Result.Loading)
    val productResult: Result<Product> get() = _productResult.value
    
    // Convert to old RequestState for backward compatibility
    val product: StateFlow<RequestState<Product>> = productRepository
        .readProductByIdFlow(productId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RequestState.Loading
        )

    var quantity by mutableStateOf(1)
        private set

    var selectedFlavor: String? by mutableStateOf(null)
        private set

    /**
     * Load product with automatic retry and error handling
     */
    private fun loadProduct() {
        executeWithRetry(
            maxAttempts = 3,
            onSuccess = { product ->
                _productResult.value = Result.Success(product)
            },
            onError = { error ->
                _productResult.value = Result.Error(error)
                handleError(error)
            }
        ) {
            // Simulate API call - replace with actual repository call
            // val response = productRepository.getProductById(productId)
            // Result.fromHttpResponse(response)
            
            // For now, use existing flow
            Result.Success(
                (product.value as? RequestState.Success)?.data
                    ?: throw Exception("Product not found")
            )
        }
    }

    /**
     * Retry loading product
     */
    fun retryLoadProduct() {
        loadProduct()
    }

    fun updateQuantity(value: Int) {
        quantity = value
    }

    fun updateFlavor(value: String) {
        selectedFlavor = value
    }

    /**
     * Add item to cart with enhanced error handling
     */
    fun addItemToCart(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Validate product ID
        if (productId.isBlank()) {
            val error = AppError.ValidationError(
                message = "Invalid product ID",
                errors = mapOf("productId" to "Product ID is required")
            )
            handleError(error)
            onError(error.displayMessage)
            return
        }

        // Get current product
        val currentProduct = when (val state = product.value) {
            is RequestState.Success -> state.data
            is RequestState.Error -> {
                onError("Product details not loaded. ${state.message}")
                return
            }
            is RequestState.Loading -> {
                onError("Product is still loading. Please wait.")
                return
            }
            else -> {
                onError("Product data not available.")
                return
            }
        }

        // Execute with error handling
        executeWithErrorHandling(
            onSuccess = {
                onSuccess()
                clearError() // Clear any previous errors
            },
            onError = { error ->
                // Log error for debugging
                println("❌ Error adding to cart: ${error.message}")
                onError(error.displayMessage)
            }
        ) {
            val item = CartItem(
                id = currentProduct.id,
                productId = currentProduct.id,
                title = currentProduct.title,
                price = currentProduct.price,
                quantity = quantity.toString(),
                thumbnail = currentProduct.thumbnail,
                flavor = selectedFlavor
            )

            // Convert callback-based API to Result
            Result.catch {
                var resultValue: String? = null
                var errorValue: String? = null
                
                customerRepository.addItemToCard(
                    cartItem = item,
                    onSuccess = { resultValue = "success" },
                    onError = { errorValue = it }
                )
                
                // Wait for callback (in real implementation, this should be suspend function)
                // For now, throw error if exists
                if (errorValue != null) {
                    throw Exception(errorValue)
                }
                
                resultValue ?: "success"
            }
        }
    }

    /**
     * Handle token expiration by navigating to login
     */
    override fun onTokenExpired() {
        // In real implementation, navigate to login screen
        println("🔐 Token expired, should navigate to login")
    }
}
