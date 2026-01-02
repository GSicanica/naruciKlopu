package com.appbosna.data.remote

import TokenStore
import com.appbosna.data.domain.AdminRepository
import com.appbosna.data.domain.CategoryRepository
import com.appbosna.data.domain.CustomerRepository
import com.appbosna.data.domain.OrderRepository
import com.appbosna.data.domain.ProductRepository
import com.appbosna.data.domain.ServerCategoryRepositoryImpl
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

object RepositoryProvider {

    data class Deps(
        val tokenStore: TokenStore,
        val engine: HttpClientEngine = CIO.create(),
        val fileBytesProvider: FileBytesProvider,
    )

    private fun createApi(deps: Deps): ApiService {
        val httpClient = HttpClientProvider(
            tokenStore = deps.tokenStore,
            engine = deps.engine
        ).client

        return ApiService(
            client = httpClient,
            apiKeyProvider = { deps.tokenStore.getToken() ?: "" }
        )
    }

    private fun createUnifiedApi(deps: Deps): UnifiedApiService {
        val restApi = createApi(deps)
        val httpClient = HttpClientProvider(
            tokenStore = deps.tokenStore,
            engine = deps.engine
        ).client
        val graphqlClient = com.appbosna.data.remote.graphql.GraphQLClient(
            client = httpClient,
            baseUrl = ApiConfig.baseUrl.replace("/api", "")
        )
        val graphqlRepo = com.appbosna.data.remote.graphql.GraphQLRepository(graphqlClient)
        
        return UnifiedApiService(
            rest = restApi,
            graphql = graphqlRepo
        )
    }

    fun admin(deps: Deps): AdminRepository {
        val api = createUnifiedApi(deps)
        return ServerAdminRepositoryImpl(
            api = api,
            tokenProvider = { deps.tokenStore.getToken().orEmpty() }
        )
    }

    fun products(deps: Deps): ProductRepository {
        val api = createUnifiedApi(deps)
        return ServerProductRepositoryImpl(
            api = api
        )
    }

    fun customers(deps: Deps): CustomerRepository {
        val api = createUnifiedApi(deps)
        return ServerCustomerRepositoryImpl(
            api = api,
            tokenStore = deps.tokenStore
        )
    }

    fun orders(deps: Deps): OrderRepository {
        val api = createUnifiedApi(deps)
        return ServerOrderRepositoryImpl(
            api = api,
            tokenProvider = { deps.tokenStore.getToken().orEmpty() }
        )
    }

    fun categories(deps: Deps): CategoryRepository {
        val api = createUnifiedApi(deps)
        return ServerCategoryRepositoryImpl(
            api = api
        )
    }
}
