package com.example.weatherapp.model

data class WeatherData(
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCode: Int,
    val isDay: Boolean
)

data class CityPreset(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

object WeatherConstants {
    val PRESETS = listOf(
        CityPreset("San Francisco", 37.7749, -122.4194),
        CityPreset("New York", 40.7128, -74.0060),
        CityPreset("London", 51.5074, -0.1278),
        CityPreset("Tokyo", 35.6762, 139.6503),
        CityPreset("Sydney", -33.8688, 151.2093)
    )

    fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing Drizzle"
            61, 63, 65 -> "Rainy"
            66, 67 -> "Freezing Rain"
            71, 73, 75 -> "Snowy"
            77 -> "Snow Grains"
            80, 81, 82 -> "Rain Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with Hail"
            else -> "Unknown Weather"
        }
    }

    fun getWeatherEmoji(code: Int): String {
        return when (code) {
            0 -> "☀️"
            1 -> "🌤️"
            2 -> "⛅"
            3 -> "☁️"
            45, 48 -> "🌫️"
            51, 53, 55 -> "🌧️"
            56, 57 -> "🌨️"
            61, 63, 65 -> "🌧️"
            66, 67 -> "🌨️"
            71, 73, 75 -> "❄️"
            77 -> "❄️"
            80, 81, 82 -> "🌧️"
            85, 86 -> "🌨️"
            95 -> "⛈️"
            96, 99 -> "⛈️"
            else -> "🌡️"
        }
    }
}
