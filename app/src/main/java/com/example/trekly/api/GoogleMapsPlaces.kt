package com.example.trekly.api

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.maps.android.SphericalUtil
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PlacesClient(context: Context) {
    private val placesClient: PlacesClient = Places.createClient(context)

    data class PlaceDetails(
        val id: String,
        val latLng: LatLng,
        val name: String?,
        val address: String?,
        val rating: Double?,
        var photo: Bitmap?,
        val notes: String? = null
    )

    fun getPlaceDetailsFromPlaceName(placeName: String, callback: (PlaceDetails?) -> Unit) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(placeName)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            val prediction = response.autocompletePredictions.firstOrNull()
            prediction?.let {
                val placeId = it.placeId
                val placeFields = listOf(Place.Field.ID, Place.Field.LAT_LNG)
                val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()

                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener { fetchResponse ->
                    val place = fetchResponse.place
                    val details = PlaceDetails(
                        id = place.id!!,
                        latLng = place.latLng!!,
                        name = null,
                        address = null,
                        rating = null,
                        photo = null
                    )
                    callback(details)
                }.addOnFailureListener {
                    callback(null)
                }
            } ?: run {
                callback(null)
            }
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun getPlaceDetailsFromPlaceId(placeId: String, callback: (PlaceDetails?) -> Unit) {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.LAT_LNG,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.RATING,
            Place.Field.PHOTO_METADATAS
        )
        val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener { fetchResponse ->
            val place = fetchResponse.place
            val photoMetadata = place.photoMetadatas?.firstOrNull()
            if (photoMetadata != null) {
                val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(1000)
                    .setMaxHeight(1000)
                    .build()
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener { photoResponse ->
                    val details = PlaceDetails(
                        id = place.id!!,
                        latLng = place.latLng ?: LatLng(0.0, 0.0),
                        name = place.name ?: "",
                        address = place.address ?: "",
                        rating = place.rating,
                        photo = photoResponse.bitmap
                    )
                    callback(details)
                }.addOnFailureListener {
                    callback(null)
                }
            } else {
                val details = PlaceDetails(
                    id = place.id!!,
                    latLng = place.latLng ?: LatLng(0.0, 0.0),
                    name = place.name ?: "",
                    address = place.address ?: "",
                    rating = place.rating,
                    photo = null
                )
                callback(details)
            }
        }.addOnFailureListener {
            callback(null)
        }
    }

    suspend fun getAddressPredictions(
        sessionToken: AutocompleteSessionToken = AutocompleteSessionToken.newInstance(),
        inputString: String,
        location: LatLng? = null,
        radius: Double? = null
    ) = suspendCoroutine<List<AutocompletePrediction>> { continuation ->
        val requestBuilder = FindAutocompletePredictionsRequest.builder()
            .setOrigin(location)
            .setSessionToken(sessionToken)
            .setQuery(inputString)

        if (location != null && radius != null) {
            requestBuilder.setLocationBias(
                RectangularBounds.newInstance(
                    SphericalUtil.computeOffset(location, radius, 225.0),
                    SphericalUtil.computeOffset(location, radius, 45.0)
                )
            )
        }

        placesClient.findAutocompletePredictions(requestBuilder.build())
            .addOnCompleteListener { completedTask ->
                if (completedTask.exception != null) {
                    Log.e(
                        "PlacesClient",
                        "Error getting address predictions: ${completedTask.exception?.message}"
                    )
                    continuation.resume(listOf())
                } else {
                    continuation.resume(completedTask.result.autocompletePredictions)
                }
            }
    }

    fun searchNearbyActivities(
        query: String,
        location: LatLng,
        radius: Double,
        callback: (List<PlaceDetails>?) -> Unit
    ) {
        // Define the fields to return
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.RATING,
            Place.Field.PHOTO_METADATAS
        )

        // Build the search request
        val searchByTextRequest = SearchByTextRequest.builder(query, placeFields)
            .setMaxResultCount(10) // Set the maximum number of results
            .setLocationBias(
                CircularBounds.newInstance(
                    location,
                    radius
                )
            ) // Restrict to a circular area
            .setRankPreference(SearchByTextRequest.RankPreference.DISTANCE) // Rank by distance
            .build()

        // Perform the search
        placesClient.searchByText(searchByTextRequest)
            .addOnSuccessListener { response ->
                val places = response.places.mapNotNull { place ->
                    val photoMetadata = place.photoMetadatas?.firstOrNull()
                    val details = PlaceDetails(
                        id = place.id ?: "",
                        name = place.name ?: "",
                        address = place.address ?: "",
                        latLng = place.latLng ?: LatLng(0.0, 0.0),
                        rating = place.rating,
                        photo = null // Initialize with null, will be updated if photo is available
                    )
                    if (photoMetadata != null) {
                        val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                            .setMaxWidth(1000) // You can adjust the size as needed
                            .setMaxHeight(1000)
                            .build()
                        placesClient.fetchPhoto(photoRequest)
                            .addOnSuccessListener { photoResponse ->
                                details.photo =
                                    photoResponse.bitmap // Update the photo in the details object
                            }.addOnFailureListener {
                            // Log error or handle photo fetch failure
                            println("Error fetching photo: $it")
                        }
                    }
                    details
                }
                callback(places) // Invoke the callback with the list of places
            }
            .addOnFailureListener { exception ->
                Log.e("PlacesClient", "Error searching for nearby activities: ${exception.message}")
                callback(null) // Invoke the callback with null in case of failure
            }
    }

    fun getPlacePhoto(
        placeId: String,
        width: Int,
        height: Int,
        callback: (Bitmap?) -> Unit
    ) {
        val placeFields = listOf(Place.Field.PHOTO_METADATAS)
        val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener { response ->
            val place = response.place
            val photoMetadatas = place.photoMetadatas
            if (photoMetadatas == null || photoMetadatas.isEmpty()) {
                callback(null)
                return@addOnSuccessListener
            }
            val photoMetadata = photoMetadatas.first()
            val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(width)
                .setMaxHeight(height)
                .build()
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener {
                callback(it.bitmap)
            }.addOnFailureListener {
                callback(null)
            }
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun getPlaceAddress(placeId: String, callback: (String?) -> Unit) {
        val placeFields = listOf(Place.Field.ADDRESS)
        val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener { response ->
            val place = response.place
            callback(place.address)
        }.addOnFailureListener {
            callback(null)
        }
    }
}
