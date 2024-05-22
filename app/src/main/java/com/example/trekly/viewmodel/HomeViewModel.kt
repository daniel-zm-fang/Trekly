package com.example.trekly.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trekly.TreklyApplication
import com.example.trekly.model.Itinerary
import com.example.trekly.util.SupabaseManager
import com.example.trekly.util.ViewState
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    // Clients
    val supabase: SupabaseManager = getApplication<TreklyApplication>().supabaseManager

    // View model state
    private val _myItineraries =
        MutableStateFlow<ViewState<List<Itinerary>>>(ViewState.Loading(null))
    val myItineraries: StateFlow<ViewState<List<Itinerary>>> = _myItineraries
    private val _firstName = MutableStateFlow<ViewState<String>>(ViewState.Loading(""))
    val firstName: StateFlow<ViewState<String>> = _firstName

    // Fetches itineraries created by the current user, sorted by creation time.
    fun getMyItineraries() {
        viewModelScope.launch {
            try {
                val result = supabase.client.from("itinerary").select {
                    order(column = "created_at", order = Order.DESCENDING)
                }.decodeList<Itinerary>()
                _myItineraries.value = ViewState.Success(result)
            } catch (e: Exception) {
                println(e.message)
                _myItineraries.value = ViewState.Error(e)
            }
        }
    }

    suspend fun getUserSession() {
        val session = supabase.client.auth.currentSessionOrNull()
        if (session != null) {
            val result =
                supabase.client.auth.retrieveUserForCurrentSession(updateSession = true)
            val parsedName = parseUserName(result)
            if (parsedName != null) {
                // Assuming result is a data class with an email property
                _firstName.value = ViewState.Success(parsedName)
            } else {
                _firstName.value = ViewState.Error(Exception("Failed to parse username"))
            }
        } else {
            _firstName.value = ViewState.Error(Exception("User session is null"))
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val session = supabase.client.auth.currentSessionOrNull()
                if (session != null) {
                    supabase.client.auth.signOut()
                }
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    // Function to parse JSON data and extract email
    private fun parseUserName(userInfo: UserInfo): String? {
        return try {
            val jsonObject = userInfo.userMetadata ?: return null // Handle null case
            jsonObject["first_name"]?.jsonPrimitive?.contentOrNull
        } catch (e: Exception) {
            null
        }
    }
}