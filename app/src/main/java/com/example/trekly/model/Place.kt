package com.example.trekly.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Place(
    val id: Int = 0,
    @SerialName("created_at") val createdAt: Instant = Clock.System.now(),
    val name: String,
    val lat: Double,
    val lng: Double,
    @SerialName("google_maps_place_id") val googleMapsPlaceId: String,
    // Local usage
    var address: String? = null,
)