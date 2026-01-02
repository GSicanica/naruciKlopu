package com.appbosna.auth

import ContentWithMessageBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appbosna.auth.component.SimpleLoginButtonContainer
import com.appbosna.shared.fonts.Alpha
import com.appbosna.shared.fonts.BebasNeueFont
import com.appbosna.shared.fonts.FontSize
import com.appbosna.shared.fonts.Surface
import com.appbosna.shared.fonts.SurfaceBrand
import com.appbosna.shared.fonts.SurfaceError
import com.appbosna.shared.fonts.TextPrimary
import com.appbosna.shared.fonts.TextSecondary
import com.appbosna.shared.fonts.TextWhite
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

@Composable
fun AuthScreen(
    navigateToHome: () -> Unit
) {
    val viewModel = koinViewModel<AuthViewModel>()
    val messageBarState = rememberMessageBarState()
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }

    Scaffold { padding ->
        ContentWithMessageBar(
            modifier = Modifier.padding(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            ),
            contentBackgroundColor = Surface,
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
                    .padding(24.dp)
            ) {

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Naruči Klopu",
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = BebasNeueFont(),
                        fontSize = FontSize.EXTRA_LARGE,
                        textAlign = TextAlign.Center,
                        color = TextSecondary,
                    )

                    Text(
                        text = "Prijava putem emaila" +
                                " bez registracije",
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(Alpha.HALF),
                        fontSize = FontSize.EXTRA_REGULAR,
                        textAlign = TextAlign.Center,
                        color = TextPrimary
                    )
                }

                // LOGIN UI
                SimpleLoginButtonContainer(
                    onLogin = { result ->
                        result.onSuccess { user ->
                            loading = true
                            // Backend automatically creates customer record if it doesn't exist
                            // Don't call createCustomer to preserve existing data
                            scope.launch { 
                                navigateToHome() 
                                loading = false
                            }
                        }.onFailure { error ->
                            messageBarState.addError(error.message ?: "Neuspješan login.")
                            loading = false
                        }
                    }
                ) {
                    Column {

                        EmailField()

                        NameField(
                            modifier = Modifier.padding(top = 12.dp)
                        )

                        LoginButton(
                            loading = loading,
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                }
            }
        }
    }
}
