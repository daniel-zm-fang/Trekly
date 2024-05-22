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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.trekly.model.Accommodation
import com.example.trekly.util.ShownPickerState
import com.example.trekly.util.dateToMillis
import com.example.trekly.util.extractLocalDateFromDatePicker
import com.example.trekly.util.extractLocalTimeFromTimePicker
import com.example.trekly.util.formatAccommodationDate
import com.example.trekly.util.formatAccommodationTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccommodationDialog(
    accommodation: Accommodation,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    updateAccommodation: (Accommodation) -> Unit,
    deleteAccommodation: (Int) -> Unit,
) {
    // Composable state
    var shownDatePicker by remember { mutableStateOf(ShownPickerState.None) }
    var shownTimePicker by remember { mutableStateOf(ShownPickerState.None) }
    val checkInState = rememberTimePickerState(
        initialHour = accommodation.checkIn.hour,
        initialMinute = accommodation.checkIn.minute
    )
    val checkOutState = rememberTimePickerState(
        initialHour = accommodation.checkOut.hour,
        initialMinute = accommodation.checkOut.minute
    )
    val fromDateState =
        rememberDatePickerState(initialSelectedDateMillis = dateToMillis(accommodation.fromDate))
    val toDateState =
        rememberDatePickerState(initialSelectedDateMillis = dateToMillis(accommodation.toDate))
    val saveEnabled by remember {
        derivedStateOf {
            fromDateState.selectedDateMillis != null && toDateState.selectedDateMillis != null
        }
    }
    // Model state
    var accommodationState by remember { mutableStateOf(accommodation) }

    // Time picker composable
    val onTimePickerDismissRequest: (() -> Unit) = {
        shownTimePicker = ShownPickerState.None
    }
    when (shownTimePicker) {
        ShownPickerState.From -> {
            TimePickerDialog(
                timePickerState = checkInState,
                onDismissRequest = onTimePickerDismissRequest,
                onConfirmation = {
                    accommodationState = accommodationState.copy(
                        checkIn = extractLocalTimeFromTimePicker(checkInState)
                    )
                    onTimePickerDismissRequest()
                })
        }

        ShownPickerState.To -> {
            TimePickerDialog(
                timePickerState = checkOutState,
                onDismissRequest = onTimePickerDismissRequest,
                onConfirmation = {
                    accommodationState = accommodationState.copy(
                        checkOut = extractLocalTimeFromTimePicker(checkOutState)
                    )
                    onTimePickerDismissRequest()
                })
        }

        ShownPickerState.None -> {}
    }

    // Date picker composable
    val onDatePickerDismissRequest: (() -> Unit) = {
        shownDatePicker = ShownPickerState.None
    }
    when (shownDatePicker) {
        ShownPickerState.From -> {
            DatePickerDialog(
                onDismissRequest = onDatePickerDismissRequest,
                dismissButton = {
                    TextButton(onClick = onDatePickerDismissRequest) {
                        Text(text = "Discard")
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            accommodationState = accommodationState.copy(
                                fromDate = extractLocalDateFromDatePicker(fromDateState)
                            )
                            onDatePickerDismissRequest()
                        },
                        enabled = fromDateState.selectedDateMillis != null
                    ) {
                        Text(text = "OK")
                    }
                }) {
                DatePicker(state = fromDateState)
            }
        }

        ShownPickerState.To -> {
            DatePickerDialog(
                onDismissRequest = onDatePickerDismissRequest,
                dismissButton = {
                    TextButton(onClick = onDatePickerDismissRequest) {
                        Text(text = "Discard")
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            accommodationState = accommodationState.copy(
                                toDate = extractLocalDateFromDatePicker(toDateState)
                            )
                            onDatePickerDismissRequest()
                        },
                        enabled = toDateState.selectedDateMillis != null
                    ) {
                        Text(text = "OK")
                    }
                }) {
                DatePicker(state = toDateState)
            }
        }

        ShownPickerState.None -> {}
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
            ) {
                // TODO: add Maps autocomplete and update Place object
                if (accommodation.place != null) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Accommodation name") },
                        value = accommodationState.place!!.name,
                        onValueChange = {
                            accommodationState =
                                accommodationState.copy(place = accommodationState.place!!.copy(name = it))
                        },
                        maxLines = 2,
                    )
                } else {
                    Text(
                        "Edit accommodation",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )
                }
                // Check in date and time
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClickableOutlinedTextField(
                        value = formatAccommodationDate(accommodationState.fromDate),
                        label = "Check in date",
                        modifier = Modifier.weight(1f),
                        onClick = { shownDatePicker = ShownPickerState.From },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.DateRange,
                                contentDescription = "Select check in date"
                            )
                        },
                    )
                    ClickableOutlinedTextField(
                        value = formatAccommodationTime(accommodationState.checkIn),
                        label = "Check in time",
                        modifier = Modifier.weight(1f),
                        onClick = { shownTimePicker = ShownPickerState.From },
                    )
                }
                // Check out date and time
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClickableOutlinedTextField(
                        value = formatAccommodationDate(accommodationState.toDate),
                        label = "Check out date",
                        modifier = Modifier.weight(1f),
                        onClick = { shownDatePicker = ShownPickerState.To },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.DateRange,
                                contentDescription = "Select check out date"
                            )
                        },
                    )
                    ClickableOutlinedTextField(
                        value = formatAccommodationTime(accommodationState.checkOut),
                        label = "Check out time",
                        modifier = Modifier.weight(1f),
                        onClick = { shownTimePicker = ShownPickerState.To },
                    )
                }
                OutlinedTextField(
                    label = { Text("Notes") },
                    value = accommodationState.notes,
                    onValueChange = { accommodationState = accommodationState.copy(notes = it) },
                    maxLines = 5,
                )
                DialogButtons(
                    confirmLabel = "Save",
                    dismissLabel = "Discard",
                    onDismissRequest = onDismissRequest,
                    onConfirmation = {
                        updateAccommodation(accommodationState)
                        onConfirmation()
                    },
                    onDelete = {
                        deleteAccommodation(accommodation.id)
                        onDismissRequest()
                    },
                    confirmEnabled = saveEnabled,
                )
            }
        }
    }
}