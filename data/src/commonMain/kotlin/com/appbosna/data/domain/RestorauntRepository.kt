package com.appbosna.data.domain

import com.appbosna.shared.domain.Restaurant

interface RestorauntRepository {
    suspend fun getRestaurants(): List<Restaurant>
}