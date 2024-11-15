package com.example.nflstatsapp.data.teams

import androidx.annotation.WorkerThread
import com.example.nflstatsapp.data.teams.TeamDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TeamRepository(private val teamDao: TeamDao) {

    // Function to get team by ID
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getTeamById(teamId: Int): Team? = withContext(Dispatchers.IO) {
        teamDao.getTeamById(teamId)
    }

    // Function to insert a team
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(team: Team) = withContext(Dispatchers.IO) {
        teamDao.insert(team)
    }

    // Function to delete all teams
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        teamDao.deleteAll()
    }
}
