package com.example.trekly.view

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.trekly.composeable.ClickableTextField
import com.example.trekly.composeable.PlacesAutocompleteTextField
import com.example.trekly.composeable.Spinner
import com.example.trekly.model.transportationTypes
import com.example.trekly.viewmodel.NewItineraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewItineraryView(
    viewModel: NewItineraryViewModel,
    navigateBack: () -> Unit,
    navigateToNewItinerary: (id: Int) -> Unit,
) {
    var itinerary by viewModel.itinerary
    val recommendationResponse by viewModel.recommendationResponse.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val selectedTransportationType by viewModel.selectedTransportationType
    val (prompt, setPrompt) = remember { mutableStateOf("") }
    val (showPromptError, setShowPromptError) = remember { mutableStateOf(false) }
    val (showItineraryError, setShowItineraryError) = remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    DisposableEffect(null) {
        viewModel.onCompose()
        // Prevents adding multiple listeners when Composable recomposes.
        onDispose {
            viewModel.onDispose()
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = {
                Text("New itinerary")
            },
            navigationIcon = {
                // Finish this activity to go back to previous activity
                IconButton(onClick = navigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to home page"
                    )
                }
            },
        )
    }) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.padding(16.dp)) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = itinerary.name,
                    onValueChange = {
                        itinerary = itinerary.copy(name = it)
                        setShowItineraryError(false)
                    },
                    label = { Text("Itinerary name") },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                    }),
                    isError = showItineraryError
                )
                Spacer(modifier = Modifier.height(8.dp))
                PlacesAutocompleteTextField(
                    label = "Destination",
                    value = itinerary.destination,
                    onValueChange = {
                        itinerary = itinerary.copy(destination = it)
                        setShowItineraryError(false)
                    },
                    keyboardController = keyboardController,
                    isError = showItineraryError,
                )
                if (showItineraryError) {
                    Text(
                        text = "Itinerary name and destination cannot be empty",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    ClickableTextField(
                        value = itinerary.fromDate.toString(),
                        label = "Trip start",
                        leadingIcon = {
                            Icon(
                                Icons.Filled.DateRange,
                                contentDescription = "Select trip start date"
                            )
                        },
                        Modifier.weight(1.0F),
                        onClick = { viewModel.showDatePicker((context as AppCompatActivity).supportFragmentManager) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ClickableTextField(
                        value = itinerary.toDate.toString(),
                        label = "Trip end",
                        leadingIcon = {
                            Icon(
                                Icons.Filled.DateRange,
                                contentDescription = "Select trip end date"
                            )
                        },
                        Modifier.weight(1.0F),
                        onClick = { viewModel.showDatePicker((context as AppCompatActivity).supportFragmentManager) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = prompt,
                    onValueChange = {
                        setPrompt(it)
                        setShowPromptError(false)
                    },
                    label = { Text("Describe this trip with a few sentences") },
                    isError = showPromptError,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                    }),
                    maxLines = 5
                )
                if (showPromptError) {
                    Text(
                        text = "Prompt cannot be empty",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
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
                                    viewModel.selectedTransportationType.value = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (itinerary.name.isBlank() || itinerary.destination.isBlank()) {
                                setShowItineraryError(true)
                            } else {
                                viewModel.addEmptyItinerary(navigateToNewItinerary)
                            }
                        },
                        enabled = !isLoading,
                        content = { Text(if (isLoading) "Creating..." else "Create Empty") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (itinerary.name.isBlank() || itinerary.destination.isBlank()) {
                                setShowItineraryError(true)
                            } else if (prompt.isBlank()) {
                                setShowPromptError(true)
                            } else {
                                viewModel.getRecommendations(prompt, navigateToNewItinerary)
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF588157)),
                        content = { Text("Create w/ AI") }
                    )
                    if (isLoading) {
                        Spinner(loadingText = "Generating itinerary...")
                    }
                    if (recommendationResponse.isNotEmpty() && recommendationResponse[0] != '[') {
                        Text(
                            text = recommendationResponse,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
