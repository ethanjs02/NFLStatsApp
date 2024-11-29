package com.example.nflstatsapp.ui.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nflstatsapp.data.api.ApiService
import com.example.nflstatsapp.data.api.RetrofitClient
import com.example.nflstatsapp.data.api.PlayerStats
import com.example.nflstatsapp.data.api.Stat
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

    fun mapPlayerStatsToList(playerStats: PlayerStats): List<Stat> {
        val statList = mutableListOf<Stat>()

        // Map each stat to a name and value
        playerStats.gamesPlayed?.let { statList.add(Stat("Games Played", it)) }
        playerStats.fumblesLost?.let { statList.add(Stat("Fumbles Lost", it)) }
        playerStats.totalPassingYards?.let { statList.add(Stat("Total Passing Yards", it)) }
        playerStats.avgPassingYards?.let { statList.add(Stat("Avg Passing Yards", it)) }
        playerStats.totalPassingTDs?.let { statList.add(Stat("Total Passing TDs", it)) }
        playerStats.totalPassAttempts?.let { statList.add(Stat("Total Pass Attempts", it)) }
        playerStats.totalRushingYards?.let { statList.add(Stat("Total Rushing Yards", it)) }
        playerStats.avgRushingYards?.let { statList.add(Stat("Avg Rushing Yards", it)) }
        playerStats.totalRushingTDs?.let { statList.add(Stat("Total Rushing TDs", it)) }
        playerStats.totalRushAttempts?.let { statList.add(Stat("Total Rush Attempts", it)) }
        playerStats.avgRushAttempts?.let { statList.add(Stat("Avg Rush Attempts", it)) }
        playerStats.totalInterceptions?.let { statList.add(Stat("Total Interceptions", it)) }
        playerStats.totalReceivingYards?.let { statList.add(Stat("Total Receiving Yards", it)) }
        playerStats.receivingYardsPerGame?.let { statList.add(Stat("Receiving Yards Per Game", it)) }
        playerStats.totalReceivingTDs?.let { statList.add(Stat("Total Receiving TDs", it)) }
        playerStats.totalTargets?.let { statList.add(Stat("Total Targets", it)) }
        playerStats.totalFumbles?.let { statList.add(Stat("Total Fumbles", it)) }
        playerStats.extraPointAttempts?.let { statList.add(Stat("Extra Point Attempts", it)) }
        playerStats.extraPointPct?.let { statList.add(Stat("Extra Point %", it)) }
        playerStats.extraPointsMade?.let { statList.add(Stat("Extra Points Made", it)) }
        playerStats.fieldGoalAttempts?.let { statList.add(Stat("Field Goal Attempts", it)) }
        playerStats.fieldGoalPct?.let { statList.add(Stat("Field Goal %", it)) }
        playerStats.fieldGoalsMade?.let { statList.add(Stat("Field Goals Made", it)) }
        playerStats.longFieldGoalMade?.let { statList.add(Stat("Long Field Goal Made", it)) }
        playerStats.teamPassAttempts?.let { statList.add(Stat("Team Pass Attempts", it)) }
        playerStats.teamRushAttempts?.let { statList.add(Stat("Team Rush Attempts", it)) }

        return statList
    }

}
