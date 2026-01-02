package com.appbosna.data.domain

import com.appbosna.shared.domain.Order

interface OrderRepository {
    fun getCurrentUserId(): String?
    suspend fun createTheOrder(
        order: Order,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}