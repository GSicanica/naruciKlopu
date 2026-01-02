package com.appbosna.home.domain

import com.appbosna.shared.fonts.Resources
import com.appbosna.shared.navigation.Screen
import org.jetbrains.compose.resources.DrawableResource

enum class BottomBarDestination(
    val icon: DrawableResource,
    val title: String,
    val screen: Screen
) {
    ProductsOverview(
        icon = Resources.Icon.Home,
        title = "Ponuda",
        screen = Screen.ProductsOverview
    ),
    Cart(
        icon = Resources.Icon.ShoppingCart,
        title = "Košarica",
        screen = Screen.Cart
    ),
    Categories(
        icon = Resources.Icon.Categories,
        title = "Restorani",
        screen = Screen.Categories
    )
}