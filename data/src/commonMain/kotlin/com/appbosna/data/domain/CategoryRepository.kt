package com.appbosna.data.domain

import com.appbosna.data.remote.CategoryDto
import com.appbosna.data.remote.UnifiedApiService
import com.appbosna.shared.util.RequestState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository za kategorije proizvoda
 */
interface CategoryRepository {
    fun readAll(): Flow<RequestState<List<Category>>>
    fun refresh()
}

/**
 * Domain model za kategoriju
 */
data class Category(
    val id: String,
    val name: String
)

/**
 * Extension za konverziju iz DTO u domain model
 */
fun CategoryDto.toDomain() = Category(
    id = id,
    name = name
)

/**
 * Implementacija repository-a koja koristi Symfony API
 */
class ServerCategoryRepositoryImpl(
    private val api: UnifiedApiService
) : CategoryRepository {

    private var cachedCategories: List<Category>? = null

    override fun readAll(): Flow<RequestState<List<Category>>> = flow {
        emit(RequestState.Loading)

        try {
            // Koristi cache ako postoji
            cachedCategories?.let {
                emit(RequestState.Success(it))
                return@flow
            }

            // Inače dohvati sa API-ja
            val categories = api.getCategories().map { it.toDomain() }
            cachedCategories = categories
            emit(RequestState.Success(categories))
        } catch (e: Exception) {
            emit(RequestState.Error("e"))
        }
    }

    override fun refresh() {
        cachedCategories = null
    }
}
