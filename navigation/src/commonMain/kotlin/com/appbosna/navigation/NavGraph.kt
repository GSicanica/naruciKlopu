package com.appbosna.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.appbosna.admin_panel.AdminDashboardScreen
import com.appbosna.admin_panel.AdminPanelScreen
import com.appbosna.auth.AuthScreen
import com.appbosna.category_search.CategorySearchScreen
import com.appbosna.checkout.CheckoutScreen
import com.appbosna.details.DetailsScreen
import com.appbosna.home.HomeGraphScreen
import com.appbosna.locations.LocationsScreen
import com.appbosna.manage_product.ManageProductScreen
import com.appbosna.payment_completed.PaymentCompleted
import com.appbosna.profile.ProfileScreen
import com.appbosna.shared.navigation.Screen

/**
 * State-based navigation bez externa biblioteka
 */
@Composable
fun SetupNavGraph(
    modifier: Modifier = Modifier,
    startDestination: Screen = Screen.Auth
) {
    // Navigation Stack - čuva sve otvorene screening
    val backStack = remember { 
        println("🗺️ NavGraph: Initializing backStack with startDestination = $startDestination")
        mutableStateOf<List<Screen>>(listOf(startDestination))
    }
    val currentScreen = remember { 
        println("🗺️ NavGraph: Initializing currentScreen = $startDestination")
        mutableStateOf(startDestination) 
    }

    fun navigate(screen: Screen, clearStack: Boolean = false, singleTop: Boolean = false) {
        println("🗺️ NavGraph.navigate: screen=$screen, clearStack=$clearStack, singleTop=$singleTop")
        if (singleTop && currentScreen.value == screen) return

        val newStack = if (clearStack) {
            listOf(screen)
        } else {
            backStack.value + screen
        }
        backStack.value = newStack
        currentScreen.value = screen
        println("🗺️ NavGraph: Stack updated. Current = ${currentScreen.value}, Stack size = ${backStack.value.size}")
    }

    fun navigateUp() {
        if (backStack.value.size > 1) {
            val newStack = backStack.value.dropLast(1)
            backStack.value = newStack
            currentScreen.value = newStack.last()
        }
    }

    fun popUpTo(screen: Screen, inclusive: Boolean = false) {
        val index = backStack.value.indexOfLast { it::class == screen::class }
        if (index >= 0) {
            val newStack = if (inclusive) {
                backStack.value.take(index)
            } else {
                backStack.value.take(index + 1)
            }
            if (newStack.isNotEmpty()) {
                backStack.value = newStack
                currentScreen.value = newStack.last()
            }
        }
    }

    // Presretni hardware back button na Androidu
    // UVIJEK presreći back button - nikada ne dozvoli da se app zatvori
    // Ako smo na početnom ekranu, samo ostanemo tamo (ne radi ništa)
    BackHandler(enabled = true) {
        if (backStack.value.size > 1) {
            navigateUp()
        }
        // Ako je size == 1, ne radi ništa - ostajemo na početnom ekranu
    }

    // Prikaži trenutni ekran na osnovu stanja
    when (val screen = currentScreen.value) {
        is Screen.Auth -> {
            AuthScreen(
                navigateToHome = {
                    navigate(Screen.HomeGraph, clearStack = true)
                }
            )
        }

        is Screen.HomeGraph -> {
            HomeGraphScreen(
                navigateToAuth = {
                    navigate(Screen.Auth, clearStack = true)
                },
                navigateToProfile = {
                    navigate(Screen.Profile, singleTop = true)
                },
                navigateToAdminPanel = {
                    navigate(Screen.AdminPanel, singleTop = true)
                },
                navigateToDetails = { productId ->
                    println("🚀 Navigating to Details with productId: $productId")
                    navigate(Screen.Details(id = productId))
                },
                navigateToCategorySearch = { restaurantId ->
                    navigate(Screen.RestoranSearch(restaurantId), singleTop = true)
                },
                navigateToCheckout = { totalAmount ->
                    navigate(Screen.Checkout(totalAmount), singleTop = true)
                },
                navigateToLocations = {
                    navigate(Screen.Locations)
                }
            )
        }

        is Screen.Profile -> {
            ProfileScreen(navigateBack = ::navigateUp)
        }

        is Screen.AdminPanel -> {
            AdminPanelScreen(
                navigateBack = ::navigateUp,
                navigateToManageProduct = { id ->
                    navigate(Screen.ManageProduct(id = id), singleTop = true)
                },
                navigateToStatistics = {
                    navigate(Screen.AdminDashboard)
                }
            )
        }

        is Screen.ManageProduct -> {
            ManageProductScreen(
                id = screen.id,
                navigateBack = ::navigateUp
            )
        }

        is Screen.AdminDashboard -> {
            AdminDashboardScreen(
                navigateBack = ::navigateUp
            )
        }

        is Screen.Details -> {
            DetailsScreen(
                id = screen.id,
                navigateBack = ::navigateUp
            )
        }

        is Screen.RestoranSearch -> {
            CategorySearchScreen(
                restaurantId = screen.restaurantId,
                navigateToDetails = { id ->
                    navigate(Screen.Details(id))
                },
                navigateBack = ::navigateUp
            )
        }

        is Screen.Checkout -> {
            CheckoutScreen(
                totalAmount = screen.totalAmount.toDoubleOrNull() ?: 0.0,
                navigateBack = ::navigateUp,
                navigateToPaymentCompleted = { isSuccess, error ->
                    navigate(Screen.PaymentCompleted(isSuccess, error), singleTop = true)
                }
            )
        }

        is Screen.Locations -> {
            LocationsScreen(
                navigateBack = ::navigateUp,
                navigateToAddLocation = {
                    navigate(Screen.AddEditLocation())
                },
                navigateToEditLocation = { locationId ->
                    navigate(Screen.AddEditLocation(locationId))
                }
            )
        }

        is Screen.PaymentCompleted -> {
            PaymentCompleted(
                isSuccess = screen.isSuccess,
                error = screen.error,
                token = screen.token,
                navigateBack = {
                    navigate(Screen.HomeGraph, clearStack = true)
                }
            )
        }

        else -> {
            AuthScreen(navigateToHome = {
                navigate(Screen.HomeGraph, clearStack = true)
            })
        }
    }
}
