package com.example.trekly.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.trekly.R
import com.example.trekly.api.PlacesClient
import com.example.trekly.composeable.AddNearbyPlaceDialog
import com.example.trekly.composeable.Spinner
import com.example.trekly.viewmodel.ItineraryMapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MapsView(viewModel: ItineraryMapViewModel, itineraryId: Int) {
    val places by viewModel.places.collectAsState()
    val routes by viewModel.routes.collectAsState()
    val autocompletePredictions by viewModel.autocompletePredictions.collectAsState()

    var isLoading by remember { mutableStateOf(true) }
    var isCameraPositionSet by remember { mutableStateOf(false) }
    val userLocation by produceState<LatLng?>(initialValue = null) {
        val location = viewModel.getCurrentLocation()
        value = location?.let { LatLng(it.latitude, it.longitude) }
        isLoading = location == null
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f)
    }
    var showRouteInfoDialog by remember { mutableStateOf(false) }
    var routeInfoDialogContent by remember { mutableStateOf("") }
    val radius = 50000.0
    var showPlaceInfoBox by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<PlacesClient.PlaceDetails?>(null) }
    var clickedNearbyMarkerIndex by remember { mutableStateOf<Int?>(null) }
    val colorPalette = listOf(Color(0xFF1E88E5), Color(0xFF2E7D32), Color(0xFFC2185B))
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val onShowSearchBarChange: (Boolean) -> Unit = { showValue ->
        showSearchBar = showValue
    }
    val placesMarkers = remember { mutableStateListOf<MarkerState>() }
    var nearbyPlaces by remember { mutableStateOf<List<PlacesClient.PlaceDetails>>(emptyList()) }
    val nearbyPlacesMarkers = remember { mutableStateListOf<MarkerState>() }
    var showAddNearbyPlaceDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showNoResultsDialog by remember { mutableStateOf(false) }
    var addOrDeleteButton by remember { mutableStateOf(false) }
    val showUpdateMapButton = viewModel.showUpdateMapButton

    LaunchedEffect(key1 = itineraryId, key2 = userLocation) {
        viewModel.getPlacesDetailsFromItinerary(itineraryId)
        viewModel.fetchRoutesForPlaces()
        isLoading = false
    }
    LaunchedEffect(places, userLocation) {
        if (places.isNotEmpty()) {
            places.firstOrNull()?.let {
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            it.latLng.latitude,
                            it.latLng.longitude
                        ), 12f
                    )
                )
                isCameraPositionSet = true
            }
        } else {
            userLocation?.let {
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 10f))
                isCameraPositionSet = true
            }
        }
    }
    LaunchedEffect(places) {
        placesMarkers.clear()
        places.forEach { place ->
            placesMarkers.add(MarkerState(position = place.latLng))
        }
    }
    LaunchedEffect(nearbyPlaces) {
        nearbyPlacesMarkers.clear()
        nearbyPlaces.forEach { place ->
            nearbyPlacesMarkers.add(MarkerState(position = place.latLng))
        }
    }

    if (isLoading || !isCameraPositionSet) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Spinner("Loading Map...")
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = {
                    showPlaceInfoBox = false
                    selectedPlace = null
                    clickedNearbyMarkerIndex = null
                    showSearchBar = false
                },
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false
                )
            ) {
                userLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Your Location",
                        snippet = "You are here",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                placesMarkers.forEachIndexed { index, markerState ->
                    Marker(
                        state = markerState,
                        icon = BitmapDescriptorFactory.fromBitmap(
                            createNumberedMarkerBitmap(
                                LocalContext.current,
                                "${index + 1}"
                            )
                        ),
                        onClick = {
                            selectedPlace = places[index]
                            addOrDeleteButton = false
                            showPlaceInfoBox = true
                            true
                        }
                    )
                }

                nearbyPlacesMarkers.forEachIndexed { index, markerState ->
                    Marker(
                        state = markerState,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                        onClick = {
                            clickedNearbyMarkerIndex = index
                            selectedPlace = nearbyPlaces[index]
                            addOrDeleteButton = true
                            showPlaceInfoBox = true
                            true
                        }
                    )
                }

                routes.forEachIndexed { index, route ->
                    val isVisible =
                        selectedPlace == null || index == places.indexOf(selectedPlace) || index == places.indexOf(
                            selectedPlace
                        ) - 1
                    Polyline(
                        points = route.polyline,
                        color = colorPalette[index % colorPalette.size],
                        width = 8f,
                        pattern = listOf(Dash(30f), Gap(20f)),
                        geodesic = true,
                        jointType = JointType.ROUND,
                        clickable = true,
                        visible = isVisible,
                        onClick = {
                            routeInfoDialogContent = "Distance: ${route.distance}\n" +
                                    "Duration: ${route.duration}"
                            showRouteInfoDialog = true
                        }
                    )
                }

                if (showRouteInfoDialog) {
                    AlertDialog(
                        onDismissRequest = { showRouteInfoDialog = false },
                        title = { Text(text = "Route Info") },
                        text = { Text(text = routeInfoDialogContent) },
                        confirmButton = {
                            Button(
                                onClick = { showRouteInfoDialog = false },
                                content = { Text("Close") }
                            )
                        }
                    )
                }
            }

            if (showAddNearbyPlaceDialog) {
                showPlaceInfoBox = false
                showSearchBar = false
                val selectedNearbyPlace = nearbyPlaces[clickedNearbyMarkerIndex!!]
                AddNearbyPlaceDialog(
                    originalPlaceName = nearbyPlaces[clickedNearbyMarkerIndex!!].name!!,
                    onDismissRequest = { showAddNearbyPlaceDialog = false },
                    onConfirmation = { fromTime, toTime, notes ->
                        viewModel.addNearbyPlace(
                            selectedNearbyPlace,
                            itineraryId,
                            fromTime,
                            toTime,
                            notes
                        )
                        showAddNearbyPlaceDialog = false
                    }
                )
            }

            if (showDeleteConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmationDialog = false },
                    title = { Text(text = "Confirm Deletion") },
                    text = { Text(text = "Are you sure you want to delete this activity from your itinerary?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                // logic for deleting the activity
                                showDeleteConfirmationDialog = false
                            },
                            content = { Text("Delete") }
                        )
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDeleteConfirmationDialog = false },
                            content = { Text("Cancel") }
                        )
                    }
                )
            }

            AnimatedVisibility(
                visible = showUpdateMapButton,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .align(Alignment.BottomCenter),
            ) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onClick = { viewModel.getPlacesDetailsFromItinerary(itineraryId) },
                    icon = { Icon(Icons.Filled.Refresh, "Update map") },
                    text = { Text(text = "Update map") },
                )
            }

            AnimatedVisibility(
                visible = !showPlaceInfoBox,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SearchNearbyActivities(
                    showSearchBar = true,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { newQuery ->
                        searchQuery = newQuery
                        val searchLocation = cameraPositionState.position.target
                        viewModel.getAutocompletePredictions(newQuery, searchLocation, radius)
                    },
                    onSearchClick = {
                        viewModel.searchNearbyActivities(
                            query = searchQuery,
                            location = cameraPositionState.position.target,
                            radius = radius
                        ) { places ->
                            if (places != null) {
                                if (places.isEmpty()) {
                                    showNoResultsDialog = true // Show dialog if no results
                                } else {
                                    nearbyPlaces = places
                                }
                            }
                            onShowSearchBarChange(false) // Hide the search bar after completing the search
                        }
                    },

                    autocompletePredictions = autocompletePredictions,
                    onPredictionClick = { prediction ->
                        searchQuery = prediction.getPrimaryText(null).toString()
                    },
                    onShowSearchBarChange = onShowSearchBarChange
                )
            }

            AnimatedVisibility(
                visible = showPlaceInfoBox,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                ) {
                    PlaceInfoBox(
                        selectedPlace = selectedPlace,
                        addOrDelete = addOrDeleteButton,
                        onAddClick = {
                            showAddNearbyPlaceDialog = true
                        },
                        onDeleteClick = {
                            showDeleteConfirmationDialog = true
                        }
                    )
                }
            }

            if (showNoResultsDialog) {
                AlertDialog(
                    onDismissRequest = { showNoResultsDialog = false },
                    title = { Text(text = "No Results Found") },
                    text = { Text(text = "No nearby activities were found for your search. Please try a different search term or location.") },
                    confirmButton = {
                        Button(
                            onClick = { showNoResultsDialog = false },
                            content = { Text("OK") }
                        )
                    }
                )
            }
        }
    }
}

fun createNumberedMarkerBitmap(context: Context, number: String): Bitmap {
    val drawable =
        ContextCompat.getDrawable(context, R.drawable.map_marker) ?: return Bitmap.createBitmap(
            1,
            1,
            Bitmap.Config.ARGB_8888
        )
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)

    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.draw(canvas)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.textSize = 40f
    paint.color = android.graphics.Color.WHITE
    paint.style = Paint.Style.FILL_AND_STROKE
    paint.strokeWidth = 4f
    paint.textAlign = Paint.Align.CENTER

    val x = bitmap.width / 2f
    val y = (bitmap.height / 2f) - ((paint.descent() + paint.ascent()) / 2f) - 5f
    canvas.drawText(number, x, y, paint)

    return bitmap
}