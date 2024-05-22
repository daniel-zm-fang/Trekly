package com.example.trekly.api

import com.example.trekly.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.json.JSONObject
import kotlin.random.Random

suspend fun getItineraryImageUrl(destination: String): String? {
    val imageSize = "small" // raw / full / regular / small / thumb
    val perPage = 10
    val query =
        "https://api.unsplash.com/search/photos?page=1&query=$destination&per_page=$perPage&orientation=landscape"
    val client = HttpClient(Android)

    val response: HttpResponse = try {
        client.get(query) {
            contentType(ContentType.Application.Json)
            // Unsplash uses Client-ID prefix in its Authorization header
            header("Authorization", "Client-ID ${BuildConfig.UNSPLASH_API_KEY}")
        }
    } catch (e: Exception) {
        println("Unsplash API Error: ${e.message}")
        return null
    }

    // Parse JSON response if request succeeded and choose a random picture
    if (response.status.isSuccess()) {
        val responseBody = response.body<String>()
        val jsonResponse = JSONObject(responseBody)
        val results = jsonResponse.optJSONArray("results")
        if (results != null && results.length() > 0) {
            val numResults = results.length()
            // Randomly select a picture
            val randomIndex = Random.nextInt(0, numResults + 1)
            return try {
                val picture = results.getJSONObject(randomIndex)
                val urls = picture.getJSONObject("urls")
                val pictureUrl = urls.getString(imageSize)
                pictureUrl
            } catch (e: Exception) {
                println("Unsplash API Error: ${e.message}")
                null
            }
        } else {
            // Handle no results
            return null
        }
    }

    return null
}