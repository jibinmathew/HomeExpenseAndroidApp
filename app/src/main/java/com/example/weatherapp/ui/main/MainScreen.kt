package com.example.weatherapp.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.weatherapp.model.WeatherConstants
import com.example.weatherapp.model.WeatherData

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel { MainScreenViewModel() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    // Determine background gradient depending on weather condition
    val backgroundBrush = when (val state = uiState) {
        is WeatherUiState.Success -> {
            val code = state.weather.weatherCode
            when {
                code == 0 -> Brush.verticalGradient(listOf(Color(0xFF2196F3), Color(0xFFFF9800))) // Sunny sky/warm
                code in listOf(1, 2, 3) -> Brush.verticalGradient(listOf(Color(0xFF607D8B), Color(0xFF90A4AE))) // Cloudy
                code in listOf(45, 48) -> Brush.verticalGradient(listOf(Color(0xFF78909C), Color(0xFFB0BEC5))) // Fog
                code in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82) -> Brush.verticalGradient(listOf(Color(0xFF37474F), Color(0xFF1A237E))) // Rainy
                code in listOf(71, 73, 75, 77, 85, 86) -> Brush.verticalGradient(listOf(Color(0xFF80DEEA), Color(0xFF00ACC1))) // Snowy
                code in listOf(95, 96, 99) -> Brush.verticalGradient(listOf(Color(0xFF212121), Color(0xFF3E2723))) // Thunderstorm
                else -> Brush.verticalGradient(listOf(Color(0xFF1E88E5), Color(0xFF1565C0)))
            }
        }
        else -> Brush.verticalGradient(listOf(Color(0xFF121212), Color(0xFF1E1E1E)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App title
            Text(
                text = "ANTIGRAVITY WEATHER",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Premium Custom Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onSearch = { viewModel.searchAndFetchWeather(searchQuery) },
                isSearching = isSearching
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Preset Quick Select Chips
            PresetChips(onPresetSelected = { name, lat, lon ->
                viewModel.fetchWeather(name, lat, lon)
            })

            Spacer(modifier = Modifier.height(24.dp))

            // Content depending on state
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                when (val state = uiState) {
                    is WeatherUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                    is WeatherUiState.Success -> {
                        WeatherDashboard(weather = state.weather)
                    }
                    is WeatherUiState.Error -> {
                        ErrorCard(message = state.message)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isSearching: Boolean
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search any city (e.g. Paris)...", color = Color.White.copy(alpha = 0.6f)) },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.15f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.10f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() }
        ),
        trailingIcon = {
            if (isSearching) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(onClick = onSearch) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(30.dp))
    )
}

@Composable
fun PresetChips(onPresetSelected: (String, Double, Double) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(WeatherConstants.PRESETS) { preset ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    .clickable { onPresetSelected(preset.name, preset.latitude, preset.longitude) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = preset.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun WeatherDashboard(weather: WeatherData) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Main Weather Glassmorphic Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = weather.cityName.uppercase(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Large Weather Icon / Emoji
                Text(
                    text = WeatherConstants.getWeatherEmoji(weather.weatherCode),
                    fontSize = 84.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Current Temperature
                Text(
                    text = "${weather.temperature}°C",
                    fontSize = 68.sp,
                    fontWeight = FontWeight.W300,
                    color = Color.White
                )

                // Weather Condition Text
                Text(
                    text = WeatherConstants.getWeatherDescription(weather.weatherCode),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Secondary Info Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricCard(
                    title = "Feels Like",
                    value = "${weather.apparentTemperature}°C",
                    icon = "🌡️"
                )
                MetricCard(
                    title = "Wind Speed",
                    value = "${weather.windSpeed} km/h",
                    icon = "💨"
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricCard(
                    title = "Humidity",
                    value = "${weather.humidity}%",
                    icon = "💧"
                )
                MetricCard(
                    title = "Time of Day",
                    value = if (weather.isDay) "Day" else "Night",
                    icon = if (weather.isDay) "☀️" else "🌙"
                )
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, icon: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ErrorCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFD32F2F).copy(alpha = 0.2f))
            .border(1.dp, Color(0xFFD32F2F).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "⚠️",
                fontSize = 40.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
