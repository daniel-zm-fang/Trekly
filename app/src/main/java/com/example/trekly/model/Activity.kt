package com.example.trekly.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Activity (
    val id: Int = 0,
    @SerialName("created_at") val createdAt: Instant = Clock.System.now(),
    @SerialName("itinerary_id") val itineraryId: Int, // FK to Itinerary
    @SerialName("from_time") val fromTime: LocalDateTime,
    @SerialName("to_time") val toTime: LocalDateTime,
    @SerialName("place_id") val placeId: Int, // FK to Place
    val notes: String = "",
    // Local usage
    val place: Place? = null,
)