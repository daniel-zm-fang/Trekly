package com.example.trekly

import com.example.trekly.util.formatItineraryDates
import com.example.trekly.util.formatItineraryDuration
import com.example.trekly.util.generateShareCode
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ItineraryUtilsTest {

    @Test
    fun testGenerateShareCode() {
        val shareCode = generateShareCode(5)
        assertEquals(5, shareCode.length)
        assert(shareCode.all { it.isLetterOrDigit() })
    }

    @Test
    fun testFormatItineraryDuration() {
        val fromDate = LocalDate(2023, 4, 1)
        val toDate = LocalDate(2023, 4, 5)
        assertEquals("4 days", formatItineraryDuration(fromDate, toDate))

        val fromDate2 = LocalDate(2023, 4, 1)
        val toDate2 = LocalDate(2023, 4, 2)
        assertEquals("1 day", formatItineraryDuration(fromDate2, toDate2))
    }

    @Test
    fun testFormatItineraryDates() {
        val fromDate = LocalDate(2023, 4, 1)
        val toDate = LocalDate(2023, 4, 5)
        assertEquals("Apr. 1 - Apr. 5", formatItineraryDates(fromDate, toDate))
    }
}