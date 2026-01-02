// shared/src/commonMain/kotlin/com/appbosna/di/Koin.kt

package com.appbosna.di

import TokenStore
import com.appbosna.admin_panel.AdminDashboardViewModel
import com.appbosna.admin_panel.AdminPanelViewModel
import com.appbosna.auth.AuthViewModel
import com.appbosna.cart.CartViewModel
import com.appbosna.categories.CategoriesViewModel
import com.appbosna.category_search.CategorySearchViewModel
import com.appbosna.checkout.CheckoutViewModel
import com.appbosna.checkout.domain.PaypalApi
import com.appbosna.data.LocationRepositoryImpl
import com.appbosna.data.domain.AdminRepository
import com.appbosna.data.domain.CustomerRepository
import com.appbosna.data.domain.LocationRepository
import com.appbosna.data.domain.OrderRepository
import com.appbosna.data.domain.ProductRepository
import com.appbosna.data.domain.RestorauntRepository
import com.appbosna.data.remote.ApiService
import com.appbosna.data.remote.AppPreferences
import com.appbosna.data.remote.AuthRepository
import com.appbosna.data.remote.FileBytesProvider
import com.appbosna.data.remote.RepositoryProvider
import com.appbosna.data.remote.ServerAdminRepositoryImpl
import com.appbosna.data.remote.ServerRestaurantRepositryImpl
import com.appbosna.data.usecase.GetDashboardAnalyticsUseCase
import com.appbosna.data.usecase.GetUserStatisticsUseCase
import com.appbosna.details.DetailsViewModel
import com.appbosna.home.HomeGraphViewModel
import com.appbosna.locations.LocationsViewModel
import com.appbosna.manage_product.ManageProductViewModel
import com.appbosna.payment_completed.PaymentViewModel
import com.appbosna.products_overview.ProductsOverviewViewModel
import com.appbosna.profile.ProfileViewModel
import com.appbosna.shared.util.IntentHandler
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModelOf

val sharedModule = module {

    /* ---------- Infra ---------- */

    single<FileBytesProvider> {
        object : FileBytesProvider {
            override suspend fun fileName(path: String) = path.substringAfterLast('/')
            override suspend fun bytes(path: String) = ByteArray(0)
        }
    }

    // Ktor konfiguracija
    single<AppPreferences> { AppPreferences() }

    single<HttpClient> {
        HttpClient(CIO) {
            expectSuccess = false

            install(ContentNegotiation) {
                json(
                    kotlinx.serialization.json.Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true
                        encodeDefaults = true
                    }
                )
            }

            defaultRequest {
                header(HttpHeaders.UserAgent, "Mozilla/5.0 (Android)")
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
        }
    }



    single {
        ApiService(get(), apiKeyProvider =
            { get<TokenStore>().getToken() ?: "" })
    }

    single {
        com.appbosna.data.remote.UnifiedApiService(
            rest = get(),
            graphql = get()
        )
    }

    // Use UnifiedApiService for AuthRepository
    single { AuthRepository(get<com.appbosna.data.remote.UnifiedApiService>(), get()) }

    /* ---------- GraphQL ---------- */

    single {
        com.appbosna.data.remote.HttpClientProvider(
            tokenStore = get(),
            engine = io.ktor.client.engine.cio.CIO.create()
        )
    }

    single {
        com.appbosna.data.remote.graphql.GraphQLClient(
            client = get<com.appbosna.data.remote.HttpClientProvider>().client,
            baseUrl = com.appbosna.data.remote.ApiConfig.baseUrl.replace("/api", "") // Remove /api suffix for GraphQL
        )
    }

    single {
        com.appbosna.data.remote.graphql.GraphQLRepository(
            client = get()
        )
    }

    /* ---------- Repozitoriji (SERVER only) ---------- */

    // Customer Repo (PHP)
    single<CustomerRepository> {
        RepositoryProvider.customers(
            RepositoryProvider.Deps(
                tokenStore = get(),
                engine = CIO.create(),
                fileBytesProvider = get()
            )
        )
    }

    // Product Repo (PHP)
    single<ProductRepository> {
        RepositoryProvider.products(
            RepositoryProvider.Deps(
                tokenStore = get(),
                engine = CIO.create(),
                fileBytesProvider = get()
            )
        )
    }

    // Order Repo (PHP)
    single<OrderRepository> {
        RepositoryProvider.orders(
            RepositoryProvider.Deps(
                tokenStore = get(),
                engine = CIO.create(),
                fileBytesProvider = get()
            )
        )
    }


    // ✔️ Admin repo — samo ServerAdminRepositoryImpl
    single<AdminRepository> {
        ServerAdminRepositoryImpl(
            api = get(),
            tokenProvider = { get<TokenStore>().getToken() ?: "" }

        )
    }

    single<RestorauntRepository> {
        ServerRestaurantRepositryImpl(
            api = get()
        )
    }

    /* ---------- UseCase ---------- */

    single { GetDashboardAnalyticsUseCase(get()) }
    single { GetUserStatisticsUseCase(get()) }

    // GraphQL-only use case (legacy)
    single { com.appbosna.data.usecase.PlaceOrderWithGraphQLUseCase(get()) }

    // Unified use case - supports both REST and GraphQL
    single {
        com.appbosna.data.usecase.PlaceOrderUseCase(
            restApi = get(),
            graphqlRepo = get(),
            tokenProvider = { get<TokenStore>().getToken() ?: "" }
        )
    }


    /* ---------- Ostalo ---------- */

    single { IntentHandler() }
    single { PaypalApi() }
    //single<LocationRepository> { LocationRepositoryImpl() }

    /* ---------- ViewModeli ---------- */

    viewModelOf(::AuthViewModel)
    viewModelOf(::HomeGraphViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::ManageProductViewModel)
    viewModelOf(::AdminPanelViewModel)
    viewModelOf(::ProductsOverviewViewModel)
    viewModel { (productId: String) ->
        DetailsViewModel(
            productRepository = get(),
            customerRepository = get(),
            productId = productId
        )
    }
    viewModelOf(::CartViewModel)
    viewModel { (restaurantId: Int) ->
        CategorySearchViewModel(
            productRepository = get(),
            restaurantId = restaurantId
        )
    }
    viewModel { (totalAmount: Double) ->
        CheckoutViewModel(
            customerRepository = get(),
            orderRepository = get(),
            savedStateHandle = get(),
            paypalApi = get(),
            totalAmount = totalAmount
        )
    }
    viewModel { (isSuccess: Boolean?, error: String?, token: String?) ->
        PaymentViewModel(
            isSuccess = isSuccess,
            error = error,
            token = token,
            customerRepository = get(),
            orderRepository = get(),
            productRepository = get()
        )
    }
    viewModelOf(::AdminDashboardViewModel)
    viewModelOf(::CategoriesViewModel)
    viewModelOf(::LocationsViewModel)
}

/* ------------ Platform-specific module -------------- */
expect val targetModule: org.koin.core.module.Module

fun initializeKoin(config: (KoinApplication.() -> Unit)? = null) {
    startKoin {
        config?.invoke(this)
        modules(sharedModule, targetModule)
    }
}
