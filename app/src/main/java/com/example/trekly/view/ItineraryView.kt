package com.example.trekly.view

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.trekly.R
import com.example.trekly.composeable.AccommodationCard
import com.example.trekly.composeable.ActivityCard
import com.example.trekly.composeable.ActivityDialog
import com.example.trekly.composeable.Spinner
import com.example.trekly.composeable.TransportationCard
import com.example.trekly.model.Accommodation
import com.example.trekly.model.Activity
import com.example.trekly.model.Itinerary
import com.example.trekly.model.Place
import com.example.trekly.model.Transportation
import com.example.trekly.util.ViewState
import com.example.trekly.util.dateToMillis
import com.example.trekly.util.formatActivityDate
import com.example.trekly.util.formatItineraryDates
import com.example.trekly.viewmodel.ItineraryMapViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItineraryView(
    viewModel: ItineraryMapViewModel,
    itineraryId: Int,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    // View model states
    val itineraryState by viewModel.itinerary.collectAsState()
    val accommodationState by viewModel.accommodation.collectAsState()
    val activitiesState by viewModel.activitiesByDate.collectAsState(
        initial = ViewState.Loading(
            null
        )
    )
    val transportationState by viewModel.transportation.collectAsState()
    val placePhotos by viewModel.placePhotos.collectAsState()
    // View modifier constants
    val paddingModifier = Modifier.padding(start = 16.dp, end = 16.dp)
    // Composable states
    var initialDate by remember {
        mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    }
    var showAddActivityDialog by remember { mutableStateOf(false) }

    val openAddActivityDialog: (LocalDate) -> Unit = {
        initialDate = it
        showAddActivityDialog = true
    }

    if (showAddActivityDialog) {
        when (val state = itineraryState) {
            is ViewState.Success -> {
                val itinerary: Itinerary = state.value
                ActivityDialog(
                    // Blank activity with the correct time
                    activity = Activity(
                        itineraryId = itineraryId,
                        placeId = 0,
                        fromTime = LocalDateTime(date = initialDate, time = LocalTime(0, 0)),
                        toTime = LocalDateTime(date = initialDate, time = LocalTime(0, 0)),
                        place = Place(name = "", lat = 0.0, lng = 0.0, googleMapsPlaceId = ""),
                    ),
                    confirmLabel = "Add",
                    onDismissRequest = { showAddActivityDialog = false },
                    onConfirmation = { showAddActivityDialog = false },
                    mutateActivity = viewModel.addActivity,
                    keyboardController = keyboardController,
                    fromUTCTimeMillis = dateToMillis(itinerary.fromDate),
                    toUTCTimeMillis = dateToMillis(itinerary.toDate),
                )
            }

            is ViewState.Loading -> {}
            is ViewState.Error -> {}
        }
    }

    when (val state = itineraryState) {
        is ViewState.Loading -> {
            Spinner("Loading itinerary...")
        }

        is ViewState.Success -> {
            val itinerary = state.value
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    ItineraryHeaderImage(itinerary)
                }
                item {
                    ItineraryHeader(itinerary)
                }

                // Accommodation
                item {
                    ItinerarySectionHeader(
                        "Accommodations",
                        "Accommodations section",
                        R.drawable.baseline_hotel_24,
                        paddingModifier,
                    )
                }
                when (accommodationState) {
                    is ViewState.Loading -> {
                        item {
                            Spinner("Loading accommodations")
                        }
                    }

                    is ViewState.Success -> {
                        val accommodation =
                            (accommodationState as ViewState.Success<List<Accommodation>>).value
                        if (accommodation.isEmpty()) {
                            item {
                                Text(text = "No accommodations so far", modifier = paddingModifier)
                            }
                        } else {
                            items(accommodation) {
                                AccommodationCard(
                                    accommodation = it,
                                    updateAccommodation = viewModel.updateAccommodation,
                                    deleteAccommodation = viewModel.deleteAccommodation,
                                    modifier = paddingModifier,
                                )
                            }
                        }
                    }

                    is ViewState.Error -> {
                        val error =
                            (accommodationState as ViewState.Error<List<Accommodation>>).exception
                        item {
                            Text(
                                text = "Failed to fetch accommodations",
                                modifier = paddingModifier
                            )
                            Text(text = error.message ?: "", modifier = paddingModifier)
                        }
                    }
                }

                // Transportation
                item {
                    ItinerarySectionHeader(
                        "Transportation",
                        "Transportation section",
                        R.drawable.baseline_flight_24,
                        paddingModifier,
                    )
                }
                when (transportationState) {
                    is ViewState.Loading -> {
                        item {
                            Spinner("Loading activities")
                        }
                    }

                    is ViewState.Success -> {
                        val transportation =
                            (transportationState as ViewState.Success<List<Transportation>>).value
                        if (transportation.isEmpty()) {
                            item {
                                Text(text = "No transportation so far", modifier = paddingModifier)
                            }
                        } else {
                            items(transportation) {
                                TransportationCard(
                                    transportation = it,
                                    updateTransportation = viewModel.updateTransportation,
                                    deleteTransportation = viewModel.deleteTransportation,
                                    modifier = paddingModifier,
                                )
                            }
                        }
                    }

                    is ViewState.Error -> {
                        val error =
                            (transportationState as ViewState.Error<List<Transportation>>).exception
                        item {
                            Text(
                                text = "Failed to fetch transportations",
                                modifier = paddingModifier
                            )
                            Text(text = error.message ?: "", modifier = paddingModifier)
                        }
                    }
                }

                // Activities
                item {
                    ItinerarySectionHeader(
                        "Activities",
                        "Activities section",
                        R.drawable.baseline_hiking_24,
                        modifier = paddingModifier,
                    )
                }
                when (activitiesState) {
                    is ViewState.Loading -> {
                        item {
                            Spinner("Loading activities")
                        }
                    }

                    is ViewState.Success -> {
                        val activitiesByDate =
                            (activitiesState as ViewState.Success<out Map<LocalDate, List<Activity>>>).value

                        // Find the latest date in the activities
                        val latestActivityDate = activitiesByDate.keys.maxOrNull()

                        // Extend toDate if it's earlier than the latest activity date
                        val extendedToDate = if (latestActivityDate != null && latestActivityDate > itinerary.toDate) {
                            latestActivityDate
                        } else {
                            itinerary.toDate
                        }

                        var currentDate = itinerary.fromDate
                        while (currentDate <= extendedToDate) {
                            println("currentDate: $currentDate, toDate: ${itinerary.toDate}")
                            // These must be outside of LazyItemScope for the closures to work.
                            val header = formatActivityDate(currentDate)
                            // Must do this to explicitly capture the value of currentDate in the closure.
                            val onAddClickCreator: (LocalDate) -> () -> Unit = { date ->
                                {
                                    openAddActivityDialog(date)
                                }
                            }
                            val onAddClick = onAddClickCreator(currentDate)

                            stickyHeader {
                                ItineraryActivityHeader(
                                    header = header,
                                    onAddClick = onAddClick,
                                )
                            }
                            val activities = activitiesByDate[currentDate]
                            if (activities != null) {
                                items(activities, key = { it.id }) { activity ->
                                    ActivityCard(
                                        activity = activity,
                                        fromUTCTimeMillis = dateToMillis(itinerary.fromDate),
                                        toUTCTimeMillis = dateToMillis(itinerary.toDate),
                                        photo = placePhotos[activity.id],
                                        updateActivity = viewModel.updateActivity,
                                        deleteActivity = viewModel.deleteActivity,
                                        modifier = paddingModifier,
                                    )
                                }
                            } else {
                                // Render the empty state for this date
                                item {
                                    Text(
                                        text = "Nothing planned for this date yet",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                            currentDate = currentDate.plus(1, DateTimeUnit.DAY)
                        }
                    }

                    is ViewState.Error -> {
                        val error = (activitiesState as ViewState.Error<*>).exception
                        item {
                            Text(text = "Failed to fetch activities", modifier = paddingModifier)
                            Text(text = error.message ?: "", modifier = paddingModifier)
                        }
                    }
                }

                // Extra padding at the bottom to make it look nice.
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        is ViewState.Error -> {
            val error = state.exception
            Text(text = "Failed to fetch itinerary details", modifier = paddingModifier)
            Text(text = error.message ?: "", modifier = paddingModifier)
        }
    }
}

@Composable
fun ItineraryHeaderImage(itinerary: Itinerary) {
    AsyncImage(
        model = itinerary.thumbnail,
        contentDescription = "Image for itinerary",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    )
}

@Composable
fun ItineraryHeader(itinerary: Itinerary) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .offset(y = (-16).dp) // Counteracts spacing on parent LazyColumn
            .clip(shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Text(
            itinerary.destination,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            formatItineraryDates(itinerary.fromDate, itinerary.toDate),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun ItinerarySectionHeader(
    text: String,
    contentDescription: String,
    @DrawableRes id: Int,
    modifier: Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(id),
            contentDescription,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(text, fontSize = 20.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ItineraryActivityHeader(header: String, onAddClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = header,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
        )
        Button(
            onClick = onAddClick,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
            colors = buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.secondary,
                disabledContainerColor = Color.Transparent,
            )
        ) {
            Text(
                text = "+",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 0.dp, end = 0.dp)
            )
        }
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}