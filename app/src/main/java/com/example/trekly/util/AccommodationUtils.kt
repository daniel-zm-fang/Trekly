package com.example.trekly.util

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

fun formatAccommodationDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d")
    return date.toJavaLocalDate().format(formatter)
}

fun formatAccommodationDates(from: LocalDate, to: LocalDate): String {
    return "${formatAccommodationDate(from)} - ${formatAccommodationDate(to)}"
}

fun formatAccommodationTime(time: LocalTime): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    return time.toJavaLocalTime().format(formatter)
}

fun dateToMillis(date: LocalDate): Long {
    return date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

@OptIn(ExperimentalMaterial3Api::class)
fun extractLocalTimeFromTimePicker(timePickerState: TimePickerState): LocalTime {
    return LocalTime(hour = timePickerState.hour, minute = timePickerState.minute)
}

@OptIn(ExperimentalMaterial3Api::class)
fun extractLocalDateFromDatePicker(datePickerState: DatePickerState): LocalDate {
    return Instant.fromEpochMilliseconds(datePickerState.selectedDateMillis!!).toLocalDateTime(
        TimeZone.UTC
    ).date
}