package com.example.trekly.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.core.util.Pair
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aallam.openai.client.OpenAI
import com.example.trekly.TreklyApplication
import com.example.trekly.api.PlacesClient
import com.example.trekly.api.getOptimalRoute
import com.example.trekly.api.recommendItinerary
import com.example.trekly.model.Itinerary
import com.example.trekly.util.SupabaseManager
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime


class NewItineraryViewModel(application: Application) : AndroidViewModel(application) {
    // Clients
    private val gson = Gson()
    private val supabase: SupabaseManager = getApplication<TreklyApplication>().supabaseManager
    private val openAI: OpenAI = getApplication<TreklyApplication>().openAI
    private val placesClient = getApplication<TreklyApplication>().placesClient

    private val dateRangePickerConstraints =
        CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
            .build()
    private val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
        .setTitleText("Select trip dates")
        .setCalendarConstraints(dateRangePickerConstraints)
        .build()

    var itinerary = mutableStateOf(Itinerary())
    private val _recommendationResponse = MutableStateFlow("")
    val recommendationResponse: StateFlow<String> = _recommendationResponse
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _autocompletePredictions =
        MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val autocompletePredictions: StateFlow<List<AutocompletePrediction>> =
        _autocompletePredictions.asStateFlow()
    var selectedTransportationType = mutableStateOf("")

    fun addEmptyItinerary(navigateToNewItinerary: (id: Int) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val itineraryId = supabase.addItinerary(
                    name = itinerary.value.name,
                    destination = itinerary.value.destination,
                    fromDate = itinerary.value.fromDate,
                    toDate = itinerary.value.toDate,
                    isPublic = false,
                )
                itinerary.value = Itinerary()
                _isLoading.value = false
                // Redirect to the newly created Itinerary's screen
                navigateToNewItinerary(itineraryId)
            } catch (e: Exception) {
                println(e.message)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getRecommendations(prompt: String, navigateToNewItinerary: (id: Int) -> Unit) {
        data class ActivityDetails(
            val activityId: Int,
            val latitude: Double,
            val longitude: Double,
            val toTime: LocalDateTime
        )

        viewModelScope.launch {
            _isLoading.value = true
            val response = recommendItinerary(
                openAI = openAI,
                userInput = prompt,
                itineraryName = itinerary.value.name,
                destination = itinerary.value.destination,
                fromDate = itinerary.value.fromDate,
                toDate = itinerary.value.toDate,
                transportationType = selectedTransportationType.value
            )
            if (response.isEmpty() || response[0] != '[') {
                _recommendationResponse.value = "Unable to get recommendations. Please try again."
            } else {
                _recommendationResponse.value = response
                try {
                    val itineraryArray = gson.fromJson(response, JsonArray::class.java)
                    val newItineraryId = supabase.addItinerary(
                        name = itinerary.value.name,
                        destination = itinerary.value.destination,
                        fromDate = itinerary.value.fromDate,
                        toDate = itinerary.value.toDate,
                        isPublic = false
                    )
                    val activityDetails = mutableListOf<ActivityDetails>()
                    val deferredActivityDetails = itineraryArray.map { element ->
                        async {
                            val gptItinerary = element.asJsonObject
                            val placeName = gptItinerary.get("place_name").asString
                            val details = withContext(Dispatchers.IO) {
                                val detailsDeferred =
                                    CompletableDeferred<PlacesClient.PlaceDetails?>()
                                placesClient.getPlaceDetailsFromPlaceName(placeName) { details ->
                                    detailsDeferred.complete(details)
                                }
                                detailsDeferred.await()
                            }
                            val newPlaceId = supabase.addPlace(
                                name = placeName,
                                lat = details?.latLng?.latitude ?: 0.0,
                                lng = details?.latLng?.longitude ?: 0.0,
                                googleMapsPlaceId = details?.id ?: ""
                            )
                            val toTime = LocalDateTime.parse(gptItinerary.get("to_time").asString)
                            val activityId = supabase.addActivity(
                                itineraryId = newItineraryId,
                                fromTime = LocalDateTime.parse(gptItinerary.get("from_time").asString),
                                toTime = toTime,
                                placeId = newPlaceId,
                                notes = gptItinerary.get("description").asString,
                            )
                            ActivityDetails(
                                activityId,
                                details?.latLng?.latitude ?: 0.0,
                                details?.latLng?.longitude ?: 0.0,
                                toTime
                            )
                        }
                    }

                    deferredActivityDetails.awaitAll().forEach {
                        activityDetails.add(it)
                    }

                    // Calculate routes between activities and add transportation
                    for (i in 1 until activityDetails.size) {
                        val fromActivityDetails = activityDetails[i - 1]
                        val toActivityDetails = activityDetails[i]

                        val routeInfo = getOptimalRoute(
                            originLat = fromActivityDetails.latitude,
                            originLng = fromActivityDetails.longitude,
                            destinationLat = toActivityDetails.latitude,
                            destinationLng = toActivityDetails.longitude,
                            mode = selectedTransportationType.value.uppercase()
                        )

                        if (routeInfo != null) {
                            val transitStepsString =
                                routeInfo.transitSteps?.joinToString(separator = ", ") { step ->
                                    "${step.mode} towards ${step.headSign} on ${step.transitLine?.name} for ${step.distance} (${step.duration})"
                                } ?: "Direct route"
                            supabase.addTransportation(
                                itineraryId = newItineraryId,
                                time = fromActivityDetails.toTime,
                                type = selectedTransportationType.value,
                                number = null,
                                bookingReference = null,
                                notes = transitStepsString,
                                fromActivity = fromActivityDetails.activityId,
                                toActivity = toActivityDetails.activityId,
                                duration = routeInfo.duration,
                                distance = routeInfo.distance
                            )
                        }
                    }

                    // Redirect to the newly created Itinerary's screen
                    navigateToNewItinerary(newItineraryId)
                } catch (e: JsonSyntaxException) {
                    _recommendationResponse.value =
                        "Unable to parse recommendations. Please try again."
                }
            }
            _isLoading.value = false
        }
    }

    fun showDatePicker(supportFragmentManager: FragmentManager) {
        dateRangePicker.show(supportFragmentManager, dateRangePicker.toString())
    }

    fun onCompose() {
        dateRangePicker.addOnPositiveButtonClickListener(listener)
    }

    fun onDispose() {
        dateRangePicker.removeOnPositiveButtonClickListener(listener)
    }

    private val listener: ((Pair<Long, Long>) -> Unit) = {
        itinerary.value = itinerary.value.copy(
            fromDate = LocalDate.fromEpochDays((it.first / 1000 / 86400).toInt()),
            toDate = LocalDate.fromEpochDays((it.second / 1000 / 86400).toInt())
        )
    }
}
