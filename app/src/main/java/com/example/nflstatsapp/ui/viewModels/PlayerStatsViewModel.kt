package com.example.nflstatsapp.ui.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nflstatsapp.data.api.ApiService
import com.example.nflstatsapp.data.api.RetrofitClient
import com.example.nflstatsapp.data.api.PlayerStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class PlayerStatsViewModel : ViewModel() {

    private val apiService: ApiService = RetrofitClient.apiService

    // LiveData to hold the player stats
    private val _playerStats = MutableLiveData<PlayerStats?>()
    val playerStats: LiveData<PlayerStats?> get() = _playerStats

    // LiveData for loading status (to show loading spinner in the UI)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Function to fetch player stats from API
    fun fetchPlayerStats(playerId: String, teamId: String, posId: String) {
        // Show loading spinner
        _isLoading.postValue(true)

        // Make the network request in a coroutine to avoid blocking the UI thread
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Make the GET request
                val response: Response<PlayerStats> =
                    apiService.getPlayerData(playerId, teamId, posId)

                // Check if the request was successful
                if (response.isSuccessful) {
                    // If successful, update playerStats on the main thread
                    _playerStats.postValue(response.body())
                    withContext(Dispatchers.Main) {
                        Log.d("PlayerStatsViewModel", "Player Stats: ${response.body()}")
                    }
                } else {
                    // If the request failed, set error message and handle in the UI thread
                    _errorMessage.postValue("Error: ${response.code()} - ${response.message()}")
                    withContext(Dispatchers.Main) {
                        Log.e("PlayerStatsViewModel", "Error: ${response.code()} - ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                // Handle any exceptions (e.g., network errors)
                _errorMessage.postValue("Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    Log.e("PlayerStatsViewModel", "Exception: ${e.message}")
                }
            } finally {
                // Hide the loading spinner
                _isLoading.postValue(false)
            }
        }
    }
}
