package com.example.trekly.util

import android.content.Context
import com.example.trekly.BuildConfig
import com.example.trekly.api.getItineraryImageUrl
import com.example.trekly.model.Activity
import com.example.trekly.model.IdResponse
import com.example.trekly.model.Itinerary
import com.example.trekly.model.Place
import com.example.trekly.model.Profile
import com.example.trekly.model.Transportation
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class SupabaseManager(private val context: Context) {
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Auth)
        }
    }

    // Gets the currently logged in user's id
    private fun getUserId(): String? {
        return client.auth.currentUserOrNull()?.id
    }

    suspend fun addItinerary(
        name: String,
        destination: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        isPublic: Boolean
    ): Int {
        // Generate share code (6 alphanumeric characters)
        val shareCode = generateShareCode()
        // Use the city name to search for images
        val thumbnail = getItineraryImageUrl(destination.substringBefore(','))

        val itinerary = Itinerary(
            name = name,
            destination = destination,
            fromDate = fromDate,
            toDate = toDate,
            shareCode = shareCode,
            owner = getUserId(),
            isPublic = isPublic,
            thumbnail = thumbnail
        )

        val idInserted = withContext(Dispatchers.IO) {
            client.from("itinerary")
                .insert(itinerary) {
                    select(columns = Columns.raw("id"))
                }.decodeSingle<IdResponse>().id
        }
        return idInserted
    }

    suspend fun addActivity(
        itineraryId: Int,
        fromTime: LocalDateTime,
        toTime: LocalDateTime,
        placeId: Int,
        notes: String,

        ): Int {
        val activity = Activity(
            createdAt = Clock.System.now(),
            itineraryId = itineraryId,
            fromTime = fromTime,
            toTime = toTime,
            placeId = placeId,
            notes = notes
        )

        val idInserted = withContext(Dispatchers.IO) {
            client.from("activity").insert(activity) {
                select(columns = Columns.raw("id"))
            }.decodeSingle<IdResponse>().id
        }
        return idInserted
    }

    suspend fun addPlace(
        name: String,
        lat: Double,
        lng: Double,
        googleMapsPlaceId: String
    ): Int {
        val place = Place(
            createdAt = Clock.System.now(),
            name = name,
            lat = lat,
            lng = lng,
            googleMapsPlaceId = googleMapsPlaceId
        )

        val idInserted = withContext(Dispatchers.IO) {
            client.from("place").insert(place) {
                select(columns = Columns.raw("id"))
            }.decodeSingle<IdResponse>().id
        }
        return idInserted
    }

    suspend fun addPreferences(
        travelPace: String,
        languagesSpoken: String,
        countriesToVisit: String,
        travelBudget: String
    ): Int {
        val profile = Profile(
            id = getUserId(),
            travel_pace = travelPace,
            languages_spoken = languagesSpoken,
            countries_to_visit = countriesToVisit,
            travel_budget = travelBudget
        )
        try {
            val idInserted = withContext(Dispatchers.IO) {
                client.from("profiles").insert(profile) {
                    select(columns = Columns.raw("id"))
                }.decodeSingle<IdResponse>().id
            }
            return idInserted
        } catch (e: Exception) {
            println("Error inserting data: ${e.message}")
            e.printStackTrace()
            return -1
        }
    }

        suspend fun addTransportation(
            itineraryId: Int,
            time: LocalDateTime,
            type: String,
            number: String?,
            bookingReference: String?,
            notes: String,
            fromActivity: Int?,
            toActivity: Int?,
            duration: String?,
            distance: String?
        ): Int {
            val transportation = Transportation(
                createdAt = Clock.System.now(),
                itineraryId = itineraryId,
                time = time,
                type = type,
                number = number,
                bookingReference = bookingReference,
                notes = notes,
                fromActivityId = fromActivity,
                toActivityId = toActivity,
                duration = duration,
                distance = distance
            )

            val idInserted = withContext(Dispatchers.IO) {
                client.from("transportation").upsert(transportation, ignoreDuplicates = true) {
                    select(columns = Columns.raw("id"))
                }.decodeSingle<IdResponse>().id
            }
            return idInserted
        }

}