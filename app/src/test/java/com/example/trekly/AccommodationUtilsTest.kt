package com.example.trekly.utils

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import com.example.trekly.util.dateTimeToMillis
import com.example.trekly.util.extractLocalDateFromDatePicker
import com.example.trekly.util.extractLocalTimeFromTimePicker
import com.example.trekly.util.formatAccommodationDate
import com.example.trekly.util.formatAccommodationDates
import com.example.trekly.util.formatActivityTime
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toLocalDateTime
import java.util.Locale

class AccommodationUtilsTest {
    @Test
    fun testFormatAccommodationDate() {
        val date = LocalDate(2023, 4, 1)
        assertEquals("Apr. 1", formatAccommodationDate(date))
    }

    @Test
    fun testFormatAccommodationDates() {
        val fromDate = LocalDate(2023, 4, 1)
        val toDate = LocalDate(2023, 4, 5)
        assertEquals("Apr. 1 - Apr. 5", formatAccommodationDates(fromDate, toDate))
    }

    @Test
    fun testFormatAccommodationTime() {
        val time = LocalDateTime(2023, 4, 1, 13, 30)
        assertEquals("1:30 p.m.", formatActivityTime(time))
    }

    @Test
    fun testDateTimeToMillis() {
        val dateTime = LocalDateTime(2023, 4, 1, 9, 0)
        val expectedMillis = 1680339600000
        // val expectedMillis = dateTime.toInstant(UtcOffset(hours=4)).toEpochMilliseconds()
        assertEquals(expectedMillis, dateTimeToMillis(dateTime))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun testExtractLocalTimeFromTimePicker() {
        val timePickerState = TimePickerState(initialHour = 9, initialMinute = 30, is24Hour = false)
        val expectedTime = LocalTime(9, 30)
        assertEquals(expectedTime, extractLocalTimeFromTimePicker(timePickerState))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun testExtractLocalDateFromDatePicker() {
        val selectedDateMillis = Instant.parse("2023-04-01T00:00:00Z").toEpochMilliseconds()
        val datePickerState = DatePickerState(initialSelectedDateMillis = selectedDateMillis, locale = Locale.getDefault())
        val expectedDate = Instant.fromEpochMilliseconds(selectedDateMillis)
            .toLocalDateTime(TimeZone.UTC)
            .date
        assertEquals(expectedDate, extractLocalDateFromDatePicker(datePickerState))
    }

}