package com.example.trekly.utils

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import com.example.trekly.model.Activity
import com.example.trekly.model.Place
import com.example.trekly.util.dateTimeToMillis
import com.example.trekly.util.extractDatePickerState
import com.example.trekly.util.extractTimePickerState
import com.example.trekly.util.formatActivityDate
import com.example.trekly.util.formatActivityDuration
import com.example.trekly.util.formatActivityTime
import com.example.trekly.util.formatActivityTimes
import com.example.trekly.util.groupActivityByDate
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.TimeZone

class ActivityUtilsTest {

    @Test
    fun testFormatActivityTime() {
        val time = LocalDateTime(2023, 4, 1, 13, 30)
        assertEquals("1:30 p.m.", formatActivityTime(time))
    }

    @Test
    fun testFormatActivityTimes() {
        val fromTime = LocalDateTime(2023, 4, 1, 9, 0)
        val toTime = LocalDateTime(2023, 4, 1, 17, 30)
        assertEquals("9:00 a.m. - 5:30 p.m.", formatActivityTimes(fromTime, toTime))
    }

    @Test
    fun testFormatActivityDuration() {
        val fromTime = LocalDateTime(2023, 4, 1, 9, 0)
        val toTime = LocalDateTime(2023, 4, 1, 13, 30)
        assertEquals("4h", formatActivityDuration(fromTime, toTime))
    }

    @Test
    fun testFormatActivityDate() {
        val date = LocalDateTime(2023, 4, 1, 0, 0).date
        assertEquals("Sat., Apr. 1", formatActivityDate(date))
    }

    @Test
    fun testGroupActivityByDate() {
        val mockPlace1 = mockk<Place>()
        val mockPlace2 = mockk<Place>()
        val mockPlace3 = mockk<Place>()

        every { mockPlace1.id } returns 1
        every { mockPlace2.id } returns 2
        every { mockPlace3.id } returns 3

        val activities = listOf(
            Activity(
                id = 1,
                createdAt = Instant.parse("2023-04-01T00:00:00Z"),
                itineraryId = 1,
                fromTime = LocalDateTime(2023, 4, 1, 9, 0),
                toTime = LocalDateTime(2023, 4, 1, 10, 0),
                placeId = 1,
                place = mockPlace1
            ),
            Activity(
                id = 2,
                createdAt = Instant.parse("2023-04-01T00:00:00Z"),
                itineraryId = 1,
                fromTime = LocalDateTime(2023, 4, 1, 11, 0),
                toTime = LocalDateTime(2023, 4, 1, 12, 0),
                placeId = 2,
                place = mockPlace2
            ),
            Activity(
                id = 3,
                createdAt = Instant.parse("2023-04-02T00:00:00Z"),
                itineraryId = 1,
                fromTime = LocalDateTime(2023, 4, 2, 14, 0),
                toTime = LocalDateTime(2023, 4, 2, 16, 0),
                placeId = 3,
                place = mockPlace3
            )
        )

        val expected = mapOf(
            LocalDateTime(2023, 4, 1, 0, 0).date to listOf(activities[0], activities[1]),
            LocalDateTime(2023, 4, 2, 0, 0).date to listOf(activities[2])
        )

        assertEquals(expected, groupActivityByDate(activities))
    }

    @Test
    fun testDateTimeToMillis() {
        val dateTime = LocalDateTime(2023, 4, 1, 9, 0)
        val expectedMillis = 1680339600000
        assertEquals(expectedMillis, dateTimeToMillis(dateTime))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun testExtractTimePickerState() {
        val baseDate = LocalDateTime(2023, 4, 1, 10, 0)
        val timePickerState = TimePickerState(initialHour = 14, initialMinute = 30, is24Hour = false)
        val expected = LocalDateTime(2023, 4, 1, 14, 30)
        assertEquals(expected, extractTimePickerState(baseDate, timePickerState))
    }

    @Test
    fun testExtractDatePickerState() {
        val baseDate = LocalDateTime(2023, 4, 1, 10, 0)
        val selectedDateMillis = Instant.parse("2023-04-05T00:00:00Z").toEpochMilliseconds()
        val expected = LocalDateTime(2023, 4, 5, 10, 0)
        assertEquals(expected, extractDatePickerState(baseDate, selectedDateMillis))
    }
}