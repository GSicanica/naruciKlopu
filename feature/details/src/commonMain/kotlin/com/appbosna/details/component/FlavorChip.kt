package com.appbosna.details.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appbosna.shared.fonts.BorderIdle
import com.appbosna.shared.fonts.BorderSecondary
import com.appbosna.shared.fonts.FontSize
import com.appbosna.shared.fonts.Surface
import com.appbosna.shared.fonts.TextPrimary
import com.appbosna.shared.fonts.TextSecondary

@Composable
fun FlavorChip(
    flavor: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(Surface)
            .border(
                width = 1.dp,
                color = if (isSelected) BorderSecondary else BorderIdle,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = flavor,
            fontSize = FontSize.SMALL,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) TextPrimary else TextSecondary
        )
    }
}
