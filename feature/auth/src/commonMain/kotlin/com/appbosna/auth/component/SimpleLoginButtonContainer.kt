package com.appbosna.auth.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.appbosna.data.remote.ApiUser
import com.appbosna.data.remote.AuthRepository
import com.appbosna.shared.util.PreferencesRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SimpleLoginButtonContainer(
    onLogin: (Result<ApiUser>) -> Unit,
    content: @Composable SimpleLoginButtonContainerScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val authRepo = koinInject<AuthRepository>()

    // Load saved credentials
    var email by remember { mutableStateOf(PreferencesRepository.getLastLoginEmail() ?: "") }
    var name by remember { mutableStateOf(PreferencesRepository.getLastLoginName() ?: "") }

    val scopeObj = SimpleLoginButtonContainerScope(
        email = email,
        name = name,
        onEmailChange = { email = it },
        onNameChange = { name = it },
        onClick = {
            scope.launch {
                try {
                    if (email.isBlank()) {
                        onLogin(Result.failure(Exception("Email je prazan.")))
                        return@launch
                    }
                    if (name.isBlank()) {
                        onLogin(Result.failure(Exception("Ime je prazno.")))
                        return@launch
                    }

                    val user = authRepo.loginWithEmail(
                        email = email.trim(),
                        name = name.trim()
                    )

                    // Save credentials on successful login
                    PreferencesRepository.saveLastLoginCredentials(
                        email = email.trim(),
                        name = name.trim()
                    )

                    onLogin(Result.success(user))

                } catch (e: Exception) {
                    onLogin(Result.failure(e))
                }
            }
        }
    )

    content(scopeObj)
}


class SimpleLoginButtonContainerScope(
    val email: String,
    val name: String,
    val onEmailChange: (String) -> Unit,
    val onNameChange: (String) -> Unit,
    val onClick: suspend () -> Unit
) {

    @Composable
    fun EmailField(modifier: Modifier = Modifier) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            modifier = modifier.fillMaxWidth()
        )
    }

    @Composable
    fun NameField(modifier: Modifier = Modifier) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Ime i prezime") },
            singleLine = true,
            modifier = modifier.fillMaxWidth()
        )
    }

    @Composable
    fun LoginButton(
        loading: Boolean,
        modifier: Modifier = Modifier
    ) {
        val scope = rememberCoroutineScope()

        Button(
            onClick = {
                if (!loading) {
                    scope.launch { onClick() }
                }
            },
            enabled = !loading,
            modifier = modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Molimo pričekajte..." else "Prijavi se")
        }
    }
}
