package com.example.trekly.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trekly.TreklyApplication
import com.example.trekly.util.SupabaseManager
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

enum class OAuthResult {
    Success,
    Failure,
    None,
}

class OAuthViewModel(application: Application) : AndroidViewModel(application) {
    private val supabase: SupabaseManager = getApplication<TreklyApplication>().supabaseManager
    var signUpResult = mutableStateOf(OAuthResult.None)
    var isSigningUp = mutableStateOf(true) // Added to toggle between sign up and login
    var firstName = mutableStateOf("")
    var lastName = mutableStateOf("")
    var emailInput = mutableStateOf("")
    var passwordInput = mutableStateOf("")
    var errorMessage = mutableStateOf<String?>(null)

    fun signUp(
        firstName: String,
        lastName: String,
        emailInput: String,
        passwordInput: String,
        callback: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                println("Trying to signUp: $firstName $lastName")
                // If auto confirm is enabled, result = null, auto login.
                val result = supabase.client.auth.signUpWith(Email) {
                    email = emailInput
                    password = passwordInput
                    data = buildJsonObject {
                        put("first_name", firstName)
                        put("last_name", lastName)
                    }
                }
                if (result == null) {
                    println("Welcome, auto logged in!")
                } else {
                    println(result)
                }

                // Now we auto login, check the session id
                val user = supabase.client.auth.retrieveUserForCurrentSession(updateSession = true)
                println(user.id)

                signUpResult.value = OAuthResult.Success
                callback()
            } catch (e: Exception) {
                errorMessage.value = e.message?.split('.')?.first()
                signUpResult.value = OAuthResult.Failure
            }
        }
    }

    fun signIn(emailInput: String, passwordInput: String, callback: () -> Unit) {
        viewModelScope.launch {
            try {
                println("TRYING SIGN IN!!!")
                supabase.client.auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                callback()
            } catch (e: Exception) {
                errorMessage.value = e.message?.split('.')?.first()
            }
        }
    }

    fun dismissError() {
        errorMessage.value = null
    }

    fun toggleSignup() {
        isSigningUp.value = !isSigningUp.value
    }

    fun resetSignup() {
        // Reset the state to show the login form
        signUpResult.value = OAuthResult.None
        isSigningUp.value = false // Switch to log in if the user was signing up
    }
}