package com.appbosna.products_overview.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.appbosna.shared.domain.Product
import com.appbosna.shared.fonts.Alpha
import com.appbosna.shared.fonts.FontSize
import com.appbosna.shared.fonts.IconWhite
import com.appbosna.shared.fonts.Resources
import com.appbosna.shared.fonts.RobotoCondensedFont
import com.appbosna.shared.fonts.TextBrand
import com.appbosna.shared.fonts.TextWhite
import org.jetbrains.compose.resources.painterResource

@Composable
fun MainProductCard(
    modifier: Modifier = Modifier,
    product: Product,
    isLarge: Boolean = false,
    onClick: (String) -> Unit
) {

    // ANIMACIJE
    val infiniteTransition = rememberInfiniteTransition()

    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val animatedRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val restaurantName = product.restaurantName.ifBlank {
        "Restoran #${product.restaurantId}"   // fallback ako backend ne pošalje ime
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                println("🟡 MainProductCard clicked: ID=${product.id}, Title=${product.title}")
                onClick(product.id)
            }
    ) {
        // POZADINSKA SLIKA
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .animateContentSize()
                .then(
                    if (isLarge)
                        Modifier.scale(animatedScale).rotate(animatedRotation)
                    else Modifier
                ),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(product.thumbnail)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        // GRADIENT OVERLAY
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black,
                            Color.Black.copy(alpha = 0f)
                        ),
                        startY = Float.POSITIVE_INFINITY,
                        endY = 0f
                    )
                )
        )

        // TEKSTOVI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Bottom
        ) {

            // NAZIV PROIZVODA
            Text(
                text = product.title,
                fontSize = FontSize.EXTRA_MEDIUM,
                fontWeight = FontWeight.Medium,
                color = TextWhite,
                fontFamily = RobotoCondensedFont(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            // OPIS
            Text(
                text = product.description,
                fontSize = FontSize.REGULAR,
                color = TextWhite.copy(alpha = Alpha.HALF),
                lineHeight = FontSize.REGULAR * 1.3f,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // RESTORAN + TEŽINA
                Row(verticalAlignment = Alignment.CenterVertically) {

                    // 📌 Restoran
                    Text(
                        text = restaurantName,
                        fontSize = FontSize.SMALL,
                        color = TextWhite.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.width(8.dp))

                    // 📌 Težina
                    if (!product.weight.isNullOrBlank()) {
                        Icon(
                            modifier = Modifier.size(14.dp),
                            painter = painterResource(Resources.Icon.Weight),
                            contentDescription = null,
                            tint = IconWhite
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${product.weight}g",
                            fontSize = FontSize.EXTRA_SMALL,
                            color = TextWhite
                        )
                    }
                }

                // CIJENA
                Text(
                    text = "BAM ${product.price}",
                    fontSize = FontSize.EXTRA_REGULAR,
                    color = TextBrand,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
