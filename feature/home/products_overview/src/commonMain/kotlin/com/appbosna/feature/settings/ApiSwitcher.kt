package com.appbosna.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appbosna.data.config.ApiConfig
import com.appbosna.data.config.ApiType

/**
 * API Switcher UI Component
 *
 * Allows users to switch between REST and GraphQL APIs
 */
@Composable
fun ApiSwitcher(
    modifier: Modifier = Modifier,
    onApiChanged: (ApiType) -> Unit = {}
) {
    var currentApi by remember { mutableStateOf(ApiConfig.currentApiType) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "API Backend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = currentApi.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (currentApi) {
                        ApiType.REST -> Color(0xFF2196F3)
                        ApiType.GRAPHQL -> Color(0xFFE10098)
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // API Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // REST Option
                ApiOptionCard(
                    modifier = Modifier.weight(1f),
                    title = "REST API",
                    description = "Traditional RESTful",

                    isSelected = currentApi == ApiType.REST,
                    color = Color(0xFF2196F3),
                    onClick = {
                        ApiConfig.currentApiType = ApiType.REST
                        currentApi = ApiType.REST
                        onApiChanged(ApiType.REST)
                    }
                )

                // GraphQL Option
                ApiOptionCard(
                    modifier = Modifier.weight(1f),
                    title = "GraphQL",
                    description = "Modern query language",

                    isSelected = currentApi == ApiType.GRAPHQL,
                    color = Color(0xFFE10098),
                    onClick = {
                        ApiConfig.currentApiType = ApiType.GRAPHQL
                        currentApi = ApiType.GRAPHQL
                        onApiChanged(ApiType.GRAPHQL)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Text
            Text(
                text = when (currentApi) {
                    ApiType.REST -> "Using traditional REST API with multiple endpoints"
                    ApiType.GRAPHQL -> "Using GraphQL for optimized data fetching"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ApiOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,

    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) color.copy(alpha = 0.1f) else Color.Transparent
    val borderColor = if (isSelected) color else Color.LightGray
    val textColor = if (isSelected) color else Color.Gray

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {


            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.7f)
            )

            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✓ Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Compact API Switcher - for navigation bar or toolbar
 */
@Composable
fun CompactApiSwitcher(
    modifier: Modifier = Modifier,
    onApiChanged: (ApiType) -> Unit = {}
) {
    var currentApi by remember { mutableStateOf(ApiConfig.currentApiType) }

    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // REST Button
        Button(
            onClick = {
                ApiConfig.currentApiType = ApiType.REST
                currentApi = ApiType.REST
                onApiChanged(ApiType.REST)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentApi == ApiType.REST)
                    Color(0xFF2196F3) else Color.Transparent,
                contentColor = if (currentApi == ApiType.REST)
                    Color.White else Color.Gray
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("REST", style = MaterialTheme.typography.labelMedium)
        }

        // GraphQL Button
        Button(
            onClick = {
                ApiConfig.currentApiType = ApiType.GRAPHQL
                currentApi = ApiType.GRAPHQL
                onApiChanged(ApiType.GRAPHQL)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentApi == ApiType.GRAPHQL)
                    Color(0xFFE10098) else Color.Transparent,
                contentColor = if (currentApi == ApiType.GRAPHQL)
                    Color.White else Color.Gray
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("GraphQL", style = MaterialTheme.typography.labelMedium)
        }
    }
}
