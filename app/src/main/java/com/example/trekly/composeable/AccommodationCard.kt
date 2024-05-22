package com.example.trekly.composeable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trekly.model.Accommodation
import com.example.trekly.util.formatAccommodationDates

@Composable
fun AccommodationCard(
    accommodation: Accommodation,
    updateAccommodation: (Accommodation) -> Unit,
    deleteAccommodation: (Int) -> Unit,
    modifier: Modifier,
) {
    var isDialogVisible by remember { mutableStateOf(false) }

    if (isDialogVisible) {
        AccommodationDialog(
            accommodation = accommodation,
            onDismissRequest = { isDialogVisible = false },
            onConfirmation = { isDialogVisible = false },
            updateAccommodation = updateAccommodation,
            deleteAccommodation = deleteAccommodation,
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        onClick = { isDialogVisible = true },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            if (accommodation.place != null) {
                Text(
                    text = accommodation.place.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (accommodation.place.address != null) {
                    Text(
                        text = accommodation.place.address!!,
                        lineHeight = 16.sp, // 12.sp + 4.sp space
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatAccommodationDates(
                    accommodation.fromDate,
                    accommodation.toDate
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = accommodation.notes)
        }
    }
}