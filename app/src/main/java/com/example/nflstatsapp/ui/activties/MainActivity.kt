package com.example.nflstatsapp.ui.activties

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nflstatsapp.NFLStatsApplication
import com.example.nflstatsapp.R
import com.example.nflstatsapp.data.players.PlayerRepository
import com.example.nflstatsapp.data.teams.TeamRepository
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.example.nflstatsapp.ui.adapters.PlayerAdapter
import com.example.nflstatsapp.ui.viewModels.PlayerViewModel
import com.example.nflstatsapp.ui.viewModels.PlayerViewModelFactory

class MainActivity : AppCompatActivity() {

    // Access the repositories directly from the application class
    private val playerRepository: PlayerRepository by lazy {
        (application as NFLStatsApplication).playerRepository
    }

    private lateinit var playerAdapter: PlayerAdapter
    private lateinit var playerViewModel: PlayerViewModel


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Apply system bars insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        playerAdapter = PlayerAdapter { player ->
            val intent = Intent(this, StatsActivity::class.java).apply {
                putExtra("player_data", player) // Pass the player object
            }
            startActivity(intent)
        }
        recyclerView.adapter = playerAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val viewModelFactory = PlayerViewModelFactory(playerRepository)
        playerViewModel = ViewModelProvider(this, viewModelFactory)[PlayerViewModel::class.java]

        playerViewModel.searchResults.observe(this) { players ->
            playerAdapter.setData(players)
        }



        val searchView = findViewById<SearchView>(R.id.searchView)

        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchIcon.setColorFilter(ContextCompat.getColor(this, R.color.cardBackground), PorterDuff.Mode.SRC_IN)

        val closeIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        closeIcon.setColorFilter(ContextCompat.getColor(this, R.color.red), PorterDuff.Mode.SRC_IN)




        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                playerViewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }
}
