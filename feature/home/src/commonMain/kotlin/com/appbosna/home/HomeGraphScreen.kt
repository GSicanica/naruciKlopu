package com.appbosna.home

import ContentWithMessageBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
// State-based navigation - bez androidx.navigation
import com.appbosna.cart.CartScreen
import com.appbosna.categories.CategoriesScreen
import com.appbosna.home.component.BottomBar
import com.appbosna.home.component.CustomDrawer
import com.appbosna.home.domain.BottomBarDestination
import com.appbosna.home.domain.CustomDrawerState
import com.appbosna.home.domain.isOpened
import com.appbosna.home.domain.opposite
import com.appbosna.products_overview.ProductsOverviewScreen
import com.appbosna.shared.fonts.Alpha
import com.appbosna.shared.fonts.BebasNeueFont
import com.appbosna.shared.fonts.FontSize
import com.appbosna.shared.fonts.IconPrimary
import com.appbosna.shared.fonts.Resources
import com.appbosna.shared.fonts.Surface
import com.appbosna.shared.fonts.SurfaceBrand
import com.appbosna.shared.fonts.SurfaceError
import com.appbosna.shared.fonts.SurfaceLighter
import com.appbosna.shared.fonts.TextPrimary
import com.appbosna.shared.fonts.TextWhite
import com.appbosna.shared.navigation.Screen
import com.appbosna.shared.util.RequestState
import com.appbosna.shared.util.getScreenWidth
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeGraphScreen(
    navigateToAuth: () -> Unit,
    navigateToProfile: () -> Unit,
    navigateToAdminPanel: () -> Unit,
    navigateToDetails: (String) -> Unit,
    navigateToCategorySearch: (Int) -> Unit,
    navigateToCheckout: (String) -> Unit,
    navigateToLocations: () -> Unit,
) {
    // State-based navigation umjesto NavHost
    var currentTab by remember { mutableStateOf<Screen>(Screen.ProductsOverview) }
    
    val selectedDestination = when (currentTab) {
        is Screen.ProductsOverview -> BottomBarDestination.ProductsOverview
        is Screen.Cart -> BottomBarDestination.Cart
        is Screen.Categories -> BottomBarDestination.Categories
        else -> BottomBarDestination.ProductsOverview
    }

    val screenWidth = remember { getScreenWidth() }
    val offsetValue = (screenWidth / 1.5).dp

    var drawerState by remember { mutableStateOf(CustomDrawerState.Closed) }
    val isDrawerOpened = drawerState.isOpened()

    // Sve bez animacija – samo snap vrijednosti
    val offset = if (isDrawerOpened) offsetValue else 0.dp
    val background = SurfaceLighter
    val scale = if (isDrawerOpened) 0.9f else 1f
    val radius = if (isDrawerOpened) 20.dp else 0.dp

    val viewModel = koinViewModel<HomeGraphViewModel>()
    val customer by viewModel.customer.collectAsState()
    val totalAmount by viewModel.totalAmountFlow.collectAsState(RequestState.Loading)
    val messageBarState = rememberMessageBarState()

    // Track if we've already checked authentication to avoid immediate navigation on old error state
    var hasCheckedAuth by remember { mutableStateOf(false) }

    // Only navigate to auth if specifically not logged in (not for network errors)
    androidx.compose.runtime.LaunchedEffect(customer) {
        // Wait for first real check to complete (skip old cached error states)
        if (customer is RequestState.Loading) {
            hasCheckedAuth = false
            return@LaunchedEffect
        }
        
        if (!hasCheckedAuth) {
            hasCheckedAuth = true
            // Give the flow time to refresh after navigation
            kotlinx.coroutines.delay(200)
        }
        
        if (customer is RequestState.Error) {
            val errorMessage = (customer as RequestState.Error).message
            // Only navigate to auth if the error is about authentication/token, not network issues
            if (errorMessage.contains("Niste prijavljeni", ignoreCase = true)) {
                println("🔐 HomeGraphScreen: User not authenticated, navigating to Auth")
                navigateToAuth()
            } else {
                // Network or other errors - just show error message, don't logout
                println("⚠️ HomeGraphScreen: Customer error (not auth): $errorMessage")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .systemBarsPadding()
    ) {
        CustomDrawer(
            customer = customer,
            onProfileClick = navigateToProfile,
            onContactUsClick = {},
            onSignOutClick = {
                viewModel.signOut(
                    onSuccess = navigateToAuth,
                    onError = { message -> messageBarState.addError(message) }
                )
            },
            onAdminPanelClick = navigateToAdminPanel,
            onLocationsClick = navigateToLocations
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(radius))
                .offset(x = offset)
                .scale(scale)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(radius),
                    ambientColor = Color.Black.copy(alpha = Alpha.DISABLED),
                    spotColor = Color.Black.copy(alpha = Alpha.DISABLED)
                )
        ) {
            Scaffold(
                containerColor = Surface,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = selectedDestination.title,
                                fontFamily = BebasNeueFont(),
                                fontSize = FontSize.LARGE,
                                color = TextPrimary
                            )
                        },
                        actions = {
                            val cartItems = (customer as? RequestState.Success)?.data?.cart.orEmpty()
                            val showCheckout = selectedDestination == BottomBarDestination.Cart && cartItems.isNotEmpty()

                            if (showCheckout) {
                                IconButton(
                                    onClick = {
                                        val total = (totalAmount as? RequestState.Success)?.data
                                        if (total != null) {
                                            navigateToCheckout(total.toString())
                                        } else {
                                            messageBarState.addError("Ne mogu izračunati total.")
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(Resources.Icon.RightArrow),
                                        contentDescription = "Right icon",
                                        tint = IconPrimary
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { drawerState = drawerState.opposite() }) {
                                Icon(
                                    painter = painterResource(
                                        if (isDrawerOpened) Resources.Icon.Close else Resources.Icon.Menu
                                    ),
                                    contentDescription = if (isDrawerOpened) "Close icon" else "Menu icon",
                                    tint = IconPrimary
                                )
                            }
                        },
                        colors = topAppBarColors(
                            containerColor = Surface,
                            scrolledContainerColor = Surface,
                            navigationIconContentColor = IconPrimary,
                            titleContentColor = TextPrimary,
                            actionIconContentColor = IconPrimary
                        )
                    )
                }
            ) { padding ->
                ContentWithMessageBar(
                    contentBackgroundColor = Surface,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding()
                        ),
                    messageBarState = messageBarState,
                    errorMaxLines = 2,
                    errorContainerColor = SurfaceError,
                    errorContentColor = TextWhite,
                    successContainerColor = SurfaceBrand,
                    successContentColor = TextPrimary
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Prikaži trenutni tab na osnovu state-a
                        Box(modifier = Modifier.weight(1f)) {
                            when (currentTab) {
                                is Screen.ProductsOverview -> {
                                    ProductsOverviewScreen(
                                        navigateToDetails = navigateToDetails
                                    )
                                }
                                is Screen.Cart -> {
                                    CartScreen()
                                }
                                is Screen.Categories -> {
                                    CategoriesScreen(
                                        navigateToCategorySearch = navigateToCategorySearch
                                    )
                                }
                                else -> {
                                    ProductsOverviewScreen(
                                        navigateToDetails = navigateToDetails
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            BottomBar(
                                customer = customer,
                                selected = selectedDestination,
                                onSelect = { destination ->
                                    currentTab = destination.screen
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
