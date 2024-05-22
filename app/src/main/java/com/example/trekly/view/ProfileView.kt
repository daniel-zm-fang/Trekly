package com.example.trekly.view

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trekly.R
import com.example.trekly.composeable.Spinner
import com.example.trekly.util.ViewState
import com.example.trekly.viewmodel.ProfileViewModel
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(viewModel: ProfileViewModel, navigateToHome: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") }, // Added title to the AppBar
                navigationIcon = {
                    IconButton(onClick = navigateToHome) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to home page"
                        )
                    }
                }
            )
        }
    ) { innerPadding -> // Use the padding provided by Scaffold to offset the content
        val paddingModifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        val profileState by viewModel.profile.collectAsState()
        var firstName by remember { mutableStateOf<String?>(null) }


        LaunchedEffect(Unit) {
            viewModel.getUserProfile()

            val session = viewModel.supabase.client.auth.currentSessionOrNull()
            if (session != null) {
                val result =
                    viewModel.supabase.client.auth.retrieveUserForCurrentSession(updateSession = true)
                firstName =
                    parseUserName(result) // Assuming result is a data class with an email property
            }

        }

        when (val state = profileState) {
            is ViewState.Loading -> {
                Spinner("Loading profile...")
            }

            is ViewState.Success -> {
                val profile = state.value
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .then(paddingModifier),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Placeholder for a profile picture, replace with actual image resource
                    Image(
                        painter = painterResource(id = R.drawable.profile_placeholder),
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RectangleShape)
                            .padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Hi $firstName!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Travel pace: ${profile.travel_pace}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Travel budget: ${profile.travel_budget}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Countries to visit: ${profile.countries_to_visit}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Languages spoken: ${profile.languages_spoken}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // Repeat for other profile details...
                }
            }

            is ViewState.Error -> {
                val error = state.exception
                Column(modifier = Modifier
                    .padding(innerPadding)
                    .then(paddingModifier)) {
                    Text(text = "Failed to fetch profile details")
                    Text(text = error.message ?: "")
                }
            }
        }
    }
}

private fun parseUserName(userInfo: UserInfo): String? {
    return try {
        val jsonObject = userInfo.userMetadata ?: return null // Handle null case
        jsonObject["first_name"]?.jsonPrimitive?.contentOrNull
    } catch (e: Exception) {
        null
    }
}