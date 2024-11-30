package com.example.nflstatsapp.ui.activties

import androidx.appcompat.app.AppCompatActivity
import com.example.nflstatsapp.NFLStatsApplication
import com.example.nflstatsapp.data.teams.TeamRepository
import com.example.nflstatsapp.ui.viewModels.PlayerStatsViewModel

class ComparisonActivity: AppCompatActivity() {

    private val playerStatsViewModel: PlayerStatsViewModel by lazy {
        val teamRepository: TeamRepository = (application as NFLStatsApplication).teamRepository
        PlayerStatsViewModel(teamRepository)
    }




}