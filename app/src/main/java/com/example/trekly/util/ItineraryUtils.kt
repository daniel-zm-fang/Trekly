package com.example.trekly.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

fun generateShareCode(length: Int = 5): String {
    val universe = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length).map { universe.random() }.joinToString("")
}

fun formatItineraryDuration(fromDate: LocalDate, toDate: LocalDate): String {
    val duration = fromDate.daysUntil(toDate) + 1
    return if (duration > 1) "$duration days" else "$duration day"
}

fun formatItineraryDates(fromDate: LocalDate, toDate: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d")
    return "${fromDate.toJavaLocalDate().format(formatter)} - ${
        toDate.toJavaLocalDate().format(formatter)
    }"
}