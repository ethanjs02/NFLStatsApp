package com.example.nflstatsapp.Repository.Players

import androidx.room.Dao
import androidx.room.Query

@Dao
interface PlayerDAO {
    @Query("Select fullName from players")
    fun getPlayerNames(): List<String>

    @Query("Select * from players WHERE fullName = :fullName")
    fun getPlayerByName(fullName: String): Player?
}