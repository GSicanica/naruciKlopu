package com.appbosna.shared.domain

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.serialization.Serializable

@OptIn(ExperimentalTime::class)
@Serializable
data class Review(
    val id: String,
    val productId: String,
    val userId: String,
    val username: String,
    val rating: Float,
    val comment: String,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val isVerifiedPurchase: Boolean = false,
    val isApproved: Boolean = true
)