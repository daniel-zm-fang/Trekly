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
import androidx.compose.material3.SelectableDates
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
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.trekly.R
import com.example.trekly.model.Activity
import com.example.trekly.util.ShownPickerState
import com.example.trekly.util.dateTimeToMillis
import com.example.trekly.util.extractDatePickerState
import com.example.trekly.util.extractTimePickerState
import com.example.trekly.util.formatActivityDate
import com.example.trekly.util.formatActivityTime

// Used to edit an Activity's details
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDialog(
    activity: Activity,
    fromUTCTimeMillis: Long,
    toUTCTimeMillis: Long,
    confirmLabel: String = "Save",
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    mutateActivity: (Activity) -> Unit, // create or update activity
    deleteActivity: ((Int) -> Unit)? = null,
    keyboardController: SoftwareKeyboardController? = null,
) {
    // Composable state
    var shownTimePicker by remember { mutableStateOf(ShownPickerState.None) }
    var showDatePicker by remember { mutableStateOf(false) }
    val fromTimeState = rememberTimePickerState(
        initialHour = activity.fromTime.hour,
        initialMinute = activity.fromTime.minute
    )
    val toTimeState = rememberTimePickerState(
        initialHour = activity.toTime.hour,
        initialMinute = activity.toTime.minute
    )
    val selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            return utcTimeMillis in fromUTCTimeMillis..toUTCTimeMillis
        }
    }
    val dateState =
        rememberDatePickerState(
            initialSelectedDateMillis = dateTimeToMillis(activity.fromTime),
            selectableDates = selectableDates
        )
    val showTimeError by remember {
        derivedStateOf {
            fromTimeState.hour > toTimeState.hour || (fromTimeState.hour == toTimeState.hour && fromTimeState.minute > toTimeState.minute)
        }
    }
    // Model state
    var activityState by remember { mutableStateOf(activity) }
    val showActivityError by remember {
        derivedStateOf {
            activityState.place == null || activityState.place!!.name.isBlank()
        }
    }

    // Time picker composable
    val onTimePickerDismissRequest: (() -> Unit) = {
        shownTimePicker = ShownPickerState.None
    }
    when (shownTimePicker) {
        ShownPickerState.From -> {
            TimePickerDialog(
                timePickerState = fromTimeState,
                onDismissRequest = onTimePickerDismissRequest,
                onConfirmation = {
                    activityState = activityState.copy(
                        fromTime = extractTimePickerState(
                            activityState.fromTime,
                            fromTimeState
                        )
                    )
                    onTimePickerDismissRequest()
                })
        }

        ShownPickerState.To -> {
            TimePickerDialog(
                timePickerState = toTimeState,
                onDismissRequest = onTimePickerDismissRequest,
                onConfirmation = {
                    activityState = activityState.copy(
                        toTime = extractTimePickerState(
                            activityState.toTime,
                            toTimeState
                        )
                    )
                    onTimePickerDismissRequest()
                })
        }

        ShownPickerState.None -> {}
    }

    // Date picker composable
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = "Discard")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (dateState.selectedDateMillis != null) {
                            activityState =
                                activityState.copy(
                                    fromTime = extractDatePickerState(
                                        activityState.fromTime,
                                        dateState.selectedDateMillis!!
                                    ),
                                    toTime = extractDatePickerState(
                                        activityState.toTime,
                                        dateState.selectedDateMillis!!,
                                    )
                                )
                        }
                        showDatePicker = false
                    },
                    enabled = dateState.selectedDateMillis != null,
                ) {
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
                PlacesAutocompleteOutlinedTextField(
                    label = "Place name",
                    value = activityState.place!!.name,
                    onValueChange = { name, id ->
                        activityState = if (id != null) {
                            activityState.copy(
                                place = activityState.place!!.copy(
                                    name = name,
                                    googleMapsPlaceId = id
                                )
                            )
                        } else {
                            activityState.copy(place = activityState.place!!.copy(name = name))
                        }
                    },
                    keyboardController = keyboardController,
                    isError = showActivityError,
                )
                if (showActivityError) {
                    Text(
                        text = "Place name cannot be empty",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                ClickableOutlinedTextField(
                    value = formatActivityDate(activityState.fromTime.date),
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
                        value = formatActivityTime(activityState.fromTime),
                        label = "From",
                        modifier = Modifier.weight(5f),
                        onClick = { shownTimePicker = ShownPickerState.From },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.baseline_access_time_filled_24),
                                contentDescription = "Select activity start time"
                            )
                        },
                        isError = showTimeError,
                    )
                    ClickableOutlinedTextField(
                        value = formatActivityTime(activityState.toTime),
                        label = "To",
                        modifier = Modifier.weight(4f),
                        onClick = { shownTimePicker = ShownPickerState.To },
                        isError = showTimeError,
                    )
                }
                if (showTimeError) {
                    Text(
                        text = "From time cannot be after to time",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                OutlinedTextField(
                    label = { Text("Notes") },
                    value = activityState.notes ?: "",
                    onValueChange = { activityState = activityState.copy(notes = it) },
                    maxLines = 5,
                )
                DialogButtons(
                    confirmLabel = confirmLabel,
                    dismissLabel = "Discard",
                    onDismissRequest = onDismissRequest,
                    onConfirmation = {
                        mutateActivity(activityState)
                        onConfirmation()
                    },
                    onDelete = if (deleteActivity != null) {
                        {
                            deleteActivity(activity.id)
                            onDismissRequest()
                        }
                    } else {
                        null
                    },
                    confirmEnabled = !(showTimeError || showActivityError),
                )
            }
        }
    }
}