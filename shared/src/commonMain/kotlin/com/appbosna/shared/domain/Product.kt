package com.appbosna.shared.domain

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val createdAt: String = "",
    val title: String,
    val description: String,
    val thumbnail: String,
    val restaurantId: Int,
    val restaurantName: String = "",
    val categoryName: String? = null,
    val categoryId: Int? = null,
    val flavors: String = "",
    val ingredients: List<String> = emptyList(),
    val weight: String? = null,
    val price: String,
    val isPopular: Boolean = false,
    val isDiscounted: Boolean = false,
    val isNew: Boolean = false,
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

enum class OpenStatus { OPEN, CLOSING_SOON, CLOSED }

data class Restaurant(
    val id: Int,
    val name: String,
    val color: String?,
    val image: String?,
    val active: Boolean,
    val sortOrder: Int = 0,
    val openTime: String? = null,
    val closeTime: String? = null,
    val isOpen: Boolean = false,
    val openStatus: OpenStatus = OpenStatus.CLOSED
)


