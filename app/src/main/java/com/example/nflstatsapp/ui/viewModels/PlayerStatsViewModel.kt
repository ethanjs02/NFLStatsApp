package com.example.nflstatsapp.ui.viewModels

import PlayerStatsData
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.nflstatsapp.data.api.ApiService
import com.example.nflstatsapp.data.api.RetrofitClient
import com.example.nflstatsapp.data.api.PlayerStats
import com.example.nflstatsapp.data.stats.Comparison
import com.example.nflstatsapp.data.stats.Stat
import com.example.nflstatsapp.data.teams.TeamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.math.RoundingMode


class PlayerStatsViewModel(private val teamRepository: TeamRepository) : ViewModel() {

    private val apiService: ApiService = RetrofitClient.apiService

    private val _headshotData = MutableLiveData<Bitmap?>()
    val headshotData: LiveData<Bitmap?> get() = _headshotData

    // LiveData to hold the player stats
    private val _playerStats = MutableLiveData<PlayerStats?>()
    val playerStats: LiveData<PlayerStats?> get() = _playerStats

    private var gamesPlayed = 0

    private var teamData = ""

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

    fun fetchTeamData(teamId: Int) = liveData(Dispatchers.IO) {
        val team = teamRepository.getTeamById(teamId) // Suspend function call
        teamData = team?.name.toString()
        emit(team)
    }

