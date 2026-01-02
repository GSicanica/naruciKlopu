package com.appbosna.shared.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appbosna.shared.fonts.FontSize
import com.appbosna.shared.fonts.Resources
import com.appbosna.shared.fonts.TextPrimary
import com.appbosna.shared.fonts.TextSecondary
import org.jetbrains.compose.resources.painterResource

/**
 * Reusable Error Display Component
 */
@Composable
fun ErrorView(
    error: AppError,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error icon based on error type
        val iconResource = when (error) {
            is AppError.NetworkError -> Resources.Icon.Warning
            is AppError.NotFoundError -> Resources.Icon.Search
            is AppError.AuthenticationError -> Resources.Icon.Warning
            else -> Resources.Icon.Warning
        }

        Icon(
            painter = painterResource(iconResource),
            contentDescription = null,
            tint = Color(0xFFE74C3C),
            modifier = Modifier.size(64.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = getErrorTitle(error),
            fontSize = FontSize.MEDIUM,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = error.displayMessage,
            fontSize = FontSize.REGULAR,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = FontSize.REGULAR * 1.4
        )

        // Show retry button for retryable errors
        if (error.isRetryable() && onRetry != null) {
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3498DB)
                )
            ) {
                Icon(
                    painter = painterResource(Resources.Icon.Checkmark),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Pokušaj ponovo")
            }
        }

        // Show additional info for specific errors
        when (error) {
            is AppError.ValidationError -> {
                if (error.errors.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    ValidationErrorsList(error.errors)
                }
            }
            is AppError.RateLimitError -> {
                error.retryAfter?.let { seconds ->
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Pokušajte ponovo za $seconds sekundi",
                        fontSize = FontSize.SMALL,
                        color = TextSecondary.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun ValidationErrorsList(errors: Map<String, String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        errors.forEach { (field, message) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "• ",
                    fontSize = FontSize.SMALL,
                    color = Color(0xFFE74C3C)
                )
                Text(
                    text = "$field: $message",
                    fontSize = FontSize.SMALL,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun getErrorTitle(error: AppError): String {
    return when (error) {
        is AppError.NetworkError -> "Problem sa konekcijom"
        is AppError.AuthenticationError -> "Potrebna prijava"
        is AppError.AuthorizationError -> "Pristup odbijen"
        is AppError.ValidationError -> "Nevažeći podaci"
        is AppError.NotFoundError -> "Nije pronađeno"
        is AppError.ServerError -> "Greška servera"
        is AppError.RateLimitError -> "Previše zahtjeva"
        is AppError.BusinessError -> "Operacija nije dozvoljena"
        is AppError.UnknownError -> "Neočekivana greška"
        is AppError.EmptyError -> "Nema podataka"
    }
}

/**
 * Inline error message (smaller, for inline display)
 */
@Composable
fun InlineErrorMessage(
    error: AppError,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Resources.Icon.Warning),
            contentDescription = null,
            tint = Color(0xFFE74C3C),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = error.displayMessage,
            fontSize = FontSize.SMALL,
            color = Color(0xFFE74C3C)
        )
    }
}
