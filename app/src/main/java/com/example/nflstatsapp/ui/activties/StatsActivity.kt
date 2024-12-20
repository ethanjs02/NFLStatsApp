package com.example.nflstatsapp.ui.activties

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nflstatsapp.NFLStatsApplication
import com.example.nflstatsapp.R
import com.example.nflstatsapp.data.players.Player
import com.example.nflstatsapp.data.teams.TeamRepository
import com.example.nflstatsapp.ui.StatsAdapter
import com.example.nflstatsapp.ui.fragments.SearchPlayerFragment
import com.example.nflstatsapp.ui.viewModels.PlayerStatsViewModel
import com.google.android.material.tabs.TabLayout

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
    private lateinit var progressBar: ProgressBar
    private lateinit var compareButton: Button


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
        progressBar = findViewById(R.id.progressBar)
        compareButton = findViewById(R.id.compareButton)
        var tabValue = "Standard"

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
        Log.d("Headshot URL", player?.href ?: "No headshot URL available")
//        playerHeadshot.setImageURI(Uri.parse(player?.href))
        playerName.text = player?.fullName
        playerPosition.text = player?.position
        playerJerseyTextView.text = "#${player?.jersey}" // Set jersey number
        playerHeightTextView.text = "Height: ${player?.displayHeight}" // Set height
        playerWeightTextView.text = "Weight: ${player?.displayWeight}" // Set weight

        playerStatsViewModel.headshotData.observe(this) { bitmap ->
            if (bitmap != null) {
                playerHeadshot.setImageBitmap(bitmap) // Display the image
            } else {
                // Handle the case where the image could not be loaded
                Log.d("StatsActivity", "Unable to retrieve headshot")
                playerHeadshot.setImageResource(R.drawable.default_player_image)
            }
        }


        if (player != null) {
            player.href?.let { playerStatsViewModel.fetchHeadshot(it) }
        }

        // Fetch player stats using ViewModel
        playerStatsViewModel.fetchPlayerStats(player?.id.toString(), player?.teamId.toString(), player?.positionId.toString(), tabValue)

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

        playerStatsViewModel.isLoading.observe(this, {
            isLoading -> isLoading?.let {
                if(isLoading) {
                    progressBar.visibility = View.VISIBLE
                }
                else {
                    progressBar.visibility = View.GONE
                }
            }
        })

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Observe the LiveData for updates
        playerStatsViewModel.playerStats.observe(this, Observer { stats ->
            stats?.let {
                // Map the PlayerStats to a list of Stat objects
                val statList = player?.let { it1 -> playerStatsViewModel.mapPlayerStatsToList(it, it1.position) }
                val adapter = statList?.let { it1 -> StatsAdapter(it1) }
                recyclerView.adapter = adapter
            }
        })

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectedTab = tab?.text.toString()
                tabValue = selectedTab
                Log.d("StatsActivity", "Selected Tab: $selectedTab")
                // Update data or UI based on the selected tab
                // Example: Filter stats by scoring type
                playerStatsViewModel.onScoringTypeSelected(selectedTab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        compareButton.setOnClickListener {
            compareButton.visibility = View.GONE
            val data = playerStatsViewModel.aggregateData(player?.fullName, player?.position, player?.jersey)
            Log.d("compare", data.toString())
            val searchFragment = SearchPlayerFragment.newInstance(data, tabValue)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, searchFragment)
                .addToBackStack(null)
                .commit()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, searchFragment) // 'container' is the ID of your main container
            transaction.addToBackStack(null) // Allows back navigation to return to the previous screen
            transaction.commit()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                compareButton.visibility = View.VISIBLE
            }
        }
    }
}
