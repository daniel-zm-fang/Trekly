package com.example.trekly.view

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trekly.viewmodel.UserPreferencesViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPreferencesView(
    viewModel: UserPreferencesViewModel,
    navigateToHome: () -> Unit
) {
    var travelPace by remember { mutableStateOf("") }
    var languagesSpoken by remember { mutableStateOf("") }
    var countriesToVisit by remember { mutableStateOf("") }
    var travelBudget by remember { mutableStateOf("") }
    val paddingModifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter your travel preferences") }, // Added title to the AppBar
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .then(paddingModifier),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = travelPace,
                onValueChange = { travelPace = it },
                label = { Text("Travel Pace") }
            )

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = languagesSpoken,
                onValueChange = { languagesSpoken = it },
                label = { Text("Languages Spoken") }
            )

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = countriesToVisit,
                onValueChange = { countriesToVisit = it },
                label = { Text("Countries to Visit") }
            )

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = travelBudget,
                onValueChange = { travelBudget = it },
                label = { Text("Travel Budget") }
            )

            Button(
                onClick = {
                    viewModel.callRequest {
                        // Save user preferences to ViewModel or database
                        viewModel.saveUserPreferences(
                            travelPace,
                            languagesSpoken,
                            countriesToVisit,
                            travelBudget
                        )
                    }
                    println("Saved!")

                    navigateToHome() // Navigate back to home screen
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Preferences")
            }
        }
    }
}