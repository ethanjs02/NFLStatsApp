package com.example.nflstatsapp.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import com.example.nflstatsapp.data.NFLDatabase
import com.example.nflstatsapp.data.api.PlayerStats
import com.example.nflstatsapp.data.api.RetrofitClient
import com.example.nflstatsapp.data.players.Player
import com.example.nflstatsapp.data.players.PlayerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class PlayerViewModel(private val repository: PlayerRepository) : ViewModel() {

    private val _searchQuery = MutableLiveData<String>("")
    val searchResults: LiveData<List<Player>> = _searchQuery.switchMap { query ->
        if (query.isEmpty()) {
            MutableLiveData(emptyList()) // Show nothing when query is empty
        } else {
            repository.searchPlayers(query)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

class PlayerViewModelFactory(private val repository: PlayerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun fetchPlayerStats(playerId: String, teamId: String, posId: String) {
    // Make the network request in a coroutine to avoid blocking the UI thread
    GlobalScope.launch(Dispatchers.IO) {
        try {
            // Get the API service instance
            val apiService = RetrofitClient.apiService

            // Make the GET request
            val response: Response<PlayerStats> =
                apiService.getPlayerData(playerId, teamId, posId)

            // Check if the request was successful
            if (response.isSuccessful) {
                // If successful, handle the response on the main thread
                val playerStats = response.body()
                withContext(Dispatchers.Main) {
                    Log.d("MainActivity", "Player Stats: $playerStats")
                }
            } else {
                // If the request failed, log the error
                withContext(Dispatchers.Main) {
                    Log.e("MainActivity", "Error: ${response.code()} - ${response.message()}")
                }
            }
        } catch (e: Exception) {
            // Handle any exceptions (e.g., network errors)
            withContext(Dispatchers.Main) {
                Log.e("MainActivity", "Exception: ${e.message}")
            }
        }
    }
}
