package com.appbosna.locations

import ContentWithMessageBar
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appbosna.CommonScaffold
import com.appbosna.shared.fonts.BebasNeueFont
import com.appbosna.shared.fonts.BorderIdle
import com.appbosna.shared.fonts.ButtonDisabled
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
import com.appbosna.shared.domain.LocationCategory
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

@Composable
fun AddEditLocationScreen(
    locationId: String?,
    navigateBack: () -> Unit
) {
    val messageBarState = rememberMessageBarState()
    val locationsViewModel = koinViewModel<LocationsViewModel>()
    val addEditState = locationsViewModel.addEditState
    val screenState = locationsViewModel.screenState

        val isEditMode = locationId != null
    val title = if (isEditMode) "Edit Address" else "Add New Address"

    LaunchedEffect(locationId) {
        if (locationId != null && !addEditState.isEditMode) {
            val locations = screenState.locations
            if (locations is RequestState.Success) {
                val location = locations.data.find { it.id == locationId }
                if (location != null) {
                    locationsViewModel.startEditingLocation(location)
                }
            }
        } else if (locationId == null && !addEditState.isEditMode) {
            locationsViewModel.startAddingLocation()
        }
    }

    val animatedVisibility by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 600, easing = EaseOutCubic)
    )

    CommonScaffold(
        title = title,
        navigateBack = {
            locationsViewModel.clearAddEditState()
            navigateBack()
        }
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .scale(animatedVisibility),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { AddEditHeader(isEditMode = isEditMode) }

                    item {
                        AddressTitleSection(
                            title = addEditState.title,
                            onTitleChange = locationsViewModel::updateTitle,
                            error = locationsViewModel.getValidationError("title")
                        )
                    }

                    item {
                        CategorySelectionSection(
                            selectedCategory = addEditState.category,
                            onCategoryChange = locationsViewModel::updateCategory
                        )
                    }

                    item {
                        AddressDetailsSection(
                            fullAddress = addEditState.fullAddress,
                            city = addEditState.city,
                            state = addEditState.state,
                            postalCode = addEditState.postalCode,
                            country = addEditState.country,
                            onFullAddressChange = locationsViewModel::updateFullAddress,
                            onCityChange = locationsViewModel::updateCity,
                            onStateChange = locationsViewModel::updateState,
                            onPostalCodeChange = locationsViewModel::updatePostalCode,
                            onCountryChange = locationsViewModel::updateCountry,
                            fullAddressError = locationsViewModel.getValidationError("fullAddress"),
                            cityError = locationsViewModel.getValidationError("city"),
                            postalCodeError = locationsViewModel.getValidationError("postalCode")
                        )
                    }

                    item {
                        DefaultAddressSection(
                            isDefault = addEditState.isDefault,
                            onIsDefaultChange = locationsViewModel::updateIsDefault
                        )
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    item {
                        SubmitButton(
                            isEditMode = isEditMode,
                            isEnabled = locationsViewModel.isAddEditFormValid,
                            isSubmitting = screenState.isAddingLocation || screenState.isUpdatingLocation,
                            onClick = {
                                if (isEditMode) {
                                    locationsViewModel.updateLocation(
                                        onSuccess = {
                                            messageBarState.addSuccess("✅ Address updated successfully!")
                                            navigateBack()
                                        },
                                        onError = { error ->
                                            messageBarState.addError(error)
                                        }
                                    )
                                } else {
                                    locationsViewModel.addLocation(
                                        onSuccess = {
                                            messageBarState.addSuccess("🎉 Address added successfully!")
                                            navigateBack()
                                        },
                                        onError = { error ->
                                            messageBarState.addError(error)
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddEditHeader(isEditMode: Boolean) {
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isEditMode) "✏️ Edit Address" else "🏠 Add New Address",
                    fontSize = FontSize.EXTRA_LARGE,
                    fontWeight = FontWeight.Bold,
                    fontFamily = BebasNeueFont(),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isEditMode)
                        "Update your delivery address details"
                    else
                        "Fill in the details for your new delivery address",
                    fontSize = FontSize.SMALL,
                    fontFamily = RobotoCondensedFont(),
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AddressTitleSection(
    title: String,
    onTitleChange: (String) -> Unit,
    error: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = TextSecondary.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "🏷️ Address Title",
                fontSize = FontSize.LARGE,
                fontWeight = FontWeight.SemiBold,
                fontFamily = BebasNeueFont(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Give this address a memorable name (e.g., Home, Office, Mom's House)",
                fontSize = FontSize.SMALL,
                fontFamily = RobotoCondensedFont(),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "e.g., Home, Office, Work",
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontFamily = RobotoCondensedFont(),
                            fontSize = FontSize.MEDIUM
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (error != null) SurfaceError else SurfaceBrand,
                        unfocusedBorderColor = if (error != null) SurfaceError else BorderIdle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = SurfaceBrand,
                        focusedContainerColor = SurfaceBrand.copy(alpha = 0.05f),
                        unfocusedContainerColor = SurfaceLighter
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    isError = error != null,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Resources.Icon.Edit),
                            contentDescription = "Vertical menu icon",
                            tint = if (error != null) SurfaceError else SurfaceBrand
                        )
                    }
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠️ $error",
                        fontFamily = RobotoCondensedFont(),
                        fontSize = FontSize.EXTRA_SMALL,
                        color = SurfaceError,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressDetailsSection(
    fullAddress: String,
    city: String,
    state: String,
    postalCode: String,
    country: String,
    onFullAddressChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onStateChange: (String) -> Unit,
    onPostalCodeChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    fullAddressError: String? = null,
    cityError: String? = null,
    postalCodeError: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = TextSecondary.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "📍 Address Details",
                fontSize = FontSize.LARGE,
                fontWeight = FontWeight.SemiBold,
                fontFamily = BebasNeueFont(),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                OutlinedTextField(
                    value = fullAddress,
                    onValueChange = onFullAddressChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = {
                        Text(
                            text = "Enter your complete street address including apartment/unit number...",
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontFamily = RobotoCondensedFont(),
                            fontSize = FontSize.SMALL
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (fullAddressError != null) SurfaceError else SurfaceBrand,
                        unfocusedBorderColor = if (fullAddressError != null) SurfaceError else BorderIdle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = SurfaceBrand,
                        focusedContainerColor = SurfaceBrand.copy(alpha = 0.05f),
                        unfocusedContainerColor = SurfaceLighter
                    ),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 4,
                    isError = fullAddressError != null,
                    leadingIcon = {

                        Icon(
                            painter = painterResource(Resources.Icon.MapPin),
                            contentDescription = "Address",
                            tint = if (fullAddressError != null) SurfaceError else SurfaceBrand

                        )
                    }
                )
                
                if (fullAddressError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠️ $fullAddressError",
                        fontFamily = RobotoCondensedFont(),
                        fontSize = FontSize.EXTRA_SMALL,
                        color = SurfaceError,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = onCityChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "City",
                                color = TextSecondary.copy(alpha = 0.7f),
                                fontFamily = RobotoCondensedFont()
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (cityError != null) SurfaceError else SurfaceBrand,
                            unfocusedBorderColor = if (cityError != null) SurfaceError else BorderIdle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = SurfaceBrand,
                            focusedContainerColor = SurfaceBrand.copy(alpha = 0.05f),
                            unfocusedContainerColor = SurfaceLighter
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = cityError != null,
                        leadingIcon = {


                            Icon(
                                painter = painterResource(Resources.Icon.MapPin),
                                contentDescription = "City",

                                tint = if (cityError != null) SurfaceError else SurfaceBrand

                            )
                        }
                    )
                    
                    if (cityError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠️ $cityError",
                            fontFamily = RobotoCondensedFont(),
                            fontSize = FontSize.EXTRA_SMALL,
                            color = SurfaceError,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = state,
                    onValueChange = onStateChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "State (Optional)",
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontFamily = RobotoCondensedFont()
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SurfaceBrand,
                        unfocusedBorderColor = BorderIdle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = SurfaceBrand,
                        focusedContainerColor = SurfaceBrand.copy(alpha = 0.05f),
                        unfocusedContainerColor = SurfaceLighter
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    leadingIcon = {

                        Icon(
                            painter = painterResource(Resources.Icon.Warning),
                            contentDescription = "State",
                            tint = SurfaceBrand
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = postalCode,
                        onValueChange = onPostalCodeChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Postal Code",
                                color = TextSecondary.copy(alpha = 0.7f),
                                fontFamily = RobotoCondensedFont()
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (postalCodeError != null) SurfaceError else SurfaceBrand,
                            unfocusedBorderColor = if (postalCodeError != null) SurfaceError else BorderIdle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = SurfaceBrand,
                            focusedContainerColor = SurfaceBrand.copy(alpha = 0.05f),
                            unfocusedContainerColor = SurfaceLighter
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = postalCodeError != null,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Resources.Icon.Menu),
                                contentDescription = "Postal Code",
                                tint = if (cityError != null) SurfaceError else SurfaceBrand
                            )
                        }
                    )
                    
                    if (postalCodeError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠️ $postalCodeError",
                            fontFamily = RobotoCondensedFont(),
                            fontSize = FontSize.EXTRA_SMALL,
                            color = SurfaceError,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = country,
                    onValueChange = onCountryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "Enter Country",
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontFamily = RobotoCondensedFont()
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SurfaceBrand,
                        unfocusedBorderColor = BorderIdle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = SurfaceBrand,
                        focusedContainerColor = SurfaceBrand.copy(alpha = 0.05f),
                        unfocusedContainerColor = SurfaceLighter
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true

                )
            }
        }
    }
}

@Composable
private fun DefaultAddressSection(
    isDefault: Boolean,
    onIsDefaultChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isDefault) SurfaceBrand.copy(alpha = 0.3f) else TextSecondary.copy(
                    alpha = 0.2f
                )
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isDefault) {
                        Brush.linearGradient(
                            colors = listOf(
                                SurfaceBrand.copy(alpha = 0.1f),
                                Surface
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "⭐ Set as Default Address",
                        fontSize = FontSize.MEDIUM,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = RobotoCondensedFont(),
                        color = TextPrimary
                    )
                    Text(
                        text = "This will be your primary delivery address",
                        fontSize = FontSize.SMALL,
                        fontFamily = RobotoCondensedFont(),
                        color = TextSecondary
                    )
                }

                Switch(
                    checked = isDefault,
                    onCheckedChange = onIsDefaultChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SurfaceBrand,
                        checkedTrackColor = SurfaceBrand.copy(alpha = 0.5f),
                        uncheckedThumbColor = BorderIdle,
                        uncheckedTrackColor = SurfaceLighter
                    )
                )
            }
        }
    }
}

