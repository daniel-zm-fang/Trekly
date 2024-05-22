package com.example.trekly.composeable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trekly.viewmodel.PlacesAutocompleteViewModel

@Composable
fun PlacesAutocompleteTextField(
    viewModel: PlacesAutocompleteViewModel = viewModel(),
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardController: SoftwareKeyboardController?,
    isError: Boolean = false,
) {
    val predictions by viewModel.autocompletePredictions.collectAsState()
    var showPredictions by remember { mutableStateOf(false) }

    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = {
            onValueChange(it)
            showPredictions = if (it.isNotEmpty()) {
                viewModel.getAutocompletePredictions(it)
                true
            } else {
                false
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            keyboardController?.hide()
            showPredictions = false
        }),
        isError = isError,
    )
    // Dropdown list for autocomplete predictions
    if (showPredictions) {
        LazyColumn {
            items(predictions) { prediction ->
                Text(
                    text = prediction.getPrimaryText(null).toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onValueChange(
                                prediction
                                    .getFullText(null)
                                    .toString()
                            )
                            showPredictions = false
                            keyboardController?.hide()
                        }
                        .padding(8.dp)
                )
            }
        }
    }
}