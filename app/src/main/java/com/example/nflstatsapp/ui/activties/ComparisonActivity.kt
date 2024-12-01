package com.example.nflstatsapp.ui.activties

import PlayerStatsData
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nflstatsapp.NFLStatsApplication
import com.example.nflstatsapp.R
import com.example.nflstatsapp.data.api.PlayerStats
import com.example.nflstatsapp.data.players.Player
import com.example.nflstatsapp.ui.StatsAdapter
import com.example.nflstatsapp.ui.adapters.ComparisonAdapter
import com.example.nflstatsapp.ui.viewModels.PlayerStatsViewModel
import java.math.BigDecimal

class ComparisonActivity : AppCompatActivity() {

    private val playerStatsViewModel: PlayerStatsViewModel by lazy {
        val teamRepository = (application as NFLStatsApplication).teamRepository
        PlayerStatsViewModel(teamRepository)
    }

    private var isComparisonUpdated = false

    private lateinit var recyclerView: RecyclerView
    private lateinit var player1Image: ImageView
    private lateinit var player1Name: TextView
    private lateinit var player1Jersey: TextView
    private lateinit var player1Pos: TextView
    private lateinit var player2Image: ImageView
    private lateinit var player2Name: TextView
    private lateinit var player2Jersey: TextView
    private lateinit var player2Pos: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comparison)

        // Initialize views
        recyclerView = findViewById(R.id.statsRecyclerView)
        player1Image = findViewById(R.id.player1Image)
        player1Name = findViewById(R.id.player1Name)
        player1Jersey = findViewById(R.id.player1Jersey)
        player1Pos = findViewById(R.id.player1Position)
        player2Image = findViewById(R.id.player2Image)
        player2Name = findViewById(R.id.player2Name)
        player2Jersey = findViewById(R.id.player2Jersey)
        player2Pos = findViewById(R.id.player2Position)

        var fantasyPpg: BigDecimal = BigDecimal("0.0")

        // Retrieve player data from the Intent
        val player1 = intent.extras?.get("player1_data") as? PlayerStatsData
        val player2 = intent.extras?.get("player2_data") as? Player
        val tabValue = intent.extras?.get("tabValue") as String

        Log.d("in comparison", tabValue)

        if (player1 != null) {
            player1Image.setImageBitmap(byteToBitMap(player1.headshotData))
        }

        playerStatsViewModel.headshotData.observe(this) { bitmap ->
            if (bitmap != null) {
                player2Image.setImageBitmap(bitmap) // Display the image
            } else {
                Log.d("StatsActivity", "Unable to retrieve headshot")
                player2Image.setImageResource(R.drawable.default_player_image)
            }
        }

        player2?.href?.let { playerStatsViewModel.fetchHeadshot(it) }

        // Set player names and images
        player1Name.text = player1?.fullName ?: "Player 1"
        player1Jersey.text = "#${player1?.jerseyNumber}"
        player1Pos.text = player1?.position
        player2Name.text = player2?.fullName ?: "Player 2"
        player2Jersey.text = "#${player2?.jersey}"
        player2Pos.text = player2?.position

        playerStatsViewModel.fetchPlayerStats(player2?.id.toString(), player2?.teamId.toString(), player2?.positionId.toString(), tabValue)

        // Set up RecyclerView layout manager
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observe the fantasy points per game
        playerStatsViewModel.fantasyPointsPerGame.observe(this, Observer { fantasyPointsPerGame ->
            fantasyPointsPerGame?.let {
                // Check if player stats are already loaded
                val stats = playerStatsViewModel.playerStats.value
                if (stats != null && !isComparisonUpdated) {
                    updateComparisonStats(player1, stats, fantasyPointsPerGame)
                    isComparisonUpdated = true
                }
            }
        })

        playerStatsViewModel.isLoading.observe(this, {
                isLoading -> isLoading?.let {
            if(isLoading) {
//                progressBar.visibility = View.VISIBLE
            }
            else {
//                progressBar.visibility = View.GONE
            }
        }
        })

    }

    private fun updateComparisonStats(player1: PlayerStatsData?, player2Stats: PlayerStats, fantasyPpg: BigDecimal) {
        val player1Stats = player1?.stats
        if (player1Stats != null) {
            player1Stats.fantasyPpg = player1.fantasyPointsPerGame.toString()
            player2Stats.fantasyPpg = fantasyPpg.toString()

            val statList = playerStatsViewModel.mapStatsToComparison(player1Stats, player2Stats)

            // Create the adapter and set it to the RecyclerView
            val adapter = ComparisonAdapter(statList)
            recyclerView.adapter = adapter
        }
    }

    private fun byteToBitMap(byteArray: ByteArray): Bitmap? {
        return try {
            // Decode the byteArray to a Bitmap
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            // Handle any exceptions that may occur
            e.printStackTrace()
            null
        }
    }
}
