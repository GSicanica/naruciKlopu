package com.appbosna.shared.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appbosna.shared.Constants.MAX_QUANTITY
import com.appbosna.shared.Constants.MIN_QUANTITY
import com.appbosna.shared.domain.QuantityCounterSize
import com.appbosna.shared.fonts.FontSize
import com.appbosna.shared.fonts.IconPrimary
import com.appbosna.shared.fonts.Resources
import com.appbosna.shared.fonts.SurfaceBrand
import com.appbosna.shared.fonts.SurfaceLighter
import com.appbosna.shared.fonts.TextPrimary
import org.jetbrains.compose.resources.painterResource

@Composable
fun QuantityCounter(
    modifier: Modifier = Modifier,
    size: QuantityCounterSize,
    value: Int,
    onMinusClick: (Int) -> Unit,
    onPlusClick: (Int) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(size.spacing)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(size = 6.dp))
                .background(SurfaceBrand)
                .clickable { if (value > MIN_QUANTITY) onMinusClick(value - 1) }
                .padding(size.padding),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(14.dp),
                painter = painterResource(Resources.Icon.Minus),
                contentDescription = "Minus icon",
                tint = IconPrimary
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(size = 6.dp))
                .background(SurfaceLighter)
                .padding(size.padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+$value",
                fontSize = FontSize.SMALL,
                lineHeight = FontSize.SMALL * 1,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(size = 6.dp))
                .background(SurfaceBrand)
                .clickable { if (value < MAX_QUANTITY) onPlusClick(value + 1) }
                .padding(size.padding),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(14.dp),
                painter = painterResource(Resources.Icon.Plus),
                contentDescription = "Plus icon",
                tint = IconPrimary
            )
        }
    }
}