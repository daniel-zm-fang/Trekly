package com.example.trekly.util

// Generic class for handling data fetching states inside View Model.
sealed class ViewState<T> {
    data class Success<T>(val value: T) : ViewState<T>()
    data class Loading<T>(val value: T? = null) : ViewState<T>()
    data class Error<T>(val exception: Exception) : ViewState<T>()
}