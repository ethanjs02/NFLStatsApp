package com.example.nflstatsapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nflstatsapp.data.players.PlayerRepository
import com.example.nflstatsapp.data.teams.TeamRepository


class MainActivity : AppCompatActivity() {

    // Access the repositories directly from the application class
    private val playerRepository: PlayerRepository by lazy {
        (application as NFLStatsApplication).playerRepository
    }

    private val teamRepository: TeamRepository by lazy {
        (application as NFLStatsApplication).teamRepository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Apply system bars insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


    }
}
