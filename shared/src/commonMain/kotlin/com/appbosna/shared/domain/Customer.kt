package com.appbosna.shared.domain

import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val createdAt: String = "",
    val city: String? = null,
    val postalCode: Int? = null,
    val address: String? = null,
    val phoneNumber: String? = null,
    val cart: List<CartItem> = emptyList(),
    val isAdmin: String = ""
)

@Serializable
data class PhoneNumber(
    val dialCode: Int,
    val number: String
)


