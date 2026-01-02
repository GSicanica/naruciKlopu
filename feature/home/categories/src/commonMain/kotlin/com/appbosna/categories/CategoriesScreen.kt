package com.appbosna.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.appbosna.categories.component.CategoryCard
import com.appbosna.shared.domain.Restaurant
import com.appbosna.shared.util.RequestState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoriesScreen(
    navigateToCategorySearch: (Int) -> Unit,      // ID restorana
) {
    val viewModel = koinViewModel<CategoriesViewModel>()
    val restaurantsState by viewModel.restaurants.collectAsState()

    var query by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    val filteredRestaurants: List<Restaurant> = when (restaurantsState) {
        is RequestState.Success -> {
            val list = (restaurantsState as RequestState.Success<List<Restaurant>>).data
            val q = query.trim()
            if (q.isEmpty()) list
            else list.filter { it.name.contains(q, ignoreCase = true) }
        }
        else -> emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .imePadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Nađi restoran") },
            placeholder = { Text("") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        when (restaurantsState) {
            is RequestState.Loading -> {
                Text(
                    text = "Učitavanje...",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Gray
                )
            }

            is RequestState.Error -> {
                Text(
                    text = (restaurantsState as RequestState.Error).message,
                    modifier = Modifier.padding(8.dp),
                    color = Color.Red
                )
            }

            is RequestState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    if (filteredRestaurants.isEmpty()) {
                        item {
                            Text(
                                text = "Nema rezultata za \"$query\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else {
                        items(
                            items = filteredRestaurants,
                            key = { it.id }
                        ) { restaurant ->
                            CategoryCard(
                                restaurant = restaurant,
                                onClick = { navigateToCategorySearch(restaurant.id) }
                            )
                        }
                    }
                }
            }

            RequestState.Idle -> Unit
        }
    }
}
