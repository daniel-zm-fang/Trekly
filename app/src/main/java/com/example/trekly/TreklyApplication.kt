// initializing global resources
package com.example.trekly

import android.app.Application
import android.util.Log
import com.aallam.openai.client.OpenAI
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.LoggingConfig
import com.example.trekly.api.PlacesClient
import com.example.trekly.util.SupabaseManager
import kotlin.time.Duration.Companion.seconds
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.MapsInitializer.Renderer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback

internal class TreklyApplication : Application(), OnMapsSdkInitializedCallback {
    lateinit var openAI: OpenAI
        private set
    lateinit var supabaseManager: SupabaseManager
        private set

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
        private set
    lateinit var placesClient: PlacesClient
        private set

    override fun onCreate() {
        super.onCreate()

        MapsInitializer.initialize(applicationContext, Renderer.LATEST, this)

        openAI = OpenAI(
            token = BuildConfig.OPENAI_API_KEY,
            logging = LoggingConfig(LogLevel.All),
            timeout = Timeout(socket = 60.seconds)
        )

        supabaseManager = SupabaseManager(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        Places.initialize(this, BuildConfig.MAPS_API_KEY)
        placesClient = PlacesClient(this)
    }

    override fun onMapsSdkInitialized(renderer: Renderer) {
        when (renderer) {
            Renderer.LATEST -> Log.d("TreklyApplication", "The latest version of the renderer is used.")
            Renderer.LEGACY -> Log.d("TreklyApplication", "The legacy version of the renderer is used.")
        }
    }
}
