package com.appbosna.payment_completed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appbosna.data.domain.CustomerRepository
import com.appbosna.data.domain.OrderRepository
import com.appbosna.data.domain.ProductRepository
import com.appbosna.data.usecase.toIntSafe
import com.appbosna.shared.domain.CartItem
import com.appbosna.shared.domain.Customer
import com.appbosna.shared.domain.Order
import com.appbosna.shared.domain.Product
import com.appbosna.shared.util.RequestState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val isSuccess: Boolean?,
    private val error: String?,
    private val token: String?,
    private val customerRepository: CustomerRepository,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {

    var screenState: RequestState<Unit> by mutableStateOf(RequestState.Loading)
        private set

    // ------------------------------------------------------------
    // CUSTOMER
    // ------------------------------------------------------------

    private val customer: StateFlow<RequestState<Customer>> =
        customerRepository.readCustomerFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RequestState.Loading
            )

    // ------------------------------------------------------------
    // TOTAL AMOUNT
    // ------------------------------------------------------------

    @OptIn(ExperimentalCoroutinesApi::class)
    private val totalAmount: StateFlow<RequestState<Double>> =
        customer.flatMapLatest { customerState ->
            when {
                customerState.isSuccess() -> {
                    val cartItems = customerState.getSuccessData().cart
                    val productIds = cartItems.map { it.productId }

                    if (productIds.isEmpty()) {
                        flowOf(RequestState.Success(0.0))
                    } else {
                        productRepository.readProductsByIdsFlow(productIds)
                            .map { productsState ->
                                when {
                                    productsState.isSuccess() -> {
                                        val total = calculateTotalPrice(
                                            cartItems = cartItems,
                                            products = productsState.getSuccessData()
                                        )
                                        RequestState.Success(total)
                                    }
                                    productsState.isError() -> {
                                        RequestState.Error(productsState.getErrorMessage())
                                    }
                                    else -> RequestState.Loading
                                }
                            }
                    }
                }

                customerState.isError() -> {
                    flowOf(RequestState.Error(customerState.getErrorMessage()))
                }

                else -> {
                    flowOf(RequestState.Loading)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RequestState.Loading
        )

    // ------------------------------------------------------------
    // INIT – reakcija na totalAmount + payment rezultat
    // ------------------------------------------------------------

    init {
        viewModelScope.launch {
            totalAmount.collectLatest { amountState ->
                when {
                    amountState.isSuccess() -> {
                        handlePaymentResult(amountState.getSuccessData())
                    }

                    amountState.isError() -> {
                        screenState = RequestState.Error(amountState.getErrorMessage())
                    }

                    else -> {
                        screenState = RequestState.Loading
                    }
                }
            }
        }
    }

    private fun handlePaymentResult(total: Double) {
        when {
            // ✅ Success case - order already created for "Pay on Delivery"
            // Only need to create order if we have a PayPal token
            isSuccess == true -> {
                screenState = RequestState.Success(Unit)

                // Clear the cart after successful order
                clearCart()

                // Only create order if we have a PayPal token
                // For "Pay on Delivery", order is already created in CheckoutViewModel
                if (token != null) {
                    createTheOrder(
                        totalAmount = total,
                        token = token,
                        onError = { message ->
                            screenState = RequestState.Error(message)
                        }
                    )
                }
            }

            // Error case - show error message
            error != null -> {
                screenState = RequestState.Error(error)
            }

            // Should never reach here if navigation is correct
            else -> {
                screenState =
                    RequestState.Error("Unknown error. Contact us at: example@gmail.com")
            }
        }
    }

    // ------------------------------------------------------------
    // CLEAR CART
    // ------------------------------------------------------------

    private fun clearCart() {
        viewModelScope.launch(Dispatchers.IO) {
            customerRepository.deleteAllCartItems(
                onSuccess = {
                    println("Cart cleared successfully after order creation")
                },
                onError = { message ->
                    println("Failed to clear cart: $message")
                    // Don't show error to user, cart clearing is not critical
                }
            )
        }
    }

    // ------------------------------------------------------------
    // CREATE ORDER
    // ------------------------------------------------------------

    private fun createTheOrder(
        totalAmount: Double,
        token: String,
        onError: (String) -> Unit,
    ) {
        val customerState = customer.value

        if (customerState.isSuccess()) {
            val customerData = customerState.getSuccessData()

            val order = Order(
                customerId = customerData.id,
                items = customerData.cart,
                totalAmount = totalAmount,
                token = token
            )

            viewModelScope.launch(Dispatchers.IO) {
                orderRepository.createTheOrder(
                    order = order,
                    onSuccess = { println("ORDER SUCCESSFULLY CREATED!") },
                    onError = onError
                )
            }
        } else if (customerState.isError()) {
            onError(customerState.getErrorMessage())
        }
    }

    // ------------------------------------------------------------
    // UTILS
    // ------------------------------------------------------------

    private fun calculateTotalPrice(
        cartItems: List<CartItem>,
        products: List<Product>
    ): Double {
        return cartItems.sumOf { cartItem ->
            val product = products.find { it.id == cartItem.productId }
            product
                ?.price
                ?.toDoubleOrNull()
                ?.times(cartItem.quantity.toIntSafe())
                ?: 0.0
        }
    }
}
