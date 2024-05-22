package com.example.trekly.model
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile (
    @SerialName("id") val id: String? = null, // FK to auth.users
    val first_name: String? = null,
    val last_name: String? = null,
    val travel_pace: String = "",
    val languages_spoken: String = "",
    val countries_to_visit: String = "",
    val travel_budget: String = ""
)
