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
import java.math.BigDecimal
import java.math.RoundingMode


class PlayerStatsViewModel : ViewModel() {

    private val apiService: ApiService = RetrofitClient.apiService

    // LiveData to hold the player stats
    private val _playerStats = MutableLiveData<PlayerStats?>()
    val playerStats: LiveData<PlayerStats?> get() = _playerStats

    private var gamesPlayed = 0

    private val _totalFantasyPoints = MutableLiveData<BigDecimal>()
    val totalFantasyPoints: LiveData<BigDecimal> get() = _totalFantasyPoints

    private val _fantasyPointsPerGame = MutableLiveData<BigDecimal>()
    val fantasyPointsPerGame: LiveData<BigDecimal> get() = _fantasyPointsPerGame

    var standard = BigDecimal.ZERO
    var halfPpr = BigDecimal.ZERO
    var ppr = BigDecimal.ZERO

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
                    val playerStatsResponse = response.body()
                    _playerStats.postValue(playerStatsResponse)

                    // Access the gamesPlayed value from the response
                    gamesPlayed = playerStatsResponse?.gamesPlayed?.toInt() ?:0

                    //Calculate fantasy points
                    if (playerStatsResponse != null) {
                        fantasyPointsCalculator(playerStatsResponse, posId)
                    }

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

    private fun fantasyPointsCalculator(playerStats: PlayerStats, posId: String) {
        // Safely access nullable properties and perform calculations
        playerStats.totalPassingYards?.toBigDecimal()?.let {
            val points = it.divide(BigDecimal(25), 2, RoundingMode.HALF_UP) // 1 point for every 25 passing yards
            standard = standard.add(points)
            halfPpr = halfPpr.add(points)
            ppr = ppr.add(points)
        }

        playerStats.totalPassingTDs?.toBigDecimal()?.let {
            val points = it.multiply(BigDecimal(4)) // 4 points per passing TD
            standard = standard.add(points)
            halfPpr = halfPpr.add(points)
            ppr = ppr.add(points)
        }

        playerStats.totalInterceptions?.toBigDecimal()?.let {
            val points = it.multiply(BigDecimal(2)) // -2 points per interception
            standard = standard.subtract(points)
            halfPpr = halfPpr.subtract(points)
            ppr = ppr.subtract(points)
        }

        playerStats.totalRushingYards?.toBigDecimal()?.let {
            val points = it.divide(BigDecimal(10), 2, RoundingMode.HALF_UP) // 1 point for every 10 rushing yards
            standard = standard.add(points)
            halfPpr = halfPpr.add(points)
            ppr = ppr.add(points)
        }

        playerStats.totalRushingTDs?.toBigDecimal()?.let {
            val points = it.multiply(BigDecimal(6)) // 6 points per rushing TD
            standard = standard.add(points)
            halfPpr = halfPpr.add(points)
            ppr = ppr.add(points)
        }

        playerStats.totalReceptions?.toBigDecimal()?.let { receptions ->
            val halfPprPoints = receptions.multiply(BigDecimal("0.5")) // Half PPR scoring
            val pprPoints = receptions // Full PPR scoring
            halfPpr = halfPpr.add(halfPprPoints)
            ppr = ppr.add(pprPoints)
        }

        playerStats.totalReceivingYards?.toBigDecimal()?.let {
            val points = it.divide(BigDecimal(10), 2, RoundingMode.HALF_UP) // 1 point for every 10 receiving yards
            standard = standard.add(points)
            halfPpr = halfPpr.add(points)
            ppr = ppr.add(points)
        }

        playerStats.totalReceivingTDs?.toBigDecimal()?.let {
            val points = it.multiply(BigDecimal(6)) // 6 points per receiving TD
            standard = standard.add(points)
            halfPpr = halfPpr.add(points)
            ppr = ppr.add(points)
        }

        // Handle kickers (posId == "K")
        if (posId == "K") {
            playerStats.fieldGoalsMade?.toBigDecimal()?.let { fgMade ->
                val points = fgMade.multiply(BigDecimal(3)) // 3 points per field goal
                standard = standard.add(points)
                halfPpr = halfPpr.add(points)
                ppr = ppr.add(points)
            }

            playerStats.extraPointsMade?.toBigDecimal()?.let { xpMade ->
                val points = xpMade.multiply(BigDecimal(1)) // 1 point per extra point
                standard = standard.add(points)
                halfPpr = halfPpr.add(points)
                ppr = ppr.add(points)
            }
        }

        // Log or use the calculated fantasy points
        Log.d("FantasyPoints", "Standard: $standard, Half PPR: $halfPpr, PPR: $ppr")
        _totalFantasyPoints.postValue(standard)
        _fantasyPointsPerGame.postValue(standard.divide(BigDecimal(gamesPlayed), 2, RoundingMode.HALF_UP))
    }

