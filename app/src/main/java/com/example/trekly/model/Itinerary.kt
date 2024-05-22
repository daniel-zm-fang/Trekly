package com.example.trekly.model

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Itinerary(
    val id: Int = 0,
    @SerialName("created_at") val createdAt: Instant = Clock.System.now(),
    var name: String = "",
    var destination: String = "",
    @SerialName("from_date") var fromDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    @SerialName("to_date") var toDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(
        DatePeriod(0, 0, 1)
    ),
    @SerialName("share_code") val shareCode: String? = null,
    val owner: String? = null, // FK to users table
    @SerialName("is_public") var isPublic: Boolean = false,
    var thumbnail: String? = null,
)