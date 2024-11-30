package com.example.nflstatsapp.data.players

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlayerDao {
    @Query("Select fullName from players")
    fun getPlayerNames(): List<String>

    @Query("Select * from players WHERE fullName = :fullName")
    fun getPlayerByName(fullName: String): Player?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(player: Player)

    @Query("DELETE from players")
    fun deleteAll()

    @Query("SELECT * FROM players WHERE fullName LIKE :query || '%' ORDER BY fullName ASC")
    fun searchPlayers(query: String): LiveData<List<Player>>
}