    private fun updateFantasyPoints(totalPoints: BigDecimal, gamesPlayed: Int) {
        _totalFantasyPoints.value = totalPoints
        _fantasyPointsPerGame.value = if (gamesPlayed > 0) {
            totalPoints.divide(BigDecimal(gamesPlayed), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
    }

    fun onScoringTypeSelected(scoringType: String) {
        when (scoringType) {
            "Standard" -> updateFantasyPoints(standard, gamesPlayed)
            "Half-PPR" -> updateFantasyPoints(halfPpr, gamesPlayed)
            "PPR" -> updateFantasyPoints(ppr, gamesPlayed)
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
        playerStats.totalInterceptions?.let { statList.add(Stat("Total Interceptions", it)) }
        playerStats.totalRushingYards?.let { statList.add(Stat("Total Rushing Yards", it)) }
        playerStats.avgRushingYards?.let { statList.add(Stat("Avg Rushing Yards", it)) }
        playerStats.totalRushingTDs?.let { statList.add(Stat("Total Rushing TDs", it)) }
        playerStats.totalRushAttempts?.let { statList.add(Stat("Total Rush Attempts", it)) }
        playerStats.avgRushAttempts?.let { statList.add(Stat("Avg Rush Attempts", it)) }
        playerStats.rushShare?.let { statList.add(Stat("Rushing Share %", it)) }
        playerStats.totalReceivingYards?.let { statList.add(Stat("Total Receiving Yards", it)) }
        playerStats.receivingYardsPerGame?.let { statList.add(Stat("Receiving Yards Per Game", it)) }
        playerStats.totalReceivingTDs?.let { statList.add(Stat("Total Receiving TDs", it)) }
        playerStats.totalReceptions?.let { statList.add(Stat("Total Receptions", it)) }
        playerStats.targetShare?.let { statList.add(Stat("Target Share %", it)) }
        playerStats.totalFumbles?.let { statList.add(Stat("Total Fumbles", it)) }
        playerStats.extraPointAttempts?.let { statList.add(Stat("Extra Point Attempts", it)) }
        playerStats.extraPointPct?.let { statList.add(Stat("Extra Point %", it)) }
        playerStats.extraPointsMade?.let { statList.add(Stat("Extra Points Made", it)) }
        playerStats.fieldGoalAttempts?.let { statList.add(Stat("Field Goal Attempts", it)) }
        playerStats.fieldGoalAttempts1_19?.let { statList.add(Stat("Field Goal Attempts 1-19", it)) }
        playerStats.fieldGoalAttempts20_29?.let { statList.add(Stat("Field Goal Attempts 20-29", it)) }
        playerStats.fieldGoalAttempts30_39?.let { statList.add(Stat("Field Goal Attempts 30-39", it)) }
        playerStats.fieldGoalAttempts40_49?.let { statList.add(Stat("Field Goal Attempts 40-49", it)) }
        playerStats.fieldGoalAttempts50_59?.let { statList.add(Stat("Field Goal Attempts 50-59", it)) }
        playerStats.fieldGoalAttempts60_99?.let { statList.add(Stat("Field Goal Attempts 60-99", it)) }
        playerStats.fieldGoalPct?.let { statList.add(Stat("Field Goal %", it)) }
        playerStats.fieldGoalsMade?.let { statList.add(Stat("Field Goals Made", it)) }
        playerStats.fieldGoalsMade1_19?.let { statList.add(Stat("Field Goals Made 1-19", it)) }
        playerStats.fieldGoalsMade20_29?.let { statList.add(Stat("Field Goals Made 20-29", it)) }
        playerStats.fieldGoalsMade30_39?.let { statList.add(Stat("Field Goals Made 30-39", it)) }
        playerStats.fieldGoalsMade40_49?.let { statList.add(Stat("Field Goals Made 40-49", it)) }
        playerStats.fieldGoalsMade50_59?.let { statList.add(Stat("Field Goals Made 50-59", it)) }
        playerStats.fieldGoalsMade60_99?.let { statList.add(Stat("Field Goals Made 60-99", it)) }
        playerStats.longFieldGoalMade?.let { statList.add(Stat("Long Field Goal Made", it)) }

        return statList
    }


}
