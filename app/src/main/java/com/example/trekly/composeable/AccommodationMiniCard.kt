package com.example.trekly.composeable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trekly.model.Accommodation
import kotlinx.datetime.LocalDate

@Composable
fun AccommodationMiniCard(
    accommodation: Accommodation,
    relativeDate: LocalDate // Gives context to check in/check out dates
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (accommodation.place != null) {
                Text(
                    text = accommodation.place.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (accommodation.fromDate == relativeDate) {
                Text(text = "Check in")
            } else if (accommodation.toDate == relativeDate) {
                Text(text = "Check out")
            }
        }
    }
}