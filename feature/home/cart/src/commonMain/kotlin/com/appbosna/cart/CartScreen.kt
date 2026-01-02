package com.appbosna.cart

import ContentWithMessageBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appbosna.cart.component.CartItemCard
import com.appbosna.shared.component.InfoCard
import com.appbosna.shared.component.LoadingCard
import com.appbosna.shared.fonts.Resources
import com.appbosna.shared.fonts.Surface
import com.appbosna.shared.fonts.SurfaceBrand
import com.appbosna.shared.fonts.SurfaceError
import com.appbosna.shared.fonts.TextPrimary
import com.appbosna.shared.fonts.TextWhite
import com.appbosna.shared.util.DisplayResult
import com.appbosna.shared.util.RequestState
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

@Composable
fun CartScreen() {
    val messageBarState = rememberMessageBarState()
    val viewModel = koinViewModel<CartViewModel>()
    val cartItemsWithProducts by viewModel.cartItemsWithProducts.collectAsState(RequestState.Loading)

    ContentWithMessageBar(
        contentBackgroundColor = Surface,
        messageBarState = messageBarState,
        errorMaxLines = 2,
        errorContainerColor = SurfaceError,
        errorContentColor = TextWhite,
        successContainerColor = SurfaceBrand,
        successContentColor = TextPrimary
    ) {
        cartItemsWithProducts.DisplayResult(
            onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
            onSuccess = { data ->
                if (data.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = data,
                            key = { it.first.id }   // ključ osigurava da se update-a samo taj red
                        ) { pair ->
                            val cartItem = pair.first
                            val product = pair.second

                            CartItemCard(
                                cartItem = cartItem,
                                product = product,
                                onMinusClick = { quantity ->
                                    viewModel.updateCartItemQuantity(
                                        productId = cartItem.productId,
                                        quantity = quantity.toString(),
                                        onSuccess = {},
                                        onError = { messageBarState.addError(it) }
                                    )
                                },
                                onPlusClick = { quantity ->
                                    viewModel.updateCartItemQuantity(
                                        productId = cartItem.productId,
                                        quantity = quantity.toString(),
                                        onSuccess = {},
                                        onError = { messageBarState.addError(it) }
                                    )
                                },
                                onDeleteClick = {
                                    viewModel.deleteCartItem(
                                        id = cartItem.id,
                                        onSuccess = {},
                                        onError = { messageBarState.addError(it) }
                                    )
                                }
                            )
                        }
                    }
                } else {
                    InfoCard(
                        image = Resources.Image.ShoppingCart,
                        title = "Prazna Košarica",
                        subtitle = ""
                    )
                }
            },
            onError = { message ->
                InfoCard(
                    image = Resources.Image.Cat,
                    title = "Ooaps!",
                    subtitle = message
                )
            }
        )
    }
}
