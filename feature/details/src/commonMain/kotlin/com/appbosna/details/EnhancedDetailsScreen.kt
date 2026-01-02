package com.appbosna.details

import ContentWithMessageBar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.appbosna.details.component.FlavorChip
import com.appbosna.shared.component.InfoCard
import com.appbosna.shared.component.LoadingCard
import com.appbosna.shared.component.PrimaryButton
import com.appbosna.shared.component.QuantityCounter
import com.appbosna.shared.domain.QuantityCounterSize
import com.appbosna.shared.error.ErrorView
import com.appbosna.shared.fonts.BebasNeueFont
import com.appbosna.shared.fonts.BorderIdle
import com.appbosna.shared.fonts.FontSize
import com.appbosna.shared.fonts.IconPrimary
import com.appbosna.shared.fonts.Resources
import com.appbosna.shared.fonts.RobotoCondensedFont
import com.appbosna.shared.fonts.Surface
import com.appbosna.shared.fonts.SurfaceLighter
import com.appbosna.shared.fonts.TextPrimary
import com.appbosna.shared.fonts.TextSecondary
import com.appbosna.shared.util.DisplayResult
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import rememberMessageBarState

/**
 * Enhanced DetailsScreen with improved error handling
 * Shows usage of new ErrorView component and error states
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedDetailsScreen(
    id: String,
    navigateBack: () -> Unit
) {
    println("📱 EnhancedDetailsScreen created with id: $id")

    val messageBarState = rememberMessageBarState()
    
    val viewModel = koinViewModel<DetailsViewModel>(
        key = id
    ) { 
        parametersOf(id) 
    }
    
    val product by viewModel.product.collectAsState()
    val quantity = viewModel.quantity
    val selectedFlavor = viewModel.selectedFlavor

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Details",
                        fontFamily = BebasNeueFont(),
                        fontSize = FontSize.LARGE,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = "Back Arrow icon",
                            tint = IconPrimary
                        )
                    }
                },
                actions = {
                    QuantityCounter(
                        size = QuantityCounterSize.Large,
                        value = quantity,
                        onMinusClick = viewModel::updateQuantity,
                        onPlusClick = viewModel::updateQuantity
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface
                )
            )
        }
    ) { padding ->

        product.DisplayResult(
            onLoading = { 
                LoadingCard(Modifier.fillMaxSize()) 
            },
            onSuccess = { selectedProduct ->

                val restaurantName = selectedProduct.restaurantName.ifBlank {
                    "Restoran #${selectedProduct.restaurantId}"
                }

                ContentWithMessageBar(
                    contentBackgroundColor = Surface,
                    modifier = Modifier.padding(
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding()
                    ),
                    messageBarState = messageBarState
                ) {

                    Column {

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp)
                                .padding(top = 12.dp)
                        ) {

                            AsyncImage(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, BorderIdle, RoundedCornerShape(12.dp)),
                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(selectedProduct.thumbnail)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )

                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Text(
                                    text = restaurantName,
                                    fontSize = FontSize.SMALL,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "BAM ${selectedProduct.price}",
                                    fontSize = FontSize.MEDIUM,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = selectedProduct.title,
                                fontSize = FontSize.EXTRA_MEDIUM,
                                fontWeight = FontWeight.Medium,
                                fontFamily = RobotoCondensedFont(),
                                color = TextPrimary
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = selectedProduct.description,
                                fontSize = FontSize.REGULAR,
                                lineHeight = FontSize.REGULAR * 1.3,
                                color = TextPrimary
                            )
                        }

                        Column(
                            modifier = Modifier
                                .background(
                                    if (selectedProduct.flavors.isNotEmpty())
                                        SurfaceLighter else Surface
                                )
                                .padding(24.dp)
                        ) {

                            if (selectedProduct.flavors.isNotEmpty()) {

                                val flavorList = selectedProduct.flavors
                                    .split(",")
                                    .filter { it.isNotBlank() }

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    flavorList.forEach { flavor ->
                                        FlavorChip(
                                            flavor = flavor,
                                            isSelected = selectedFlavor == flavor,
                                            onClick = { viewModel.updateFlavor(flavor) }
                                        )
                                        Spacer(Modifier.width(8.dp))
                                    }
                                }

                                Spacer(Modifier.height(24.dp))
                            }

                            PrimaryButton(
                                icon = Resources.Icon.ShoppingCart,
                                text = "Dodaj u košaricu",
                                enabled = true,
                                onClick = {
                                    viewModel.addItemToCart(
                                        onSuccess = {
                                            messageBarState.addSuccess("Obrok dodan u košaricu")
                                        },
                                        onError = { msg ->
                                            messageBarState.addError(msg)
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            },
            onError = { errorMessage ->
                // Enhanced error display with retry option
                ErrorView(
                    error = com.appbosna.shared.error.AppError.UnknownError(
                        message = errorMessage
                    ),
                    onRetry = {
                        // Retry loading product
                        // viewModel.retryLoadProduct()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        )
    }
}
