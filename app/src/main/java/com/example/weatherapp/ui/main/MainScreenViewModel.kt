package com.example.weatherapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.service.WeatherService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(val weather: WeatherData) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class MainScreenViewModel : ViewModel() {
    private val weatherService = WeatherService()

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        // Load default city (San Francisco) on launch
        fetchWeather("San Francisco", 37.7749, -122.4194)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun fetchWeather(cityName: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val data = weatherService.fetchWeather(cityName, latitude, longitude)
                _uiState.value = WeatherUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun searchAndFetchWeather(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isSearching.value = true
            _uiState.value = WeatherUiState.Loading
            try {
                val result = weatherService.searchCity(query)
                if (result != null) {
                    val (displayName, coords) = result
                    val (lat, lon) = coords
                    val data = weatherService.fetchWeather(displayName, lat, lon)
                    _uiState.value = WeatherUiState.Success(data)
                    _searchQuery.value = "" // Reset query after successful search
                } else {
                    _uiState.value = WeatherUiState.Error("City '$query' not found. Please try again.")
                }
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(e.message ?: "Failed to find or load weather for '$query'")
            } finally {
                _isSearching.value = false
            }
        }
    }
}