@Composable
private fun SubmitButton(
    isEditMode: Boolean,
    isEnabled: Boolean,
    isSubmitting: Boolean,
    onClick: () -> Unit
) {
    val buttonText = when {
        isSubmitting && isEditMode -> "✨ Updating Address..."
        isSubmitting && !isEditMode -> "✨ Adding Address..."
        isEditMode -> "💾 Update Address"
        else -> "🏠 Add Address"
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = Modifier.scale(animatedScale)
    ) {
        if (isSubmitting) {
            Button(
                onClick = { },
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceBrand.copy(alpha = 0.7f),
                    disabledContainerColor = SurfaceBrand.copy(alpha = 0.7f)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = buttonText,
                        fontSize = FontSize.MEDIUM,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = RobotoCondensedFont(),
                        color = TextPrimary
                    )
                }
            }
        } else {
            Button(
                onClick = onClick,
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = if (isEnabled) 8.dp else 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = if (isEnabled) SurfaceBrand.copy(alpha = 0.3f) else Color.Transparent
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEnabled) SurfaceBrand else ButtonDisabled,
                    contentColor = TextPrimary,
                    disabledContainerColor = ButtonDisabled,
                    disabledContentColor = TextPrimary.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = buttonText,
                    fontSize = FontSize.MEDIUM,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = RobotoCondensedFont()
                )
            }
        }
    }
}

@Composable
private fun CategorySelectionSection(
    selectedCategory: LocationCategory,
    onCategoryChange: (LocationCategory) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = TextSecondary.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "🏷️ Category",
                fontSize = FontSize.LARGE,
                fontWeight = FontWeight.SemiBold,
                fontFamily = BebasNeueFont(),
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Choose a category for easy organization",
                fontSize = FontSize.SMALL,
                fontFamily = RobotoCondensedFont(),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LocationCategory.entries.chunked(3).forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowCategories.forEach { category ->
                            CategoryChip(
                                category = category,
                                isSelected = selectedCategory == category,
                                onSelect = { onCategoryChange(category) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - rowCategories.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: LocationCategory,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onSelect() },
        color = if (isSelected) SurfaceBrand.copy(alpha = 0.2f) else SurfaceLighter,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = category.icon,
                fontSize = FontSize.LARGE
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.displayName,
                fontFamily = RobotoCondensedFont(),
                fontSize = FontSize.EXTRA_SMALL,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) SurfaceBrand else TextSecondary,
                maxLines = 1
            )
        }
    }
} 