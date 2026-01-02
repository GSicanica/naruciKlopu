package com.appbosna.shared.domain

import kotlin.uuid.ExperimentalUuidApi
import kotlinx.serialization.Serializable

@Serializable
@OptIn(ExperimentalUuidApi::class)
data class CartItem(
    val id: String,
    val productId: String,
    val title: String,
    val price: String,
    val quantity: String,
    val thumbnail: String?,
    val flavor: String? = null
)

