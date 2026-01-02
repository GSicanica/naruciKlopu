package com.appbosna.products_overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appbosna.data.domain.ProductRepository
import com.appbosna.shared.util.RequestState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ProductsOverviewViewModel(
    private val productRepository: ProductRepository,
) : ViewModel() {
    val products = combine(
        productRepository.readNewProducts(),
        productRepository.readDiscountedProducts()
    ) { new, discounted ->
        when {
            new is RequestState.Success && discounted is RequestState.Success ->
                RequestState.Success(new.data + discounted.data)
            new is RequestState.Error -> new
            discounted is RequestState.Error -> discounted
            else -> RequestState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RequestState.Loading)

    fun refresh() = productRepository.refresh()
}