package com.example.trekly.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trekly.api.PlacesClient
import com.google.android.libraries.places.api.model.AutocompletePrediction

@Composable
fun PlaceInfoBox(
    selectedPlace: PlacesClient.PlaceDetails?,
    addOrDelete: Boolean,
    onAddClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    if (selectedPlace != null) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(2f)
                ) {
                    selectedPlace.name?.let { name ->
                        Text(
                            text = name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    selectedPlace.rating?.let { rating ->
                        Text(
                            text = "Rating: $rating / 5.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    selectedPlace.address?.let { address ->
                        Text(
                            text = address,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    if (selectedPlace.notes != null && selectedPlace.notes != "") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedPlace.notes,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                if (selectedPlace.photo != null) {
                    Image(
                        bitmap = selectedPlace.photo!!.asImageBitmap(),
                        contentDescription = "Photo of activity",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .width(100.dp)
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                if (addOrDelete) {
                    Button(
                        onClick = onAddClick,
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Add", color = Color.White)
                    }
                } else {
//                        Button(
//                            onClick = onDeleteClick,
//                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
//                        ) {
//                            Text("Delete", color = Color.White)
//                        }
                }
            }
        }
    }
}

@Composable
fun SearchNearbyActivities(
    showSearchBar: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    autocompletePredictions: List<AutocompletePrediction>,
    onPredictionClick: (AutocompletePrediction) -> Unit,
    onShowSearchBarChange: (Boolean) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    AnimatedVisibility(
        visible = showSearchBar,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search for nearby activities") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                IconButton(
                    onClick = {
                        onSearchClick()
                        onShowSearchBarChange(false)
                        onSearchQueryChange("")
                        keyboardController?.hide()
                    }
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .padding(8.dp),
                        tint = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(4.dp))
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(autocompletePredictions) { index, prediction ->
                            Text(
                                text = prediction.getPrimaryText(null).toString(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onPredictionClick(prediction)
                                    }
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}