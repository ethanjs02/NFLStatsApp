package com.example.nflstatsapp.ui.activties

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.nflstatsapp.R
import com.example.nflstatsapp.data.api.PlayerStats
import com.example.nflstatsapp.data.players.Player
import com.example.nflstatsapp.ui.viewModels.PlayerStatsViewModel

class StatsActivity : AppCompatActivity() {

    private lateinit var player: Player
    private lateinit var playerHeadshot: ImageView
    private lateinit var playerName: TextView
    private lateinit var playerPosition: TextView
    private lateinit var playerTeam: TextView

    // Stats views
    private lateinit var gamesPlayed: TextView
    private lateinit var fumblesLost: TextView
    private lateinit var passingYards: TextView
    private lateinit var avgPassingYards: TextView
    private lateinit var passingTDs: TextView
    private lateinit var rushAttempts: TextView
    private lateinit var rushingYards: TextView
    private lateinit var avgRushingYards: TextView
    private lateinit var rushingTDs: TextView
    private lateinit var interceptions: TextView
    private lateinit var receivingYards: TextView
    private lateinit var receivingYardsPerGame: TextView
    private lateinit var receivingTDs: TextView
    private lateinit var targets: TextView
    private lateinit var totalFumbles: TextView
    private lateinit var extraPointAttempts: TextView
    private lateinit var extraPointPct: TextView
    private lateinit var extraPointsMade: TextView
    private lateinit var fieldGoalAttempts: TextView
    private lateinit var fieldGoalPct: TextView
    private lateinit var fieldGoalsMade: TextView
    private lateinit var longFieldGoalMade: TextView
    private lateinit var teamPassAttempts: TextView
    private lateinit var teamRushAttempts: TextView

    private val playerStatsViewModel: PlayerStatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("NFL Stats App", "in Stats Activity onCreate")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val player = intent.extras?.get("player_data") as? Player
        Log.d("Player Intent", player.toString())

        // Initialize the UI elements
        playerHeadshot = findViewById(R.id.playerHeadshot)
        playerName = findViewById(R.id.playerName)
        playerPosition = findViewById(R.id.playerPosition)
        playerTeam = findViewById(R.id.playerTeam)

        gamesPlayed = findViewById(R.id.gamesPlayed)
        fumblesLost = findViewById(R.id.fumblesLost)
        passingYards = findViewById(R.id.passingYards)
        avgPassingYards = findViewById(R.id.avgPassingYards)
        passingTDs = findViewById(R.id.passingTDs)
        rushAttempts = findViewById(R.id.rushAttempts)
        rushingYards = findViewById(R.id.rushingYards)
        avgRushingYards = findViewById(R.id.avgRushingYards)
        rushingTDs = findViewById(R.id.rushingTDs)
        interceptions = findViewById(R.id.interceptions)
        receivingYards = findViewById(R.id.receivingYards)
        receivingYardsPerGame = findViewById(R.id.receivingYardsPerGame)
        receivingTDs = findViewById(R.id.receivingTDs)
        targets = findViewById(R.id.targets)
        totalFumbles = findViewById(R.id.totalFumbles)
        extraPointAttempts = findViewById(R.id.extraPointAttempts)
        extraPointPct = findViewById(R.id.extraPointPct)
        extraPointsMade = findViewById(R.id.extraPointsMade)
        fieldGoalAttempts = findViewById(R.id.fieldGoalAttempts)
        fieldGoalPct = findViewById(R.id.fieldGoalPct)
        fieldGoalsMade = findViewById(R.id.fieldGoalsMade)
        longFieldGoalMade = findViewById(R.id.longFieldGoalMade)
        teamPassAttempts = findViewById(R.id.teamPassAttempts)
        teamRushAttempts = findViewById(R.id.teamRushAttempts)

        // Set player name, position, and team
        playerName.text = player?.fullName
        playerPosition.text = player?.position

        // Load the player's headshot with Glide
        Glide.with(this)
            .load(player?.headshotUrl)
            .placeholder(R.drawable.default_player_image)  // Optional placeholder while loading
            .error(R.drawable.default_player_image)       // Optional error image if loading fails
            .into(playerHeadshot);

        // Fetch player stats using ViewModel
        playerStatsViewModel.fetchPlayerStats(player?.id.toString(), player?.teamId.toString(), player?.positionId.toString())

        // Observe the LiveData for updates
        playerStatsViewModel.playerStats.observe(this, Observer { stats ->
            stats?.let {
                // Update the stats TextViews with the data from the ViewModel
                gamesPlayed.text = it.gamesPlayed?.let { "Games Played: $it" } ?: ""
                fumblesLost.text = it.fumblesLost?.let { "Fumbles Lost: $it" } ?: ""
                passingYards.text = it.totalPassingYards?.let { "Passing Yards: $it" } ?: ""
                avgPassingYards.text = it.avgPassingYards?.let { "Avg Passing Yards: $it" } ?: ""
                passingTDs.text = it.totalPassingTDs?.let { "Passing TDs: $it" } ?: ""
                rushAttempts.text = it.totalRushAttempts?.let { "Rush Attempts: $it" } ?: ""
                rushingYards.text = it.totalRushingYards?.let { "Rushing Yards: $it" } ?: ""
                avgRushingYards.text = it.avgRushingYards?.let { "Avg Rushing Yards: $it" } ?: ""
                rushingTDs.text = it.totalRushingTDs?.let { "Rushing TDs: $it" } ?: ""
                interceptions.text = it.totalInterceptions?.let { "Interceptions: $it" } ?: ""
                receivingYards.text = it.totalReceivingYards?.let { "Receiving Yards: $it" } ?: ""
                receivingYardsPerGame.text = it.receivingYardsPerGame?.let { "Receiving Yards/Game: $it" } ?: ""
                receivingTDs.text = it.totalReceivingTDs?.let { "Receiving TDs: $it" } ?: ""
                targets.text = it.totalTargets?.let { "Targets: $it" } ?: ""
                totalFumbles.text = it.totalFumbles?.let { "Total Fumbles: $it" } ?: ""
                extraPointAttempts.text = it.extraPointAttempts?.let { "XP Attempts: $it" } ?: ""
                extraPointPct.text = it.extraPointPct?.let { "XP %: $it" } ?: ""
                extraPointsMade.text = it.extraPointsMade?.let { "XP Made: $it" } ?: ""
                fieldGoalAttempts.text = it.fieldGoalAttempts?.let { "FG Attempts: $it" } ?: ""
                fieldGoalPct.text = it.fieldGoalPct?.let { "FG %: $it" } ?: ""
                fieldGoalsMade.text = it.fieldGoalsMade?.let { "FG Made: $it" } ?: ""
                longFieldGoalMade.text = it.longFieldGoalMade?.let { "Long FG Made: $it" } ?: ""
                teamPassAttempts.text = it.teamPassAttempts?.let { "Team Pass Attempts: $it" } ?: ""
                teamRushAttempts.text = it.teamRushAttempts?.let { "Team Rush Attempts: $it" } ?: ""
            }
        })
    }
}
