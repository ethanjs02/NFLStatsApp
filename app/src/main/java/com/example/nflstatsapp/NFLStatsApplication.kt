package com.example.nflstatsapp

import android.app.Application
import android.util.Log
import com.example.nflstatsapp.data.NFLDatabase
import com.example.nflstatsapp.data.players.PlayerRepository
import com.example.nflstatsapp.data.teams.TeamRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class NFLStatsApplication : Application() {

    // Application-wide CoroutineScope to be used across the app
    val applicationScope = CoroutineScope(SupervisorJob())

    // Lazy-initialized database instance
    val database by lazy { NFLDatabase.getDatabase(this, applicationScope) }

    // Lazy-initialized repositories, passing DAOs from the database
    val playerRepository by lazy { PlayerRepository(database.playerDao()) }
    val teamRepository by lazy { TeamRepository(database.teamDao()) }

    override fun onCreate() {
        super.onCreate()
        Log.d("NFLStatsApplication", "Application created")
        database
    }
}
