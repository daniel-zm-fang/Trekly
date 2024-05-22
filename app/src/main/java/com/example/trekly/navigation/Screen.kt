package com.example.trekly.navigation

sealed class Screen(val route: String) {
    data object Splash: Screen("splash")
    data object Home: Screen("home")
    data object Login: Screen("login")
    data object Signup: Screen("signup")
    data object Itinerary: Screen("itinerary")
    data object NewItinerary: Screen("new_itinerary")
    data object Map: Screen("map")
    data object Profile: Screen("profile")
    data object UserPreferences: Screen("user_preferences")
}