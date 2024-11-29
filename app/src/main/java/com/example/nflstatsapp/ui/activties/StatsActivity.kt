package com.example.nflstatsapp.ui.activties

import android.graphics.drawable.Drawable
import android.net.Uri
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
import com.example.nflstatsapp.NFLStatsApplication
import com.example.nflstatsapp.R
import com.example.nflstatsapp.data.api.PlayerStats
import com.example.nflstatsapp.data.players.Player
import com.example.nflstatsapp.data.teams.TeamRepository
import com.example.nflstatsapp.ui.StatsAdapter
import com.example.nflstatsapp.ui.viewModels.PlayerStatsViewModel
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso

class StatsActivity : AppCompatActivity() {

    private val playerStatsViewModel: PlayerStatsViewModel by lazy {
        val teamRepository: TeamRepository = (application as NFLStatsApplication).teamRepository
        PlayerStatsViewModel(teamRepository)
    }

    private lateinit var player: Player
    private lateinit var playerHeadshot: ImageView
    private lateinit var playerName: TextView
    private lateinit var playerPosition: TextView
    private lateinit var playerTeam: TextView
    private lateinit var fantasyPointsTextView: TextView
    private lateinit var fantasyPpgTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var playerJerseyTextView: TextView
    private lateinit var playerHeightTextView: TextView
    private lateinit var playerWeightTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        // Initialize the UI elements
        playerHeadshot = findViewById(R.id.playerHeadshot)
        playerName = findViewById(R.id.playerName)
        playerPosition = findViewById(R.id.playerPosition)
        playerTeam = findViewById(R.id.playerTeam)
        fantasyPointsTextView = findViewById(R.id.fantasyPoints)
        fantasyPpgTextView = findViewById(R.id.fantasyPointsPerGame)
        recyclerView = findViewById(R.id.recyclerView)
        playerJerseyTextView= findViewById(R.id.playerJersey)
        playerHeightTextView= findViewById(R.id.playerHeight)
        playerWeightTextView= findViewById(R.id.playerWeight)

        val player = intent.extras?.get("player_data") as? Player
        player?.teamId?.let {
            playerStatsViewModel.fetchTeamData(it).observe(this) { team ->
                if (team != null) {
                    playerTeam.text = team.name
                }
            }
        }

        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val tabNames = listOf("Standard", "Half-PPR", "PPR")

        tabNames.forEach { tabName ->
            tabLayout.addTab(tabLayout.newTab().setText(tabName))
        }

        // Set player name, position, and team
//        playerHeadshot.setImageURI(Uri.parse(player?.headshotUrl))
        playerName.text = player?.fullName
        playerPosition.text = player?.position
        playerJerseyTextView.text = "Jersey: #${player?.jersey}" // Set jersey number
        playerHeightTextView.text = "Height: ${player?.displayHeight}" // Set height
        playerWeightTextView.text = "Weight: ${player?.displayWeight}" // Set weight

//        val imageUrl = "https://www.gstatic.com/webp/gallery3/1.png"

        //Someone pls figure out how to get the images to work
//        Picasso.get()
//            .load(imageUrl)
//            .error(R.drawable.default_player_image)
//            .into(playerHeadshot)

        // Load the player's headshot with Glide
        Glide.with(this)
            .load(player?.headshotUrl)
            .placeholder(R.drawable.default_player_image)
            .error(R.drawable.default_player_image)
            .fallback(R.drawable.default_player_image)
            .into(playerHeadshot)

        // Fetch player stats using ViewModel
        playerStatsViewModel.fetchPlayerStats(player?.id.toString(), player?.teamId.toString(), player?.positionId.toString())

        playerStatsViewModel.totalFantasyPoints.observe(this, Observer { fantasyPoints ->
            fantasyPoints?.let {
                // Update Fantasy Points TextView
                fantasyPointsTextView.text = "Fantasy Points: $it"
            }
        })

        playerStatsViewModel.fantasyPointsPerGame.observe(this, Observer { fantasyPointsPerGame ->
            fantasyPointsPerGame?.let {
                // Update Fantasy Points Per Game TextView
                fantasyPpgTextView.text = "Fantasy Points Per Game: $it"
            }
        })

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

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectedTab = tab?.text.toString()
                Log.d("StatsActivity", "Selected Tab: $selectedTab")
                // Update data or UI based on the selected tab
                // Example: Filter stats by scoring type
                playerStatsViewModel.onScoringTypeSelected(selectedTab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}
