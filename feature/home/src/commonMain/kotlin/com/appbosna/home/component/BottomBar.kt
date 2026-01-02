package com.appbosna.home.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.appbosna.home.domain.BottomBarDestination
import com.appbosna.shared.domain.Customer
import com.appbosna.shared.fonts.IconPrimary
import com.appbosna.shared.fonts.IconSecondary
import com.appbosna.shared.fonts.SurfaceLighter
import com.appbosna.shared.util.RequestState
import org.jetbrains.compose.resources.painterResource

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    customer: RequestState<Customer>,
    selected: BottomBarDestination,
    onSelect: (BottomBarDestination) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceLighter)
            .padding(vertical = 16.dp, horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomBarDestination.entries.forEach { destination ->

            val isSelected = selected == destination

            val hasItemsInCart = destination == BottomBarDestination.Cart &&
                    customer.isSuccess() &&
                    customer.getSuccessData().cart.isNotEmpty()

            val tint by animateColorAsState(
                targetValue = if (isSelected) IconSecondary else IconPrimary,
                label = "icon_tint"
            )

            val scale by animateFloatAsState(
                targetValue = when {
                    isSelected -> 1.3f
                    hasItemsInCart -> 1.15f
                    else -> 1f
                },
                label = "icon_scale"
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    // ⛔️ bez ripple efekta
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onSelect(destination) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(destination.icon),
                    contentDescription = destination.name,
                    tint = tint,
                    modifier = Modifier.scale(scale)
                )

                if (destination == BottomBarDestination.Cart) {
                    AnimatedContent(targetState = customer) { customerState ->
                        if (customerState.isSuccess() &&
                            customerState.getSuccessData().cart.isNotEmpty()
                        ) {
                            val yOffset by animateDpAsState(
                                targetValue = if (isSelected) (-18).dp else (-14).dp,
                                label = "dot_offset"
                            )

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 8.dp, y = yOffset)
                                    .clip(CircleShape)
                                    .background(IconSecondary)
                            )
                        }
                    }
                }
            }
        }
    }
}
