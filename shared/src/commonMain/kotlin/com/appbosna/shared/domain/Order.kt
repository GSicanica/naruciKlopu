package com.appbosna.shared.domain

import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@Serializable
data class Order(
    val orderId: String = Uuid.random().toHexString(),
    val customerId: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val createdAt: String = "",
    val token: String? = null,
    val currency: String = "usd",
    val paymentIntentId: String? = null,
    val status: String = "PENDING",
    val shippingAddress: String = ""
)