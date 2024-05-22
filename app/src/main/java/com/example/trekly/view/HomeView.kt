package com.example.trekly.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trekly.composeable.ItineraryCard
import com.example.trekly.composeable.Spinner
import com.example.trekly.util.ViewState
import com.example.trekly.viewmodel.HomeViewModel

@Composable
fun HomeView(
    viewModel: HomeViewModel,
    navigateToItinerary: (itineraryId: Int) -> Unit,
    navigateToCreateNewItinerary: () -> Unit,
    navigateToOAuth: () -> Unit,
    navigateToProfile: () -> Unit
) {
    // Use LaunchedEffect to launch a coroutine for fetching user data
    LaunchedEffect(Unit) {
        viewModel.getUserSession()
    }
    Column(modifier = Modifier.padding(16.dp)) {
        ProfileBar(viewModel, onEditProfile = {
            // Handle edit profile action here
            navigateToProfile()
        }, onLogout = {
            navigateToOAuth()
        })
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My itineraries", fontSize = 24.sp, fontWeight = FontWeight.Medium)
            Button(
                onClick = navigateToCreateNewItinerary,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Create new itinerary",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("New")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        MyItinerariesView(viewModel, navigateToItinerary)
    }
}

@Composable
fun ProfileBar(
    viewModel: HomeViewModel,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val firstNameState by viewModel.firstName.collectAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        when (val state = firstNameState) {
            is ViewState.Success -> {
                val firstName = state.value
                // Non-editable circle with the first letter of the username
                Text(
                    text = ("Hello $firstName!"),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Left,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onEditProfile,
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }
            }

            is ViewState.Loading -> {
                Text(
                    text = ("Loading..."),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Left,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }

            is ViewState.Error -> {}
        }
        Button(
            onClick = {
                viewModel.logout()
                onLogout()
            },
        ) {
            Icon(
                Icons.Filled.ExitToApp,
                contentDescription = "Sign out",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
        }
    }
}

@Composable
fun MyItinerariesView(viewModel: HomeViewModel, navigateToItinerary: (itineraryId: Int) -> Unit) {
    val myItinerariesState by viewModel.myItineraries.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getMyItineraries()
    }

    when (val state = myItinerariesState) {
        is ViewState.Loading -> {
            Spinner("Loading itineraries")
        }

        is ViewState.Success -> {
            val myItineraries = state.value
            if (myItineraries.isEmpty()) {
                Text(text = "No itineraries yet, click the New button to create one!")
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(myItineraries) {
                    ItineraryCard(it, navigateToItinerary)
                }
            }
        }

        is ViewState.Error -> {
            Text("Failed to get your itineraries")
            println("ViewState error: $state")
        }
    }
}