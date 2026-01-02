package com.appbosna.products_overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appbosna.products_overview.component.MainProductCard
import com.appbosna.shared.component.InfoCard
import com.appbosna.shared.component.LoadingCard
import com.appbosna.shared.component.ProductCard
import com.appbosna.shared.domain.Product
import com.appbosna.shared.fonts.Alpha
import com.appbosna.shared.fonts.FontSize
import com.appbosna.shared.fonts.Resources
import com.appbosna.shared.fonts.TextPrimary
import com.appbosna.shared.util.DisplayResult
import com.appbosna.shared.util.RequestState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProductsOverviewScreen(
    navigateToDetails: (String) -> Unit,
) {
    val viewModel = koinViewModel<ProductsOverviewViewModel>()
    val products by viewModel.products.collectAsState()

    val horizontalListState = rememberLazyListState()

    // 🔹 Ovdje koristimo stateOf umjesto svakog puta recalculating
    val productsDistinct = remember { mutableStateOf(emptyList<Product>()) }
    val newProducts = remember { mutableStateOf(emptyList<Product>()) }
    val discountedProducts = remember { mutableStateOf(emptyList<Product>()) }

    // Kada se promijene proizvodi — ažuriramo state samo ako je potrebno
    LaunchedEffect(products) {
        // 👇 Ne koristi Composable funkciju u LaunchedEffect!
        if (products is RequestState.Success) {
            val productList = (products as RequestState.Success<List<Product>>).data

            val distinct = productList.distinctBy { it.id }

            productsDistinct.value = distinct
            newProducts.value = distinct
                .asSequence()
                .filter { it.isNew }
                .sortedByDescending { it.createdAt }
                .take(6)
                .toList()

            discountedProducts.value = distinct
                .asSequence()
                .filter { it.isDiscounted }
                .sortedByDescending { it.createdAt }
                .toList()

            horizontalListState.scrollToItem(0)
        }
    }


    products.DisplayResult(
        onLoading = {
            LoadingCard(modifier = Modifier.fillMaxSize())
        },
        onSuccess = {
            val allProducts = productsDistinct.value
            val new = newProducts.value
            val discounted = discountedProducts.value

            if (allProducts.isEmpty()) {
                InfoCard(
                    image = Resources.Image.Cat,
                    title = "Nema proizvoda",
                    subtitle = ""
                )
                return@DisplayResult
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // 🔹 Novi proizvodi
                if (new.isNotEmpty()) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Novo u ponudi",
                                fontSize = FontSize.EXTRA_REGULAR,
                                color = TextPrimary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyRow(
                                state = horizontalListState,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                            ) {
                                items(new, key = { it.id }) { product ->
                                    val productId = product.id  // Capture ID explicitly
                                    MainProductCard(
                                        modifier = Modifier
                                            .fillParentMaxWidth(0.6f)
                                            .height(250.dp),
                                        product = product,
                                        isLarge = false,
                                        onClick = { navigateToDetails(productId) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 🔹 Snizeni proizvodi
                if (discounted.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(Alpha.HALF),
                            text = "Sniženo",
                            fontSize = FontSize.EXTRA_REGULAR,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(discounted, key = { it.id }) { product ->
                        val productId = product.id  // Capture ID explicitly
                        ProductCard(
                            product = product,
                            onClick = { navigateToDetails(productId) }
                        )
                    }
                } else {
                    item {
                        InfoCard(
                            image = Resources.Image.Cat,
                            title = "Nema sniženih proizvoda",
                            subtitle = ""
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        },
        onError = { message ->
            InfoCard(
                image = Resources.Image.Cat,
                title = "Greška",
                subtitle = message
            )
        }
    )
}
