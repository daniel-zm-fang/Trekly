package com.example.trekly.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trekly.TreklyApplication
import com.example.trekly.util.SupabaseManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class UserPreferencesViewModel(application: Application) : AndroidViewModel(application){
    private val supabase: SupabaseManager = getApplication<TreklyApplication>().supabaseManager
    fun callRequest(requestFunction: suspend() -> Unit) {
        runBlocking {
            launch {
                requestFunction()
            }
        }
    }
    suspend fun saveUserPreferences(
        travelPace: String,
        languagesSpoken: String,
        countriesToVisit: String,
        travelBudget: String): Unit
    {
        println("I am in saveUserPreferences")

        viewModelScope.launch {
            try {
                supabase.addPreferences(
                    travelPace = travelPace,
                    languagesSpoken = languagesSpoken,
                    countriesToVisit = countriesToVisit,
                    travelBudget = travelBudget
                )
                println("Saved user preferences!")
            } catch (e: Exception) {
                println(e.message)
            }
        }

    }
}
