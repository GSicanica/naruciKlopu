package com.appbosna.checkout

import ContentWithMessageBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appbosna.shared.component.PrimaryButton
import com.appbosna.shared.component.ProfileForm
import com.appbosna.shared.fonts.BebasNeueFont
import com.appbosna.shared.fonts.FontSize
import com.appbosna.shared.fonts.IconPrimary
import com.appbosna.shared.fonts.Resources
import com.appbosna.shared.fonts.Surface
import com.appbosna.shared.fonts.SurfaceBrand
import com.appbosna.shared.fonts.SurfaceError
import com.appbosna.shared.fonts.TextPrimary
import com.appbosna.shared.fonts.TextWhite
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import rememberMessageBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    totalAmount: Double,
    navigateBack: () -> Unit,
    navigateToPaymentCompleted: (Boolean?, String?) -> Unit,
) {
    val messageBarState = rememberMessageBarState()
    val viewModel = koinViewModel<CheckoutViewModel> { parametersOf(totalAmount) }
    val screenState = viewModel.screenState
    val isFormValid = viewModel.isFormValid

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Checkout",
                        fontFamily = BebasNeueFont(),
                        fontSize = FontSize.LARGE,
                        color = TextPrimary
                    )
                },
                actions = {
                    Text(
                        text = "BAM $totalAmount",
                        fontSize = FontSize.EXTRA_MEDIUM,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = "Back arrow icon",
                            tint = IconPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 12.dp,
                        bottom = 24.dp
                    )
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                ProfileForm(
                    modifier = Modifier.weight(1f),
                    country = screenState.country,
                    onCountrySelect = {},
                    firstName = screenState.firstName,
                    onFirstNameChange = viewModel::updateFirstName,
                    lastName = screenState.lastName,
                    onLastNameChange = viewModel::updateLastName,
                    email = screenState.email,
                    city = screenState.city,
                    onCityChange = viewModel::updateCity,
                    postalCode = screenState.postalCode,
                    onPostalCodeChange = viewModel::updatePostalCode,
                    address = screenState.address,
                    onAddressChange = viewModel::updateAddress,
                    phoneNumber = screenState.phoneNumber,
                    onPhoneNumberChange = viewModel::updatePhoneNumber
                )
                Column {
                    PrimaryButton(
                        text = "Pay with PayPal",
                        icon = Resources.Image.PaypalLogo,
                        enabled = isFormValid,
                        onClick = {
                            viewModel.payWithPayPal(
                                onSuccess = {

                                },
                                onError = { message ->
                                    messageBarState.addError(message)
                                }
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PrimaryButton(
                        text = "Pay on Delivery",
                        icon = Resources.Icon.ShoppingCart,
                        secondary = true,
                        enabled = isFormValid,
                        onClick = {
                            viewModel.payOnDelivery(
                                onSuccess = {
                                    navigateToPaymentCompleted(true, null)
                                },
                                onError = { message ->
                                    navigateToPaymentCompleted(null, message)
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}