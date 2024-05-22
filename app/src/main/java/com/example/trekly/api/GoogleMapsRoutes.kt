package com.example.trekly.api

import com.example.trekly.BuildConfig
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.json.JSONArray
import org.json.JSONObject

data class TransitStep(
    val mode: String,
    val headSign: String?,
    val transitLine: TransitLine?,
    val distance: String,
    val duration: String
)

data class TransitLine(
    val name: String,
    val shortName: String?,
    val vehicle: Vehicle?
)

data class Vehicle(
    val name: String,
    val type: String
)

data class RouteInfo(
    val polyline: MutableList<LatLng>,
    val distance: String,
    val duration: String,
    val transitSteps: List<TransitStep>?
)

suspend fun getOptimalRoute(
    originLat: Double,
    originLng: Double,
    destinationLat: Double,
    destinationLng: Double,
    mode: String
): RouteInfo? {
    // Documentation: https://developers.google.com/maps/documentation/routes/reference/rest/v2/TopLevel/computeRoutes
    val requestBody = """
        {
          "origin":{
            "location":{
              "latLng":{
                "latitude": $originLat,
                "longitude": $originLng
              }
            }
          },
          "destination":{
            "location":{
              "latLng":{
                "latitude": $destinationLat,
                "longitude": $destinationLng
              }
            }
          },
          "travelMode": "$mode",
          "polylineQuality": "OVERVIEW",
          "computeAlternativeRoutes": false,
          "routeModifiers": {
            "avoidTolls": false,
            "avoidHighways": false,
            "avoidFerries": false
          },
          "languageCode": "en-US",
          "units": "METRIC"
        }
    """.trimIndent()

    val client = HttpClient(Android)

    val response: HttpResponse = try {
        client.post("https://routes.googleapis.com/directions/v2:computeRoutes") {
            contentType(ContentType.Application.Json)
            header("X-Goog-Api-Key", BuildConfig.MAPS_API_KEY)
            if (mode == "TRANSIT") {
                header("X-Goog-FieldMask", "routes.localizedValues.distance.text,routes.localizedValues.duration.text,routes.polyline.encodedPolyline,routes.legs")
            } else {
                header("X-Goog-FieldMask", "routes.localizedValues.distance.text,routes.localizedValues.duration.text,routes.polyline.encodedPolyline")
            }
            setBody(requestBody)
        }
    } catch (e: Exception) {
        println("Google Maps Routes Error: $e")
        return null
    }

//    println("Google Maps Routes Response: $response, ${response.body<String>()}")

    if (response.status.isSuccess()) {
        val responseBody = response.body<String>()
        val jsonResponse = JSONObject(responseBody)
        val routes = jsonResponse.optJSONArray("routes")
        if (routes != null && routes.length() > 0) {
            val route = routes.getJSONObject(0)
            val polyline = route.optJSONObject("polyline")
            val localizedValues = route.optJSONObject("localizedValues")
            val distanceText = localizedValues?.optJSONObject("distance")?.optString("text")
            val durationText = localizedValues?.optJSONObject("duration")?.optString("text")

            val transitSteps = if (mode == "TRANSIT") {
                val steps = route.optJSONArray("legs")?.getJSONObject(0)?.optJSONArray("steps")
                steps?.let { parseTransitSteps(it) }
            } else {
                null
            }

            return if (polyline != null) {
                val encodedPolyline = polyline.optString("encodedPolyline")
                if (encodedPolyline.isNotEmpty()) {
                    val decodedPolyline = PolyUtil.decode(encodedPolyline)
                    val temp = RouteInfo(decodedPolyline.toMutableList(), distanceText ?: "", durationText ?: "", transitSteps)
                    println("RouteInfo Object: $temp")
                    temp
                } else {
                    // Handle missing or invalid encodedPolyline
                    null
                }
            } else {
                // Handle missing polyline object
                null
            }
        } else {
            // Handle missing or empty routes array
            return null
        }
    } else {
        // Handle error
        return null
    }
}

private fun parseTransitSteps(stepsArray: JSONArray): List<TransitStep> {
    val transitSteps = mutableListOf<TransitStep>()

    for (i in 0 until stepsArray.length()) {
        val stepObject = stepsArray.getJSONObject(i)
        val travelMode = stepObject.optString("travelMode")
        val transitDetails = stepObject.optJSONObject("transitDetails")

        if (travelMode == "TRANSIT" && transitDetails != null) {
            val headsign = transitDetails.optString("headsign")
            val transitLine = parseTransitLine(transitDetails.optJSONObject("transitLine"))
            val distance = stepObject.optJSONObject("localizedValues")?.optJSONObject("distance")?.optString("text")
            val duration = stepObject.optJSONObject("localizedValues")?.optJSONObject("duration")?.optString("text")

            val transitStep = TransitStep(travelMode, headsign, transitLine, distance ?: "", duration ?: "")
            transitSteps.add(transitStep)
        }
    }

    return transitSteps
}

private fun parseTransitLine(lineObject: JSONObject?): TransitLine? {
    if (lineObject != null) {
        val name = lineObject.optString("name")
        val shortName = lineObject.optString("nameShort")
        val vehicle = parseVehicle(lineObject.optJSONObject("vehicle"))
        return TransitLine(name, shortName, vehicle)
    }
    return null
}

private fun parseVehicle(vehicleObject: JSONObject?): Vehicle? {
    if (vehicleObject != null) {
        val name = vehicleObject.optJSONObject("name")?.optString("text")
        val type = vehicleObject.optString("type")
        return Vehicle(name ?: "", type)
    }
    return null
}