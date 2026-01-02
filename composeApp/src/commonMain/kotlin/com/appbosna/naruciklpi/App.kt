package com.appbosna.naruciklpi

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.appbosna.data.domain.CustomerRepository
import com.appbosna.navigation.SetupNavGraph
import com.appbosna.shared.navigation.Screen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    MaterialTheme {
        val customerRepository = koinInject<CustomerRepository>()
        
        // Check authentication on every composition (including app resume from background)
        var authToken by remember { mutableStateOf<String?>(null) }
        var authCheckComplete by remember { mutableStateOf(false) }
        
        LaunchedEffect(Unit) {
            // Always check current token status when app starts/resumes
            authToken = customerRepository.getCurrentUserId()
            authCheckComplete = true
            println("🔐 Auth check: token = ${authToken?.take(10)}..., isAuthenticated = ${authToken != null}")
        }

        // Show nothing until we know the auth state
        if (!authCheckComplete) {
            return@MaterialTheme
        }

        val startDestination = if (authToken != null) {
            Screen.HomeGraph
        } else {
            Screen.Auth
        }

        // Use key() to force recreation of NavGraph when auth state changes
        key(authToken) {
            SetupNavGraph(
                modifier = Modifier.fillMaxSize(),
                startDestination = startDestination
            )
        }
    }
}
