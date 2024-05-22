package com.example.trekly.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Accommodation (
    val id: Int = 0,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("itinerary_id") val itineraryId: Int, // FK to Itinerary
    @SerialName("from_date") val fromDate: LocalDate,
    @SerialName("to_date") val toDate: LocalDate,
    @SerialName("check_in") val checkIn: LocalTime,
    @SerialName("check_out") val checkOut: LocalTime,
    @SerialName("place_id") val placeId: Int, // FK to Place
    val notes: String,
    // Local usage
    val place: Place? = null,
)