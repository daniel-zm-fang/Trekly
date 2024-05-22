package com.example.trekly.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trekly.TreklyApplication
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlacesAutocompleteViewModel(application: Application) : AndroidViewModel(application) {
    // Clients
    private val placesClient = getApplication<TreklyApplication>().placesClient

    // State
    private val _autocompletePredictions =
        MutableStateFlow<List<AutocompletePrediction>>(emptyList())
    val autocompletePredictions: StateFlow<List<AutocompletePrediction>> =
        _autocompletePredictions.asStateFlow()

    fun getAutocompletePredictions(query: String) {
        viewModelScope.launch {
            try {
                val predictions = placesClient.getAddressPredictions(inputString = query)
                _autocompletePredictions.value = predictions
            } catch (e: Exception) {
                Log.e("PlacesAutocompleteViewModel", "Error getting autocomplete predictions", e)
            }
        }
    }
}