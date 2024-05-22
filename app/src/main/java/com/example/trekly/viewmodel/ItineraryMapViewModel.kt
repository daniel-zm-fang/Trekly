package com.example.trekly.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trekly.TreklyApplication
import com.example.trekly.api.PlacesClient
import com.example.trekly.api.RouteInfo
import com.example.trekly.api.getOptimalRoute
import com.example.trekly.model.Accommodation
import com.example.trekly.model.Activity
import com.example.trekly.model.Itinerary
import com.example.trekly.model.Place
import com.example.trekly.model.Transportation
import com.example.trekly.util.SupabaseManager
import com.example.trekly.util.ViewState
import com.example.trekly.util.groupActivityByDate
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.model.AutocompletePrediction
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ItineraryMapViewModel(application: Application) : AndroidViewModel(application) {
    private val supabase: SupabaseManager = getApplication<TreklyApplication>().supabaseManager
    private val placesClient = getApplication<TreklyApplication>().placesClient
    private val fusedLocationProviderClient =
        getApplication<TreklyApplication>().fusedLocationProviderClient

    // Itinerary data
    private val _itinerary = MutableStateFlow<ViewState<Itinerary>>(ViewState.Loading(null))
    val itinerary: StateFlow<ViewState<Itinerary>> = _itinerary

    // Accommodation data
    private val _accommodation =
        MutableStateFlow<ViewState<List<Accommodation>>>(ViewState.Loading(null))
    val accommodation: StateFlow<ViewState<List<Accommodation>>> = _accommodation

    // Transportation data
    private val _transportation =
        MutableStateFlow<ViewState<List<Transportation>>>(ViewState.Loading(null))
    val transportation: StateFlow<ViewState<List<Transportation>>> = _transportation

    // Activity data
    private val _activities =
        MutableStateFlow<ViewState<List<Activity>>>(ViewState.Loading(null))
    private val _placePhotos = MutableStateFlow<Map<Int, Bitmap>>(emptyMap())
    val activitiesByDate: Flow<ViewState<Map<LocalDate, List<Activity>>>> =
        _activities.map { state ->
            when (state) {
                is ViewState.Success -> ViewState.Success(groupActivityByDate(state.value))
                is ViewState.Loading -> ViewState.Loading(null)
                is ViewState.Error -> ViewState.Error(state.exception)
            }
        }
    val placePhotos: StateFlow<Map<Int, Bitmap>> = _placePhotos

    // Map data
    private val _places = MutableStateFlow<List<PlacesClient.PlaceDetails>>(emptyList())
    val places: StateFlow<List<PlacesClient.PlaceDetails>> = _places
    private val _routes = MutableStateFlow<List<RouteInfo>>(emptyList())
    val routes: StateFlow<List<RouteInfo>> = _routes
    private val _autocompletePredictions =
        MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val autocompletePredictions = _autocompletePredictions.asStateFlow()
    var showUpdateMapButton by mutableStateOf(false)

    // Initialize data
    fun initializeData(itineraryId: Int) {
        getItinerary(itineraryId)
        getAccommodation(itineraryId)
        getTransportation(itineraryId)
        getActivities(itineraryId)
        getPlacesDetailsFromItinerary(itineraryId)
        fetchRoutesForPlaces()
    }

    // ItineraryView functions
    fun getItinerary(itineraryId: Int) {
        viewModelScope.launch {
            try {
                val result: Itinerary = supabase.client.from("itinerary").select {
                    filter {
                        eq("id", itineraryId)
                    }
                }.decodeSingle<Itinerary>()
                _itinerary.value = ViewState.Success(result)
            } catch (e: Exception) {
                println("getItinerary error: ${e.message}")
                _itinerary.value = ViewState.Error(e)
            }
        }
    }

    fun getAccommodation(itineraryId: Int) {
        viewModelScope.launch {
            try {
                // Join on place_id to get columns from the Place table
                val columns = Columns.raw(
                    """*,place(*)""".trimIndent()
                )
                val result = supabase.client.from("accommodation").select(columns = columns) {
                    filter {
                        eq("itinerary_id", itineraryId)
                    }
                }.decodeList<Accommodation>()
                // Fetch address of the Place
                result.forEach {
                    if (it.place != null) {
                        it.place.let { place ->
                            place.address = fetchPlaceAddress(it.place.googleMapsPlaceId)
                        }
                    }
                }
                _accommodation.value = ViewState.Success(result)
            } catch (e: Exception) {
                println(e.message)
                _accommodation.value = ViewState.Error(e)
            }
        }
    }

    fun getTransportation(itineraryId: Int) {
        viewModelScope.launch {
            try {
                val result = supabase.client.from("transportation").select {
                    filter {
                        eq("itinerary_id", itineraryId)
                    }
                }.decodeList<Transportation>()
                _transportation.value = ViewState.Success(result)
            } catch (e: Exception) {
                println(e.message)
                _transportation.value = ViewState.Error(e)
            }
        }
    }

    fun getActivities(itineraryId: Int) {
        viewModelScope.launch {
            try {
                // Join on place_id to get columns from the Place table
                val columns = Columns.raw(
                    """*,place(*)""".trimIndent()
                )
                val result = supabase.client.from("activity").select(columns = columns) {
                    filter {
                        eq("itinerary_id", itineraryId)
                    }
                    order(column = "from_time", order = Order.ASCENDING)
                }.decodeList<Activity>()
                // Fetch address of the Place
                val placePhotos = mutableMapOf<Int, Bitmap>()
                result.forEach {
                    if (it.place != null) {
                        val photo = fetchPlacePhoto(it.place.googleMapsPlaceId)
                        if (photo != null) {
                            placePhotos[it.id] = photo
                        }
                    }
                }
                _placePhotos.value = placePhotos
                _activities.value = ViewState.Success(result)
            } catch (e: Exception) {
                println(e.message)
                _activities.value = ViewState.Error(e)
            }
        }
    }

    val updateAccommodation: ((accommodation: Accommodation) -> Unit) = { accommodation ->
        viewModelScope.launch {
            try {
                // Update local state
                _accommodation.update { currentState ->
                    when (currentState) {
                        is ViewState.Success -> {
                            val updatedAccommodation =
                                currentState.value.map { existingAccommodation ->
                                    if (existingAccommodation.id == accommodation.id) accommodation else existingAccommodation
                                }
                            ViewState.Success(updatedAccommodation)
                        }

                        else -> currentState // Preserve loading or error state
                    }
                }
                // TODO: integrate Place updates from Accommodation
                // Update supabase database
                supabase.client.from("accommodation").update({
                    Accommodation::fromDate setTo accommodation.fromDate
                    Accommodation::toDate setTo accommodation.toDate
                    Accommodation::checkIn setTo accommodation.checkIn
                    Accommodation::checkOut setTo accommodation.checkOut
                    Accommodation::notes setTo accommodation.notes
                }) {
                    filter {
                        eq("id", accommodation.id)
                    }
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    val deleteAccommodation: ((Int) -> Unit) = { id ->
        viewModelScope.launch {
            try {
                // Delete locally
                _accommodation.update { currentState ->
                    when (currentState) {
                        is ViewState.Success -> {
                            // Filter out the accommodation with the given ID
                            val updatedAccommodation =
                                currentState.value.filterNot { it.id == id }
                            ViewState.Success(updatedAccommodation)
                        }

                        else -> currentState // Preserve loading or error state
                    }
                }
                // Delete from supabase
                supabase.client.from("accommodation").delete {
                    filter {
                        eq("id", id)
                    }
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    val addActivity: ((activity: Activity) -> Unit) = { activity ->
        viewModelScope.launch {
            try {
                // Update supabase
                val details = withContext(Dispatchers.IO) {
                    val detailsDeferred =
                        CompletableDeferred<PlacesClient.PlaceDetails?>()
                    placesClient.getPlaceDetailsFromPlaceId(activity.place!!.googleMapsPlaceId) { details ->
                        detailsDeferred.complete(details)
                    }
                    detailsDeferred.await()
                }
                // Create a new Activity object with place details
                var newActivity = activity.copy(
                    place = activity.place!!.copy(
                        lat = details?.latLng?.latitude ?: 0.0,
                        lng = details?.latLng?.longitude ?: 0.0
                    )
                )
                // Create a new Place entry in supabase
                val newPlaceId = supabase.addPlace(
                    name = newActivity.place!!.name,
                    googleMapsPlaceId = newActivity.place!!.googleMapsPlaceId,
                    lat = newActivity.place!!.lat,
                    lng = newActivity.place!!.lng,
                )
                // Update the foreign keys on the Activity
                newActivity = newActivity.copy(
                    placeId = newPlaceId,
                    place = newActivity.place!!.copy(id = newPlaceId)
                )
                val newActivityId = supabase.addActivity(
                    itineraryId = activity.itineraryId,
                    fromTime = activity.fromTime,
                    toTime = activity.toTime,
                    notes = activity.notes,
                    placeId = newPlaceId,
                )
                // Update the Activity's id
                newActivity = newActivity.copy(
                    id = newActivityId
                )
                // Update local state
                _activities.update { currentState ->
                    when (currentState) {
                        is ViewState.Success -> {
                            val updatedActivities = currentState.value.toMutableList().apply {
                                add(newActivity)
                            }
                            ViewState.Success(updatedActivities.toList())
                        }

                        else -> currentState // Preserve loading or error state
                    }
                }
                // Fetch a photo for this new Activity
                val photo = fetchPlacePhoto(newActivity.place!!.googleMapsPlaceId)
                if (photo != null) {
                    val updatedPlacePhotos = _placePhotos.value.toMutableMap()
                    updatedPlacePhotos[newActivity.id] = photo
                    _placePhotos.value = updatedPlacePhotos.toMap()
                }
                // Show a button so user can update the map view
                showUpdateMapButton = true
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    val updateActivity: ((activity: Activity) -> Unit) = { activity ->
        viewModelScope.launch {
            try {
                var isPlaceUpdated = false
                // Update local state
                _activities.update { currentState ->
                    when (currentState) {
                        is ViewState.Success -> {
                            val updatedActivities = currentState.value.map { existingActivity ->
                                if (existingActivity.id == activity.id) {
                                    isPlaceUpdated =
                                        existingActivity.place?.googleMapsPlaceId != activity.place?.googleMapsPlaceId
                                    activity
                                } else {
                                    existingActivity
                                }
                            }
                            ViewState.Success(updatedActivities)
                        }

                        else -> currentState // Preserve loading or error state
                    }
                }
                if (isPlaceUpdated && activity.place != null) {
                    placesClient.getPlaceDetailsFromPlaceId(activity.place.googleMapsPlaceId) { details ->
                        viewModelScope.launch {
                            supabase.client.from("place").update({
                                Place::name setTo activity.place.name
                                Place::lat setTo details?.latLng?.latitude
                                Place::lng setTo details?.latLng?.longitude
                                Place::googleMapsPlaceId setTo details?.id
                            }) {
                                filter {
                                    eq("id", activity.place.id)
                                }
                            }
                        }
                    }
                    // Fetch a photo for this new Activity
                    val photo = fetchPlacePhoto(activity.place.googleMapsPlaceId)
                    if (photo != null) {
                        val updatedPlacePhotos = _placePhotos.value.toMutableMap()
                        updatedPlacePhotos[activity.id] = photo
                        _placePhotos.value = updatedPlacePhotos.toMap()
                    }
                }
                // Update supabase database
                supabase.client.from("activity").update({
                    Activity::fromTime setTo activity.fromTime
                    Activity::toTime setTo activity.toTime
                    Activity::notes setTo activity.notes
                }) {
                    filter {
                        eq("id", activity.id)
                    }
                }
                // Show a button so user can update the map view
                showUpdateMapButton = true
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    val deleteActivity: ((activityId: Int) -> Unit) = { id ->
        viewModelScope.launch {
            try {
                // Delete locally
                _activities.update { currentState ->
                    when (currentState) {
                        is ViewState.Success -> {
                            // Filter out the activity with the given ID
                            val updatedActivities =
                                currentState.value.filterNot { it.id == id }
                            ViewState.Success(updatedActivities)
                        }

                        else -> currentState // Preserve loading or error state
                    }
                }
                // Delete from supabase
                supabase.client.from("activity").delete {
                    filter {
                        eq("id", id)
                    }
                }
                // Show a button so user can update the map view
                showUpdateMapButton = true
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    val updateTransportation: ((transportation: Transportation) -> Unit) = {
        viewModelScope.launch {
            try {
                supabase.client.from("transportation").update({
                    Transportation::time setTo it.time
                    Transportation::number setTo it.number
                    Transportation::type setTo it.type
                    Transportation::notes setTo it.notes
                }) {
                    filter {
                        eq("id", it.id)
                    }
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    val deleteTransportation: ((transportationId: Int) -> Unit) = { id ->
        viewModelScope.launch {
            try {
                // Delete locally
                _transportation.update { currentState ->
                    when (currentState) {
                        is ViewState.Success -> {
                            // Filter out the transportation with the given ID
                            val updatedTransportation =
                                currentState.value.filterNot { it.id == id }
                            ViewState.Success(updatedTransportation)
                        }

                        else -> currentState // Preserve loading or error state
                    }
                }
                // Delete from supabase
                supabase.client.from("transportation").delete {
                    filter {
                        eq("id", id)
                    }
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    // TODO: make this a common function
    suspend fun fetchPlacePhoto(placeId: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                println("Fetching place photo!")
                placesClient.getPlacePhoto(placeId, 200, 200) { bitmap ->
                    continuation.resume(bitmap)
                }
            }
        }
    }

    private suspend fun fetchPlaceAddress(placeId: String): String? {
        return suspendCoroutine { continuation ->
            placesClient.getPlaceAddress(placeId) { address ->
                continuation.resume(
                    address
                )
            }
        }
    }

    // MapView functions
    fun searchNearbyActivities(
        query: String,
        location: LatLng,
        radius: Double,
        callback: (List<PlacesClient.PlaceDetails>?) -> Unit
    ) {
        placesClient.searchNearbyActivities(query, location, radius, callback)
    }

    suspend fun getCurrentLocation(): Location? {
        val cancellationTokenSource = CancellationTokenSource()

        return try {
            fusedLocationProviderClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()
        } catch (e: SecurityException) {
            println("Security exception: $e")
            null
        } catch (e: Exception) {
            println("Exception: $e")
            null
        }
    }

    fun fetchRoutesForPlaces() {
        viewModelScope.launch {
            val places = _places.value
            val newRoutes = mutableListOf<RouteInfo>()
            for (i in 0 until places.size - 1) {
                val routeInfo = getOptimalRoute(
                    places[i].latLng.latitude,
                    places[i].latLng.longitude,
                    places[i + 1].latLng.latitude,
                    places[i + 1].latLng.longitude,
                    "DRIVING"
                )
                if (routeInfo != null) {
                    newRoutes.add(routeInfo)
                }
            }
            _routes.value = newRoutes
        }
    }

    fun getPlacesDetailsFromItinerary(itineraryId: Int) {
        /*
        Run the following SQL query in Supabase to create the get_places_from_itinerary function:
        CREATE OR REPLACE FUNCTION public.get_places_from_itinerary(itinerary_id INT)
        RETURNS TABLE (
            placeId TEXT,
            name TEXT,
            notes TEXT
        )
        AS $$
        BEGIN
            RETURN QUERY
            SELECT place.google_maps_place_id, place.name, activity.notes
            FROM place
            JOIN activity ON place.id = activity.place_id
            JOIN itinerary ON activity.itinerary_id = itinerary.id
            WHERE itinerary.id = get_places_from_itinerary.itinerary_id
            ORDER BY activity.from_time;
        END;
        $$ LANGUAGE plpgsql;
         */
        viewModelScope.launch {
            showUpdateMapButton = false
            val placesFromItinerary = supabase.client.postgrest.rpc(
                "get_places_from_itinerary",
                mapOf("itinerary_id" to itineraryId)
            ).decodeList<DataFromPlaceTable>()
            _places.value = placesFromItinerary.mapNotNull { placeFromItinerary ->
                getPlaceDetailsAsync(
                    placeFromItinerary.placeId,
                    placeFromItinerary.name,
                    placeFromItinerary.notes ?: ""
                )
            }
        }
    }

    private suspend fun getPlaceDetailsAsync(
        placeId: String,
        name: String,
        notes: String
    ): PlacesClient.PlaceDetails? {
        return suspendCoroutine { continuation ->
            placesClient.getPlaceDetailsFromPlaceId(placeId) { placeDetails ->
                continuation.resume(
                    // copy from name if placeDetails.name is nullYea
                    placeDetails?.copy(notes = notes, name = placeDetails.name ?: name)
                )
            }
        }
    }

    fun getAutocompletePredictions(query: String, location: LatLng, radius: Double) {
        viewModelScope.launch {
            try {
                val predictions = placesClient.getAddressPredictions(
                    inputString = query,
                    location = location,
                    radius = radius
                )
                _autocompletePredictions.value = predictions
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error getting autocomplete predictions", e)
            }
        }
    }

    fun addNearbyPlace(
        selectedNearbyPlace: PlacesClient.PlaceDetails,
        itineraryId: Int,
        fromTime: String,
        toTime: String,
        notes: String
    ) {
        viewModelScope.launch {
            val placeIdInserted = supabase.addPlace(
                selectedNearbyPlace.name!!,
                selectedNearbyPlace.latLng.latitude,
                selectedNearbyPlace.latLng.longitude,
                selectedNearbyPlace.id
            )
            supabase.addActivity(
                itineraryId,
                LocalDateTime.parse(fromTime),
                LocalDateTime.parse(toTime),
                placeIdInserted,
                notes,
            )

            // Update the places list for the map
            val updatedPlaces = _places.value.toMutableList()
            updatedPlaces.add(selectedNearbyPlace)
            _places.value = updatedPlaces

            // Refresh the activities data for the itinerary view
            getActivities(itineraryId)
        }
    }

    @Serializable
    private data class DataFromPlaceTable(
        @SerialName("placeid") val placeId: String,
        val name: String,
        val notes: String? = null
    )
}