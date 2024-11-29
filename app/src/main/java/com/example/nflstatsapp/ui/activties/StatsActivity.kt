package com.example.nflstatsapp.ui.activties

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nflstatsapp.R
import com.example.nflstatsapp.data.api.PlayerStats
import com.example.nflstatsapp.data.players.Player
import com.example.nflstatsapp.ui.StatsAdapter
import com.example.nflstatsapp.ui.viewModels.PlayerStatsViewModel
class StatsActivity : AppCompatActivity() {

    private lateinit var player: Player
    private lateinit var playerHeadshot: ImageView
    private lateinit var playerName: TextView
    private lateinit var playerPosition: TextView
    private lateinit var playerTeam: TextView
    private lateinit var recyclerView: RecyclerView

    private val playerStatsViewModel: PlayerStatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val player = intent.extras?.get("player_data") as? Player

        // Initialize the UI elements
        playerHeadshot = findViewById(R.id.playerHeadshot)
        playerName = findViewById(R.id.playerName)
        playerPosition = findViewById(R.id.playerPosition)
        playerTeam = findViewById(R.id.playerTeam)
        recyclerView = findViewById(R.id.recyclerView)

        // Set player name, position, and team
        playerName.text = player?.fullName
        playerPosition.text = player?.position

        val imageUrl = "https://www.gstatic.com/webp/gallery3/1.png"

        // Load the player's headshot with Glide
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.default_player_image)
            .error(R.drawable.default_player_image)
            .fallback(R.drawable.default_player_image)
            .into(playerHeadshot)

        // Fetch player stats using ViewModel
        playerStatsViewModel.fetchPlayerStats(player?.id.toString(), player?.teamId.toString(), player?.positionId.toString())

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observe the LiveData for updates
        playerStatsViewModel.playerStats.observe(this, Observer { stats ->
            stats?.let {
                // Map the PlayerStats to a list of Stat objects
                val statList = playerStatsViewModel.mapPlayerStatsToList(it)
                val adapter = StatsAdapter(statList)
                recyclerView.adapter = adapter
            }
        })
    }
}
