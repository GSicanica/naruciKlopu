package com.appbosna.locations.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.appbosna.shared.domain.Location
import com.appbosna.shared.fonts.BebasNeueFont
import com.appbosna.shared.fonts.BorderIdle
import com.appbosna.shared.fonts.FontSize
import com.appbosna.shared.fonts.Resources
import com.appbosna.shared.fonts.RobotoCondensedFont
import com.appbosna.shared.fonts.Surface
import com.appbosna.shared.fonts.SurfaceBrand
import com.appbosna.shared.fonts.SurfaceError
import com.appbosna.shared.fonts.SurfaceLighter
import com.appbosna.shared.fonts.TextPrimary
import com.appbosna.shared.fonts.TextSecondary
import org.jetbrains.compose.resources.painterResource


@Composable
fun LocationCard(
    location: Location,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSetDefaultClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (location.isDefault) SurfaceBrand.copy(alpha = 0.3f)
                else TextSecondary.copy(alpha = 0.15f)
            )
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = if (location.isDefault) {
                        Brush.linearGradient(
                            colors = listOf(
                                SurfaceBrand.copy(alpha = 0.15f),
                                Surface,
                                SurfaceBrand.copy(alpha = 0.05f)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                SurfaceLighter.copy(alpha = 0.5f),
                                Surface
                            )
                        )
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = if (location.isDefault) SurfaceBrand.copy(alpha = 0.2f)
                            else SurfaceLighter,
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (location.isDefault) "⭐" else location.category.icon,
                                    fontSize = FontSize.MEDIUM
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${location.title} • ${location.category.displayName}",
                                    fontFamily = RobotoCondensedFont(),
                                    fontSize = FontSize.MEDIUM,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )

                                if (location.isDefault) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = SurfaceBrand.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "DEFAULT",
                                            fontFamily = RobotoCondensedFont(),
                                            fontSize = FontSize.EXTRA_SMALL,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${location.city}, ${location.country}",
                                fontFamily = RobotoCondensedFont(),
                                fontSize = FontSize.SMALL,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Icon(
                        painter = painterResource(
                            if (isExpanded) Resources.Icon.Close else Resources.Icon.Plus
                        ),
                        contentDescription = if (isExpanded) "Close" else "Add",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Unspecified
                    )


                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Divider(
                            color = BorderIdle.copy(alpha = 0.3f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Surface(
                            color = SurfaceLighter.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "📍 Full Address",
                                    fontFamily = BebasNeueFont(),
                                    fontSize = FontSize.SMALL,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                Text(
                                    text = location.fullAddress,
                                    fontFamily = RobotoCondensedFont(),
                                    fontSize = FontSize.SMALL,
                                    color = TextPrimary,
                                    lineHeight = FontSize.SMALL * 1.4
                                )

                                if (location.state?.isNotBlank() == true || location.postalCode.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = buildString {
                                            if (location.state?.isNotBlank() == true) {
                                                append(location.state)
                                                append(" ")
                                            }
                                            append(location.postalCode)
                                        },
                                        fontFamily = RobotoCondensedFont(),
                                        fontSize = FontSize.SMALL,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!location.isDefault) {
                                OutlinedButton(
                                    onClick = onSetDefaultClick,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Surface
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder().copy(
                                        brush = Brush.linearGradient(listOf(SurfaceBrand, SurfaceBrand))
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(Resources.Icon.Home),
                                        contentDescription = "Set Default",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )


                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Set Default",
                                        fontFamily = RobotoCondensedFont(),
                                        fontSize = FontSize.EXTRA_SMALL
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = onEditClick,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(Resources.Icon.Edit),
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit",
                                    fontFamily = RobotoCondensedFont(),
                                    fontSize = FontSize.EXTRA_SMALL
                                )
                            }

                            OutlinedButton(
                                onClick = onDeleteClick,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = SurfaceError
                                ),
                                border = ButtonDefaults.outlinedButtonBorder().copy(
                                    brush = Brush.linearGradient(listOf(SurfaceError, SurfaceError))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(Resources.Icon.Delete),
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Delete",
                                    fontFamily = RobotoCondensedFont(),
                                    fontSize = FontSize.EXTRA_SMALL
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}