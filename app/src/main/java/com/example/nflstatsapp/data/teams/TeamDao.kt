package com.example.nflstatsapp.data.teams

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TeamDao {
    @Query("SELECT * from teams where id = :teamId")
    fun getTeamById(teamId: Int): Team?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(team: Team)

    @Query("DELETE from teams")
    fun deleteAll()
}