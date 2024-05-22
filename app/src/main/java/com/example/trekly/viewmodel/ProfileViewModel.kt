package com.example.trekly.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trekly.TreklyApplication
import com.example.trekly.model.Profile
import com.example.trekly.util.SupabaseManager
import com.example.trekly.util.ViewState
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    val supabase: SupabaseManager = getApplication<TreklyApplication>().supabaseManager
    private val _profile = MutableStateFlow<ViewState<Profile>>(ViewState.Loading(null))
    val profile: StateFlow<ViewState<Profile>> = _profile
    fun getUserProfile() {
        viewModelScope.launch {
            try {
                val userId = getUserId()
                val result: Profile = supabase.client.from("profiles").select {
                    filter {
                        if (userId != null) {
                            eq("id", userId)
                        }
                    }
                }.decodeSingle<Profile>()
                _profile.value = ViewState.Success(result)
            } catch (e: Exception) {
                println("getProfile error: ${e.message}")
                _profile.value = ViewState.Error(e)
            }
        }
    }
    private fun getUserId(): String? {
        return supabase.client.auth.currentUserOrNull()?.id
    }

}