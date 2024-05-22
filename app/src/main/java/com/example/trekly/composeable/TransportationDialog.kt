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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.trekly.model.Transportation
import com.example.trekly.model.transportationTypes
import com.example.trekly.util.extractTimePickerState
import com.example.trekly.util.formatActivityDate
import com.example.trekly.util.formatActivityTime

// Used to edit an Activity's details
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportationDialog(
    transportation: Transportation,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    updateTransportation: (Transportation) -> Unit,
    deleteTransportation: (Int) -> Unit,
) {
    // Composable state
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val timeState = rememberTimePickerState()
    val toTimeState = rememberTimePickerState()
    val dateState = rememberDatePickerState()
    // Model state
    var transportationState by remember { mutableStateOf(transportation) }
    var expanded by remember { mutableStateOf(false) }
    var selectedTransportationType by remember { mutableStateOf(transportationState.type) }

    if (showTimePicker) {
        Dialog(
            onDismissRequest = { showTimePicker = false },
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
                    TimePicker(state = timeState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        DialogButtons(
                            onDismissRequest = { showTimePicker = false },
                            onConfirmation = {
                                transportationState = transportationState.copy(
                                    time = extractTimePickerState(
                                        transportationState.time,
                                        timeState
                                    )
                                )
                                showTimePicker = false
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
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = "Discard")
                }
            },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = "OK")
                }
            }) {
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
                Text(
                    "Edit transportation",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )
                // Transportation type dropdown menu
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedTransportationType,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        label = { Text("Transportation type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        transportationTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedTransportationType = type
                                    transportationState = transportationState.copy(type = type)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                ClickableOutlinedTextField(
                    value = formatActivityDate(transportationState.time.date),
                    label = "Date",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDatePicker = true },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.DateRange,
                            contentDescription = "Select transportation date"
                        )
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ClickableOutlinedTextField(
                        value = formatActivityTime(transportationState.time),
                        label = "Start time",
                        modifier = Modifier.weight(1.0F),
                        onClick = { showTimePicker = true },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_access_time_filled_24),
                                contentDescription = "Select transportation start time"
                            )
                        },
                    )
                }
                OutlinedTextField(
                    label = { Text("Booking reference") },
                    value = transportationState.bookingReference ?: "",
                    onValueChange = { transportationState = transportationState.copy(bookingReference = it) },
                    maxLines = 1,
                )
                OutlinedTextField(
                    label = { Text("Notes") },
                    value = transportationState.notes,
                    onValueChange = { transportationState = transportationState.copy(notes = it) },
                    maxLines = 5,
                )
                DialogButtons(
                    confirmLabel = "Save",
                    dismissLabel = "Discard",
                    onDismissRequest = onDismissRequest,
                    onConfirmation = {
                        updateTransportation(transportationState)
                        onConfirmation()
                    },
                    onDelete = {
                        deleteTransportation(transportation.id)
                        onDismissRequest()
                    },
                )
            }
        }
    }
}