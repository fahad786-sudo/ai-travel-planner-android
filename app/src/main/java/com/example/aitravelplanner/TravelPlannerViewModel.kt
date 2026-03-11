package com.example.aitravelplanner // Important: Replace with your actual package name

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Represents the different states of our UI
sealed interface TravelPlanUiState {
    object Initial : TravelPlanUiState
    object Loading : TravelPlanUiState
    data class Success(val itinerary: String) : TravelPlanUiState
    data class Error(val errorMessage: String) : TravelPlanUiState
}

class TravelPlannerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<TravelPlanUiState>(TravelPlanUiState.Initial)
    val uiState: StateFlow<TravelPlanUiState> = _uiState.asStateFlow()

    // Initialize the Gemini Model
    // Note: If you didn't set up BuildConfig for the key, you can TEMPORARILY paste
    // your raw API key here like: apiKey = "AIzaSy..." to test it quickly.
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "................"
    )


    fun generateItinerary(destination: String) {
        if (destination.isBlank()) return

        // Set state to loading so UI shows a spinner
        _uiState.value = TravelPlanUiState.Loading

        viewModelScope.launch {
            try {
                // The prompt instructing the AI what to do
                val prompt = """
                    You are an expert travel planner. Create a detailed 3-day travel itinerary for $destination. 
                    Format the output clearly by Days (Day 1, Day 2, Day 3).
                    For each place to visit, provide a short, engaging description and why it is worth visiting.
                """.trimIndent()

                // Call the Gemini API
                val response = generativeModel.generateContent(prompt)

                // Update UI upon success
                if (response.text != null) {
                    _uiState.value = TravelPlanUiState.Success(response.text!!)
                } else {
                    _uiState.value = TravelPlanUiState.Error("Received empty response from AI.")
                }
            } catch (e: Exception) {
                // Update UI if there's an error (e.g., no internet, bad API key)
                _uiState.value = TravelPlanUiState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }
}
