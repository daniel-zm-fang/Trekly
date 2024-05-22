package com.example.trekly.composeable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.example.trekly.model.Transportation

@Composable
fun TransportationCard(
    transportation: Transportation,
    updateTransportation: (Transportation) -> Unit,
    deleteTransportation: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDialogVisible by remember { mutableStateOf(false) }

    if (isDialogVisible) {
        TransportationDialog(
            transportation = transportation,
            onDismissRequest = { isDialogVisible = false },
            onConfirmation = { isDialogVisible = false },
            updateTransportation = updateTransportation,
            deleteTransportation = deleteTransportation,
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        onClick = { isDialogVisible = true },
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    KeyValueText(key = "Type", value = transportation.type)
                    KeyValueText(key = "Date", value = transportation.time.date.toString())
                    KeyValueText(key = "Time", value = transportation.time.time.toString())
                    transportation.number?.let {
                        KeyValueText(key = "Number", value = it)
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    transportation.duration?.let {
                        KeyValueText(key = "Duration", value = it)
                    }
                    transportation.distance?.let {
                        KeyValueText(key = "Distance", value = it)
                    }
                }
            }
            transportation.notes.let {
                if (it.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    KeyValueText(key = "Notes", value = it)
                }
            }
        }
    }
}

@Composable
fun KeyValueText(key: String, value: String) {
    Row {
        Text(
            text = "$key:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(8.dp))
        Text(text = value)
    }
}