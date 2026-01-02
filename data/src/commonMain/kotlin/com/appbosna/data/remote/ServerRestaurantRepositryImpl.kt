package com.appbosna.data.remote

import com.appbosna.data.domain.RestorauntRepository
import com.appbosna.shared.domain.Restaurant

class ServerRestaurantRepositryImpl(
    private val api: UnifiedApiService
) : RestorauntRepository {

    override suspend fun getRestaurants(): List<Restaurant> {
        return try {
            // /restaurants je public – token ti realno ne treba
            println("restaurants response = ${api.getRestaurants()}")
            api.getRestaurants().map { it.toDomain() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
