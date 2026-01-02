package com.appbosna.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appbosna.data.domain.CustomerRepository
import com.appbosna.data.remote.ApiUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class AuthViewModel(
    private val customerRepository: CustomerRepository,
) : ViewModel() {
    fun createCustomer(
        user: ApiUser?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
       viewModelScope.launch(Dispatchers.IO) {
           customerRepository.createCustomer(
               user = user,
               onSuccess = onSuccess,
               onError = onError
           )
       }
    }
}