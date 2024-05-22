package com.example.trekly.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transportation (
    val id: Int = 0,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("itinerary_id") val itineraryId: Int, // FK to Itinerary
    val time: LocalDateTime,
    @SerialName("booking_reference") val bookingReference: String?,
    val number: String?, // Flight number, bus number etc.
    val type: String,
    val notes: String,
    @SerialName("from_activity_id") val fromActivityId: Int? = null,
    @SerialName("to_activity_id") val toActivityId: Int? = null,
    val duration: String? = null,
    val distance: String? = null
)

val transportationTypes = listOf("Drive", "Bicycle", "Walk", "Two_wheeler", "Transit", "Undecided")
