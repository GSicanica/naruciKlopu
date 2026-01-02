package com.appbosna.categories.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.appbosna.shared.domain.OpenStatus
import com.appbosna.shared.domain.Restaurant

@Composable
fun CategoryCard(
    restaurant: Restaurant,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable(onClick = onClick)
            .background(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ---------- Slika ----------
            AsyncImage(
                model = restaurant.image,
                contentDescription = restaurant.name,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.LightGray, RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleMedium
                )

                RestaurantStatusBadge(restaurant)
            }
        }
    }
}

@Composable
fun RestaurantStatusBadge(restaurant: Restaurant) {

    val (text, bgColor, textColor) = when (restaurant.openStatus) {
        OpenStatus.OPEN ->
            Triple("OPEN", Color(0xFFDFFFE0), Color(0xFF008A00))

        OpenStatus.CLOSING_SOON ->
            Triple("CLOSING SOON", Color(0xFFFFF4D0), Color(0xFFE99600))

        OpenStatus.CLOSED ->
            Triple("CLOSED", Color(0xFFFFE0E0), Color(0xFFB00020))
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
