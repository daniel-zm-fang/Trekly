package com.example.trekly

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.trekly.navigation.NavGraph
import com.example.trekly.ui.theme.TreklyTheme

class MainActivity : AppCompatActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TreklyTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. You can notify your ViewModel or update your UI as needed.
            } else {
                // Permission is denied. Show an explanation to the user.
                Toast.makeText(this, "Location permission is required for this feature.", Toast.LENGTH_LONG).show()
            }
        }

        checkAndRequestLocationPermission()
    }

    private fun checkAndRequestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted. You can proceed with accessing the user's location.
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // Show an explanation to the user why you need the permission.
                // You can show a dialog or some other form of educational UI.
                // After the explanation, you can request the permission again.
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                // No explanation needed, you can request the permission.
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
}