    fun fetchHeadshot(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("fetchHeadshot", "Starting to fetch headshot from URL: $url")

                // Make the API call to fetch the player headshot
                val response = apiService.getPlayerHeadshot(url)

                // Check if the response is successful
                if (response.isSuccessful) {
                    // Get the image body as bytes
                    Log.d("fetchHeadshot", response.body().toString())

                    val imageBody = response.body()?.byteStream()

                    // Decode the InputStream to a Bitmap
                    val bitmap = imageBody?.let { BitmapFactory.decodeStream(it) }

                    // Check if the bitmap is valid
                    if (bitmap != null) {
                        // Post the Bitmap to LiveData
                        withContext(Dispatchers.Main) {
                            _headshotData.postValue(bitmap)
                            Log.d("fetchHeadshot", "Successfully fetched and decoded the image")
                        }
                    } else {
                        // Handle decoding failure
                        withContext(Dispatchers.Main) {
                            _headshotData.postValue(null)
                            Log.e("fetchHeadshot", "Failed to decode image data")
                        }
                    }
                } else {
                    // Handle non-2xx HTTP responses
                    withContext(Dispatchers.Main) {
                        _headshotData.postValue(null)
                        Log.e("fetchHeadshot", "Error fetching image: ${response.code()} ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                // Handle general exceptions (e.g., network errors)
                withContext(Dispatchers.Main) {
                    _headshotData.postValue(null)
                    Log.e("fetchHeadshot", "Exception occurred while fetching image: ${e.localizedMessage}", e)
                }
            }
        }
    }


    // Function to fetch player stats from API
    fun fetchPlayerStats(playerId: String, teamId: String, posId: String, tabValue: String) {
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
                        fantasyPointsCalculator(playerStatsResponse, tabValue)
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

    private fun fantasyPointsCalculator(playerStats: PlayerStats, tabValue: String) {
        // Safely access nullable properties and perform calculations
        playerStats.totalPassingYards?.toBigDecimal()?.let {
            val points = it.divide(BigDecimal(25), 2, RoundingMode.HALF_UP) // 1 point for every 25 passing yards
            standard = standard.add(points)
        }

        playerStats.totalPassingTDs?.toBigDecimal()?.let {
            val points = it.multiply(BigDecimal(4)) // 4 points per passing TD
            standard = standard.add(points)
        }

        playerStats.totalInterceptions?.toBigDecimal()?.let {
            val points = it.multiply(BigDecimal(2)) // -2 points per interception
            standard = standard.subtract(points)
        }

        playerStats.totalRushingYards?.toBigDecimal()?.let {
            val points = it.divide(BigDecimal(10), 2, RoundingMode.HALF_UP) // 1 point for every 10 rushing yards
            standard = standard.add(points)
        }

        playerStats.totalRushingTDs?.toBigDecimal()?.let {
            val points = it.multiply(BigDecimal(6)) // 6 points per rushing TD
            standard = standard.add(points)
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
        }

        playerStats.totalReceivingTDs?.toBigDecimal()?.let {
            val points = it.multiply(BigDecimal(6)) // 6 points per receiving TD
            standard = standard.add(points)
        }

        playerStats.extraPointsMade?.toBigDecimal()?.let { xpMade ->
            val points = xpMade.multiply(BigDecimal(1)) // 1 point per extra point
            standard = standard.add(points)
        }

        // Handle fumbles lost (-2 points for each fumble lost)
        playerStats.totalFumbles?.toBigDecimal()?.let {
            val points = it.multiply(BigDecimal(2)) // -2 points for each fumble lost
            standard = standard.subtract(points)
        }

        // Handle field goal distance
        playerStats.fieldGoalsMade1_19?.toBigDecimal()?.let { fg1To19 ->
            val points = fg1To19.multiply(BigDecimal(3)) // 3 points for field goals made from 1-19 yards
            standard = standard.add(points)
        }
        playerStats.fieldGoalsMade20_29?.toBigDecimal()?.let { fg20To29 ->
            val points = fg20To29.multiply(BigDecimal(3)) // 3 points for field goals made from 20-29 yards
            standard = standard.add(points)
        }
        playerStats.fieldGoalsMade30_39?.toBigDecimal()?.let { fg30To39 ->
            val points = fg30To39.multiply(BigDecimal(3)) // 3 points for field goals made from 30-39 yards
            standard = standard.add(points)
        }
        playerStats.fieldGoalsMade40_49?.toBigDecimal()?.let { fg40To49 ->
            val points = fg40To49.multiply(BigDecimal(4)) // 4 points for field goals made from 40-49 yards
            standard = standard.add(points)
        }
        playerStats.fieldGoalsMade50_59?.toBigDecimal()?.let { fg50To59 ->
            val points = fg50To59.multiply(BigDecimal(5)) // 5 points for field goals made from 50-59 yards
            standard = standard.add(points)
        }
        playerStats.fieldGoalsMade60_99?.toBigDecimal()?.let { fg60To99 ->
            val points = fg60To99.multiply(BigDecimal(6)) // 6 points for field goals made from 60-99 yards
            standard = standard.add(points)
        }

        halfPpr = halfPpr.add(standard)
        ppr = ppr.add(standard)

        // Log or use the calculated fantasy points
        Log.d("FantasyPoints", "Standard: $standard, Half PPR: $halfPpr, PPR: $ppr")

        // Calculate total fantasy points per game
        if (tabValue == "Standard") {
            _totalFantasyPoints.postValue(standard)
            _fantasyPointsPerGame.postValue(standard.divide(BigDecimal(playerStats.gamesPlayed?.toInt() ?: 1), 2, RoundingMode.HALF_UP))
        } else if (tabValue == "Half-PPR") {
            // Logic for "Half-PPR"
            _totalFantasyPoints.postValue(halfPpr)
            _fantasyPointsPerGame.postValue(halfPpr.divide(BigDecimal(playerStats.gamesPlayed?.toInt() ?: 1), 2, RoundingMode.HALF_UP))
        } else {
            // Logic for "PPR"
            _totalFantasyPoints.postValue(ppr)
            _fantasyPointsPerGame.postValue(ppr.divide(BigDecimal(playerStats.gamesPlayed?.toInt() ?: 1), 2, RoundingMode.HALF_UP))
        }

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

    fun aggregateData(name: String?, pos: String?, jersey: String?): PlayerStatsData {
        // Convert Bitmap to ByteArray
        val byteArrayOutputStream = ByteArrayOutputStream()
        _headshotData.value?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val headshotByteArray = byteArrayOutputStream.toByteArray()

        return PlayerStatsData(
            fullName = name,
            position = pos,
            teamName = teamData,
            jerseyNumber = jersey,
            fantasyPointsPerGame = _fantasyPointsPerGame.value,
            headshotData = headshotByteArray,
            stats = _playerStats.value
        )
    }

    fun mapPlayerStatsToList(playerStats: PlayerStats, pos: String): List<Stat> {
        val statList = mutableListOf<Stat>()

        // Games Played is always the top stat
        playerStats.gamesPlayed?.let { statList.add(Stat("Games Played", it)) }

        when (pos) {
            "Quarterback" -> {
                playerStats.totalPassingYards?.let { statList.add(Stat("Total Passing Yards", it)) }
                playerStats.avgPassingYards?.let { statList.add(Stat("Avg Passing Yards", it)) }
                playerStats.totalPassingTDs?.let { statList.add(Stat("Total Passing TDs", it)) }
                playerStats.totalPassAttempts?.let { statList.add(Stat("Total Pass Attempts", it)) }
                playerStats.totalInterceptions?.let { statList.add(Stat("Total Interceptions", it)) }
                playerStats.teamPassAttempts?.let { statList.add(Stat("Team Passing Attempts", it)) }
                playerStats.totalRushingYards?.let { statList.add(Stat("Total Rushing Yards", it)) }
                playerStats.avgRushingYards?.let { statList.add(Stat("Avg Rushing Yards", it)) }
                playerStats.totalRushingTDs?.let { statList.add(Stat("Total Rushing TDs", it)) }
                playerStats.totalRushAttempts?.let { statList.add(Stat("Total Rush Attempts", it)) }
                playerStats.rushShare?.let { statList.add(Stat("Rushing Share %", it)) }
                playerStats.teamRushAttempts?.let { statList.add(Stat("Team Rushing Attempts", it))}
            }
            "Running Back", "Fullback" -> {
                playerStats.totalRushingYards?.let { statList.add(Stat("Total Rushing Yards", it)) }
                playerStats.avgRushingYards?.let { statList.add(Stat("Avg Rushing Yards", it)) }
                playerStats.totalRushingTDs?.let { statList.add(Stat("Total Rushing TDs", it)) }
                playerStats.totalRushAttempts?.let { statList.add(Stat("Total Rush Attempts", it)) }
                playerStats.rushShare?.let { statList.add(Stat("Rushing Share %", it)) }
                playerStats.teamRushAttempts?.let { statList.add(Stat("Team Rushing Attempts", it))}
                playerStats.totalReceivingYards?.let { statList.add(Stat("Total Receiving Yards", it)) }
                playerStats.receivingYardsPerGame?.let { statList.add(Stat("Receiving Yards Per Game", it)) }
                playerStats.totalReceivingTDs?.let { statList.add(Stat("Total Receiving TDs", it)) }
                playerStats.totalReceptions?.let { statList.add(Stat("Total Receptions", it)) }
                playerStats.targetShare?.let { statList.add(Stat("Target Share %", it)) }
                playerStats.teamPassAttempts?.let { statList.add(Stat("Team Passing Attempts", it))}
            }
            "Wide Receiver", "Tight End" -> {
                playerStats.totalReceivingYards?.let { statList.add(Stat("Total Receiving Yards", it)) }
                playerStats.receivingYardsPerGame?.let { statList.add(Stat("Receiving Yards Per Game", it)) }
                playerStats.totalReceivingTDs?.let { statList.add(Stat("Total Receiving TDs", it)) }
                playerStats.totalReceptions?.let { statList.add(Stat("Total Receptions", it)) }
                playerStats.targetShare?.let { statList.add(Stat("Target Share %", it)) }
                playerStats.teamPassAttempts?.let { statList.add(Stat("Team Passing Attempts", it))}

                if ((playerStats.totalRushingYards?.toInt())!! > 0) {
                    playerStats.totalRushingYards.let { statList.add(Stat("Total Rushing Yards", it)) }
                    playerStats.avgRushingYards?.let { statList.add(Stat("Avg Rushing Yards", it)) }
                    playerStats.totalRushingTDs?.let { statList.add(Stat("Total Rushing TDs", it)) }
                    playerStats.teamRushAttempts?.let { statList.add(Stat("Team Rushing Attempts", it))}
                }
            }

            "Place kicker" -> {
                playerStats.extraPointAttempts?.let { statList.add(Stat("Extra Point Attempts", it)) }
                playerStats.extraPointPct?.let { statList.add(Stat("Extra Point %", it)) }
                playerStats.extraPointsMade?.let { statList.add(Stat("Extra Points Made", it)) }
                playerStats.fieldGoalAttempts?.let { statList.add(Stat("Field Goal Attempts", it)) }
                playerStats.fieldGoalPct?.let { statList.add(Stat("Field Goal %", it)) }
                playerStats.fieldGoalsMade?.let { statList.add(Stat("Field Goals Made", it)) }
                playerStats.fieldGoalAttempts1_19?.let { statList.add(Stat("Field Goal Attempts 1-19", it)) }
                playerStats.fieldGoalsMade1_19?.let { statList.add(Stat("Field Goals Made 1-19", it)) }
                playerStats.fieldGoalAttempts20_29?.let { statList.add(Stat("Field Goal Attempts 20-29", it)) }
                playerStats.fieldGoalsMade20_29?.let { statList.add(Stat("Field Goals Made 20-29", it)) }
                playerStats.fieldGoalAttempts30_39?.let { statList.add(Stat("Field Goal Attempts 30-39", it)) }
                playerStats.fieldGoalsMade30_39?.let { statList.add(Stat("Field Goals Made 30-39", it)) }
                playerStats.fieldGoalAttempts40_49?.let { statList.add(Stat("Field Goal Attempts 40-49", it)) }
                playerStats.fieldGoalsMade40_49?.let { statList.add(Stat("Field Goals Made 40-49", it)) }
                playerStats.fieldGoalAttempts50_59?.let { statList.add(Stat("Field Goal Attempts 50-59", it)) }
                playerStats.fieldGoalsMade50_59?.let { statList.add(Stat("Field Goals Made 50-59", it)) }
                playerStats.fieldGoalAttempts60_99?.let { statList.add(Stat("Field Goal Attempts 60-99", it)) }
                playerStats.fieldGoalsMade60_99?.let { statList.add(Stat("Field Goals Made 60-99", it)) }
                playerStats.longFieldGoalMade?.let { statList.add(Stat("Long Field Goal Made", it)) }

            }
        }

        // Include fumbles for all positions
        playerStats.fumblesLost?.let { statList.add(Stat("Fumbles Lost", it)) }

        return statList
    }

    fun mapStatsToComparison(player1: PlayerStats, player2: PlayerStats): List<Comparison> {
        val comparisonList = mutableListOf<Comparison>()

        // Helper function to add comparisons
        fun addComparison(statName: String, value1: String?, value2: String?) {
            if (value1 != null && value2 != null) {
                comparisonList.add(Comparison(statName, value1, value2))
            }
        }

        player1.fantasyPpg?.let { Log.d("in map", it) }
        player2.fantasyPpg?.let { Log.d("in map", it) }


        // Games Played comparison
        addComparison("Games Played", player1.gamesPlayed, player2.gamesPlayed)
        addComparison("Fantasy PPG", player1.fantasyPpg, player2.fantasyPpg)

        // Quarterback Stats
        addComparison("Total Passing Yards", player1.totalPassingYards, player2.totalPassingYards)
        addComparison("Avg Passing Yards", player1.avgPassingYards, player2.avgPassingYards)
        addComparison("Total Passing TDs", player1.totalPassingTDs, player2.totalPassingTDs)
        addComparison("Total Pass Attempts", player1.totalPassAttempts, player2.totalPassAttempts)
        addComparison("Total Interceptions", player1.totalInterceptions, player2.totalInterceptions)

        // Rushing Stats (for all players)
        addComparison("Total Rushing Yards", player1.totalRushingYards, player2.totalRushingYards)
        addComparison("Avg Rushing Yards", player1.avgRushingYards, player2.avgRushingYards)
        addComparison("Total Rushing TDs", player1.totalRushingTDs, player2.totalRushingTDs)
        addComparison("Total Rush Attempts", player1.totalRushAttempts, player2.totalRushAttempts)
        addComparison("Rushing Share %", player1.rushShare, player2.rushShare)
        addComparison("Team Rushing Attempts", player1.teamRushAttempts, player2.teamRushAttempts)

        // Receiving Stats
        addComparison("Total Receiving Yards", player1.totalReceivingYards, player2.totalReceivingYards)
        addComparison("Receiving Yards Per Game", player1.receivingYardsPerGame, player2.receivingYardsPerGame)
        addComparison("Total Receiving TDs", player1.totalReceivingTDs, player2.totalReceivingTDs)
        addComparison("Total Receptions", player1.totalReceptions, player2.totalReceptions)
        addComparison("Target Share %", player1.targetShare, player2.targetShare)
        addComparison("Team Passing Attempts", player1.teamPassAttempts, player2.teamPassAttempts)

        // Place Kicker Stats
        addComparison("Extra Point Attempts", player1.extraPointAttempts, player2.extraPointAttempts)
        addComparison("Extra Point %", player1.extraPointPct, player2.extraPointPct)
        addComparison("Extra Points Made", player1.extraPointsMade, player2.extraPointsMade)
        addComparison("Field Goal Attempts", player1.fieldGoalAttempts, player2.fieldGoalAttempts)
        addComparison("Field Goal %", player1.fieldGoalPct, player2.fieldGoalPct)
        addComparison("Field Goals Made", player1.fieldGoalsMade, player2.fieldGoalsMade)
        addComparison("Field Goal Attempts 1-19", player1.fieldGoalAttempts1_19, player2.fieldGoalAttempts1_19)
        addComparison("Field Goals Made 1-19", player1.fieldGoalsMade1_19, player2.fieldGoalsMade1_19)
        addComparison("Field Goal Attempts 20-29", player1.fieldGoalAttempts20_29, player2.fieldGoalAttempts20_29)
        addComparison("Field Goals Made 20-29", player1.fieldGoalsMade20_29, player2.fieldGoalsMade20_29)
        addComparison("Field Goal Attempts 30-39", player1.fieldGoalAttempts30_39, player2.fieldGoalAttempts30_39)
        addComparison("Field Goals Made 30-39", player1.fieldGoalsMade30_39, player2.fieldGoalsMade30_39)
        addComparison("Field Goal Attempts 40-49", player1.fieldGoalAttempts40_49, player2.fieldGoalAttempts40_49)
        addComparison("Field Goals Made 40-49", player1.fieldGoalsMade40_49, player2.fieldGoalsMade40_49)
        addComparison("Field Goal Attempts 50-59", player1.fieldGoalAttempts50_59, player2.fieldGoalAttempts50_59)
        addComparison("Field Goals Made 50-59", player1.fieldGoalsMade50_59, player2.fieldGoalsMade50_59)
        addComparison("Field Goal Attempts 60-99", player1.fieldGoalAttempts60_99, player2.fieldGoalAttempts60_99)
        addComparison("Field Goals Made 60-99", player1.fieldGoalsMade60_99, player2.fieldGoalsMade60_99)
        addComparison("Long Field Goal Made", player1.longFieldGoalMade, player2.longFieldGoalMade)

        // Fumbles Lost comparison
        addComparison("Fumbles Lost", player1.fumblesLost, player2.fumblesLost)

        Log.d("In comparison map", comparisonList.toString())

        return comparisonList
    }


}

