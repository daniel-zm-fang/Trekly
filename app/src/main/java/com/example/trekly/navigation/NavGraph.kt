package com.example.trekly.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.trekly.view.HomeView
import com.example.trekly.view.ItineraryMapView
import com.example.trekly.view.NewItineraryView
import com.example.trekly.view.OAuthLoginPage
import com.example.trekly.view.ProfileView
import com.example.trekly.view.SplashView
import com.example.trekly.view.UserPreferencesView
import com.example.trekly.viewmodel.HomeViewModel
import com.example.trekly.viewmodel.ItineraryMapViewModel
import com.example.trekly.viewmodel.NewItineraryViewModel
import com.example.trekly.viewmodel.OAuthViewModel
import com.example.trekly.viewmodel.ProfileViewModel
import com.example.trekly.viewmodel.SplashViewModel
import com.example.trekly.viewmodel.UserPreferencesViewModel

fun navigateWithEmptyBackStack(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.id) {
            inclusive = true
        }
    }
}

// Holds every screen in the app and defines how to navigate between them.
@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(
            route = Screen.Splash.route
        ) {
            val splashViewModel: SplashViewModel = viewModel()
            SplashView(
                viewModel = splashViewModel,
                // Pass options to prevent user from going back to the splash screen
                navigateToHome = {
                    navigateWithEmptyBackStack(navController, Screen.Home.route)
                },
                navigateToOAuth = {
                    navigateWithEmptyBackStack(navController, Screen.Login.route)
                },
            )
        }
        composable(
            route = Screen.Login.route
        ) {
            val oAuthViewModel: OAuthViewModel = viewModel()
            OAuthLoginPage(
                viewModel = oAuthViewModel,
                navigateToPreferences = { navController.navigate(Screen.UserPreferences.route) },
                navigateToHome = { navController.navigate(Screen.Home.route) }
            )
        }
        composable(route = Screen.UserPreferences.route) {
            val userPreferencesViewModel: UserPreferencesViewModel = viewModel()
            UserPreferencesView(
                viewModel = userPreferencesViewModel,
                navigateToHome = { navController.navigate(Screen.Home.route) }
            )
        }

        composable(route = Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = viewModel()
            ProfileView(
                viewModel = profileViewModel,
                navigateToHome = { navController.navigate(Screen.Home.route) }
            )
        }

        composable(route = Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel()
            HomeView(
                viewModel = homeViewModel,
                navigateToItinerary = { itineraryId ->
                    navController.navigate("${Screen.Itinerary.route}/${itineraryId}")
                },
                navigateToCreateNewItinerary = {
                    navController.navigate(Screen.NewItinerary.route)
                },
                navigateToOAuth = {
                    navigateWithEmptyBackStack(navController, Screen.Login.route)
                },
                navigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
        composable(route = Screen.NewItinerary.route) {
            val newItineraryViewModel: NewItineraryViewModel = viewModel()
            NewItineraryView(
                viewModel = newItineraryViewModel,
                navigateBack = { navController.navigateUp() },
                navigateToNewItinerary = { itineraryId ->
                    navigateWithEmptyBackStack(
                        navController,
                        "${Screen.Itinerary.route}/${itineraryId}"
                    )
                },
            )
        }
        composable(
            route = "${Screen.Itinerary.route}/{itinerary_id}",
            arguments = listOf(
                navArgument("itinerary_id") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val itineraryId = backStackEntry.arguments?.getInt("itinerary_id")
            val itineraryMapViewModel: ItineraryMapViewModel = viewModel()
            if (itineraryId == null) {
                navigateWithEmptyBackStack(navController, Screen.Home.route)
            } else {
                ItineraryMapView(
                    itineraryMapViewModel = itineraryMapViewModel,
                    itineraryId = itineraryId,
                    // Always go back to the home screen
                    navigateBack = { navigateWithEmptyBackStack(navController, Screen.Home.route) }
                )
            }
        }
    }
}