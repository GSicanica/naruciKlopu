package com.appbosna.shared.domain

data class OrderItem(
    val id: String? = null,
    val productId: String,
    val title: String,
    val price: String,
    val quantity: String,
    val thumbnail: String? = null
)
