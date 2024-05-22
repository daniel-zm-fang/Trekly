package com.example.trekly.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.trekly.TreklyApplication
import com.example.trekly.util.SupabaseManager
import io.github.jan.supabase.gotrue.auth

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val supabase: SupabaseManager = getApplication<TreklyApplication>().supabaseManager
    val sessionState = supabase.client.auth.sessionStatus
}