package com.example.trekly.util

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import com.example.trekly.model.Activity
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.Duration
import java.time.format.DateTimeFormatter

fun formatActivityTime(time: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    return time.toJavaLocalDateTime().format(formatter)
}

fun formatActivityTimes(fromTime: LocalDateTime, toTime: LocalDateTime): String {
    return "${formatActivityTime(fromTime)} - ${formatActivityTime(toTime)}"
}

fun formatActivityDuration(fromTime: LocalDateTime, toTime: LocalDateTime): String {
    val duration =
        Duration.between(fromTime.toJavaLocalDateTime(), toTime.toJavaLocalDateTime()).toHours()
    return "${duration}h"
}

fun formatActivityDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("E, MMM d")
    return date.toJavaLocalDate().format(formatter)
}

fun groupActivityByDate(activities: List<Activity>): Map<LocalDate, List<Activity>> {
    return activities.sortedBy {
        it.fromTime
    }.groupBy {
        it.fromTime.date
    }
}

fun dateTimeToMillis(dateTime: LocalDateTime): Long {
    return dateTime.toInstant(TimeZone.UTC).toEpochMilliseconds()
}

// Combine date component of baseDate with time component of the time picker state.
@OptIn(ExperimentalMaterial3Api::class)
fun extractTimePickerState(baseDate: LocalDateTime, state: TimePickerState): LocalDateTime {
    return LocalDateTime(
        year = baseDate.year,
        monthNumber = baseDate.monthNumber,
        dayOfMonth = baseDate.dayOfMonth,
        hour = state.hour,
        minute = state.minute,
        second = 0,
        nanosecond = 0
    )
}

// Combine time component of baseDate with date component of selectedDateMillis.
fun extractDatePickerState(baseDate: LocalDateTime, selectedDateMillis: Long): LocalDateTime {
    val millisDate = Instant.fromEpochMilliseconds(selectedDateMillis).toLocalDateTime(TimeZone.UTC)
    return LocalDateTime(
        year = millisDate.year,
        monthNumber = millisDate.monthNumber,
        dayOfMonth = millisDate.dayOfMonth,
        hour = baseDate.hour,
        minute = baseDate.minute,
        second = baseDate.second,
        nanosecond = baseDate.nanosecond
    )
}