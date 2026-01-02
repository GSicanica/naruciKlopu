package com.appbosna.shared.component.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appbosna.shared.component.CustomTextField
import com.appbosna.shared.domain.Restaurant
import com.appbosna.shared.fonts.Resources
import com.appbosna.shared.fonts.Surface
import com.appbosna.shared.fonts.TextPrimary
import org.jetbrains.compose.resources.painterResource

@Composable
fun RestaurantPickerDialog(
    restaurants: List<Restaurant>,
    selectedId: Int?,
    onDismiss: () -> Unit,
    onConfirm: (Restaurant) -> Unit
) {
    // Ako je lista prazna – prikaži poruku
    if (restaurants.isEmpty()) {
        AlertDialog(
            containerColor = Surface,
            title = { Text("Nema dostupnih restorana", color = TextPrimary) },
            text = { Text("Backend nije vratio nijedan restoran.", color = TextPrimary) },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Zatvori") }
            }
        )
        return
    }

    // Sigurno početno stanje
    val initialSelected = restaurants.firstOrNull { it.id == selectedId } ?: restaurants.first()

    var search by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(initialSelected) }

    val filtered = restaurants.filter { it.name.contains(search, ignoreCase = true) }

    AlertDialog(
        containerColor = Surface,
        onDismissRequest = onDismiss,
        title = {
            Text("Odaberi restoran", color = TextPrimary)
        },
        text = {
            Column(Modifier.height(350.dp)) {

                // Search bar
                CustomTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = "Pretraga restorana..."
                )

                Spacer(Modifier.height(10.dp))

                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    filtered.forEach { rest ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selected = rest }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(rest.name, Modifier.weight(1f), color = TextPrimary)

                            if (rest == selected) {
                                Icon(
                                    painter = painterResource(Resources.Icon.Checkmark),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton({ onConfirm(selected) }) {
                Text("Potvrdi")
            }
        },
        dismissButton = {
            TextButton(onDismiss) { Text("Otkaži") }
        }
    )
}
