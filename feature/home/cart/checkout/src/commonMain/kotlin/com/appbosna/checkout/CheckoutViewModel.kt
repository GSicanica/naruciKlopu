package com.appbosna.checkout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appbosna.checkout.domain.Amount
import com.appbosna.checkout.domain.PaypalApi
import com.appbosna.checkout.domain.ShippingAddress
import com.appbosna.data.domain.CustomerRepository
import com.appbosna.data.domain.OrderRepository
import com.appbosna.shared.domain.CartItem
import com.appbosna.shared.domain.Country
import com.appbosna.shared.domain.Customer
import com.appbosna.shared.domain.Order
import com.appbosna.shared.domain.PhoneNumber
import com.appbosna.shared.util.RequestState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class CheckoutScreenState(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val city: String? = null,
    val postalCode: Int? = null,
    val address: String? = null,
    val country: Country = Country.Bosnia,
    val phoneNumber: String? = "",
    val cart: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
)

class CheckoutViewModel(
    private val customerRepository: CustomerRepository,
    private val orderRepository: OrderRepository,
    private val savedStateHandle: SavedStateHandle,
    private val paypalApi: PaypalApi,
    totalAmount: Double,
) : ViewModel() {
    var screenReady: RequestState<Unit> by mutableStateOf(RequestState.Loading)
    var screenState: CheckoutScreenState by mutableStateOf(CheckoutScreenState())
        private set

    val isFormValid: Boolean
        get() = with(screenState) {
            firstName.length in 3..50 &&
                    lastName.length in 3..50 &&
                    !city.isNullOrBlank() && city.length in 3..50 &&
                    postalCode != null && postalCode.toString().length in 3..8 &&
                    !address.isNullOrBlank() && address.length in 3..50 &&
                    !phoneNumber.isNullOrBlank() && phoneNumber.length in 5..30
        }

    init {
        viewModelScope.launch {
            paypalApi.fetchAccessToken(
                onSuccess = { token ->
                    println("TOKEN RECEIVED: $token")
                },
                onError = { message ->
                    println(message)
                }
            )
        }
        viewModelScope.launch {
            customerRepository.readCustomerFlow().collectLatest { data ->
                if (data.isSuccess()) {
                    val fetchedCustomer = data.getSuccessData()
                    screenState = CheckoutScreenState(
                        id = fetchedCustomer.id,
                        firstName = fetchedCustomer.firstName,
                        lastName = fetchedCustomer.lastName,
                        email = fetchedCustomer.email,
                        city = fetchedCustomer.city,
                        postalCode = fetchedCustomer.postalCode,
                        address = fetchedCustomer.address,
                        phoneNumber = fetchedCustomer.phoneNumber,
                        country =  Country.Bosnia,
                        cart = fetchedCustomer.cart,
                        totalAmount = totalAmount
                    )
                    screenReady = RequestState.Success(Unit)
                } else if (data.isError()) {
                    screenReady = RequestState.Error(data.getErrorMessage())
                }
            }
        }
    }

    fun updateFirstName(value: String) {
        screenState = screenState.copy(firstName = value)
        autoSaveCustomerData()
    }

    fun updateLastName(value: String) {
        screenState = screenState.copy(lastName = value)
        autoSaveCustomerData()
    }

    fun updateCity(value: String) {
        screenState = screenState.copy(city = value)
        autoSaveCustomerData()
    }

    fun updatePostalCode(value: Int?) {
        screenState = screenState.copy(postalCode = value)
        autoSaveCustomerData()
    }

    fun updateAddress(value: String) {
        screenState = screenState.copy(address = value)
        autoSaveCustomerData()
    }

/*    fun updateCountry(value: Country) {
        screenState = screenState.copy(
            country = value,
            phoneNumber = screenState.phoneNumber?.copy(
                dialCode = value.dialCode
            )
        )
    }*/

    fun updatePhoneNumber(value: String) {
        screenState = screenState.copy(
            phoneNumber = PhoneNumber(
                dialCode = screenState.country.dialCode,
                number = value
            ).number
        )
        autoSaveCustomerData()
    }

    /**
     * Auto-save customer data after each field change
     * This ensures data persists across app restarts and screen navigations
     */
    private fun autoSaveCustomerData() {
        // Only save if basic required fields are filled
        if (screenState.firstName.isNotBlank() && screenState.lastName.isNotBlank()) {
            viewModelScope.launch {
                try {
                    updateCustomerSilently()
                } catch (e: Exception) {
                    // Silent fail - don't show error to user for auto-save
                    println("Auto-save failed: ${e.message}")
                }
            }
        }
    }

    /**
     * Silent customer update (no callbacks)
     */
    private suspend fun updateCustomerSilently() {
        customerRepository.updateCustomer(
            customer = Customer(
                id = screenState.id,
                firstName = screenState.firstName,
                lastName = screenState.lastName,
                email = screenState.email,
                city = screenState.city,
                postalCode = screenState.postalCode,
                address = screenState.address,
                phoneNumber = screenState.phoneNumber
            ),
            onSuccess = { /* silent */ },
            onError = { /* silent */ }
        )
    }

    fun payOnDelivery(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        updateCustomer(
            onSuccess = {
                createTheOrder(
                    onSuccess = onSuccess,
                    onError = onError
                )
            },
            onError = onError
        )
    }

    private fun updateCustomer(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            customerRepository.updateCustomer(
                customer = Customer(
                    id = screenState.id,
                    firstName = screenState.firstName,
                    lastName = screenState.lastName,
                    email = screenState.email,
                    city = screenState.city,
                    postalCode = screenState.postalCode,
                    address = screenState.address,
                    phoneNumber = screenState.phoneNumber
                ),
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }

    private fun createTheOrder(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            orderRepository.createTheOrder(
                order = Order(
                    customerId = screenState.id,
                    items = screenState.cart,
                    totalAmount = screenState.totalAmount,
                ),
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }

    fun payWithPayPal(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (screenState.totalAmount > 0.0) {
            viewModelScope.launch {
                paypalApi.beginCheckout(
                    amount = Amount(
                        currencyCode = "USD",
                        value = screenState.totalAmount.toString()
                    ),
                    fullName = "${screenState.firstName} ${screenState.lastName}",
                    shippingAddress = ShippingAddress(
                        addressLine1 = screenState.address ?: "Unknown address",
                        city = screenState.city ?: "Unknown city",
                        state = screenState.country.name,
                        postalCode = screenState.postalCode.toString(),
                        countryCode = screenState.country.code
                    ),
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
        } else {
            onError("Total amount couldn't be calculated.")
        }
    }
}