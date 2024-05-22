package com.example.trekly.composeable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.trekly.model.Itinerary
import com.example.trekly.util.formatItineraryDates
import com.example.trekly.util.formatItineraryDuration

@Composable
fun ItineraryCard(itinerary: Itinerary, navigateToItinerary: (itineraryId: Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = {
            navigateToItinerary(itinerary.id)
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            AsyncImage(
                model = itinerary.thumbnail,
                contentDescription = "Image for itinerary",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = MaterialTheme.colorScheme.secondaryContainer)
                    .padding(4.dp)
            ) {
                Text(
                    formatItineraryDuration(itinerary.fromDate, itinerary.toDate),
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                itinerary.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(formatItineraryDates(itinerary.fromDate, itinerary.toDate))
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}