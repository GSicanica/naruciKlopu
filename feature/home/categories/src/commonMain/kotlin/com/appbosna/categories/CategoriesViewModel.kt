package com.appbosna.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appbosna.data.domain.RestorauntRepository
import com.appbosna.shared.domain.Restaurant
import com.appbosna.shared.util.RequestState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val adminRepository: RestorauntRepository
) : ViewModel() {

    private val _restaurants = MutableStateFlow<RequestState<List<Restaurant>>>(RequestState.Loading)
    val restaurants: StateFlow<RequestState<List<Restaurant>>> = _restaurants

    init {
        viewModelScope.launch {
            try {
                val list = adminRepository.getRestaurants()
                _restaurants.value = RequestState.Success(list)
            } catch (e: Exception) {
                _restaurants.value = RequestState.Error("Unable to fetch restaurants: ${e.message}")
            }
        }
    }
}
