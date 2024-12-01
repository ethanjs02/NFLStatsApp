package com.example.nflstatsapp.data.players

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.nflstatsapp.data.players.PlayerDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlayerRepository(private val playerDao: PlayerDao) {

    // Function to get player names
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getPlayerNames(): List<String> = withContext(Dispatchers.IO) {
        playerDao.getPlayerNames()
    }

    // Function to get player by name
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getPlayerByName(fullName: String): Player? = withContext(Dispatchers.IO) {
        playerDao.getPlayerByName(fullName)
    }

    // Function to insert a player
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(player: Player) = withContext(Dispatchers.IO) {
        playerDao.insert(player)
    }

    // Function to delete all players
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        playerDao.deleteAll()
    }

    fun searchPlayers(query: String): LiveData<List<Player>> {
        return playerDao.searchPlayers(query)
    }
}
