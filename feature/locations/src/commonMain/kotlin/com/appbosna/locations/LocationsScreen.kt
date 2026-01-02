package com.appbosna.locations

import ContentWithMessageBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appbosna.CommonScaffold
import com.appbosna.locations.component.LocationCard
import com.appbosna.shared.domain.Location
import com.appbosna.shared.fonts.BebasNeueFont
import com.appbosna.shared.fonts.FontSize
import com.appbosna.shared.fonts.Resources
import com.appbosna.shared.fonts.RobotoCondensedFont
import com.appbosna.shared.fonts.Surface
import com.appbosna.shared.fonts.SurfaceBrand
import com.appbosna.shared.fonts.SurfaceError
import com.appbosna.shared.fonts.SurfaceLighter
import com.appbosna.shared.fonts.TextPrimary
import com.appbosna.shared.fonts.TextSecondary
import com.appbosna.shared.fonts.TextWhite
import com.appbosna.shared.util.RequestState
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

@Composable
fun LocationsScreen(
    navigateBack: () -> Unit,
    navigateToAddLocation: () -> Unit,
    navigateToEditLocation: (String) -> Unit
) {
    val messageBarState = rememberMessageBarState()
    val locationsViewModel = koinViewModel<LocationsViewModel>()
    val screenState = locationsViewModel.screenState

    if (screenState.showDeleteConfirmDialog && screenState.selectedLocationForDelete != null) {
        DeleteLocationConfirmDialog(
            location = screenState.selectedLocationForDelete,
            isDeleting = screenState.isDeletingLocation,
            onConfirm = {
                locationsViewModel.deleteLocation(
                    location = screenState.selectedLocationForDelete,
                    onSuccess = {
                        messageBarState.addSuccess("🗑️ Location deleted successfully!")
                    },
                    onError = { error ->
                        messageBarState.addError(error)
                    }
                )
            },
            onDismiss = {
                locationsViewModel.hideDeleteConfirmDialog()
            }
        )
    }

    CommonScaffold(
        title = "Locations",
        navigateBack = navigateBack
    ) { paddingValues ->
        ContentWithMessageBar(
            modifier = Modifier
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                ),
            contentBackgroundColor = Surface,
            messageBarState = messageBarState,
            errorMaxLines = 2,
            errorContainerColor = SurfaceError,
            errorContentColor = TextWhite,
            successContainerColor = SurfaceBrand,
            successContentColor = TextPrimary
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Surface,
                                SurfaceLighter,
                                Surface
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(0f, Float.POSITIVE_INFINITY)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    LocationsHeader(
                        onAddLocationClick = navigateToAddLocation
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    when (screenState.locations) {
                        is RequestState.Loading -> {
                            LoadingState()
                        }
                        is RequestState.Success -> {
                            if (screenState.locations.data.isEmpty()) {
                                EmptyLocationsState(
                                    onAddLocationClick = navigateToAddLocation
                                )
                            } else {
                                LocationsList(
                                    locations = screenState.locations.data,
                                    onEditClick = { location ->
                                        navigateToEditLocation(location.id)
                                    },
                                    onDeleteClick = { location ->
                                        locationsViewModel.showDeleteConfirmDialog(location)
                                    },
                                    onSetDefaultClick = { location ->
                                        locationsViewModel.setDefaultLocation(
                                            location = location,
                                            onSuccess = {
                                                messageBarState.addSuccess("✅ Default location updated!")
                                            },
                                            onError = { error ->
                                                messageBarState.addError(error)
                                            }
                                        )
                                    }
                                )
                            }
                        }
                        is RequestState.Error -> {
                            ErrorState(
                                message = screenState.locations.message,
                                onRetry = {}
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationsHeader(
    onAddLocationClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = SurfaceBrand.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            SurfaceBrand.copy(alpha = 0.1f),
                            Surface,
                            SurfaceBrand.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                ) {
                    Text(
                        text = "🏠 Your Locations",
                        fontSize = FontSize.EXTRA_LARGE,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BebasNeueFont(),
                        color = TextPrimary,
                        lineHeight = FontSize.EXTRA_LARGE * 1.2
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Manage your delivery locations",
                        fontSize = FontSize.SMALL,
                        fontFamily = RobotoCondensedFont(),
                        color = TextSecondary,
                        lineHeight = FontSize.SMALL * 1.4
                    )
                }
                
                Button(
                    onClick = onAddLocationClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceBrand,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp)
                    )
                ) {

                    Icon(
                        painter = painterResource(Resources.Icon.Plus),
                        contentDescription = "Add",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Location",
                        fontFamily = RobotoCondensedFont(),
                        fontSize = FontSize.SMALL,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationsList(
    locations: List<Location>,
    onEditClick: (Location) -> Unit,
    onDeleteClick: (Location) -> Unit,
    onSetDefaultClick: (Location) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(
            items = locations,
            key = { it.id }
        ) { location ->
            LocationCard(
                location = location,
                onEditClick = { onEditClick(location) },
                onDeleteClick = { onDeleteClick(location) },
                onSetDefaultClick = { onSetDefaultClick(location) }
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(color = SurfaceBrand)
            Text(
                text = "Loading your locations...",
                fontFamily = RobotoCondensedFont(),
                fontSize = FontSize.SMALL,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun EmptyLocationsState(
    onAddLocationClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🏠",
                fontSize = FontSize.EXTRA_LARGE * 2
            )
            Text(
                text = "No locations yet",
                fontFamily = BebasNeueFont(),
                fontSize = FontSize.LARGE,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Add your first delivery location to get started",
                fontFamily = RobotoCondensedFont(),
                fontSize = FontSize.MEDIUM,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onAddLocationClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceBrand,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp)
                )
            ) {
                Icon(
                    painter = painterResource(Resources.Icon.Plus),
                    contentDescription = "Add",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Your First Location",
                    fontFamily = RobotoCondensedFont(),
                    fontSize = FontSize.MEDIUM,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "😔",
                fontSize = FontSize.EXTRA_LARGE
            )
            Text(
                text = "Unable to load locations",
                fontFamily = RobotoCondensedFont(),
                fontSize = FontSize.MEDIUM,
                fontWeight = FontWeight.Medium,
                color = SurfaceError,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                fontFamily = RobotoCondensedFont(),
                fontSize = FontSize.SMALL,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceBrand,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Retry",
                    fontFamily = RobotoCondensedFont(),
                    fontSize = FontSize.SMALL
                )
            }
        }
    }
}

@Composable
private fun DeleteLocationConfirmDialog(
    location: Location,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        containerColor = Surface,
        title = {
            Text(
                text = "🗑️ Delete Location",
                fontFamily = BebasNeueFont(),
                fontSize = FontSize.LARGE,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to delete this location?",
                    fontFamily = RobotoCondensedFont(),
                    fontSize = FontSize.MEDIUM,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${location.title} - ${location.fullAddress}\"",
                    fontFamily = RobotoCondensedFont(),
                    fontSize = FontSize.SMALL,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceError,
                    contentColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isDeleting) "Deleting..." else "Delete",
                    fontFamily = RobotoCondensedFont(),
                    fontSize = FontSize.SMALL
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = RobotoCondensedFont(),
                    fontSize = FontSize.SMALL,
                    color = TextSecondary
                )
            }
        }
    )
} 