package com.example.weatherapp.service

import com.example.weatherapp.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class WeatherService {

    suspend fun fetchWeather(cityName: String, latitude: Double, longitude: Double): WeatherData = withContext(Dispatchers.IO) {
        val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,wind_speed_10m"
        val response = getHttpResponse(urlString)
        val json = JSONObject(response)
        val currentJson = json.getJSONObject("current")

        WeatherData(
            cityName = cityName,
            latitude = latitude,
            longitude = longitude,
            temperature = currentJson.getDouble("temperature_2m"),
            apparentTemperature = currentJson.getDouble("apparent_temperature"),
            humidity = currentJson.getInt("relative_humidity_2m"),
            windSpeed = currentJson.getDouble("wind_speed_10m"),
            weatherCode = currentJson.getInt("weather_code"),
            isDay = currentJson.getInt("is_day") == 1
        )
    }

    suspend fun searchCity(query: String): Pair<String, Pair<Double, Double>>? = withContext(Dispatchers.IO) {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val urlString = "https://geocoding-api.open-meteo.com/v1/search?name=$encodedQuery&count=1&language=en&format=json"
        
        try {
            val response = getHttpResponse(urlString)
            val json = JSONObject(response)
            if (!json.has("results")) return@withContext null
            
            val results = json.getJSONArray("results")
            if (results.length() == 0) return@withContext null
            
            val firstResult = results.getJSONObject(0)
            val name = firstResult.getString("name")
            val country = if (firstResult.has("country")) firstResult.getString("country") else ""
            val displayName = if (country.isNotEmpty()) "$name, $country" else name
            val lat = firstResult.getDouble("latitude")
            val lon = firstResult.getDouble("longitude")
            
            displayName to (lat to lon)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getHttpResponse(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()
            connection.disconnect()
            return response.toString()
        } else {
            connection.disconnect()
            throw Exception("Failed HTTP request: Code $responseCode")
        }
    }
}
