package com.appbosna.payment_completed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appbosna.shared.component.InfoCard
import com.appbosna.shared.component.LoadingCard
import com.appbosna.shared.component.PrimaryButton
import com.appbosna.shared.fonts.Resources
import com.appbosna.shared.fonts.Surface
import com.appbosna.shared.util.DisplayResult
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PaymentCompleted(
    isSuccess: Boolean? = null,
    error: String? = null,
    token: String? = null,
    navigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<PaymentViewModel> {
        parametersOf(isSuccess, error, token)
    }
    val screenState = viewModel.screenState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .systemBarsPadding()
            .padding(all = 24.dp)
    ) {
        screenState.DisplayResult(
            onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
            onSuccess = {
                Column {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        InfoCard(
                            title = "Success!",
                            subtitle = "Your purchase is on the way.",
                            image = Resources.Image.Checkmark
                        )
                    }
                    PrimaryButton(
                        text = "Go back",
                        icon = Resources.Icon.RightArrow,
                        onClick = navigateBack
                    )
                }
            },
            onError = { message ->
                Column {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        InfoCard(
                            title = "pOops!",
                            subtitle = message,
                            image = Resources.Image.Cat
                        )
                    }
                    PrimaryButton(
                        text = "Go back",
                        icon = Resources.Icon.RightArrow,
                        onClick = navigateBack
                    )
                }
            }
        )
    }
}
