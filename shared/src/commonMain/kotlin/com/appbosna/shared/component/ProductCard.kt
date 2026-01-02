package com.appbosna.shared.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.appbosna.shared.domain.Product
import com.appbosna.shared.fonts.Alpha
import com.appbosna.shared.fonts.BorderIdle
import com.appbosna.shared.fonts.FontSize
import com.appbosna.shared.fonts.Resources
import com.appbosna.shared.fonts.RobotoCondensedFont
import com.appbosna.shared.fonts.SurfaceLighter
import com.appbosna.shared.fonts.TextPrimary
import com.appbosna.shared.fonts.TextSecondary
import org.jetbrains.compose.resources.painterResource


@Composable
fun ProductCard(
    modifier: Modifier = Modifier,
    product: Product,
    onClick: (String) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderIdle, RoundedCornerShape(12.dp))
            .background(SurfaceLighter)
            .clickable {
                println("🔴 ProductCard clicked: ID=${product.id}, Title=${product.title}")
                onClick(product.id)
            }
    ) {
        AsyncImage(
            modifier = Modifier
                .width(90.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BorderIdle, RoundedCornerShape(12.dp)),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(product.thumbnail)
                .crossfade(false)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp)
        ) {

            // TITLE
            Text(
                text = product.title,
                fontSize = FontSize.MEDIUM,
                color = TextPrimary,
                fontFamily = RobotoCondensedFont(),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            // DESCRIPTION
            Text(
                text = product.description,
                fontSize = FontSize.REGULAR,
                lineHeight = FontSize.REGULAR * 1.3,
                color = TextPrimary.copy(alpha = Alpha.HALF),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // WEIGHT (only if exists)
                if (!product.weight.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            modifier = Modifier.size(14.dp),
                            painter = painterResource(Resources.Icon.Weight),
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${product.weight}g",
                            fontSize = FontSize.EXTRA_SMALL,
                            color = TextPrimary
                        )
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                // PRICE
                Text(
                    text = "BAM ${product.price}",
                    fontSize = FontSize.EXTRA_REGULAR,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
