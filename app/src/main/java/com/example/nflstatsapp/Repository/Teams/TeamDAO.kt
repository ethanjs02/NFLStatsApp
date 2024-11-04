package com.example.nflstatsapp.Repository.Teams

import androidx.room.Dao
import androidx.room.Query

@Dao
interface TeamDAO {
    @Query("SELECT * from teams where id = :teamId")
    fun getTeamById(teamId: Int): Team?
}