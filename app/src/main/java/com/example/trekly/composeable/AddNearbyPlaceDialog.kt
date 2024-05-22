package com.example.trekly.composeable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.trekly.R
import com.example.trekly.util.extractTimePickerState
import com.example.trekly.util.formatActivityDate
import com.example.trekly.util.formatActivityTime
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNearbyPlaceDialog(
    originalPlaceName: String,
    onDismissRequest: () -> Unit,
    onConfirmation: (String, String, String) -> Unit,
) {
    // Composable state
    var placeName by remember { mutableStateOf(originalPlaceName) }
    var showFromTimePicker by remember { mutableStateOf(false) }
    var showToTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val fromTimeState = rememberTimePickerState()
    val toTimeState = rememberTimePickerState(
        initialHour = fromTimeState.hour,
        initialMinute = fromTimeState.minute + 1 // Set "toTime" one hour after "fromTime"
    )
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis() // Set initial date to current date
    )
    var showNotesError by remember { mutableStateOf(false) }

    // Model state
    val now = Clock.System.now()
    val tz = TimeZone.currentSystemDefault()
    val today = now.toLocalDateTime(tz).date
    var selectedDate by remember { mutableStateOf(today) }
    var selectedFromTime by remember { mutableStateOf(now.toLocalDateTime(tz)) }
    var selectedToTime by remember { mutableStateOf(now.toLocalDateTime(tz)) }
    var notes by remember { mutableStateOf("") }

    if (showFromTimePicker || showToTimePicker) {
        Dialog(
            onDismissRequest = {
                showFromTimePicker = false
                showToTimePicker = false
            },
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(
                        top = 20.dp,
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 8.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select time",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )
                    if (showFromTimePicker) {
                        TimePicker(state = fromTimeState)
                    }
                    if (showToTimePicker) {
                        TimePicker(state = toTimeState)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        DialogButtons(
                            onDismissRequest = {
                                showFromTimePicker = false
                                showToTimePicker = false
                            },
                            onConfirmation = {
                                if (showFromTimePicker) {
                                    selectedFromTime = extractTimePickerState(
                                        selectedFromTime,
                                        fromTimeState
                                    )
                                }
                                if (showToTimePicker) {
                                    selectedToTime = extractTimePickerState(
                                        selectedToTime,
                                        toTimeState
                                    )
                                }
                                showFromTimePicker = false
                                showToTimePicker = false
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    if (dateState.selectedDateMillis != null) {
                        selectedDate = Instant.fromEpochMilliseconds(dateState.selectedDateMillis!!).toLocalDateTime(TimeZone.currentSystemDefault()).date
                    }
                    showDatePicker = false
                }) {
                    Text(text = "OK")
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Place name") },
                    value = placeName,
                    onValueChange = { placeName = it },
                    maxLines = 2,
                )

                ClickableOutlinedTextField(
                    value = formatActivityDate(selectedDate),
                    label = "Date",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDatePicker = true },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.DateRange,
                            contentDescription = "Select activity date"
                        )
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ClickableOutlinedTextField(
                        value = formatActivityTime(selectedFromTime),
                        label = "From",
                        modifier = Modifier.weight(1.0F),
                        onClick = { showFromTimePicker = true },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_access_time_filled_24),
                                contentDescription = "Select activity start time"
                            )
                        },
                    )
                    ClickableOutlinedTextField(
                        value = formatActivityTime(selectedToTime),
                        label = "To",
                        modifier = Modifier.weight(1.0F),
                        onClick = { showToTimePicker = true },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_access_time_filled_24),
                                contentDescription = "Select activity end time"
                            )
                        },
                    )
                }
                OutlinedTextField(
                    label = { Text("Notes") },
                    value = notes,
                    onValueChange = {
                        notes = it
                        showNotesError = false
                    },
                    maxLines = 5,
                    isError = showNotesError
                )
                if (showNotesError) {
                    Text(
                        text = "Please enter some notes",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                DialogButtons(
                    confirmLabel = "Save",
                    dismissLabel = "Discard",
                    onDismissRequest = onDismissRequest,
                    onConfirmation = {
                        if (notes.isBlank()) {
                            showNotesError = true
                        } else {
                            onConfirmation(
                                selectedFromTime.toString(),
                                selectedToTime.toString(),
                                notes
                            )
                        }
                    }
                )
            }
        }
    }
}