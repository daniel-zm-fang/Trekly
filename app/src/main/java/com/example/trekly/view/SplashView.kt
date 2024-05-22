package com.example.trekly.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.example.trekly.composeable.Spinner
import com.example.trekly.viewmodel.SplashViewModel
import io.github.jan.supabase.gotrue.SessionStatus

@Composable
fun SplashView(
    viewModel: SplashViewModel,
    navigateToHome: () -> Unit,
    navigateToOAuth: () -> Unit
) {
    val sessionState = viewModel.sessionState.collectAsState(initial = SessionStatus.LoadingFromStorage)

    when (val status = sessionState.value) {
        is SessionStatus.Authenticated -> {
            LaunchedEffect(status) {
                navigateToHome()
            }
        }
        is SessionStatus.NotAuthenticated -> {
            LaunchedEffect(status) {
                navigateToOAuth()
            }
        }
        is SessionStatus.LoadingFromStorage -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Spinner(loadingText = "Trekly is starting...")
            }
        }
        is SessionStatus.NetworkError -> {
            LaunchedEffect(status) {
                navigateToOAuth()
            }
        }
    }
}